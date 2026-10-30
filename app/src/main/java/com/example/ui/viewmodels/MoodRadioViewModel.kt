package com.example.ui.viewmodels

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.core.model.Song
import com.example.data.preferences.AiMetadataCacheDataStore
import com.example.data.preferences.SwarixPreferences
import com.google.firebase.Firebase
import com.google.firebase.ai.ai
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.withTimeout
import org.json.JSONArray

class MoodRadioViewModel(application: Application) : AndroidViewModel(application) {

    companion object {
        private const val TAG = "MoodRadioViewModel"
    }

    val aiCache = AiMetadataCacheDataStore(application)
    private val preferences = SwarixPreferences(application)
    private val database = com.example.data.local.SwarixDatabase.getDatabase(application)

    private val model by lazy {
        Firebase.ai.generativeModel("gemini-3.8-flash")
    }

    sealed interface UiState {
        data object Idle : UiState
        data object Loading : UiState
        data class Success(
            val songs: List<Song>,
            val mood: String,
            val isFromCache: Boolean
        ) : UiState
        data class Error(val message: String) : UiState
    }

    private val _uiState = MutableStateFlow<UiState>(UiState.Idle)
    val uiState = _uiState.asStateFlow()

    private val _activeMood = MutableStateFlow("Chill")
    val activeMood = _activeMood.asStateFlow()

    fun selectMood(mood: String) {
        _activeMood.value = mood
    }

    fun generateMoodPlaylist(
        mood: String,
        library: List<Song>,
        onPlaylistReady: ((List<Song>) -> Unit)? = null
    ) {
        _activeMood.value = mood
        viewModelScope.launch {
            _uiState.value = UiState.Loading
            val offlineOnly = preferences.offlineModeEnabled.first()

            if (offlineOnly) {
                loadFromCacheOrLocal(mood, library, onPlaylistReady)
                return@launch
            }

            try {
                val librarySummary = library.take(30).joinToString(separator = "\n") { 
                    "{id: '${it.id}', title: '${it.title}', artist: '${it.artist}', genre: '${it.genre}', mood: '${it.mood}'}"
                }

                val prompt = """
                    You are an intelligent music curator for Mood Radio.
                    Given this user music library:
                    $librarySummary
                    
                    Select 10 tracks that best match the mood: "$mood".
                    Return ONLY a JSON array of the matching song IDs, like:
                    ["id1", "id2", "id3", ...]
                """.trimIndent()

                val response = withTimeout(12_000L) {
                    model.generateContent(prompt)
                }

                val text = response.text ?: ""
                val start = text.indexOf('[')
                val end = text.lastIndexOf(']')

                if (start != -1 && end != -1 && end > start) {
                    val jsonArray = JSONArray(text.substring(start, end + 1))
                    val songIds = mutableListOf<String>()
                    for (i in 0 until jsonArray.length()) {
                        songIds.add(jsonArray.getString(i))
                    }

                    val playlistSongs = deduplicatePlaylist(library.filter { it.id in songIds })
                    if (playlistSongs.isNotEmpty()) {
                        // Cache in DataStore for 100% offline access
                        aiCache.cacheMoodPlaylist(mood, playlistSongs.map { it.id })
                        aiCache.cacheMultipleSongsMoods(playlistSongs.associate { it.id to listOf(mood) })

                        _uiState.value = UiState.Success(
                            songs = playlistSongs,
                            mood = mood,
                            isFromCache = false
                        )
                        onPlaylistReady?.invoke(playlistSongs)
                        return@launch
                    }
                }
                // If model output could not be parsed, fallback to DataStore cache
                loadFromCacheOrLocal(mood, library, onPlaylistReady)
            } catch (e: Exception) {
                Log.w(TAG, "Online Mood Radio failed (${e.message}), switching to offline DataStore cache")
                loadFromCacheOrLocal(mood, library, onPlaylistReady)
            }
        }
    }

    private fun deduplicatePlaylist(songs: List<Song>): List<Song> {
        val seen = mutableSetOf<String>()
        return songs.filter { song ->
            val key = "${song.title.lowercase().trim()}_${song.artist.lowercase().trim()}"
            if (seen.contains(key)) {
                false
            } else {
                seen.add(key)
                true
            }
        }
    }

    private suspend fun loadFromCacheOrLocal(
        mood: String,
        library: List<Song>,
        onPlaylistReady: ((List<Song>) -> Unit)?
    ) {
        val cachedIds = aiCache.getCachedPlaylistForMood(mood)
        val cachedSongs = library.filter { it.id in cachedIds }

        val rawFinalSongs = if (cachedSongs.isNotEmpty()) {
            cachedSongs
        } else {
            // Intelligent local acoustic match
            val localMatches = library.filter {
                it.mood.contains(mood, ignoreCase = true) ||
                it.genre.contains(mood, ignoreCase = true) ||
                it.title.contains(mood, ignoreCase = true)
            }
            if (localMatches.isNotEmpty()) {
                localMatches.take(15)
            } else {
                library.shuffled().take(10)
            }
        }
        val finalSongs = deduplicatePlaylist(rawFinalSongs)

        // Write back to DataStore cache
        if (finalSongs.isNotEmpty()) {
            aiCache.cacheMoodPlaylist(mood, finalSongs.map { it.id })
        }

        _uiState.value = UiState.Success(
            songs = finalSongs,
            mood = mood,
            isFromCache = true
        )
        onPlaylistReady?.invoke(finalSongs)
    }

    /**
     * Exports the active AI Mood Radio playlist as an M3U file using Room database mapping.
     */
    fun exportCurrentMoodPlaylist(
        context: android.content.Context,
        onResult: (Boolean, String) -> Unit
    ) {
        val state = _uiState.value
        if (state !is UiState.Success || state.songs.isEmpty()) {
            onResult(false, "No playlist generated to export.")
            return
        }

        viewModelScope.launch {
            val result = com.example.core.export.M3uPlaylistExporter.exportAndShareMoodPlaylist(
                context = context,
                mood = state.mood,
                songs = state.songs,
                musicDao = database.musicDao()
            )
            result.onSuccess {
                onResult(true, "Exported ${it.trackCount} tracks to ${it.file.name} (${it.formattedSize})")
            }.onFailure {
                onResult(false, "Export failed: ${it.message}")
            }
        }
    }
}
