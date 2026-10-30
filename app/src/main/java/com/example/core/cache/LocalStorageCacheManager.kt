package com.example.core.cache

import android.content.Context
import android.net.Uri
import android.util.Log
import com.example.core.model.Song
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject
import java.io.File
import java.io.FileOutputStream
import java.net.HttpURLConnection
import java.net.URL

/**
 * Metadata record for an audio track saved in local offline storage.
 */
data class CachedSongRecord(
    val songId: String,
    val localPath: String,
    val fileSizeBytes: Long,
    val cachedAt: Long = System.currentTimeMillis(),
    val lastAccessedAt: Long = System.currentTimeMillis(),
    val title: String = "",
    val artist: String = "",
    val durationMs: Long = 0L,
    val artworkUri: String = "",
    val isPinned: Boolean = false
) {
    val formattedSize: String
        get() = LocalStorageCacheManager.formatBytes(fileSizeBytes)

    fun toJson(): JSONObject {
        return JSONObject().apply {
            put("songId", songId)
            put("localPath", localPath)
            put("fileSizeBytes", fileSizeBytes)
            put("cachedAt", cachedAt)
            put("lastAccessedAt", lastAccessedAt)
            put("title", title)
            put("artist", artist)
            put("durationMs", durationMs)
            put("artworkUri", artworkUri)
            put("isPinned", isPinned)
        }
    }

    companion object {
        fun fromJson(json: JSONObject): CachedSongRecord {
            return CachedSongRecord(
                songId = json.optString("songId", ""),
                localPath = json.optString("localPath", ""),
                fileSizeBytes = json.optLong("fileSizeBytes", 0L),
                cachedAt = json.optLong("cachedAt", System.currentTimeMillis()),
                lastAccessedAt = json.optLong("lastAccessedAt", System.currentTimeMillis()),
                title = json.optString("title", ""),
                artist = json.optString("artist", ""),
                durationMs = json.optLong("durationMs", 0L),
                artworkUri = json.optString("artworkUri", ""),
                isPinned = json.optBoolean("isPinned", false)
            )
        }
    }
}

/**
 * High-performance local storage caching manager that automatically saves recently
 * played songs to local storage for offline playback.
 *
 * Capabilities:
 * - Automatically caches played tracks in background without interrupting playback.
 * - Resolves local cached file URIs for ExoPlayer (0ms latency, zero mobile data, 100% offline).
 * - Enforces LRU (Least Recently Used) cache size eviction (default: 500 MB).
 * - Reactive StateFlow streams of cached song IDs, total storage used, and track count.
 * - Atomic file downloads with temp file renaming to prevent partial/corrupted files.
 * - Full manual cache controls: cache song, remove from cache, clear cache.
 */
class LocalStorageCacheManager private constructor(private val context: Context) {

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    private val mutex = Mutex()

    private val cacheDirectory: File by lazy {
        File(context.filesDir, "swarix_audio_cache").apply {
            if (!exists()) mkdirs()
        }
    }

    private val indexFile: File by lazy {
        File(context.filesDir, "swarix_cache_index.json")
    }

    // Maximum cache size limit (500 MB)
    private val maxCacheSizeBytes = 500L * 1024L * 1024L

    // Reactive StateFlows
    private val _cachedRecords = MutableStateFlow<Map<String, CachedSongRecord>>(emptyMap())
    val cachedRecords: StateFlow<Map<String, CachedSongRecord>> = _cachedRecords.asStateFlow()

    private val _cachedSongIds = MutableStateFlow<Set<String>>(emptySet())
    val cachedSongIds: StateFlow<Set<String>> = _cachedSongIds.asStateFlow()

    private val _totalCacheSizeBytes = MutableStateFlow(0L)
    val totalCacheSizeBytes: StateFlow<Long> = _totalCacheSizeBytes.asStateFlow()

    private val _isDownloading = MutableStateFlow<Set<String>>(emptySet())
    val isDownloading: StateFlow<Set<String>> = _isDownloading.asStateFlow()

    init {
        scope.launch {
            loadIndexAndVerifyFiles()
        }
    }

