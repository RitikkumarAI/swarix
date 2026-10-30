package com.example.data.preferences

import android.content.Context
import android.util.Log
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.example.core.model.Song
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import org.json.JSONArray
import org.json.JSONObject

private val Context.aiCacheDataStore by preferencesDataStore(name = "swarix_ai_metadata_cache")

/**
 * DataStore persistence layer for caching AI-generated mood tags, sub-genres,
 * dynamic Mood Radio playlists, and time-of-day Smart Shuffle sequences.
 *
 * Ensures 100% offline accessibility for Mood Radio and Smart Shuffle features
 * when there is no internet connection.
 */
class AiMetadataCacheDataStore(private val context: Context) {

    private val scope = CoroutineScope(Dispatchers.IO)

    companion object {
        private const val TAG = "AiMetadataCacheDataStore"
        val KEY_CACHED_SONG_MOODS = stringPreferencesKey("cached_song_moods_json")
        val KEY_CACHED_SONG_SUBGENRES = stringPreferencesKey("cached_song_subgenres_json")
        val KEY_CACHED_MOOD_PLAYLISTS = stringPreferencesKey("cached_mood_playlists_json")
        val KEY_CACHED_SMART_SHUFFLE_ORDERS = stringPreferencesKey("cached_smart_shuffle_orders_json")
        val KEY_CACHED_LYRIC_TRANSLATIONS = stringPreferencesKey("cached_lyric_translations_json")
        val KEY_LAST_CACHE_TIME = longPreferencesKey("last_ai_cache_time")
    }

    private val _cachedMoodsMemory = MutableStateFlow<Map<String, List<String>>>(emptyMap())
    val cachedMoodsMemory = _cachedMoodsMemory.asStateFlow()

    private val _cachedMoodPlaylistsMemory = MutableStateFlow<Map<String, List<String>>>(emptyMap())
    val cachedMoodPlaylistsMemory = _cachedMoodPlaylistsMemory.asStateFlow()

    private val _cachedSmartShuffleMemory = MutableStateFlow<Map<String, List<String>>>(emptyMap())
    val cachedSmartShuffleMemory = _cachedSmartShuffleMemory.asStateFlow()

    init {
        scope.launch {
            context.aiCacheDataStore.data.collect { prefs ->
                val moodsJson = prefs[KEY_CACHED_SONG_MOODS] ?: "{}"
                val playlistsJson = prefs[KEY_CACHED_MOOD_PLAYLISTS] ?: "{}"
                val shuffleJson = prefs[KEY_CACHED_SMART_SHUFFLE_ORDERS] ?: "{}"

                _cachedMoodsMemory.value = parseJsonToMapList(moodsJson)
                _cachedMoodPlaylistsMemory.value = parseJsonToMapList(playlistsJson)
                _cachedSmartShuffleMemory.value = parseJsonToMapList(shuffleJson)
            }
        }
    }

    val cachedSongMoodsFlow: Flow<Map<String, List<String>>> = context.aiCacheDataStore.data.map { prefs ->
        parseJsonToMapList(prefs[KEY_CACHED_SONG_MOODS] ?: "{}")
    }

    val cachedMoodPlaylistsFlow: Flow<Map<String, List<String>>> = context.aiCacheDataStore.data.map { prefs ->
        parseJsonToMapList(prefs[KEY_CACHED_MOOD_PLAYLISTS] ?: "{}")
    }

    val cachedSmartShuffleFlow: Flow<Map<String, List<String>>> = context.aiCacheDataStore.data.map { prefs ->
        parseJsonToMapList(prefs[KEY_CACHED_SMART_SHUFFLE_ORDERS] ?: "{}")
    }

