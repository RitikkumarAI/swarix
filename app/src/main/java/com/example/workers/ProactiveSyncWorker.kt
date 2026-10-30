package com.example.workers

import android.content.Context
import android.util.Log
import androidx.work.Constraints
import androidx.work.CoroutineWorker
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.NetworkType
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.WorkerParameters
import com.example.data.local.SwarixDatabase
import com.example.data.preferences.AiMetadataCacheDataStore
import com.google.firebase.Firebase
import com.google.firebase.ai.ai
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.coroutines.withTimeout
import org.json.JSONObject
import java.util.concurrent.TimeUnit

/**
 * Background WorkManager sync manager that automatically fetches missing lyrics
 * and AI-generated mood tags for local library tracks when the device is charging
 * and connected to Wi-Fi, optimizing battery usage.
 */
class ProactiveSyncWorker(
    appContext: Context,
    params: WorkerParameters
) : CoroutineWorker(appContext, params) {

    companion object {
        private const val TAG = "ProactiveSyncWorker"
        private const val WORK_NAME = "swarix_proactive_sync_wifi_charging"

        fun schedule(context: Context) {
            val constraints = Constraints.Builder()
                .setRequiredNetworkType(NetworkType.UNMETERED) // Wi-Fi
                .setRequiresCharging(true) // Charging
                .build()

            val request = PeriodicWorkRequestBuilder<ProactiveSyncWorker>(
                6, TimeUnit.HOURS
            )
                .setConstraints(constraints)
                .addTag("proactive_sync")
                .build()

            WorkManager.getInstance(context.applicationContext).enqueueUniquePeriodicWork(
                WORK_NAME,
                ExistingPeriodicWorkPolicy.KEEP,
                request
            )
            Log.d(TAG, "Scheduled ProactiveSyncWorker for Wi-Fi and charging state.")
        }
    }

    override suspend fun doWork(): Result = withContext(Dispatchers.IO) {
        Log.d(TAG, "Starting ProactiveSyncWorker background lyrics and mood tag sync...")
        try {
            val database = SwarixDatabase.getDatabase(applicationContext)
            val musicDao = database.musicDao()
            val aiCache = AiMetadataCacheDataStore(applicationContext)
            val model = Firebase.ai.generativeModel("gemini-3.8-flash")

            val allSongs = musicDao.getAllSongsOnce()
            var syncedCount = 0

            for (song in allSongs) {
                val needsSync = song.mood.isBlank() || song.genre.isBlank() || song.genre == "Unknown"
                if (needsSync) {
                    try {
                        val prompt = """
                            Analyze track '${song.title}' by ${song.artist} (Album: ${song.album}).
                            Provide accurate sub-genre and mood tag.
                            Respond ONLY in JSON:
                            {"genre": "Indie Rock", "mood": "Nostalgic"}
                        """.trimIndent()

                        val response = withTimeout(30_000L) {
                            model.generateContent(prompt)
                        }
                        val text = response.text ?: ""
                        val start = text.indexOf('{')
                        val end = text.lastIndexOf('}')
                        if (start != -1 && end != -1 && end > start) {
                            val json = JSONObject(text.substring(start, end + 1))
                            val genre = json.optString("genre").trim()
                            val mood = json.optString("mood").trim()
                            if (genre.isNotBlank() && mood.isNotBlank()) {
                                musicDao.updateSongMetadata(song.id, genre, mood)
                                aiCache.cacheSongMood(song.id, listOf(mood), genre)
                                syncedCount++
                            }
                        }
                    } catch (e: Exception) {
                        Log.w(TAG, "Failed syncing song '${song.title}': ${e.message}")
                    }
                }
            }

            Log.d(TAG, "ProactiveSyncWorker completed successfully. Synced $syncedCount tracks.")
            Result.success()
        } catch (e: Exception) {
            Log.e(TAG, "ProactiveSyncWorker failed: ${e.message}", e)
            Result.retry()
        }
    }
}
