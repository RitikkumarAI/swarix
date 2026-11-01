package com.example.playback

import android.content.Context
import android.os.Build
import android.util.Log
import androidx.media3.common.AudioAttributes
import androidx.media3.common.C
import androidx.media3.datasource.DefaultDataSource
import androidx.media3.datasource.DefaultHttpDataSource
import androidx.media3.exoplayer.DefaultLoadControl
import androidx.media3.exoplayer.DefaultRenderersFactory
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.exoplayer.source.DefaultMediaSourceFactory

/**
 * Helper class that verifies and constructs properly configured Media3 ExoPlayer instances.
 * Guarantees compliance with modern Android audio standards and streaming reliability:
 * - AudioAttributes set to USAGE_MEDIA and CONTENT_TYPE_MUSIC
 * - Automatic Audio Focus handling enabled via setAudioAttributes(..., true)
 * - DefaultRenderersFactory initialized with extension renderer fallback & playback params
 * - Audio becoming noisy receiver handling enabled
 * - High-compatibility HTTP DataSource with cross-protocol redirect & browser User-Agent
 *   to eliminate 403/connection drops across Indian ISP & CDN edge networks.
 */
object MusicPlayerInitializer {

    private const val TAG = "MusicPlayerInitializer"

    /**
     * Initializes and returns a fully compliant, high-reliability ExoPlayer instance.
     */
    fun createExoPlayer(context: Context): ExoPlayer {
        Log.d(TAG, "Initializing Media3 ExoPlayer with AudioAttributes (USAGE_MEDIA) & resilient HTTP data source...")

        val renderersFactory = DefaultRenderersFactory(context.applicationContext)
            .setExtensionRendererMode(DefaultRenderersFactory.EXTENSION_RENDERER_MODE_PREFER)
            .setEnableAudioTrackPlaybackParams(true)

        val loadControl = DefaultLoadControl.Builder()
            .setBufferDurationsMs(
                /* minBufferMs = */ 30_000,
                /* maxBufferMs = */ 120_000,
                /* bufferForPlaybackMs = */ 1_000,
                /* bufferForPlaybackAfterRebufferMs = */ 2_500
            )
            .setBackBuffer(
                /* backBufferDurationMs = */ 30_000,
                /* retainBackBufferFromKeyframe = */ true
            )
            .setPrioritizeTimeOverSizeThresholds(true)
            .build()

        val audioAttributesBuilder = AudioAttributes.Builder()
            .setContentType(C.AUDIO_CONTENT_TYPE_MUSIC)
            .setUsage(C.USAGE_MEDIA)
            .setAllowedCapturePolicy(C.ALLOW_CAPTURE_BY_ALL)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S_V2) {
            audioAttributesBuilder.setSpatializationBehavior(C.SPATIALIZATION_BEHAVIOR_AUTO)
        }
        val audioAttributes = audioAttributesBuilder.build()

        // Robust HTTP Data Source Factory:
        // Many CDN edges (Saavn, Akamai, Cloudflare) and ISPs block default ExoPlayer headers with 403.
        // A mobile Chrome user-agent, cross-protocol redirects, and 20s timeouts guarantee continuous streaming.
        val httpDataSourceFactory = DefaultHttpDataSource.Factory()
            .setUserAgent("Mozilla/5.0 (Linux; Android 10; Mobile) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/120.0.0.0 Mobile Safari/537.36")
            .setAllowCrossProtocolRedirects(true)
            .setConnectTimeoutMs(20_000)
            .setReadTimeoutMs(30_000)

        val dataSourceFactory = DefaultDataSource.Factory(context.applicationContext, httpDataSourceFactory)
        val mediaSourceFactory = DefaultMediaSourceFactory(context.applicationContext)
            .setDataSourceFactory(dataSourceFactory)

        val player = ExoPlayer.Builder(context.applicationContext, renderersFactory)
            .setMediaSourceFactory(mediaSourceFactory)
            .setAudioAttributes(audioAttributes, /* handleAudioFocus = */ true)
            .setLoadControl(loadControl)
            .setHandleAudioBecomingNoisy(true)
            .setWakeMode(C.WAKE_MODE_NETWORK)
            .build()

        Log.d(TAG, "ExoPlayer instance created successfully with USAGE_MEDIA, AudioFocus handling & Resilient DataSource.")
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
