package com.example.playback.service

import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.net.wifi.WifiManager
import android.os.Build
import android.os.PowerManager
import android.util.Log
import android.view.KeyEvent
import androidx.media3.session.DefaultMediaNotificationProvider
import androidx.media3.session.MediaSession
import androidx.media3.session.MediaSessionService
import com.example.MainActivity
import com.example.SwarixApplication
import com.example.playback.MusicPlayerInitializer
import com.example.playback.PlayerManager

class MusicPlaybackService : MediaSessionService() {

    companion object {
        private const val TAG = "MusicPlaybackService"
        const val ACTION_TOGGLE_PLAY_PAUSE = "com.example.playback.ACTION_TOGGLE_PLAY_PAUSE"
        const val ACTION_PLAY = "com.example.playback.ACTION_PLAY"
        const val ACTION_PAUSE = "com.example.playback.ACTION_PAUSE"
        const val ACTION_NEXT = "com.example.playback.ACTION_NEXT"
        const val ACTION_PREVIOUS = "com.example.playback.ACTION_PREVIOUS"
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

            // Setup MediaNotificationProvider targeting Swarix Playback Notification Channel
            setMediaNotificationProvider(
                DefaultMediaNotificationProvider.Builder(this)
                    .setChannelId(SwarixApplication.PLAYBACK_CHANNEL_ID)
                    .setNotificationId(1001)
                    .build()
            )

            val intent = Intent(this, MainActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_SINGLE_TOP
            }
            val pendingIntent = PendingIntent.getActivity(
                this,
                0,
                intent,
                PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
            )

            // MediaSession Callback with Headphone & Bluetooth hardware media button support
            val sessionCallback = object : MediaSession.Callback {
                override fun onMediaButtonEvent(
                    session: MediaSession,
                    controllerInfo: MediaSession.ControllerInfo,
                    intent: Intent
                ): Boolean {
                    val keyEvent = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                        intent.getParcelableExtra(Intent.EXTRA_KEY_EVENT, KeyEvent::class.java)
                    } else {
                        @Suppress("DEPRECATION")
                        intent.getParcelableExtra(Intent.EXTRA_KEY_EVENT)
                    }
                    if (keyEvent != null && keyEvent.action == KeyEvent.ACTION_DOWN) {
                        when (keyEvent.keyCode) {
                            KeyEvent.KEYCODE_MEDIA_PLAY -> {
                                playerManager.exoPlayer.play()
                                return true
                            }
                            KeyEvent.KEYCODE_MEDIA_PAUSE -> {
                                playerManager.exoPlayer.pause()
                                return true
                            }
                            KeyEvent.KEYCODE_MEDIA_PLAY_PAUSE, KeyEvent.KEYCODE_HEADSETHOOK -> {
                                playerManager.togglePlayPause()
                                return true
                            }
                            KeyEvent.KEYCODE_MEDIA_NEXT -> {
                                playerManager.playNext()
                                return true
                            }
                            KeyEvent.KEYCODE_MEDIA_PREVIOUS -> {
                                playerManager.playPrevious()
                                return true
                            }
                        }
                    }
                    return super.onMediaButtonEvent(session, controllerInfo, intent)
                }
            }

            mediaSession = MediaSession.Builder(this, playerManager.exoPlayer)
                .setSessionActivity(pendingIntent)
                .setCallback(sessionCallback)
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

            Log.d(TAG, "MusicPlaybackService MediaSession & System Notification initialized successfully.")
        } catch (t: Throwable) {
            Log.e(TAG, "Error initializing MusicPlaybackService: ${t.message}", t)
        }
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        try {
            val playerManager = PlayerManager.getInstance(this)
            when (intent?.action) {
                ACTION_TOGGLE_PLAY_PAUSE -> playerManager.togglePlayPause()
                ACTION_PLAY -> playerManager.exoPlayer.play()
                ACTION_PAUSE -> playerManager.exoPlayer.pause()
                ACTION_NEXT -> playerManager.playNext()
                ACTION_PREVIOUS -> playerManager.playPrevious()
            }
        } catch (t: Throwable) {
            Log.e(TAG, "Error handling playback action intent: ${t.message}", t)
        }
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
