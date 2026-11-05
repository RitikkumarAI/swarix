package com.example.playback.service

import android.app.Notification
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.ServiceInfo
import android.net.wifi.WifiManager
import android.os.Build
import android.os.PowerManager
import android.util.Log
import android.view.KeyEvent
import androidx.core.app.NotificationCompat
import androidx.core.app.ServiceCompat
import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import androidx.media3.session.MediaSession
import androidx.media3.session.MediaSessionService
import androidx.media3.session.MediaStyleNotificationHelper
import com.example.MainActivity
import com.example.R
import com.example.SwarixApplication
import com.example.playback.MusicPlayerInitializer
import com.example.playback.PlayerManager

class MusicPlaybackService : MediaSessionService() {

    companion object {
        private const val TAG = "MusicPlaybackService"
        const val NOTIFICATION_ID = 1001
        const val ACTION_TOGGLE_PLAY_PAUSE = "com.example.playback.ACTION_TOGGLE_PLAY_PAUSE"
        const val ACTION_PLAY = "com.example.playback.ACTION_PLAY"
        const val ACTION_PAUSE = "com.example.playback.ACTION_PAUSE"
        const val ACTION_NEXT = "com.example.playback.ACTION_NEXT"
        const val ACTION_PREVIOUS = "com.example.playback.ACTION_PREVIOUS"
        const val ACTION_REFRESH_NOTIFICATION = "com.example.playback.ACTION_REFRESH_NOTIFICATION"
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
                flags = Intent.FLAG_ACTIVITY_SINGLE_TOP or Intent.FLAG_ACTIVITY_CLEAR_TOP
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

            // Acquire locks when player is active and synchronize system media notification
            playerManager.exoPlayer.addListener(object : Player.Listener {
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
                    updateNotification()
                }

                override fun onMediaItemTransition(mediaItem: MediaItem?, reason: Int) {
                    updateNotification()
                }

                override fun onPlaybackStateChanged(playbackState: Int) {
                    updateNotification()
                }
            })

            // Immediately satisfy Android's foreground service contract
            promoteToForeground()

            Log.d(TAG, "MusicPlaybackService MediaSession & System Notification initialized successfully.")
        } catch (t: Throwable) {
            Log.e(TAG, "Error initializing MusicPlaybackService: ${t.message}", t)
        }
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        try {
            promoteToForeground()
            val playerManager = PlayerManager.getInstance(this)
            when (intent?.action) {
                ACTION_TOGGLE_PLAY_PAUSE -> playerManager.togglePlayPause()
                ACTION_PLAY -> playerManager.exoPlayer.play()
                ACTION_PAUSE -> playerManager.exoPlayer.pause()
                ACTION_NEXT -> playerManager.playNext()
                ACTION_PREVIOUS -> playerManager.playPrevious()
                ACTION_REFRESH_NOTIFICATION -> updateNotification()
            }
        } catch (t: Throwable) {
            Log.e(TAG, "Error handling playback action intent: ${t.message}", t)
        }
        super.onStartCommand(intent, flags, startId)
        return START_STICKY
    }

    private fun promoteToForeground() {
        try {
            val notification = buildMediaNotification()
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                ServiceCompat.startForeground(
                    this,
                    NOTIFICATION_ID,
                    notification,
                    ServiceInfo.FOREGROUND_SERVICE_TYPE_MEDIA_PLAYBACK
                )
            } else {
                startForeground(NOTIFICATION_ID, notification)
            }
        } catch (t: Throwable) {
            Log.e(TAG, "Failed to startForeground: ${t.message}", t)
        }
    }

    private fun updateNotification() {
        try {
            val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as? NotificationManager
            val notification = buildMediaNotification()
            notificationManager?.notify(NOTIFICATION_ID, notification)
        } catch (t: Throwable) {
            Log.w(TAG, "Failed to update notification: ${t.message}")
        }
    }

    private fun buildMediaNotification(): Notification {
        val playerManager = PlayerManager.getInstance(this)
        val song = playerManager.currentSong.value
        val isPlaying = playerManager.isPlaying.value

        val openAppIntent = Intent(this, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_SINGLE_TOP or Intent.FLAG_ACTIVITY_CLEAR_TOP
        }
        val contentPendingIntent = PendingIntent.getActivity(
            this,
            0,
            openAppIntent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )

        val prevIntent = Intent(this, MusicPlaybackService::class.java).apply { action = ACTION_PREVIOUS }
        val prevPendingIntent = PendingIntent.getService(this, 1, prevIntent, PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT)

        val playPauseIntent = Intent(this, MusicPlaybackService::class.java).apply { action = ACTION_TOGGLE_PLAY_PAUSE }
        val playPausePendingIntent = PendingIntent.getService(this, 2, playPauseIntent, PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT)

        val nextIntent = Intent(this, MusicPlaybackService::class.java).apply { action = ACTION_NEXT }
        val nextPendingIntent = PendingIntent.getService(this, 3, nextIntent, PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT)

        val title = song?.title?.ifBlank { "Swarix Music" } ?: "Swarix Music"
        val artist = song?.artist?.ifBlank { "High-Fidelity Audio Player" } ?: "High-Fidelity Audio Player"

        val builder = NotificationCompat.Builder(this, SwarixApplication.PLAYBACK_CHANNEL_ID)
            .setContentTitle(title)
            .setContentText(artist)
            .setSubText("Swarix Studio 320kbps")
            .setSmallIcon(R.mipmap.ic_launcher)
            .setContentIntent(contentPendingIntent)
            .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
            .setOngoing(isPlaying)
            .setOnlyAlertOnce(true)
            .addAction(android.R.drawable.ic_media_previous, "Previous", prevPendingIntent)
            .addAction(
                if (isPlaying) android.R.drawable.ic_media_pause else android.R.drawable.ic_media_play,
                if (isPlaying) "Pause" else "Play",
                playPausePendingIntent
            )
            .addAction(android.R.drawable.ic_media_next, "Next", nextPendingIntent)

        mediaSession?.let { session ->
            builder.setStyle(
                MediaStyleNotificationHelper.MediaStyle(session)
                    .setShowActionsInCompactView(0, 1, 2)
            )
        }

        playerManager.cachedArtworkBitmap?.let { bitmap ->
            builder.setLargeIcon(bitmap)
        }

        return builder.build()
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
