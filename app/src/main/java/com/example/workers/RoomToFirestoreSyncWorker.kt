package com.example.workers

import android.content.Context
import android.os.Build
import android.util.Log
import androidx.work.Constraints
import androidx.work.CoroutineWorker
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.NetworkType
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.WorkerParameters
import com.example.data.local.SwarixDatabase
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.withContext
import java.util.concurrent.TimeUnit

/**
 * Background worker that automatically syncs the Room database (containing playlists,
 * history, and favorites) to Firebase Firestore when the device is idle and on Wi-Fi.
 */
class RoomToFirestoreSyncWorker(
    appContext: Context,
    params: WorkerParameters
) : CoroutineWorker(appContext, params) {

    companion object {
        private const val TAG = "RoomToFirestoreSyncWorker"
        private const val WORK_NAME = "swarix_room_firestore_sync_idle_wifi"

        fun schedule(context: Context) {
            val constraints = Constraints.Builder()
                .setRequiredNetworkType(NetworkType.UNMETERED) // Wi-Fi
                .setRequiresDeviceIdle(true) // Device idle
                .build()

            val request = PeriodicWorkRequestBuilder<RoomToFirestoreSyncWorker>(
                12, TimeUnit.HOURS
            )
                .setConstraints(constraints)
                .addTag("room_firestore_sync")
                .build()

            WorkManager.getInstance(context.applicationContext).enqueueUniquePeriodicWork(
                WORK_NAME,
                ExistingPeriodicWorkPolicy.KEEP,
                request
            )
            Log.d(TAG, "Scheduled RoomToFirestoreSyncWorker for idle and Wi-Fi state.")
        }
    }

    override suspend fun doWork(): Result = withContext(Dispatchers.IO) {
        Log.d(TAG, "Starting Room to Firestore database sync...")
        try {
            val database = SwarixDatabase.getDatabase(applicationContext)
            val musicDao = database.musicDao()
            val firestore = FirebaseFirestore.getInstance()

            val playlists = musicDao.getAllPlaylists().first()
            val favorites = musicDao.getFavoriteSongs().first()

            val userId = Build.SERIAL.takeIf { it != Build.UNKNOWN } ?: "default_device_user"
            val userDocRef = firestore.collection("users").document(userId)

            val playlistsData = playlists.map { p ->
                mapOf(
                    "id" to p.id,
                    "name" to p.name,
                    "createdAt" to p.createdAt
                )
            }
            val favoritesData = favorites.map { s ->
                mapOf(
                    "id" to s.id,
                    "title" to s.title,
                    "artist" to s.artist,
                    "album" to s.album
                )
            }

            val syncPayload = mapOf(
                "playlists" to playlistsData,
                "favorites" to favoritesData,
                "lastSynced" to System.currentTimeMillis()
            )

            userDocRef.set(syncPayload).addOnSuccessListener {
                Log.d(TAG, "Successfully synced Room database to Firestore.")
            }.addOnFailureListener { e ->
                Log.e(TAG, "Failed syncing to Firestore: ${e.message}")
            }

            Result.success()
        } catch (e: Exception) {
            Log.e(TAG, "RoomToFirestoreSyncWorker failed: ${e.message}", e)
            Result.retry()
        }
    }
}
