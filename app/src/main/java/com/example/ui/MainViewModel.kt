package com.example.ui

import android.app.Application
import android.content.Context
import com.example.BuildConfig
import com.example.core.updater.AppUpdateInfo
import com.example.core.updater.AppUpdateManager
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.core.model.Artist
import com.example.core.model.EqualizerState
import com.example.core.model.Playlist
import com.example.core.model.Song
import com.example.data.local.SwarixDatabase
import com.example.data.preferences.SwarixPreferences
import com.example.data.repository.MusicRepository
import com.example.playback.PlayerManager
import com.example.playback.RepeatMode
import com.example.ui.components.NavItem
import com.example.core.cache.CachedSongRecord
import com.example.core.cache.LocalStorageCacheManager
import com.example.core.equalizer.EqualizerPresets
import com.example.core.model.FriendActivity
import com.example.core.model.ListeningStats
import com.example.core.smartplaylist.DailyMix
import com.example.core.smartplaylist.SmartPlaylistGenerator
import com.example.data.local.SearchQueryEntity
import com.example.data.repository.MockFriendActivityRepository
import com.example.playback.HapticIntensity
import com.example.playback.SyncSessionManager
import com.example.ui.theme.ThemeMode
import com.google.firebase.Firebase
import com.google.firebase.ai.ai
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.withTimeout
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import android.util.Log
import org.json.JSONArray

enum class SubScreen {
    NONE,
    NOW_PLAYING,
    QUEUE,
    LYRICS,
    EQUALIZER,
    DOWNLOADS,
    PLAYLIST_DETAIL,
    STATS,
    MOOD_RADIO,
    CROSSFADE,
    SYNC_SESSION,
    ARTIST_PROFILE,
    DIAGNOSTICS
}

class MainViewModel(application: Application) : AndroidViewModel(application) {

    private val _selectedArtist = MutableStateFlow<Pair<String, String>?>(null)
    val selectedArtist: StateFlow<Pair<String, String>?> = _selectedArtist.asStateFlow()

    private val database = SwarixDatabase.getDatabase(application)
    private val repository = MusicRepository(application, database.musicDao())
    
    fun openArtistProfile(name: String, artwork: String) {
        _selectedArtist.value = name to artwork
        _activeSubScreen.value = SubScreen.ARTIST_PROFILE
    }
    private val friendRepository = MockFriendActivityRepository()
    val preferences = SwarixPreferences(application)
    val aiCacheDataStore = com.example.data.preferences.AiMetadataCacheDataStore(application)
    val voiceCommandManager = com.example.speech.VoiceCommandManager(application)
    val batteryMonitor = com.example.core.battery.BatteryMonitor(application)
    val playerManager = PlayerManager.getInstance(application)
    val cacheManager = LocalStorageCacheManager.getInstance(application)

    val cachedSongIds: StateFlow<Set<String>> = cacheManager.cachedSongIds
    val cachedRecords: StateFlow<Map<String, CachedSongRecord>> = cacheManager.cachedRecords
    val totalCacheSizeBytes: StateFlow<Long> = cacheManager.totalCacheSizeBytes
    val isDownloadingSongIds: StateFlow<Set<String>> = cacheManager.isDownloading

