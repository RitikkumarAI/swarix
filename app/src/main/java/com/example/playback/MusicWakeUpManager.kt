package com.example.playback

import android.content.Context
import android.util.Log
import com.example.core.model.Song
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

/**
 * Music Wake-up Manager that uses Media3 ExoPlayer to gently ramp up
 * volume over time, playing a user-selected 'Mood Radio' playlist as an alarm.
 */
object MusicWakeUpManager {
    private const val TAG = "MusicWakeUpManager"
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Main)
    private var wakeUpJob: Job? = null

    fun startWakeUpAlarm(
        context: Context,
        playlist: List<Song>,
        rampDurationSeconds: Int = 60,
        onStarted: () -> Unit = {}
    ) {
        if (playlist.isEmpty()) return
        wakeUpJob?.cancel()

        val playerManager = PlayerManager.getInstance(context)
        
        // Start with volume at 0 for gentle wake-up
        playerManager.exoPlayer.volume = 0.0f
        
        // Play playlist starting from index 0
        playerManager.playSong(playlist[0], playlist)
        onStarted()

        wakeUpJob = scope.launch {
            val steps = (rampDurationSeconds * 10).coerceAtLeast(20)
            val delayInterval = (rampDurationSeconds * 1000L) / steps
            var currentStep = 0

            while (isActive && currentStep <= steps) {
                val progress = currentStep.toFloat() / steps.toFloat()
                // Linear / exponential volume ramp curve
                playerManager.exoPlayer.volume = progress.coerceIn(0.0f, 1.0f)
                delay(delayInterval)
                currentStep++
            }
            playerManager.exoPlayer.volume = 1.0f
            Log.d(TAG, "Music Wake-up volume ramp completed successfully.")
        }
    }

    fun cancelWakeUp() {
        wakeUpJob?.cancel()
        wakeUpJob = null
    }
}
