package com.example

import android.app.Application
import android.app.NotificationChannel
import android.app.NotificationManager
import android.os.Build
import android.util.Log

class SwarixApplication : Application() {

    companion object {
        const val PLAYBACK_CHANNEL_ID = "swarix_playback_channel"
        const val UPDATE_CHANNEL_ID = "swarix_updates_channel"
        private const val TAG = "SwarixApplication"
    }

    override fun onCreate() {
        super.onCreate()
        initNotificationChannels()
        setupGlobalCrashHandler()
    }

    private fun initNotificationChannels() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val notificationManager = getSystemService(NotificationManager::class.java) ?: return

            // 1. Playback Notification Channel for Lock Screen & Media Shade
            val playbackChannel = NotificationChannel(
                PLAYBACK_CHANNEL_ID,
                "Swarix Music Playback",
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = "Shows live music soundtrack and playback controls on lock screen"
                setShowBadge(false)
            }

            // 2. OTA In-App Update Channel
            val updateChannel = NotificationChannel(
                UPDATE_CHANNEL_ID,
                "Swarix App Updates",
                NotificationManager.IMPORTANCE_DEFAULT
            ).apply {
                description = "Shows app update and download progress"
                setShowBadge(true)
            }

            notificationManager.createNotificationChannel(playbackChannel)
            notificationManager.createNotificationChannel(updateChannel)
            Log.d(TAG, "Notification channels initialized successfully.")
        }
    }

    private fun setupGlobalCrashHandler() {
        val defaultHandler = Thread.getDefaultUncaughtExceptionHandler()
        Thread.setDefaultUncaughtExceptionHandler { thread, throwable ->
            Log.e(TAG, "Uncaught exception in thread ${thread.name}: ${throwable.message}", throwable)
            // Log telemetry or prevent crash loop if it is a non-fatal service exception
            defaultHandler?.uncaughtException(thread, throwable)
        }
    }
}