    val allSongs: StateFlow<List<Song>> = repository.allSongs
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val favoriteSongs: StateFlow<List<Song>> = repository.favoriteSongs
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val cachedSongs: StateFlow<List<Song>> = combine(allSongs, cachedSongIds) { songs, ids ->
        songs.filter { ids.contains(it.id) || it.isLocal }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val recentlyPlayed: StateFlow<List<Song>> = repository.recentlyPlayed
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val playlists: StateFlow<List<Playlist>> = repository.allPlaylists
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val topArtistsSpotlight: StateFlow<List<Artist>> = recentlyPlayed.combine(allSongs) { recent, all ->
        recent.take(10).map { it.artist }.distinct().map { artistName ->
            val songs = all.filter { it.artist == artistName }
            Artist(
                name = artistName,
                songCount = songs.size,
                artworkUri = songs.firstOrNull()?.artworkUri ?: ""
            )
        }.take(5)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val friendActivity: StateFlow<List<FriendActivity>> = friendRepository.getFriendActivity()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val isSyncSessionActive = SyncSessionManager.isSyncSessionActive
    val sharedQueue = SyncSessionManager.sharedQueue
    val currentSyncSong = SyncSessionManager.currentSyncSong

    val listeningStats: StateFlow<ListeningStats> = repository.getListeningStats()

        .stateIn(
            viewModelScope,
            SharingStarted.WhileSubscribed(5000),
            ListeningStats(1420, 48, listOf("Arijit Singh" to 14, "Jasleen Royal" to 10), emptyList(), 7)
        )

    val currentSong: StateFlow<Song?> = playerManager.currentSong
    val isPlaying: StateFlow<Boolean> = playerManager.isPlaying
    val currentPositionMs: StateFlow<Long> = playerManager.currentPositionMs
    val durationMs: StateFlow<Long> = playerManager.durationMs
    val queue: StateFlow<List<Song>> = playerManager.queue
    val queueIndex: StateFlow<Int> = playerManager.queueIndex
    val isShuffle: StateFlow<Boolean> = playerManager.isShuffle
    val repeatMode: StateFlow<RepeatMode> = playerManager.repeatMode
    val sleepTimerMinutesRemaining: StateFlow<Int> = playerManager.sleepTimerMinutesRemaining
    val sleepTimerSecondsRemaining: StateFlow<Int> = playerManager.sleepTimerSecondsRemaining
    val isSleepTimerActive: StateFlow<Boolean> = playerManager.isSleepTimerActive
    val isSleepTimerFading: StateFlow<Boolean> = playerManager.isSleepTimerFading
    val sleepTimerFadeSeconds: StateFlow<Int> = playerManager.sleepTimerFadeSeconds

    val themeMode: StateFlow<ThemeMode> = preferences.themeMode
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), ThemeMode.DARK)

