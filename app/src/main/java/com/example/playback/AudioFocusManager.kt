package com.example.playback

import android.content.Context
import android.media.AudioAttributes
import android.media.AudioFocusRequest
import android.media.AudioManager
import android.os.Build
import android.util.Log

/**
 * AudioFocusManager.
 * Manages system audio focus requests, ducking, transient losses, and focus recovery
 * following modern Android audio policies to prevent silent playback drops or audio collisions.
 */
class AudioFocusManager(
    private val context: Context,
    private val onPause: () -> Unit,
    private val onResume: () -> Unit,
    private val setVolume: (Float) -> Unit
) {

    companion object {
        private const val TAG = "AudioFocusManager"
    }

    private val audioManager = context.getSystemService(Context.AUDIO_SERVICE) as AudioManager
    private var focusRequest: AudioFocusRequest? = null
    private var isFocusGranted = false
    private var pausedForTransientLoss = false

    private val focusChangeListener = AudioManager.OnAudioFocusChangeListener { focusChange ->
        when (focusChange) {
            AudioManager.AUDIOFOCUS_GAIN -> {
                Log.d(TAG, "AudioFocus GAIN: Restoring full volume and resuming if transiently paused.")
                setVolume(1.0f)
                if (pausedForTransientLoss) {
                    pausedForTransientLoss = false
                    onResume()
                }
            }
            AudioManager.AUDIOFOCUS_LOSS_TRANSIENT_CAN_DUCK -> {
                Log.d(TAG, "AudioFocus LOSS_TRANSIENT_CAN_DUCK: Lowering volume to 0.2f")
                setVolume(0.2f)
            }
            AudioManager.AUDIOFOCUS_LOSS_TRANSIENT -> {
                Log.d(TAG, "AudioFocus LOSS_TRANSIENT: Pausing playback temporarily.")
                pausedForTransientLoss = true
                onPause()
            }
            AudioManager.AUDIOFOCUS_LOSS -> {
                Log.d(TAG, "AudioFocus LOSS: Permanent focus loss. Pausing playback.")
                isFocusGranted = false
                pausedForTransientLoss = false
                onPause()
            }
        }
    }

    /**
     * Requests audio focus from Android system.
     * Returns true if focus was successfully granted.
     */
    fun requestAudioFocus(): Boolean {
        if (isFocusGranted) return true

        val res = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val playbackAttributes = AudioAttributes.Builder()
                .setUsage(AudioAttributes.USAGE_MEDIA)
                .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                .build()

            val req = AudioFocusRequest.Builder(AudioManager.AUDIOFOCUS_GAIN)
                .setAudioAttributes(playbackAttributes)
                .setAcceptsDelayedFocusGain(true)
                .setWillPauseWhenDucked(false)
                .setOnAudioFocusChangeListener(focusChangeListener)
                .build()

            focusRequest = req
            audioManager.requestAudioFocus(req)
        } else {
            @Suppress("DEPRECATION")
            audioManager.requestAudioFocus(
                focusChangeListener,
                AudioManager.STREAM_MUSIC,
                AudioManager.AUDIOFOCUS_GAIN
            )
        }

        isFocusGranted = (res == AudioManager.AUDIOFOCUS_REQUEST_GRANTED)
        Log.d(TAG, "Request Audio Focus result: $res (Granted=$isFocusGranted)")
        return isFocusGranted
    }

    /**
     * Abandons audio focus.
     */
    fun abandonAudioFocus() {
        if (!isFocusGranted) return

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            focusRequest?.let { audioManager.abandonAudioFocusRequest(it) }
        } else {
            @Suppress("DEPRECATION")
            audioManager.abandonAudioFocus(focusChangeListener)
        }

        isFocusGranted = false
        pausedForTransientLoss = false
        Log.d(TAG, "Abandoned Audio Focus.")
    }
}