    /**
     * Loads saved index from disk and verifies that physical files exist.
     */
    private suspend fun loadIndexAndVerifyFiles() = mutex.withLock {
        withContext(Dispatchers.IO) {
            val recordsMap = mutableMapOf<String, CachedSongRecord>()
            try {
                if (indexFile.exists()) {
                    val content = indexFile.readText()
                    if (content.isNotBlank()) {
                        val jsonArray = JSONArray(content)
                        for (i in 0 until jsonArray.length()) {
                            val record = CachedSongRecord.fromJson(jsonArray.getJSONObject(i))
                            val file = File(record.localPath)
                            if (file.exists() && file.length() > 0) {
                                recordsMap[record.songId] = record.copy(fileSizeBytes = file.length())
                            }
                        }
                    }
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error reading cache index: ${e.message}")
            }

            // Also check directory for any untracked .mp3 files
            try {
                cacheDirectory.listFiles()?.forEach { file ->
                    val songId = file.nameWithoutExtension
                    if (file.isFile && file.length() > 0 && !recordsMap.containsKey(songId)) {
                        recordsMap[songId] = CachedSongRecord(
                            songId = songId,
                            localPath = file.absolutePath,
                            fileSizeBytes = file.length(),
                            cachedAt = file.lastModified(),
                            lastAccessedAt = file.lastModified()
                        )
                    }
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error scanning cache dir: ${e.message}")
            }

            updateState(recordsMap)
            saveIndexLocked(recordsMap)
        }
    }

    /**
     * Checks if a given song is cached locally and available for offline playback.
     */
    fun isSongCached(songId: String): Boolean {
        val record = _cachedRecords.value[songId] ?: return false
        val file = File(record.localPath)
        return file.exists() && file.length() > 0
    }

    /**
     * Returns the local file Uri for a cached song if it exists.
     * This is used by PlayerManager to play directly from disk.
     */
    fun getCachedFileUri(songId: String): Uri? {
        val record = _cachedRecords.value[songId] ?: return null
        val file = File(record.localPath)
        return if (file.exists() && file.length() > 0) {
            Uri.fromFile(file)
        } else {
            null
        }
    }

    /**
     * Gets detailed metadata for a cached song.
     */
    fun getCachedRecord(songId: String): CachedSongRecord? {
        return _cachedRecords.value[songId]
    }

    /**
     * Automatically caches a recently played song for offline playback.
     * Triggered automatically whenever a song starts playing.
     */
    fun cacheRecentlyPlayedSong(song: Song) {
        scope.launch {
            if (isSongCached(song.id)) {
                // Update last accessed time for LRU
                touchSong(song.id)
                return@launch
            }
            downloadAndCacheSong(song, isPinned = false)
        }
    }

    /**
     * Manually saves a song to local storage (pinned by user).
     */
    fun cacheSong(song: Song) {
        scope.launch {
            downloadAndCacheSong(song, isPinned = true)
        }
    }

    /**
     * Removes a specific song from offline storage.
     */
    fun removeCachedSong(songId: String) {
        scope.launch {
            mutex.withLock {
                withContext(Dispatchers.IO) {
                    val currentMap = _cachedRecords.value.toMutableMap()
                    val record = currentMap.remove(songId)
                    if (record != null) {
                        try {
                            val file = File(record.localPath)
                            if (file.exists()) {
                                file.delete()
                            }
                        } catch (e: Exception) {
                            Log.e(TAG, "Failed to delete cached audio file for $songId: ${e.message}")
                        }
                        updateState(currentMap)
                        saveIndexLocked(currentMap)
                    }
                }
            }
        }
    }

    /**
     * Clears all cached audio files and resets the local index.
     */
    fun clearEntireCache() {
        scope.launch {
            mutex.withLock {
                withContext(Dispatchers.IO) {
                    try {
                        cacheDirectory.listFiles()?.forEach { file ->
                            if (file.isFile) file.delete()
                        }
                        if (indexFile.exists()) {
                            indexFile.delete()
                        }
                    } catch (e: Exception) {
                        Log.e(TAG, "Error clearing cache directory: ${e.message}")
                    }
                    updateState(emptyMap())
                }
            }
        }
    }

    private suspend fun touchSong(songId: String) {
        mutex.withLock {
            val map = _cachedRecords.value.toMutableMap()
            val record = map[songId]
            if (record != null) {
                map[songId] = record.copy(lastAccessedAt = System.currentTimeMillis())
                _cachedRecords.value = map
                saveIndexLocked(map)
            }
        }
    }

    /**
     * Performs atomic streaming download from song's remote URI to local disk.
     */
    private suspend fun downloadAndCacheSong(song: Song, isPinned: Boolean) {
        val uriStr = song.contentUri
        if (uriStr.isBlank()) return

        // Mark as downloading
        _isDownloading.value = _isDownloading.value + song.id

        val targetFile = File(cacheDirectory, "${song.id}.mp3")
        val tempFile = File(cacheDirectory, "${song.id}.tmp")

        try {
            withContext(Dispatchers.IO) {
                if (uriStr.startsWith("http://") || uriStr.startsWith("https://")) {
                    val url = URL(uriStr)
                    val connection = (url.openConnection() as HttpURLConnection).apply {
                        connectTimeout = 15000
                        readTimeout = 25000
                        instanceFollowRedirects = true
                        setRequestProperty("User-Agent", "SwarixMusicPlayer/1.0")
                    }

                    if (connection.responseCode in 200..299) {
                        connection.inputStream.use { input ->
                            FileOutputStream(tempFile).use { output ->
                                input.copyTo(output, bufferSize = 8192)
                            }
                        }
                    } else {
                        Log.w(TAG, "Download failed for ${song.title} with HTTP ${connection.responseCode}")
                        tempFile.delete()
                        return@withContext
                    }
                } else if (uriStr.startsWith("content://") || uriStr.startsWith("file://")) {
                    // Local URI: copy stream to cache
                    val inputStream = context.contentResolver.openInputStream(Uri.parse(uriStr))
                    if (inputStream != null) {
                        inputStream.use { input ->
                            FileOutputStream(tempFile).use { output ->
                                input.copyTo(output, bufferSize = 8192)
                            }
                        }
                    }
                }

                if (tempFile.exists() && tempFile.length() > 0) {
                    if (targetFile.exists()) targetFile.delete()
                    val success = tempFile.renameTo(targetFile)
                    if (success) {
                        val record = CachedSongRecord(
                            songId = song.id,
                            localPath = targetFile.absolutePath,
                            fileSizeBytes = targetFile.length(),
                            cachedAt = System.currentTimeMillis(),
                            lastAccessedAt = System.currentTimeMillis(),
                            title = song.title,
                            artist = song.artist,
                            durationMs = song.durationMs,
                            artworkUri = song.artworkUri,
                            isPinned = isPinned
                        )

                        mutex.withLock {
                            val map = _cachedRecords.value.toMutableMap()
                            map[song.id] = record
                            enforceLruLimitLocked(map)
                            updateState(map)
                            saveIndexLocked(map)
                        }
                        Log.d(TAG, "Cached track \"${song.title}\" (${formatBytes(targetFile.length())})")
                    } else {
                        tempFile.delete()
                    }
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Exception caching song ${song.title}: ${e.message}")
            if (tempFile.exists()) tempFile.delete()
        } finally {
            _isDownloading.value = _isDownloading.value - song.id
        }
    }

    /**
     * Enforces LRU cache size limit by evicting oldest unpinned tracks if total exceeds threshold.
     */
    private fun enforceLruLimitLocked(map: MutableMap<String, CachedSongRecord>) {
        var total = map.values.sumOf { it.fileSizeBytes }
        if (total <= maxCacheSizeBytes) return

        // Sort unpinned songs by last accessed time (oldest first)
        val evictCandidates = map.values
            .filter { !it.isPinned }
            .sortedBy { it.lastAccessedAt }

        for (candidate in evictCandidates) {
            if (total <= maxCacheSizeBytes) break
            try {
                val file = File(candidate.localPath)
                if (file.exists()) {
                    total -= file.length()
                    file.delete()
                }
            } catch (e: Exception) {
                Log.e(TAG, "Failed to evict ${candidate.songId}: ${e.message}")
            }
            map.remove(candidate.songId)
        }
    }

    private fun updateState(recordsMap: Map<String, CachedSongRecord>) {
        _cachedRecords.value = recordsMap
        _cachedSongIds.value = recordsMap.keys
        _totalCacheSizeBytes.value = recordsMap.values.sumOf { it.fileSizeBytes }
    }

    private fun saveIndexLocked(recordsMap: Map<String, CachedSongRecord>) {
        try {
            val jsonArray = JSONArray()
            recordsMap.values.forEach { jsonArray.put(it.toJson()) }
            indexFile.writeText(jsonArray.toString(2))
        } catch (e: Exception) {
            Log.e(TAG, "Failed to save cache index: ${e.message}")
        }
    }

    companion object {
        private const val TAG = "LocalStorageCacheMgr"

        @Volatile
        private var INSTANCE: LocalStorageCacheManager? = null

        fun getInstance(context: Context): LocalStorageCacheManager {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: LocalStorageCacheManager(context.applicationContext).also {
                    INSTANCE = it
                }
            }
        }

        fun formatBytes(bytes: Long): String {
            if (bytes <= 0) return "0 MB"
            val kb = bytes / 1024.0
            val mb = kb / 1024.0
            val gb = mb / 1024.0
            return when {
                gb >= 1.0 -> String.format("%.2f GB", gb)
                mb >= 1.0 -> String.format("%.1f MB", mb)
                else -> String.format("%.0f KB", kb)
            }
        }
    }
}