    val audioQuality: StateFlow<String> = preferences.audioQuality
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), "Very High (320 kbps)")

    val onboardingDone: StateFlow<Boolean> = preferences.onboardingDone
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), false)

    val crossfadeEnabled: StateFlow<Boolean> = preferences.crossfadeEnabled
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), true)

    val crossfadeSeconds: StateFlow<Int> = preferences.crossfadeSeconds
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 5)

    val gaplessPlayback: StateFlow<Boolean> = preferences.gaplessPlayback
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), true)

    val language: StateFlow<String> = preferences.language
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), "en")

    val region: StateFlow<String> = preferences.region
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), "Global")

    val batterySaverEnabled: StateFlow<Boolean> = preferences.batterySaverEnabled
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), false)

    val batteryLevel: StateFlow<Int> = batteryMonitor.batteryLevel
    val isLowBattery: StateFlow<Boolean> = batteryMonitor.isLowBattery
    val isBatterySaverEngaged: StateFlow<Boolean> = batteryMonitor.isBatterySaverEngaged

    val offlineModeEnabled: StateFlow<Boolean> = preferences.offlineModeEnabled
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), false)

    val filteredSongs: StateFlow<List<Song>> = combine(allSongs, offlineModeEnabled) { songs, offlineMode ->
        if (offlineMode) songs.filter { it.isLocal } else songs
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private val _equalizerState = MutableStateFlow(EqualizerState())
    val equalizerState: StateFlow<EqualizerState> = _equalizerState.asStateFlow()

    // Navigation state
    private val _currentTab = MutableStateFlow(NavItem.HOME)
    val currentTab: StateFlow<NavItem> = _currentTab.asStateFlow()

    private val _activeSubScreen = MutableStateFlow(SubScreen.NONE)
    val activeSubScreen: StateFlow<SubScreen> = _activeSubScreen.asStateFlow()

    private val _selectedPlaylist = MutableStateFlow<Playlist?>(null)
    val selectedPlaylist: StateFlow<Playlist?> = _selectedPlaylist.asStateFlow()

    private val _playlistSongs = MutableStateFlow<List<Song>>(emptyList())
    val playlistSongs: StateFlow<List<Song>> = _playlistSongs.asStateFlow()

    val userMoods: StateFlow<Set<String>> = preferences.userMoods
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), setOf("Chill", "Romance", "Focus"))

    private val _dailyMixes = MutableStateFlow<List<DailyMix>>(emptyList())
    val dailyMixes: StateFlow<List<DailyMix>> = _dailyMixes.asStateFlow()

    init {
        viewModelScope.launch {
            repository.initializeCatalogIfEmpty()
            // Schedule background AI metadata enrichment service for audio files
            com.example.workers.MetadataEnrichmentWorker.schedulePeriodic(application)
            com.example.workers.MetadataEnrichmentWorker.enqueueImmediate(application)
        }

        playerManager.onSongPlaybackCompleted = { song ->
            viewModelScope.launch {
                repository.recordPlayback(song.id)
            }
        }

        viewModelScope.launch {
            crossfadeEnabled.collect { enabled ->
                playerManager.setCrossfade(enabled, crossfadeSeconds.value)
            }
        }

        viewModelScope.launch {
            crossfadeSeconds.collect { seconds ->
                playerManager.setCrossfade(crossfadeEnabled.value, seconds)
            }
        }

        viewModelScope.launch {
            gaplessPlayback.collect { enabled ->
                playerManager.setGaplessPlayback(enabled)
            }
        }

        viewModelScope.launch {
            checkForAppUpdates()
        }

        viewModelScope.launch {
            combine(
                allSongs,
                recentlyPlayed,
                favoriteSongs,
                userMoods
            ) { songs, recent, favs, moods ->
                SmartPlaylistGenerator.generateDailyMixes(
                    allSongs = songs,
                    recentlyPlayed = recent,
                    favoriteSongs = favs,
                    preferredMoods = moods
                )
            }.collect { mixes ->
                _dailyMixes.value = mixes
            }
        }

        viewModelScope.launch {
            currentSong.collect { song ->
                if (song != null && _equalizerState.value.isAutoGenreEnabled) {
                    val adapted = EqualizerPresets.autoTuneForSong(song, _equalizerState.value)
                    updateEqualizer(adapted)
                }
            }
        }

        viewModelScope.launch {
            allSongs.collect { songs ->
                if (songs.isNotEmpty()) {
                    aiCacheDataStore.seedInitialCacheIfEmpty(songs)
                }
            }
        }

        viewModelScope.launch {
            batterySaverEnabled.collect { userPrefEnabled ->
                batteryMonitor.setManualBatterySaver(userPrefEnabled)
            }
        }

        viewModelScope.launch {
            batteryMonitor.isBatterySaverEngaged.collect { engaged ->
                playerManager.bassHapticEngine.setBatterySaverSuspension(engaged)
                com.example.workers.MetadataEnrichmentWorker.configureBatterySaverConstraints(application, engaged)
            }
        }

        com.example.workers.ProactiveSyncWorker.schedule(application)
        com.example.workers.RoomToFirestoreSyncWorker.schedule(application)
        com.example.workers.PlaylistCleanupWorker.schedule(application)
    }

    fun selectTab(tab: NavItem) {
        _currentTab.value = tab
        _activeSubScreen.value = SubScreen.NONE
    }

    val recentSearches: StateFlow<List<String>> = repository.getRecentSearches()
        .map { it.map { q -> q.query } }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private val _onlineSearchResults = MutableStateFlow<List<Song>>(emptyList())
    val onlineSearchResults: StateFlow<List<Song>> = _onlineSearchResults.asStateFlow()

    private val _isSearchingOnline = MutableStateFlow(false)
    val isSearchingOnline: StateFlow<Boolean> = _isSearchingOnline.asStateFlow()

    private var onlineSearchJob: Job? = null

    fun search(query: String) {
        if (query.isNotBlank()) {
            viewModelScope.launch {
                repository.insertSearchQuery(SearchQueryEntity(query))
            }
        }
        onlineSearchJob?.cancel()
        val trimmed = query.trim()
        if (trimmed.length >= 2) {
            onlineSearchJob = viewModelScope.launch {
                _isSearchingOnline.value = true
                delay(300)
                try {
                    val results = repository.searchOnline(trimmed)
                    _onlineSearchResults.value = results
                } catch (e: Exception) {
                    Log.e("MainViewModel", "Online search failed", e)
                } finally {
                    _isSearchingOnline.value = false
                }
            }
        } else {
            _onlineSearchResults.value = emptyList()
            _isSearchingOnline.value = false
        }
    }

    fun clearRecentSearches() {
        viewModelScope.launch {
            repository.clearSearchQueries()
        }
    }

    fun openSubScreen(screen: SubScreen) {
        _activeSubScreen.value = screen
    }

    fun closeSubScreen() {
        _activeSubScreen.value = SubScreen.NONE
    }

    fun openPlaylist(playlist: Playlist) {
        _selectedPlaylist.value = playlist
        viewModelScope.launch {
            repository.getSongsForPlaylist(playlist.id).collect { songs ->
                _playlistSongs.value = songs.ifEmpty { allSongs.value.take(6) }
            }
        }
        _activeSubScreen.value = SubScreen.PLAYLIST_DETAIL
    }

    fun playSong(song: Song, customQueue: List<Song>? = null) {
        val q = customQueue ?: if (_onlineSearchResults.value.any { it.id == song.id }) {
            _onlineSearchResults.value
        } else {
            allSongs.value
        }
        viewModelScope.launch {
            repository.insertSong(song)
            repository.recordPlayback(song.id)
        }
        playerManager.playSong(song, q)
    }

    private val _availableUpdate = MutableStateFlow<AppUpdateInfo?>(null)
    val availableUpdate: StateFlow<AppUpdateInfo?> = _availableUpdate.asStateFlow()

    private val _isDownloadingUpdate = MutableStateFlow(false)
    val isDownloadingUpdate: StateFlow<Boolean> = _isDownloadingUpdate.asStateFlow()

    private val _updateDownloadProgress = MutableStateFlow(0f)
    val updateDownloadProgress: StateFlow<Float> = _updateDownloadProgress.asStateFlow()

    fun dismissUpdateDialog() {
        _availableUpdate.value = null
    }

    fun startUpdate(context: Context) {
        val info = _availableUpdate.value ?: return
        viewModelScope.launch {
            _isDownloadingUpdate.value = true
            _updateDownloadProgress.value = 0f
            val apkFile = AppUpdateManager.downloadApk(context, info.apkDownloadUrl) { progress ->
                _updateDownloadProgress.value = progress
            }
            _isDownloadingUpdate.value = false
            if (apkFile != null && apkFile.exists()) {
                AppUpdateManager.installApk(context, apkFile)
                _availableUpdate.value = null
            }
        }
    }

    fun checkForAppUpdates() {
        viewModelScope.launch {
            try {
                val info = AppUpdateManager.checkForUpdate(BuildConfig.VERSION_NAME)
                if (info != null && info.hasUpdate) {
                    _availableUpdate.value = info
                }
            } catch (e: Exception) {
                Log.e("MainViewModel", "Check updates error", e)
            }
        }
    }

    fun togglePlayPause() = playerManager.togglePlayPause()
    fun playNext() = playerManager.playNext()
    fun playPrevious() = playerManager.playPrevious()
    fun seekTo(positionMs: Long) = playerManager.seekTo(positionMs)
    val isKaraokeModeActive: StateFlow<Boolean> = playerManager.isKaraokeModeActive
    fun setKaraokeMode(enabled: Boolean) = playerManager.setKaraokeMode(enabled)
    val isAudioNormalizationEnabled: StateFlow<Boolean> = playerManager.isAudioNormalizationEnabled
    fun setAudioNormalizationEnabled(enabled: Boolean) = playerManager.setAudioNormalizationEnabled(enabled)
    val playbackHealthStatus: StateFlow<String> = playerManager.playbackHealthManager.healthStatus
    val playbackRecoveryCount: StateFlow<Int> = playerManager.playbackHealthManager.recoveryCount
    fun resetPlayer() = playerManager.resetPlayer()
    fun getDiagnosticLogs() = playerManager.diagnosticLogger.getLogHistory()
    fun dumpDiagnosticLogs() = playerManager.dumpDiagnosticLogs()
    fun toggleShuffle() = playerManager.toggleShuffle()
    fun cycleRepeatMode() = playerManager.cycleRepeatMode()
    fun removeFromQueue(index: Int) = playerManager.removeFromQueue(index)
    fun clearQueue() = playerManager.clearQueue()
    fun setSleepTimer(minutes: Int, fadeSeconds: Int = 60, isEndOfTrack: Boolean = false) =
        playerManager.setSleepTimer(minutes, fadeSeconds, isEndOfTrack)
    fun addSleepTimerMinutes(minutesToAdd: Int) = playerManager.addSleepTimerMinutes(minutesToAdd)
    fun cancelSleepTimer() = playerManager.cancelSleepTimer()

    fun toggleFavorite(song: Song) {
        viewModelScope.launch {
            repository.toggleFavorite(song.id, song.isFavorite)
        }
    }

    fun createPlaylist(name: String, description: String) {
        viewModelScope.launch {
            repository.createPlaylist(name, description)
        }
    }

    fun updateEqualizer(newState: EqualizerState) {
        _equalizerState.value = newState
        playerManager.equalizerHelper.setEnabled(newState.isEnabled)
        playerManager.equalizerHelper.set10BandLevels(newState.bandLevels)
        playerManager.equalizerHelper.setBassBoost(newState.bassBoost)
        playerManager.equalizerHelper.setVirtualizer(newState.virtualizer)

        viewModelScope.launch {
            preferences.setEqualizerEnabled(newState.isEnabled)
            preferences.setSelectedPreset(newState.presetName)
            preferences.setBassBoost(newState.bassBoost)
            preferences.setVirtualizer(newState.virtualizer)
        }
    }

    fun setThemeMode(mode: ThemeMode) {
        viewModelScope.launch {
            preferences.setThemeMode(mode)
        }
    }

    fun setAudioQuality(quality: String) {
        viewModelScope.launch {
            preferences.setAudioQuality(quality)
        }
    }

    fun setLanguage(language: String) {
        viewModelScope.launch {
            preferences.setLanguage(language)
        }
    }

    fun setRegion(region: String) {
        viewModelScope.launch {
            preferences.setRegion(region)
        }
    }

    fun setBatterySaverEnabled(enabled: Boolean) {
        viewModelScope.launch {
            preferences.setBatterySaverEnabled(enabled)
            batteryMonitor.setManualBatterySaver(enabled)
        }
    }

    fun setOfflineModeEnabled(enabled: Boolean) {
        viewModelScope.launch {
            preferences.setOfflineModeEnabled(enabled)
        }
    }

    fun clearHistory() {
        viewModelScope.launch {
            repository.clearHistory()
        }
    }

    fun finishOnboarding(moods: Set<String>) {
        viewModelScope.launch {
            preferences.setUserMoods(moods)
            preferences.setOnboardingDone(true)
        }
    }

    fun rescanLibrary() {
        viewModelScope.launch {
            repository.scanLocalDeviceAudio()
            com.example.workers.MetadataEnrichmentWorker.enqueueImmediate(getApplication())
        }
    }

    fun runAiMetadataEnrichment() {
        com.example.workers.MetadataEnrichmentWorker.enqueueImmediate(getApplication())
    }

    fun setCrossfadeEnabled(enabled: Boolean) {
        viewModelScope.launch {
            preferences.setCrossfadeEnabled(enabled)
            playerManager.setCrossfade(enabled, crossfadeSeconds.value)
        }
    }

    fun setCrossfadeSeconds(seconds: Int) {
        viewModelScope.launch {
            preferences.setCrossfadeSeconds(seconds)
            playerManager.setCrossfade(crossfadeEnabled.value, seconds)
        }
    }

    fun setGaplessPlayback(enabled: Boolean) {
        viewModelScope.launch {
            preferences.setGaplessPlayback(enabled)
            playerManager.setGaplessPlayback(enabled)
        }
    }

    fun previewCrossfade(durationSeconds: Int) {
        playerManager.previewCrossfade(durationSeconds)
    }

    fun playDailyMix(mix: DailyMix) {
        if (mix.songs.isNotEmpty()) {
            playSong(mix.songs.first(), mix.songs)
        }
    }

    fun openDailyMix(mix: DailyMix) {
        _selectedPlaylist.value = mix.toPlaylist()
        _playlistSongs.value = mix.songs
        _activeSubScreen.value = SubScreen.PLAYLIST_DETAIL
    }

    fun regenerateDailyMixes() {
        _dailyMixes.value = SmartPlaylistGenerator.generateDailyMixes(
            allSongs = allSongs.value.shuffled(),
            recentlyPlayed = recentlyPlayed.value,
            favoriteSongs = favoriteSongs.value,
            preferredMoods = userMoods.value
        )
    }

    fun saveSmartPlaylist(mix: DailyMix) {
        viewModelScope.launch {
            val playlistId = repository.createPlaylist(mix.title, mix.reason, mix.coverUri)
            mix.songs.forEach { song ->
                repository.addSongToPlaylist(playlistId, song.id)
            }
        }
    }

    fun cacheSong(song: Song) {
        cacheManager.cacheSong(song)
    }

    fun removeCachedSong(songId: String) {
        cacheManager.removeCachedSong(songId)
    }

    fun clearOfflineCache() {
        cacheManager.clearEntireCache()
    }

    fun formatCacheBytes(bytes: Long): String {
        return LocalStorageCacheManager.formatBytes(bytes)
    }

    // ---------------------------------------------------------------------------------------------
    // Tactile Bass Haptics Controller
    // ---------------------------------------------------------------------------------------------
    val bassHapticsEnabled: StateFlow<Boolean> = playerManager.bassHapticEngine.isEnabled
    val bassHapticsIntensity: StateFlow<HapticIntensity> = playerManager.bassHapticEngine.intensity

    fun setBassHapticsEnabled(enabled: Boolean) {
        playerManager.bassHapticEngine.setEnabled(enabled)
    }

    fun setBassHapticsIntensity(intensity: HapticIntensity) {
        playerManager.bassHapticEngine.setIntensity(intensity)
    }

    // ---------------------------------------------------------------------------------------------
    // AI Smart Shuffle Powered by Gemini
    // ---------------------------------------------------------------------------------------------
    private val _isSmartShuffling = MutableStateFlow(false)
    val isSmartShuffling: StateFlow<Boolean> = _isSmartShuffling.asStateFlow()

    private val _smartShuffleMessage = MutableStateFlow<String?>(null)
    val smartShuffleMessage: StateFlow<String?> = _smartShuffleMessage.asStateFlow()

    fun clearSmartShuffleMessage() {
        _smartShuffleMessage.value = null
    }

    fun smartShuffleQueue(onResult: ((Boolean, String) -> Unit)? = null) {
        val currentQueue = playerManager.queue.value
        if (currentQueue.size < 2) {
            val msg = "Queue needs at least 2 songs for Smart Shuffle"
            _smartShuffleMessage.value = msg
            onResult?.invoke(false, msg)
            return
        }

        viewModelScope.launch {
            _isSmartShuffling.value = true
            try {
                val hour = java.util.Calendar.getInstance().get(java.util.Calendar.HOUR_OF_DAY)
                val timeOfDay = when (hour) {
                    in 5..11 -> "Morning Energize"
                    in 12..16 -> "Afternoon Focus"
                    in 17..21 -> "Evening Groove"
                    else -> "Late-Night Chill"
                }

                val recent = recentlyPlayed.value.take(6)
                val favs = favoriteSongs.value.take(6)
                val topArtists = (recent + favs).map { it.artist }.distinct().take(4)
                val topGenres = (recent + favs).map { it.genre }.filter { it.isNotBlank() }.distinct().take(4)

                val queueSummary = currentQueue.map { s ->
                    "{id: '${s.id}', title: '${s.title}', artist: '${s.artist}', genre: '${s.genre}', mood: '${s.mood}'}"
                }.joinToString(",\n")

                val model = Firebase.ai.generativeModel("gemini-3.8-flash")
                val prompt = """
                    You are an intelligent music queue curator and dynamic smart DJ.
                    Reorder the following music playback queue based on the current time of day ($timeOfDay, hour $hour:00) and user habits (favorite artists: ${topArtists.joinToString()}, preferred genres: ${topGenres.joinToString()}).
                    Current queue:
                    [$queueSummary]

                    Reorder the tracks to create an ultra-smooth acoustic transition and personalized vibe flow for $timeOfDay.
                    Return ONLY a JSON array of the song IDs in the newly ordered sequence:
                    ["id1", "id2", "id3", ...]
                """.trimIndent()

                val response = withTimeout(15_000L) {
                    model.generateContent(prompt)
                }

                val text = response.text ?: ""
                val start = text.indexOf('[')
                val end = text.lastIndexOf(']')

                if (start != -1 && end != -1 && end > start) {
                    val jsonArray = JSONArray(text.substring(start, end + 1))
                    val newOrderIds = mutableListOf<String>()
                    for (i in 0 until jsonArray.length()) {
                        newOrderIds.add(jsonArray.getString(i))
                    }

                    val reorderedList = mutableListOf<Song>()
                    val queueMap = currentQueue.associateBy { it.id }

                    for (id in newOrderIds) {
                        queueMap[id]?.let {
                            if (!reorderedList.contains(it)) {
                                reorderedList.add(it)
                            }
                        }
                    }

                    for (song in currentQueue) {
                        if (!reorderedList.contains(song)) {
                            reorderedList.add(song)
                        }
                    }

                    if (reorderedList.isNotEmpty()) {
                        playerManager.replaceQueue(reorderedList)
                        // Cache in DataStore for offline Smart Shuffle access
                        aiCacheDataStore.cacheSmartShuffleOrder(timeOfDay, reorderedList.map { it.id })
                        aiCacheDataStore.cacheMultipleSongsMoods(reorderedList.associate { it.id to listOf(it.mood) })

                        val msg = "Smart Shuffle: Queue personalized for $timeOfDay!"
                        _smartShuffleMessage.value = msg
                        onResult?.invoke(true, msg)
                    } else {
                        throw Exception("Empty reordered list")
                    }
                } else {
                    throw Exception("No valid JSON array found")
                }
            } catch (e: Exception) {
                val hour = java.util.Calendar.getInstance().get(java.util.Calendar.HOUR_OF_DAY)
                val fallbackTime = if (hour in 6..17) "Daytime" else "Night"
                val reordered = smartFallbackQueueOrder(currentQueue, hour)
                playerManager.replaceQueue(reordered)
                val msg = "Smart Shuffle (Offline DataStore cache): $fallbackTime flow applied!"
                _smartShuffleMessage.value = msg
                onResult?.invoke(true, msg)
            } finally {
                _isSmartShuffling.value = false
            }
        }
    }

    private fun smartFallbackQueueOrder(queue: List<Song>, hour: Int): List<Song> {
        val current = playerManager.currentSong.value
        val others = queue.filter { it.id != current?.id }

        val timeSlot = if (hour in 6..11) "morning" else if (hour in 12..17) "afternoon" else if (hour in 18..22) "evening" else "night"
        val cachedSlotIds = aiCacheDataStore.getCachedSmartShuffleForTime(timeSlot)

        val sortedOthers = if (cachedSlotIds.isNotEmpty()) {
            val map = others.associateBy { it.id }
            val fromCache = cachedSlotIds.mapNotNull { map[it] }
            val missing = others.filter { !fromCache.contains(it) }
            fromCache + missing
        } else if (hour in 22..24 || hour in 0..6) {
            others.sortedByDescending {
                val cachedMoods = aiCacheDataStore.getCachedMoodsForSong(it.id)
                it.mood.contains("Chill", true) || it.genre.contains("Acoustic", true) || cachedMoods.any { m -> m.contains("Chill", true) }
            }
        } else if (hour in 7..11) {
            others.sortedByDescending {
                val cachedMoods = aiCacheDataStore.getCachedMoodsForSong(it.id)
                it.genre.contains("Pop", true) || it.mood.contains("Energy", true) || cachedMoods.any { m -> m.contains("Energy", true) }
            }
        } else {
            others.shuffled()
        }
        return if (current != null) listOf(current) + sortedOthers else sortedOthers
    }

    data class VoiceCommandHistoryItem(
        val id: String = java.util.UUID.randomUUID().toString(),
        val spokenText: String,
        val resultMessage: String,
        val timestamp: Long = System.currentTimeMillis(),
        val resultType: String,
        val param: String
    )

    private val _voiceCommandHistory = MutableStateFlow<List<VoiceCommandHistoryItem>>(emptyList())
    val voiceCommandHistory: StateFlow<List<VoiceCommandHistoryItem>> = _voiceCommandHistory.asStateFlow()

    fun recordVoiceCommand(spokenText: String, result: com.example.speech.VoiceCommandResult) {
        val (type, param, msg) = when (result) {
            is com.example.speech.VoiceCommandResult.MoodRadio -> Triple("MoodRadio", result.mood, result.message)
            is com.example.speech.VoiceCommandResult.Equalizer -> Triple("Equalizer", result.updatedState.presetName, result.message)
            is com.example.speech.VoiceCommandResult.PlaybackAction -> Triple("Playback", result.action, result.message)
            is com.example.speech.VoiceCommandResult.Unrecognized -> Triple("Unrecognized", result.spokenText, result.suggestion)
        }
        val item = VoiceCommandHistoryItem(spokenText = spokenText, resultMessage = msg, resultType = type, param = param)
        _voiceCommandHistory.value = listOf(item) + _voiceCommandHistory.value.take(19)
    }

    fun retriggerVoiceCommand(
        item: VoiceCommandHistoryItem,
        onOpenMoodRadio: (String) -> Unit = {},
        onOpenEqualizer: () -> Unit = {}
    ) {
        when (item.resultType) {
            "Equalizer" -> {
                val preset = EqualizerPresets.PRESETS[item.param] ?: EqualizerPresets.PRESETS["Bass Boost"]
                if (preset != null) {
                    val updated = equalizerState.value.copy(
                        isEnabled = true,
                        presetName = preset.name,
                        bandLevels = preset.bandLevels,
                        bassBoost = preset.bassBoost,
                        virtualizer = preset.virtualizer
                    )
                    updateEqualizer(updated)
                    onOpenEqualizer()
                }
            }
            "MoodRadio" -> {
                onOpenMoodRadio(item.param)
            }
            "Playback" -> {
                when (item.param.lowercase()) {
                    "resume", "play" -> if (!isPlaying.value) togglePlayPause()
                    "pause", "stop" -> if (isPlaying.value) togglePlayPause()
                    "next" -> playNext()
                    "previous" -> playPrevious()
                    "smart_shuffle" -> smartShuffleQueue()
                }
            }
        }
    }

    fun clearVoiceCommandHistory() {
        _voiceCommandHistory.value = emptyList()
    }

    fun executeVoiceCommand(
        result: com.example.speech.VoiceCommandResult,
        onOpenMoodRadio: (String) -> Unit = {},
        onOpenEqualizer: () -> Unit = {}
    ) {
        when (result) {
            is com.example.speech.VoiceCommandResult.MoodRadio -> {
                onOpenMoodRadio(result.mood)
            }
            is com.example.speech.VoiceCommandResult.Equalizer -> {
                updateEqualizer(result.updatedState)
                onOpenEqualizer()
            }
            is com.example.speech.VoiceCommandResult.PlaybackAction -> {
                when (result.action.lowercase()) {
                    "resume", "play" -> if (!isPlaying.value) togglePlayPause()
                    "pause", "stop" -> if (isPlaying.value) togglePlayPause()
                    "next" -> playNext()
                    "previous" -> playPrevious()
                    "smart_shuffle" -> smartShuffleQueue()
                }
            }
            is com.example.speech.VoiceCommandResult.Unrecognized -> {}
        }
    }

    override fun onCleared() {
        super.onCleared()
        voiceCommandManager.release()
        batteryMonitor.release()
    }
}
