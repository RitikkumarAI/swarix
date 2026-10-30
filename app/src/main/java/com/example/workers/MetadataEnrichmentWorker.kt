package com.example.workers

import android.content.Context
import android.util.Log
import androidx.work.Constraints
import androidx.work.CoroutineWorker
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.ExistingWorkPolicy
import androidx.work.NetworkType
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.WorkerParameters
import com.example.data.local.SwarixDatabase
import com.google.firebase.Firebase
import com.google.firebase.ai.ai
import kotlinx.coroutines.withTimeout
import org.json.JSONObject
import java.util.concurrent.TimeUnit

/**
 * Background WorkManager service that leverages the Gemini API to analyze audio files
 * and automatically enrich their metadata with descriptive, AI-generated sub-genres
 * and mood tags in the local Room database.
 */
class MetadataEnrichmentWorker(
    context: Context,
    params: WorkerParameters
) : CoroutineWorker(context, params) {

    private val database = SwarixDatabase.getDatabase(context)
    private val musicDao = database.musicDao()
    private val aiCache = com.example.data.preferences.AiMetadataCacheDataStore(context)

    private val model by lazy {
        Firebase.ai.generativeModel("gemini-3.8-flash")
    }

    override suspend fun doWork(): Result {
        Log.d(TAG, "Starting background AI metadata enrichment for local audio files...")
        return try {
            val batteryMonitor = com.example.core.battery.BatteryMonitor(applicationContext)
            if (batteryMonitor.isLowBattery.value || batteryMonitor.isBatterySaverEngaged.value) {
                Log.d(TAG, "Battery Saver is active (level: ${batteryMonitor.batteryLevel.value}%). Postponing AI metadata enrichment to preserve battery.")
                return Result.success()
            }

            val allSongs = musicDao.getAllSongsOnce()
            if (allSongs.isEmpty()) {
                Log.d(TAG, "No songs in library to enrich.")
                return Result.success()
            }

            var enrichedCount = 0
            for (song in allSongs) {
                // Focus on songs with placeholder or generic tags, or local files
                val needsEnrichment = song.genre.isBlank() ||
                        song.genre.equals("Pop", ignoreCase = true) ||
                        song.genre.equals("Unknown", ignoreCase = true) ||
                        song.mood.isBlank() ||
                        song.mood.equals("Chill", ignoreCase = true)

                if (needsEnrichment || song.isLocal) {
                    try {
                        val prompt = """
                            Analyze the song '${song.title}' by artist '${song.artist}' (album: '${song.album}').
                            Identify its exact, evocative music sub-genre and emotional mood tag.
                            Respond ONLY in valid JSON with keys "genre" and "mood":
                            {"genre": "Indie Acoustic", "mood": "Nostalgic"}
                        """.trimIndent()

                        val response = withTimeout(40_000L) {
                            model.generateContent(prompt)
                        }

                        val text = response.text ?: ""
                        val jsonStart = text.indexOf('{')
                        val jsonEnd = text.lastIndexOf('}')
                        if (jsonStart != -1 && jsonEnd != -1 && jsonEnd > jsonStart) {
                            val json = JSONObject(text.substring(jsonStart, jsonEnd + 1))
                            val aiGenre = json.optString("genre").trim()
                            val aiMood = json.optString("mood").trim()

                            if (aiGenre.isNotEmpty() && aiMood.isNotEmpty()) {
                                musicDao.updateSongMetadata(song.id, aiGenre, aiMood)
                                aiCache.cacheSongMood(song.id, listOf(aiMood), aiGenre)
                                enrichedCount++
                                Log.d(TAG, "Enriched '${song.title}' -> genre: $aiGenre, mood: $aiMood")
                            }
                        }
                    } catch (e: Exception) {
                        Log.w(TAG, "Skipping song '${song.title}' due to analysis error: ${e.message}")
                    }
                }
            }

            Log.d(TAG, "Metadata enrichment complete. Successfully enriched $enrichedCount songs.")
            Result.success()
        } catch (e: Exception) {
            Log.e(TAG, "Fatal failure in MetadataEnrichmentWorker", e)
            Result.retry()
        }
    }

    companion object {
        private const val TAG = "MetadataEnrichmentWorker"
        private const val WORK_NAME_PERIODIC = "swarix_metadata_enrichment_periodic"
        private const val WORK_NAME_ONE_TIME = "swarix_metadata_enrichment_onetime"

        /**
         * Enqueues an immediate background run of the metadata enrichment service.
         */
        fun enqueueImmediate(context: Context) {
            val constraints = Constraints.Builder()
                .setRequiredNetworkType(NetworkType.CONNECTED)
                .build()

            val request = OneTimeWorkRequestBuilder<MetadataEnrichmentWorker>()
                .setConstraints(constraints)
                .addTag("ai_metadata_enrichment")
                .build()

            WorkManager.getInstance(context.applicationContext).enqueueUniqueWork(
                WORK_NAME_ONE_TIME,
                ExistingWorkPolicy.REPLACE,
                request
            )
            Log.d(TAG, "Enqueued immediate metadata enrichment work.")
        }

        /**
         * Schedules periodic background enrichment every 12 hours when device has network.
         */
        fun schedulePeriodic(context: Context) {
            val constraints = Constraints.Builder()
                .setRequiredNetworkType(NetworkType.CONNECTED)
                .build()

            val request = PeriodicWorkRequestBuilder<MetadataEnrichmentWorker>(
                12, TimeUnit.HOURS,
                30, TimeUnit.MINUTES
            )
                .setConstraints(constraints)
                .addTag("ai_metadata_enrichment_periodic")
                .build()

            WorkManager.getInstance(context.applicationContext).enqueueUniquePeriodicWork(
                WORK_NAME_PERIODIC,
                ExistingPeriodicWorkPolicy.KEEP,
                request
            )
            Log.d(TAG, "Scheduled periodic metadata enrichment work.")
        }

        /**
         * Reconfigures worker constraints to automatically throttle frequency and require
         * device charging when battery saver is engaged or battery is under 20%.
         */
        fun configureBatterySaverConstraints(context: Context, isBatterySaverActive: Boolean) {
            val constraintsBuilder = Constraints.Builder()
                .setRequiredNetworkType(NetworkType.CONNECTED)

            val repeatIntervalHours = if (isBatterySaverActive) {
                constraintsBuilder
                    .setRequiresBatteryNotLow(true)
                    .setRequiresCharging(true)
                48L // Reduce background AI calls to every 48 hours and only when plugged in
            } else {
                12L // Normal 12-hour schedule
            }

            val request = PeriodicWorkRequestBuilder<MetadataEnrichmentWorker>(
                repeatIntervalHours, TimeUnit.HOURS
            )
                .setConstraints(constraintsBuilder.build())
                .addTag("ai_metadata_enrichment_periodic")
                .build()

            WorkManager.getInstance(context.applicationContext).enqueueUniquePeriodicWork(
                WORK_NAME_PERIODIC,
                ExistingPeriodicWorkPolicy.UPDATE,
                request
            )
            Log.d(TAG, "Configured metadata enrichment worker (battery saver: $isBatterySaverActive, interval: ${repeatIntervalHours}h)")
        }
    }
}
