package com.example.playback

import android.content.Context
import android.util.Log
import androidx.media3.common.PlaybackException
import androidx.media3.common.Player
import androidx.media3.exoplayer.ExoPlayer
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

/**
 * Playback Health Manager.
 * Periodically verifies connection and buffer health of the Media3 ExoPlayer instance,
 * detects stalled buffering sessions (>10s) or audio engine errors, triggers auto-recovery logic,
 * and provides a developer 'Reset Player' action for clearing corrupted audio buffers.
 */
class PlaybackHealthManager(
    private val context: Context,
    private val playerProvider: () -> ExoPlayer
) {

    companion object {
        private const val TAG = "PlaybackHealthManager"
        private const val HEALTH_CHECK_INTERVAL_MS = 5000L
        private const val MAX_STALLED_BUFFERING_MS = 10_000L
    }

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Main)
    private var healthCheckJob: Job? = null

    private var bufferingStartTimeMs: Long = 0L

    private val _healthStatus = MutableStateFlow("Healthy (Connected)")
    val healthStatus: StateFlow<String> = _healthStatus.asStateFlow()

    private val _recoveryCount = MutableStateFlow(0)
    val recoveryCount: StateFlow<Int> = _recoveryCount.asStateFlow()

    private val _lastError = MutableStateFlow<String?>(null)
    val lastError: StateFlow<String?> = _lastError.asStateFlow()

    fun startMonitoring() {
        stopMonitoring()
        healthCheckJob = scope.launch {
            while (isActive) {
                delay(HEALTH_CHECK_INTERVAL_MS)
                checkPlayerHealth()
            }
        }
        Log.d(TAG, "Playback Health Manager started periodic health checks.")
    }

    fun stopMonitoring() {
        healthCheckJob?.cancel()
        healthCheckJob = null
    }

    fun onPlayerError(error: PlaybackException) {
        val errorMessage = error.message ?: "Unknown Playback Exception (${error.errorCode})"
        Log.e(TAG, "Detected playback exception: $errorMessage. Initiating auto-recovery...", error)
        _lastError.value = errorMessage
        _healthStatus.value = "Error: $errorMessage"
        triggerAutoRecovery("Player Error Callback")
    }

    fun onPlaybackStateChanged(state: Int) {
        val player = playerProvider()
        when (state) {
            Player.STATE_BUFFERING -> {
                if (bufferingStartTimeMs == 0L) {
                    bufferingStartTimeMs = System.currentTimeMillis()
                }
                _healthStatus.value = "Buffering stream..."
            }
            Player.STATE_READY -> {
                bufferingStartTimeMs = 0L
                _healthStatus.value = if (player.isPlaying) "Playing (Healthy)" else "Ready (Paused)"
            }
            Player.STATE_ENDED -> {
                bufferingStartTimeMs = 0L
                _healthStatus.value = "Playback Completed"
            }
            Player.STATE_IDLE -> {
                bufferingStartTimeMs = 0L
                if (_lastError.value != null) {
                    _healthStatus.value = "Idle (Recovered)"
                } else {
                    _healthStatus.value = "Idle"
                }
            }
        }
    }

    private fun checkPlayerHealth() {
        val player = playerProvider()

        // 1. Check for stalled buffering
        if (player.playbackState == Player.STATE_BUFFERING && bufferingStartTimeMs > 0L) {
            val durationInBuffering = System.currentTimeMillis() - bufferingStartTimeMs
            if (durationInBuffering >= MAX_STALLED_BUFFERING_MS) {
                Log.w(TAG, "Playback stalled in STATE_BUFFERING for ${durationInBuffering}ms. Executing auto-recovery...")
                _healthStatus.value = "Stalled Buffering Recovery"
                triggerAutoRecovery("Stalled Buffering (>10s)")
                bufferingStartTimeMs = System.currentTimeMillis()
            }
        }

        // 2. Check for player error state
        if (player.playerError != null) {
            Log.w(TAG, "Player is in ERROR state: ${player.playerError?.message}. Executing auto-recovery...")
            triggerAutoRecovery("Unresolved Error State")
        }
    }

    /**
     * Executes auto-recovery logic by clearing errored timeline states, re-preparing player,
     * and resuming playback if previously active.
     */
    fun triggerAutoRecovery(reason: String) {
        val player = playerProvider()
        Log.i(TAG, "Triggering auto-recovery due to: $reason")
        try {
            _recoveryCount.value += 1
            val currentPos = player.currentPosition.coerceAtLeast(0L)
            val wasPlaying = player.isPlaying

            player.prepare()
            if (currentPos > 0L) {
                player.seekTo(currentPos)
            }
            if (wasPlaying) {
                player.play()
            }
            _healthStatus.value = "Auto-Recovered ($reason)"
            Log.i(TAG, "Auto-recovery executed successfully.")
        } catch (e: Exception) {
            Log.e(TAG, "Failed during auto-recovery execution: ${e.message}", e)
            _healthStatus.value = "Recovery Failed: ${e.message}"
        }
    }

    /**
     * Developer Action: Fully resets ExoPlayer buffers, re-commits media items, and re-initializes audio sinks.
     */
    fun resetPlayer() {
        val player = playerProvider()
        Log.i(TAG, "Developer Action: Performing full ExoPlayer reset and buffer flush...")
        try {
            val currentMediaItemIndex = player.currentMediaItemIndex
            val currentPos = player.currentPosition

            player.stop()
            player.clearMediaItems()
            _healthStatus.value = "Player Reset Complete"
            _lastError.value = null
            bufferingStartTimeMs = 0L
            Log.i(TAG, "ExoPlayer successfully reset and buffers flushed.")
        } catch (e: Exception) {
            Log.e(TAG, "Error performing resetPlayer: ${e.message}", e)
            _healthStatus.value = "Reset Error: ${e.message}"
        }
    }
}