    suspend fun cacheSongMood(songId: String, moods: List<String>, subgenre: String = "") {
        context.aiCacheDataStore.edit { prefs ->
            val existingMoods = JSONObject(prefs[KEY_CACHED_SONG_MOODS] ?: "{}")
            val arr = JSONArray()
            moods.forEach { arr.put(it) }
            existingMoods.put(songId, arr)
            prefs[KEY_CACHED_SONG_MOODS] = existingMoods.toString()

            if (subgenre.isNotBlank()) {
                val existingGenres = JSONObject(prefs[KEY_CACHED_SONG_SUBGENRES] ?: "{}")
                existingGenres.put(songId, subgenre)
                prefs[KEY_CACHED_SONG_SUBGENRES] = existingGenres.toString()
            }
            prefs[KEY_LAST_CACHE_TIME] = System.currentTimeMillis()
        }
    }

    suspend fun cacheMultipleSongsMoods(map: Map<String, List<String>>) {
        context.aiCacheDataStore.edit { prefs ->
            val existingMoods = JSONObject(prefs[KEY_CACHED_SONG_MOODS] ?: "{}")
            map.forEach { (songId, moods) ->
                val arr = JSONArray()
                moods.forEach { arr.put(it) }
                existingMoods.put(songId, arr)
            }
            prefs[KEY_CACHED_SONG_MOODS] = existingMoods.toString()
            prefs[KEY_LAST_CACHE_TIME] = System.currentTimeMillis()
        }
    }

    suspend fun cacheMoodPlaylist(mood: String, songIds: List<String>) {
        if (songIds.isEmpty()) return
        context.aiCacheDataStore.edit { prefs ->
            val existing = JSONObject(prefs[KEY_CACHED_MOOD_PLAYLISTS] ?: "{}")
            val arr = JSONArray()
            songIds.forEach { arr.put(it) }
            existing.put(mood.trim().lowercase(), arr)
            // Also store capitalized title key for convenient lookup
            existing.put(mood.trim().replaceFirstChar { it.uppercase() }, arr)
            prefs[KEY_CACHED_MOOD_PLAYLISTS] = existing.toString()
            prefs[KEY_LAST_CACHE_TIME] = System.currentTimeMillis()
        }
        Log.d(TAG, "Cached offline playlist for mood '$mood' with ${songIds.size} tracks")
    }

    suspend fun cacheSmartShuffleOrder(timeSlot: String, songIds: List<String>) {
        if (songIds.isEmpty()) return
        context.aiCacheDataStore.edit { prefs ->
            val existing = JSONObject(prefs[KEY_CACHED_SMART_SHUFFLE_ORDERS] ?: "{}")
            val arr = JSONArray()
            songIds.forEach { arr.put(it) }
            existing.put(timeSlot.trim().lowercase(), arr)
            prefs[KEY_CACHED_SMART_SHUFFLE_ORDERS] = existing.toString()
            prefs[KEY_LAST_CACHE_TIME] = System.currentTimeMillis()
        }
        Log.d(TAG, "Cached offline smart shuffle for '$timeSlot' with ${songIds.size} tracks")
    }

    fun getCachedMoodsForSong(songId: String): List<String> {
        return _cachedMoodsMemory.value[songId] ?: emptyList()
    }

    fun getCachedPlaylistForMood(mood: String): List<String> {
        val memory = _cachedMoodPlaylistsMemory.value
        return memory[mood.trim().lowercase()]
            ?: memory[mood.trim().replaceFirstChar { it.uppercase() }]
            ?: memory[mood.trim()]
            ?: emptyList()
    }

    fun getCachedSmartShuffleForTime(timeSlot: String): List<String> {
        val memory = _cachedSmartShuffleMemory.value
        return memory[timeSlot.trim().lowercase()] ?: emptyList()
    }

