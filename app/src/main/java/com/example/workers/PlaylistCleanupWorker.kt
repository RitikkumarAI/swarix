package com.example.workers

import android.content.Context
import android.util.Log
import androidx.work.Constraints
import androidx.work.CoroutineWorker
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.WorkerParameters
import com.example.data.preferences.AiMetadataCacheDataStore
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import java.io.OutputStreamWriter
import java.nio.charset.StandardCharsets
import java.util.concurrent.TimeUnit

/**
 * Background WorkManager worker that automatically identifies and removes duplicate tracks
 * from cached Mood Radio playlists and exported M3U files to maintain library cleanliness.
 */
class PlaylistCleanupWorker(
    appContext: Context,
    params: WorkerParameters
) : CoroutineWorker(appContext, params) {

    companion object {
        private const val TAG = "PlaylistCleanupWorker"
        private const val WORK_NAME = "swarix_playlist_cleanup_worker"

        fun schedule(context: Context) {
            val constraints = Constraints.Builder()
                .setRequiresDeviceIdle(true)
                .build()

            val request = PeriodicWorkRequestBuilder<PlaylistCleanupWorker>(
                24, TimeUnit.HOURS
            )
                .setConstraints(constraints)
                .addTag("playlist_cleanup")
                .build()

            WorkManager.getInstance(context.applicationContext).enqueueUniquePeriodicWork(
                WORK_NAME,
                ExistingPeriodicWorkPolicy.KEEP,
                request
            )
            Log.d(TAG, "Scheduled PlaylistCleanupWorker for background playlist deduplication.")
        }
    }

    override suspend fun doWork(): Result = withContext(Dispatchers.IO) {
        Log.d(TAG, "Starting PlaylistCleanupWorker background deduplication routine...")
        try {
            val cacheDataStore = AiMetadataCacheDataStore(applicationContext)

            // 1. Deduplicate cached Mood Radio playlists across known moods
            val moods = listOf("Chill", "Energy", "Relax", "Focus", "Party", "Romance", "Workout")
            var cleanedPlaylistsCount = 0

            for (mood in moods) {
                val cachedIds = cacheDataStore.getCachedPlaylistForMood(mood)
                if (cachedIds.isNotEmpty()) {
                    val distinctIds = cachedIds.distinct()
                    if (distinctIds.size < cachedIds.size) {
                        cacheDataStore.cacheMoodPlaylist(mood, distinctIds)
                        cleanedPlaylistsCount++
                        Log.d(TAG, "Deduplicated '$mood' Mood Radio playlist: reduced from ${cachedIds.size} to ${distinctIds.size} tracks.")
                    }
                }
            }

            // 2. Scan export directory for M3U / M3U8 files and clean duplicate track entries
            val playlistsDir = File(applicationContext.cacheDir, "playlists")
            var cleanedM3uFiles = 0

            if (playlistsDir.exists() && playlistsDir.isDirectory) {
                val files = playlistsDir.listFiles { _, name ->
                    name.endsWith(".m3u", ignoreCase = true) || name.endsWith(".m3u8", ignoreCase = true)
                } ?: emptyArray()

                for (m3uFile in files) {
                    val lines = m3uFile.readLines(StandardCharsets.UTF_8)
                    val cleanedLines = mutableListOf<String>()
                    val seenTracks = mutableSetOf<String>()
                    var currentInfLine: String? = null

                    for (line in lines) {
                        when {
                            line.startsWith("#EXTINF:") -> {
                                currentInfLine = line
                            }
                            !line.startsWith("#") && line.isNotBlank() -> {
                                val trackUri = line.trim()
                                if (!seenTracks.contains(trackUri)) {
                                    seenTracks.add(trackUri)
                                    currentInfLine?.let { cleanedLines.add(it) }
                                    cleanedLines.add(trackUri)
                                }
                                currentInfLine = null
                            }
                            else -> {
                                cleanedLines.add(line)
                            }
                        }
                    }

                    if (cleanedLines.size < lines.size) {
                        FileOutputStream(m3uFile).use { fos ->
                            OutputStreamWriter(fos, StandardCharsets.UTF_8).use { writer ->
                                writer.write(cleanedLines.joinToString("\n") + "\n")
                                writer.flush()
                            }
                        }
                        cleanedM3uFiles++
                        Log.d(TAG, "Cleaned duplicate track entries from exported file: ${m3uFile.name}")
                    }
                }
            }

            Log.d(TAG, "Playlist cleanup completed successfully. Deduplicated $cleanedPlaylistsCount Mood playlists and $cleanedM3uFiles M3U files.")
            Result.success()
        } catch (e: Exception) {
            Log.e(TAG, "PlaylistCleanupWorker failed: ${e.message}", e)
            Result.retry()
        }
    }
}
