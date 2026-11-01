package com.example.playback.service

import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.net.wifi.WifiManager
import android.os.PowerManager
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
    private var wakeLock: PowerManager.WakeLock? = null
    private var wifiLock: WifiManager.WifiLock? = null

    override fun onCreate() {
        super.onCreate()
        try {
            Log.d(TAG, "Creating MusicPlaybackService and initializing Media3 MediaSession...")
            val playerManager = PlayerManager.getInstance(this)

            // Setup WakeLock and WifiLock for background playback resilience
            val powerManager = getSystemService(Context.POWER_SERVICE) as? PowerManager
            wakeLock = powerManager?.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "Swarix::AudioWakeLock")?.apply {
                setReferenceCounted(false)
            }

            val wifiManager = applicationContext.getSystemService(Context.WIFI_SERVICE) as? WifiManager
            wifiLock = wifiManager?.createWifiLock(WifiManager.WIFI_MODE_FULL_HIGH_PERF, "Swarix::AudioWifiLock")?.apply {
                setReferenceCounted(false)
            }

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

            // Acquire locks when player is active
            playerManager.exoPlayer.addListener(object : androidx.media3.common.Player.Listener {
                override fun onIsPlayingChanged(isPlaying: Boolean) {
                    if (isPlaying) {
                        try {
                            if (wakeLock?.isHeld != true) wakeLock?.acquire(180 * 60 * 1000L) // 3 hours max
                            if (wifiLock?.isHeld != true) wifiLock?.acquire()
                        } catch (e: Exception) {
                            Log.w(TAG, "Error acquiring wake locks: ${e.message}")
                        }
                    } else {
                        try {
                            if (wakeLock?.isHeld == true) wakeLock?.release()
                            if (wifiLock?.isHeld == true) wifiLock?.release()
                        } catch (e: Exception) {
                            Log.w(TAG, "Error releasing wake locks: ${e.message}")
                        }
                    }
                }
            })

            Log.d(TAG, "MusicPlaybackService MediaSession initialized successfully.")
        } catch (t: Throwable) {
            Log.e(TAG, "Error initializing MusicPlaybackService: ${t.message}", t)
        }
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        super.onStartCommand(intent, flags, startId)
        return START_STICKY
    }

    override fun onGetSession(controllerInfo: MediaSession.ControllerInfo): MediaSession? {
        return mediaSession
    }

    override fun onDestroy() {
        Log.d(TAG, "Destroying MusicPlaybackService, releasing MediaSession...")
        try {
            if (wakeLock?.isHeld == true) wakeLock?.release()
            if (wifiLock?.isHeld == true) wifiLock?.release()
        } catch (e: Exception) {
            Log.w(TAG, "Error cleaning locks on destroy: ${e.message}")
        }
        mediaSession?.run {
            release()
            mediaSession = null
        }
        super.onDestroy()
    }
}