    /**
     * Seeds initial AI tags and mood playlists into DataStore from existing library
     * so that users launching in offline mode have instant access to Mood Radio & Smart Shuffle.
     */
    suspend fun seedInitialCacheIfEmpty(library: List<Song>) {
        if (library.isEmpty()) return

        context.aiCacheDataStore.edit { prefs ->
            val existingPlaylists = prefs[KEY_CACHED_MOOD_PLAYLISTS]
            if (existingPlaylists.isNullOrBlank() || existingPlaylists == "{}") {
                val moods = listOf("Chill", "Energy", "Relax", "Focus", "Party", "Romance", "Workout")
                val playlistsObj = JSONObject()
                val songMoodsObj = JSONObject()

                moods.forEach { mood ->
                    // Find tracks matching mood or genre
                    val matching = library.filter { s ->
                        s.mood.contains(mood, ignoreCase = true) ||
                        s.genre.contains(mood, ignoreCase = true) ||
                        s.title.contains(mood, ignoreCase = true)
                    }
                    val selected = if (matching.isNotEmpty()) {
                        matching.take(15)
                    } else {
                        // Distribute evenly
                        library.shuffled().take(10)
                    }

                    val arr = JSONArray()
                    selected.forEach { arr.put(it.id) }
                    playlistsObj.put(mood.lowercase(), arr)
                    playlistsObj.put(mood, arr)

                    selected.forEach { song ->
                        val currentArr = songMoodsObj.optJSONArray(song.id) ?: JSONArray()
                        var alreadyHas = false
                        for (i in 0 until currentArr.length()) {
                            if (currentArr.getString(i).equals(mood, ignoreCase = true)) alreadyHas = true
                        }
                        if (!alreadyHas) currentArr.put(mood)
                        songMoodsObj.put(song.id, currentArr)
                    }
                }

                prefs[KEY_CACHED_MOOD_PLAYLISTS] = playlistsObj.toString()
                prefs[KEY_CACHED_SONG_MOODS] = songMoodsObj.toString()

                // Seed Smart Shuffle for daytime and nighttime slots
                val shuffleObj = JSONObject()
                val daySlot = JSONArray()
                val nightSlot = JSONArray()
                library.shuffled().take(20).forEach { daySlot.put(it.id) }
                library.shuffled().take(20).forEach { nightSlot.put(it.id) }
                shuffleObj.put("morning", daySlot)
                shuffleObj.put("afternoon", daySlot)
                shuffleObj.put("evening", nightSlot)
                shuffleObj.put("night", nightSlot)
                shuffleObj.put("late-night", nightSlot)
                prefs[KEY_CACHED_SMART_SHUFFLE_ORDERS] = shuffleObj.toString()

                prefs[KEY_LAST_CACHE_TIME] = System.currentTimeMillis()
                Log.d(TAG, "Seeded initial AI mood playlists and Smart Shuffle cache in DataStore")
            }
        }
    }

    private fun parseJsonToMapList(jsonStr: String): Map<String, List<String>> {
        val result = mutableMapOf<String, List<String>>()
        if (jsonStr.isBlank() || jsonStr == "{}") return result
        try {
            val json = JSONObject(jsonStr)
            val keys = json.keys()
            while (keys.hasNext()) {
                val key = keys.next()
                val array = json.optJSONArray(key)
                if (array != null) {
                    val list = mutableListOf<String>()
                    for (i in 0 until array.length()) {
                        list.add(array.getString(i))
                    }
                    result[key] = list
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error parsing DataStore JSON: ${e.message}")
        }
        return result
    }

    suspend fun cacheLyricTranslation(songId: String, langCode: String, translatedText: String) {
        context.aiCacheDataStore.edit { prefs ->
            val existing = JSONObject(prefs[KEY_CACHED_LYRIC_TRANSLATIONS] ?: "{}")
            val songObj = existing.optJSONObject(songId) ?: JSONObject()
            songObj.put(langCode.lowercase(), translatedText)
            existing.put(songId, songObj)
            prefs[KEY_CACHED_LYRIC_TRANSLATIONS] = existing.toString()
        }
    }

    suspend fun getCachedLyricTranslation(songId: String, langCode: String): String? {
        val prefs = context.aiCacheDataStore.data.first()
        val jsonStr = prefs[KEY_CACHED_LYRIC_TRANSLATIONS] ?: "{}"
        try {
            val existing = JSONObject(jsonStr)
            val songObj = existing.optJSONObject(songId)
            return songObj?.optString(langCode.lowercase())?.takeIf { it.isNotBlank() }
        } catch (e: Exception) {
            return null
        }
    }
}
