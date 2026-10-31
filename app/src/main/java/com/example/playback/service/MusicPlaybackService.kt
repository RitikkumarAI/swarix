package com.example.playback.service

import android.app.PendingIntent
import android.content.Intent
import android.util.Log
import androidx.media3.session.MediaSession
import androidx.media3.session.MediaSessionService
import com.example.MainActivity
import com.example.playback.MusicPlayerInitializer
import com.example.playback.PlayerManager

class MusicPlaybackService : MediaSessionService() {

    companion object {
        private const val TAG = "MusicPlaybackService"
    }

    private var mediaSession: MediaSession? = null

    override fun onCreate() {
        super.onCreate()
        try {
            Log.d(TAG, "Creating MusicPlaybackService and initializing Media3 MediaSession...")
            val playerManager = PlayerManager.getInstance(this)

            // Verify ExoPlayer configuration complies with USAGE_MEDIA & AudioFocus
            MusicPlayerInitializer.verifyConfiguration(playerManager.exoPlayer)

            val intent = Intent(this, MainActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_SINGLE_TOP
            }
            val pendingIntent = PendingIntent.getActivity(
                this,
                0,
                intent,
                PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
            )

            mediaSession = MediaSession.Builder(this, playerManager.exoPlayer)
                .setSessionActivity(pendingIntent)
                .setId("SwarixMediaSession")
                .build()

            Log.d(TAG, "MusicPlaybackService MediaSession initialized successfully.")
        } catch (t: Throwable) {
            Log.e(TAG, "Error initializing MusicPlaybackService: ${t.message}", t)
        }
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        super.onStartCommand(intent, flags, startId)
        return START_NOT_STICKY
    }

    override fun onGetSession(controllerInfo: MediaSession.ControllerInfo): MediaSession? {
        return mediaSession
    }

    override fun onDestroy() {
        Log.d(TAG, "Destroying MusicPlaybackService, releasing MediaSession...")
        mediaSession?.run {
            release()
            mediaSession = null
        }
        super.onDestroy()
    }
}
