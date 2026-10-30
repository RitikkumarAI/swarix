package com.example.playback

import android.content.Context
import android.util.Log
import androidx.media3.common.AudioAttributes
import androidx.media3.common.C
import androidx.media3.exoplayer.DefaultLoadControl
import androidx.media3.exoplayer.DefaultRenderersFactory
import androidx.media3.exoplayer.ExoPlayer

/**
 * Helper class that verifies and constructs properly configured Media3 ExoPlayer instances.
 * Guarantees compliance with modern Android audio standards:
 * - AudioAttributes set to USAGE_MEDIA and CONTENT_TYPE_MUSIC
 * - Automatic Audio Focus handling enabled via setAudioAttributes(..., true)
 * - DefaultRenderersFactory initialized with extension renderer fallback & playback params
 * - Audio becoming noisy receiver handling enabled
 */
object MusicPlayerInitializer {

    private const val TAG = "MusicPlayerInitializer"

    /**
     * Initializes and returns a fully compliant ExoPlayer instance.
     */
    fun createExoPlayer(context: Context): ExoPlayer {
        Log.d(TAG, "Initializing Media3 ExoPlayer with AudioAttributes (USAGE_MEDIA) & AudioFocus handling...")

        val renderersFactory = DefaultRenderersFactory(context.applicationContext)
            .setExtensionRendererMode(DefaultRenderersFactory.EXTENSION_RENDERER_MODE_PREFER)
            .setEnableAudioTrackPlaybackParams(true)

        val loadControl = DefaultLoadControl.Builder()
            .setBufferDurationsMs(
                /* minBufferMs = */ 60_000,
                /* maxBufferMs = */ 180_000,
                /* bufferForPlaybackMs = */ 1_000,
                /* bufferForPlaybackAfterRebufferMs = */ 2_500
            )
            .setBackBuffer(
                /* backBufferDurationMs = */ 60_000,
                /* retainBackBufferFromKeyframe = */ true
            )
            .setPrioritizeTimeOverSizeThresholds(true)
            .build()

        val audioAttributes = AudioAttributes.Builder()
            .setContentType(C.AUDIO_CONTENT_TYPE_MUSIC)
            .setUsage(C.USAGE_MEDIA)
            .setAllowedCapturePolicy(C.ALLOW_CAPTURE_BY_ALL)
            .setSpatializationBehavior(C.SPATIALIZATION_BEHAVIOR_AUTO)
            .build()

        val player = ExoPlayer.Builder(context.applicationContext, renderersFactory)
            .setAudioAttributes(audioAttributes, /* handleAudioFocus = */ true)
            .setLoadControl(loadControl)
            .setHandleAudioBecomingNoisy(true)
            .build()

        Log.d(TAG, "ExoPlayer instance created successfully with USAGE_MEDIA and AudioFocus handling.")
        return player
    }

    /**
     * Verifies if an existing ExoPlayer instance has proper AudioAttributes configured.
     */
    fun verifyConfiguration(player: ExoPlayer): Boolean {
        val attrs = player.audioAttributes
        val isValid = attrs.usage == C.USAGE_MEDIA && attrs.contentType == C.AUDIO_CONTENT_TYPE_MUSIC
        Log.d(TAG, "ExoPlayer configuration verification: $isValid (Usage=${attrs.usage}, ContentType=${attrs.contentType})")
        return isValid
    }
}
