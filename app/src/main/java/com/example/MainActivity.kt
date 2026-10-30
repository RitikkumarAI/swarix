package com.example

import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.example.ui.components.PerformanceTracker
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.ui.unit.dp
import com.example.ui.components.VoiceCommandSheet
import com.example.ui.viewmodels.MoodRadioViewModel
import com.example.ui.MainViewModel
import com.example.ui.SubScreen
import com.example.ui.components.MiniPlayer
import com.example.ui.components.NavItem
import com.example.ui.components.SwarixBottomNavigation
import com.example.ui.screens.ai.SwarixAiScreen
import com.example.ui.screens.downloads.DownloadsScreen
import com.example.ui.screens.equalizer.EqualizerScreen
import com.example.ui.screens.home.HomeScreen
import com.example.ui.screens.library.LibraryScreen
import com.example.ui.screens.lyrics.LyricsScreen
import com.example.ui.screens.onboarding.OnboardingScreen
import com.example.ui.screens.player.NowPlayingScreen
import com.example.ui.screens.artist.ArtistProfileScreen
import com.example.ui.screens.playlist.PlaylistDetailScreen
import com.example.ui.screens.queue.PlayQueueScreen
import com.example.ui.screens.search.SearchScreen
import com.example.ui.screens.settings.CrossfadeSettingsScreen
import com.example.ui.screens.settings.SettingsScreen
import com.example.ui.screens.splash.SplashScreen
import com.example.ui.screens.stats.ListeningStatsScreen
import com.example.ui.screens.ai.MoodRadioScreen
import com.example.ui.screens.sync.SyncSessionScreen
import com.example.ui.theme.LocalSwarixColors
import com.example.ui.theme.SwarixTheme
import com.google.firebase.FirebaseApp
import com.google.firebase.appcheck.FirebaseAppCheck
import com.google.firebase.appcheck.debug.DebugAppCheckProviderFactory
import com.google.firebase.appcheck.recaptcha.RecaptchaAppCheckProviderFactory
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        // Configure App Check before any UI or Firebase operations
        configureAppCheck(this, intent)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            PerformanceTracker.startTracking(this)
        }

        enableEdgeToEdge()
        
        // Request 120Hz refresh rate safely
        try {
            val display = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                display
            } else {
                @Suppress("DEPRECATION")
                window.windowManager?.defaultDisplay
            }
            val bestModeId = display?.supportedModes?.maxByOrNull { it.refreshRate }?.modeId
            if (bestModeId != null && bestModeId != 0) {
                window.attributes = window.attributes.apply {
                    preferredDisplayModeId = bestModeId
                }
            }
        } catch (e: Exception) {
            Log.w("MainActivity", "Could not set 120Hz display mode", e)
        }

        setContent {
            val mainViewModel: MainViewModel = viewModel()
            val themeMode by mainViewModel.themeMode.collectAsStateWithLifecycle()

            SwarixTheme(themeMode = themeMode) {
                SwarixApp(mainViewModel = mainViewModel)
            }
        }
    }
}

private const val TAG = "AppCheckInit"
private const val DEBUG_TOKEN_EXTRA = "FIREBASE_APPCHECK_DEBUG_TOKEN"
private const val DEBUG_STORE_PREFIX = "com.google.firebase.appcheck.debug.store."
private const val DEBUG_SECRET_KEY = "com.google.firebase.appcheck.debug.DEBUG_SECRET"
private const val RECAPTCHA_SITE_KEY_RES = "recaptcha_site_key"

fun configureAppCheck(context: Context, intent: Intent?) {
    try {
        if (FirebaseApp.getApps(context).isEmpty()) {
            FirebaseApp.initializeApp(context)
        }

        if (BuildConfig.DEBUG) {
            val debugToken = intent?.getStringExtra(DEBUG_TOKEN_EXTRA)
            if (!debugToken.isNullOrEmpty()) {
                val persistenceKey = FirebaseApp.getInstance().persistenceKey
                val prefs = context.getSharedPreferences(
                    "$DEBUG_STORE_PREFIX$persistenceKey",
                    Context.MODE_PRIVATE
                )
                prefs.edit()
                    .putString(DEBUG_SECRET_KEY, debugToken)
                    .commit()
                Log.d(TAG, "App Check debug secret configured successfully.")
            }
            FirebaseAppCheck.getInstance().installAppCheckProviderFactory(
                DebugAppCheckProviderFactory.getInstance()
            )
            Log.d(TAG, "DebugAppCheckProviderFactory installed.")
        } else {
            val resId = context.resources.getIdentifier(RECAPTCHA_SITE_KEY_RES, "string", context.packageName)
            if (resId != 0) {
                val siteKey = context.getString(resId)
                if (siteKey.isNotBlank()) {
                    FirebaseAppCheck.getInstance().installAppCheckProviderFactory(
                        RecaptchaAppCheckProviderFactory.getInstance(siteKey)
                    )
                    Log.d(TAG, "RecaptchaAppCheckProviderFactory installed.")
                }
            }
        }
    } catch (e: Exception) {
        Log.e(TAG, "Failed to initialize Firebase App Check", e)
    }
}

@Composable
fun SwarixApp(mainViewModel: MainViewModel) {
    var showSplash by remember { mutableStateOf(true) }
    val onboardingDone by mainViewModel.onboardingDone.collectAsStateWithLifecycle()
    val colors = LocalSwarixColors.current

    // Phase 1: Splash / Loading Screen
    if (showSplash) {
        SplashScreen(
            onTimeout = { showSplash = false }
        )
        return
    }

    // Phase 2: Startup / Onboarding Flow
    if (!onboardingDone) {
        OnboardingScreen(
            onFinishOnboarding = { moods ->
                mainViewModel.finishOnboarding(moods)
            }
        )
        return
    }

    // Phase 3: Main Swarix Application Dashboard
    val currentTab by mainViewModel.currentTab.collectAsStateWithLifecycle()
    val activeSubScreen by mainViewModel.activeSubScreen.collectAsStateWithLifecycle()
    val currentSong by mainViewModel.currentSong.collectAsStateWithLifecycle()
    val isPlaying by mainViewModel.isPlaying.collectAsStateWithLifecycle()
    val progressMs by mainViewModel.currentPositionMs.collectAsStateWithLifecycle()
    val durationMs by mainViewModel.durationMs.collectAsStateWithLifecycle()
    val queue by mainViewModel.queue.collectAsStateWithLifecycle()
    val queueIndex by mainViewModel.queueIndex.collectAsStateWithLifecycle()
    val isShuffle by mainViewModel.isShuffle.collectAsStateWithLifecycle()
    val repeatMode by mainViewModel.repeatMode.collectAsStateWithLifecycle()
    val equalizerState by mainViewModel.equalizerState.collectAsStateWithLifecycle()
    val audioQuality by mainViewModel.audioQuality.collectAsStateWithLifecycle()
    val themeMode by mainViewModel.themeMode.collectAsStateWithLifecycle()
    val crossfadeEnabled by mainViewModel.crossfadeEnabled.collectAsStateWithLifecycle()
    val crossfadeSeconds by mainViewModel.crossfadeSeconds.collectAsStateWithLifecycle()
    val gaplessPlayback by mainViewModel.gaplessPlayback.collectAsStateWithLifecycle()
    val sleepTimerMinutesRemaining by mainViewModel.sleepTimerMinutesRemaining.collectAsStateWithLifecycle()
    val sleepTimerSecondsRemaining by mainViewModel.sleepTimerSecondsRemaining.collectAsStateWithLifecycle()
    val isSleepTimerFading by mainViewModel.isSleepTimerFading.collectAsStateWithLifecycle()
    val sleepTimerFadeSeconds by mainViewModel.sleepTimerFadeSeconds.collectAsStateWithLifecycle()

    val allSongs by mainViewModel.allSongs.collectAsStateWithLifecycle()
    val favoriteSongs by mainViewModel.favoriteSongs.collectAsStateWithLifecycle()
    val recentlyPlayed by mainViewModel.recentlyPlayed.collectAsStateWithLifecycle()
    val playlists by mainViewModel.playlists.collectAsStateWithLifecycle()
    val friendActivity by mainViewModel.friendActivity.collectAsStateWithLifecycle()
    val listeningStats by mainViewModel.listeningStats.collectAsStateWithLifecycle()
    val selectedPlaylist by mainViewModel.selectedPlaylist.collectAsStateWithLifecycle()
    val playlistSongs by mainViewModel.playlistSongs.collectAsStateWithLifecycle()
    val dailyMixes by mainViewModel.dailyMixes.collectAsStateWithLifecycle()
    val topArtists by mainViewModel.topArtistsSpotlight.collectAsStateWithLifecycle()
    val currentSyncSong by mainViewModel.currentSyncSong.collectAsStateWithLifecycle()
    val selectedArtist by mainViewModel.selectedArtist.collectAsStateWithLifecycle()
    val language by mainViewModel.language.collectAsStateWithLifecycle()
    val region by mainViewModel.region.collectAsStateWithLifecycle()
    val bassHapticsEnabled by mainViewModel.bassHapticsEnabled.collectAsStateWithLifecycle()
    val bassHapticsIntensity by mainViewModel.bassHapticsIntensity.collectAsStateWithLifecycle()
    val isSmartShuffling by mainViewModel.isSmartShuffling.collectAsStateWithLifecycle()

    val batteryLevel by mainViewModel.batteryLevel.collectAsStateWithLifecycle()
    val isLowBattery by mainViewModel.isLowBattery.collectAsStateWithLifecycle()
    val isBatterySaverEngaged by mainViewModel.isBatterySaverEngaged.collectAsStateWithLifecycle()
    val context = androidx.compose.ui.platform.LocalContext.current

    var showVoiceCommandSheet by remember { mutableStateOf(false) }
    var requestedVoiceMood by remember { mutableStateOf<String?>(null) }

    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    // Handle Hardware Back Button
    BackHandler(enabled = activeSubScreen != SubScreen.NONE || currentTab != NavItem.HOME) {
        if (activeSubScreen != SubScreen.NONE) {
            mainViewModel.closeSubScreen()
        } else if (currentTab != NavItem.HOME) {
            mainViewModel.selectTab(NavItem.HOME)
        }
    }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
        snackbarHost = { SnackbarHost(snackbarHostState) },
        floatingActionButton = {
            if (activeSubScreen == SubScreen.NONE) {
                FloatingActionButton(
                    onClick = { showVoiceCommandSheet = true },
                    containerColor = colors.primaryAccent,
                    contentColor = colors.mainBackground,
                    shape = CircleShape,
                    modifier = Modifier
                        .padding(bottom = 74.dp)
                        .testTag("fab_voice_command")
                ) {
                    Icon(
                        imageVector = Icons.Default.Mic,
                        contentDescription = "Voice Commands",
                        modifier = Modifier.size(24.dp)
                    )
                }
            }
        },
        bottomBar = {
            if (activeSubScreen == SubScreen.NONE) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(colors.mainBackground)
                ) {
                    // Persistent Mini Player
                    AnimatedVisibility(
                        visible = currentSong != null,
                        enter = slideInVertically(initialOffsetY = { it }) + fadeIn(),
                        exit = slideOutVertically(targetOffsetY = { it }) + fadeOut()
                    ) {
                        MiniPlayer(
                            song = currentSong,
                            nextSong = if (queueIndex in 0 until queue.size - 1) queue[queueIndex + 1] else null,
                            isPlaying = isPlaying,
                            progressMs = progressMs,
                            durationMs = durationMs,
                            onPlayPauseClick = { mainViewModel.togglePlayPause() },
                            onNextClick = { mainViewModel.playNext() },
                            onClick = { mainViewModel.openSubScreen(SubScreen.NOW_PLAYING) }
                        )
                    }

                    SwarixBottomNavigation(
                        selectedItem = currentTab,
                        onItemSelected = { mainViewModel.selectTab(it) }
                    )
                }
            }
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            // Main Tab Screens
            when (currentTab) {
                NavItem.HOME -> {
                    HomeScreen(
                        allSongs = allSongs,
                        recentlyPlayed = recentlyPlayed,
                        playlists = playlists,
                        friendActivity = friendActivity,
                        currentPlayingSong = currentSong,
                        isPlaying = isPlaying,
                        onSongClick = { mainViewModel.playSong(it, allSongs) },
                        onPlaylistClick = { mainViewModel.openPlaylist(it) },
                        topArtists = topArtists,
                        onArtistSpotlightClick = { artistName, artwork ->
                            mainViewModel.openArtistProfile(artistName, artwork)
                        },
                        onFriendClick = { friend ->
                            val match = allSongs.find { it.id == friend.songId || it.title.equals(friend.songTitle, ignoreCase = true) }
                            if (match != null) {
                                mainViewModel.playSong(match, allSongs)
                            } else if (allSongs.isNotEmpty()) {
                                mainViewModel.playSong(allSongs.first(), allSongs)
                            }
                        },
                        onOpenStats = { mainViewModel.openSubScreen(SubScreen.STATS) },
                        onArtistClick = { artistName ->
                            val artistSongs = allSongs.filter { it.artist.contains(artistName, ignoreCase = true) }
                            if (artistSongs.isNotEmpty()) {
                                mainViewModel.playSong(artistSongs.first(), artistSongs)
                            }
                        },
                        onMoodClick = { mood ->
                            val moodSongs = allSongs.filter { it.mood.contains(mood, ignoreCase = true) }
                            if (moodSongs.isNotEmpty()) {
                                mainViewModel.playSong(moodSongs.first(), moodSongs)
                            }
                        },
                        onFavoriteToggle = { mainViewModel.toggleFavorite(it) },
                        onNotificationsClick = {
                            scope.launch {
                                snackbarHostState.showSnackbar("You're all caught up with Swarix releases!")
                            }
                        },
                        onJoinSyncSession = {
                            mainViewModel.openSubScreen(SubScreen.SYNC_SESSION)
                        },
                        onSearchShortcutClick = {
                            mainViewModel.selectTab(NavItem.SEARCH)
                        },
                        dailyMixes = dailyMixes,
                        onDailyMixClick = { mix -> mainViewModel.openDailyMix(mix) },
                        onPlayDailyMix = { mix -> mainViewModel.playDailyMix(mix) },
                        onRegenerateDailyMixes = {
                            mainViewModel.regenerateDailyMixes()
                            scope.launch {
                                snackbarHostState.showSnackbar("Refreshed Daily Mixes based on your listening history & mood preferences!")
                            }
                        }
                    )
                }
                NavItem.SEARCH -> {
                    SearchScreen(
                        allSongs = allSongs,
                        currentPlayingSong = currentSong,
                        isPlaying = isPlaying,
                        playlists = playlists,
                        recentSearches = mainViewModel.recentSearches.collectAsStateWithLifecycle().value,
                        onlineResults = mainViewModel.onlineSearchResults.collectAsStateWithLifecycle().value,
                        isSearchingOnline = mainViewModel.isSearchingOnline.collectAsStateWithLifecycle().value,
                        onSongClick = { mainViewModel.playSong(it) },
                        onArtistClick = { artistName ->
                            val artistSongs = allSongs.filter { it.artist.contains(artistName, ignoreCase = true) }
                            if (artistSongs.isNotEmpty()) {
                                mainViewModel.playSong(artistSongs.first(), artistSongs)
                            }
                        },
                        onFavoriteToggle = { mainViewModel.toggleFavorite(it) },
                        onSavePlaylist = { title, songs ->
                            mainViewModel.createPlaylist(title, "Curated by Swarix AI Voice Search")
                            scope.launch {
                                snackbarHostState.showSnackbar("Playlist \"$title\" saved to your Library!")
                            }
                        },
                        onSearch = { mainViewModel.search(it) },
                        onClearRecentSearches = { mainViewModel.clearRecentSearches() }
                    )
                }
                NavItem.AI -> {
                    SwarixAiScreen(
                        catalogSongs = allSongs,
                        currentPlayingSong = currentSong,
                        isPlaying = isPlaying,
                        onPlayPlaylist = { songs ->
                            if (songs.isNotEmpty()) {
                                mainViewModel.playSong(songs.first(), songs)
                            }
                        },
                        onSongClick = { mainViewModel.playSong(it, allSongs) },
                        onSavePlaylist = { title, songs ->
                            mainViewModel.createPlaylist(title, "Curated by Swarix AI")
                            scope.launch {
                                snackbarHostState.showSnackbar("Playlist \"$title\" saved to your Library!")
                            }
                        }
                    )
                }
                NavItem.LIBRARY -> {
                    LibraryScreen(
                        allSongs = allSongs,
                        favoriteSongs = favoriteSongs,
                        playlists = playlists,
                        currentPlayingSong = currentSong,
                        isPlaying = isPlaying,
                        onSongClick = { mainViewModel.playSong(it, allSongs) },
                        onPlaylistClick = { mainViewModel.openPlaylist(it) },
                        onLikedSongsClick = {
                            if (favoriteSongs.isNotEmpty()) {
                                mainViewModel.playSong(favoriteSongs.first(), favoriteSongs)
                            }
                        },
                        onCreatePlaylist = { name, desc ->
                            mainViewModel.createPlaylist(name, desc)
                        },
                        onRescanLibrary = {
                            mainViewModel.rescanLibrary()
                            scope.launch {
                                snackbarHostState.showSnackbar("Scanning device storage for audio...")
                            }
                        },
                        onFavoriteToggle = { mainViewModel.toggleFavorite(it) },
                        recentSearches = mainViewModel.recentSearches.collectAsStateWithLifecycle().value,
                        onSearchQueryClick = { query ->
                            mainViewModel.search(query)
                            mainViewModel.selectTab(NavItem.SEARCH)
                        },
                        cachedSongIds = mainViewModel.cachedSongIds.collectAsStateWithLifecycle().value,
                        onCacheToggle = { song ->
                            if (mainViewModel.cachedSongIds.value.contains(song.id)) {
                                mainViewModel.removeCachedSong(song.id)
                            } else {
                                mainViewModel.cacheSong(song)
                            }
                        }
                    )
                }
                NavItem.SETTINGS -> {
                    SettingsScreen(
                        currentTheme = themeMode,
                        audioQuality = audioQuality,
                        crossfadeEnabled = crossfadeEnabled,
                        crossfadeSeconds = crossfadeSeconds,
                        sleepTimerMinutesRemaining = sleepTimerMinutesRemaining,
                        sleepTimerSecondsRemaining = sleepTimerSecondsRemaining,
                        isSleepTimerFading = isSleepTimerFading,
                        sleepTimerFadeSeconds = sleepTimerFadeSeconds,
                        language = language,
                        region = region,
                        onThemeChange = { mainViewModel.setThemeMode(it) },
                        onAudioQualityChange = { mainViewModel.setAudioQuality(it) },
                        onOpenEqualizer = { mainViewModel.openSubScreen(SubScreen.EQUALIZER) },
                        onOpenCrossfade = { mainViewModel.openSubScreen(SubScreen.CROSSFADE) },
                        onOpenDownloads = { mainViewModel.openSubScreen(SubScreen.DOWNLOADS) },
                        onSetSleepTimer = { mins, fadeSecs, isEnd ->
                            mainViewModel.setSleepTimer(mins, fadeSecs, isEnd)
                            scope.launch {
                                val msg = if (isEnd) {
                                    "Sleep timer set for end of track (fading volume over last ${fadeSecs}s)"
                                } else if (mins > 0) {
                                    "Sleep timer set for $mins min (fading volume over last ${fadeSecs}s)"
                                } else {
                                    "Sleep timer turned off"
                                }
                                snackbarHostState.showSnackbar(msg)
                            }
                        },
                        onCancelSleepTimer = {
                            mainViewModel.cancelSleepTimer()
                            scope.launch {
                                snackbarHostState.showSnackbar("Sleep timer turned off")
                            }
                        },
                        onAddSleepTimerMinutes = { mins ->
                            mainViewModel.addSleepTimerMinutes(mins)
                            scope.launch {
                                snackbarHostState.showSnackbar("Added $mins minutes to sleep timer")
                            }
                        },
                        onClearHistory = {
                            mainViewModel.clearHistory()
                            scope.launch {
                                snackbarHostState.showSnackbar("Playback history cleared.")
                            }
                        },
                        onLanguageChange = { mainViewModel.setLanguage(it) },
                        onRegionChange = { mainViewModel.setRegion(it) },
                        batterySaverEnabled = mainViewModel.batterySaverEnabled.collectAsStateWithLifecycle().value,
                        onBatterySaverToggle = { mainViewModel.setBatterySaverEnabled(it) },
                        batteryLevel = batteryLevel,
                        isLowBattery = isLowBattery,
                        isBatterySaverEngaged = isBatterySaverEngaged,
                        offlineModeEnabled = mainViewModel.offlineModeEnabled.collectAsStateWithLifecycle().value,
                        onOfflineModeToggle = { mainViewModel.setOfflineModeEnabled(it) },
                        bassHapticsEnabled = bassHapticsEnabled,
                        bassHapticsIntensity = bassHapticsIntensity,
                        onBassHapticsToggle = { mainViewModel.setBassHapticsEnabled(it) },
                        onBassHapticsIntensityChange = { mainViewModel.setBassHapticsIntensity(it) },
                        library = allSongs,
                        voiceCommandHistory = mainViewModel.voiceCommandHistory.collectAsStateWithLifecycle().value,
                        onRetriggerVoiceCommand = { item ->
                            mainViewModel.retriggerVoiceCommand(
                                item = item,
                                onOpenMoodRadio = { mood ->
                                    requestedVoiceMood = mood
                                    mainViewModel.openSubScreen(SubScreen.MOOD_RADIO)
                                },
                                onOpenEqualizer = {
                                    mainViewModel.openSubScreen(SubScreen.EQUALIZER)
                                }
                            )
                            scope.launch {
                                snackbarHostState.showSnackbar("Re-triggered: \"${item.spokenText}\"")
                            }
                        },
                        onClearVoiceCommandHistory = {
                            mainViewModel.clearVoiceCommandHistory()
                            scope.launch {
                                snackbarHostState.showSnackbar("Voice command history cleared.")
                            }
                        },
                        playbackHealthStatus = mainViewModel.playbackHealthStatus.collectAsStateWithLifecycle().value,
                        playbackRecoveryCount = mainViewModel.playbackRecoveryCount.collectAsStateWithLifecycle().value,
                        onResetPlayer = {
                            mainViewModel.resetPlayer()
                            scope.launch {
                                snackbarHostState.showSnackbar("ExoPlayer reset and audio buffers flushed.")
                            }
                        },
                        onOpenDiagnostics = {
                            mainViewModel.openSubScreen(SubScreen.DIAGNOSTICS)
                        },
                        onBackClick = { mainViewModel.selectTab(NavItem.HOME) }
                    )
                }
            }

            // Sub Screens with Expressive Slide & Fade Transitions
            AnimatedVisibility(
                visible = activeSubScreen == SubScreen.NOW_PLAYING,
                enter = slideInVertically(initialOffsetY = { it }, animationSpec = tween(350)) + fadeIn(),
                exit = slideOutVertically(targetOffsetY = { it }, animationSpec = tween(280)) + fadeOut()
            ) {
                NowPlayingScreen(
                    song = currentSong,
                    isPlaying = isPlaying,
                    progressMs = progressMs,
                    durationMs = durationMs,
                    isShuffle = isShuffle,
                    repeatMode = repeatMode,
                    onPlayPauseClick = { mainViewModel.togglePlayPause() },
                    onPreviousClick = { mainViewModel.playPrevious() },
                    onNextClick = { mainViewModel.playNext() },
                    onSeek = { mainViewModel.seekTo(it) },
                    onShuffleToggle = { mainViewModel.toggleShuffle() },
                    onRepeatToggle = { mainViewModel.cycleRepeatMode() },
                    onFavoriteToggle = { mainViewModel.toggleFavorite(it) },
                    onCollapse = { mainViewModel.closeSubScreen() },
                    onOpenQueue = { mainViewModel.openSubScreen(SubScreen.QUEUE) },
                    onOpenLyrics = { mainViewModel.openSubScreen(SubScreen.LYRICS) },
                    onOpenEqualizer = { mainViewModel.openSubScreen(SubScreen.EQUALIZER) },
                    sleepTimerSecondsRemaining = sleepTimerSecondsRemaining,
                    isSleepTimerFading = isSleepTimerFading,
                    onSetSleepTimer = { mins, fadeSecs, isEnd ->
                        mainViewModel.setSleepTimer(mins, fadeSecs, isEnd)
                        scope.launch {
                            val msg = if (isEnd) "Sleep timer set for end of track (fading volume over last ${fadeSecs}s)"
                                      else "Sleep timer set for $mins min (fading volume over last ${fadeSecs}s)"
                            snackbarHostState.showSnackbar(msg)
                        }
                    },
                    onCancelSleepTimer = {
                        mainViewModel.cancelSleepTimer()
                        scope.launch {
                            snackbarHostState.showSnackbar("Sleep timer turned off")
                        }
                    },
                    onAddSleepTimerMinutes = { mins ->
                        mainViewModel.addSleepTimerMinutes(mins)
                        scope.launch {
                            snackbarHostState.showSnackbar("Added $mins minutes to sleep timer")
                        }
                    },
                    bassHapticsEnabled = bassHapticsEnabled,
                    onToggleBassHaptics = {
                        val next = !bassHapticsEnabled
                        mainViewModel.setBassHapticsEnabled(next)
                        scope.launch {
                            snackbarHostState.showSnackbar(if (next) "Bass Haptics enabled" else "Bass Haptics disabled")
                        }
                    },
                    onSmartShuffle = {
                        mainViewModel.smartShuffleQueue { success, msg ->
                            scope.launch {
                                snackbarHostState.showSnackbar(msg)
                            }
                        }
                    },
                    allSongs = allSongs,
                    onSongSelected = { mainViewModel.playSong(it, allSongs) },
                    isBatterySaverEngaged = mainViewModel.isBatterySaverEngaged.collectAsStateWithLifecycle().value
                )
            }

            AnimatedVisibility(
                visible = activeSubScreen == SubScreen.QUEUE,
                enter = slideInVertically(initialOffsetY = { it }, animationSpec = tween(350)) + fadeIn(),
                exit = slideOutVertically(targetOffsetY = { it }, animationSpec = tween(280)) + fadeOut()
            ) {
                PlayQueueScreen(
                    currentSong = currentSong,
                    queue = queue,
                    queueIndex = queueIndex,
                    isPlaying = isPlaying,
                    onSongClick = { mainViewModel.playSong(it, queue) },
                    onRemoveFromQueue = { mainViewModel.removeFromQueue(it) },
                    onClearQueue = { mainViewModel.clearQueue() },
                    onShuffleQueue = { mainViewModel.toggleShuffle() },
                    onSmartShuffle = {
                        mainViewModel.smartShuffleQueue { success, msg ->
                            scope.launch {
                                snackbarHostState.showSnackbar(msg)
                            }
                        }
                    },
                    isSmartShuffling = isSmartShuffling,
                    onBackClick = { mainViewModel.closeSubScreen() }
                )
            }

            AnimatedVisibility(
                visible = activeSubScreen == SubScreen.LYRICS,
                enter = slideInVertically(initialOffsetY = { it }, animationSpec = tween(350)) + fadeIn(),
                exit = slideOutVertically(targetOffsetY = { it }, animationSpec = tween(280)) + fadeOut()
            ) {
                LyricsScreen(
                    song = currentSong,
                    progressMs = progressMs,
                    durationMs = durationMs,
                    onBackClick = { mainViewModel.closeSubScreen() },
                    onOpenQueue = { mainViewModel.openSubScreen(SubScreen.QUEUE) },
                    onSeek = { mainViewModel.seekTo(it) },
                    onToggleKaraoke = { active -> mainViewModel.setKaraokeMode(active) }
                )
            }

            AnimatedVisibility(
                visible = activeSubScreen == SubScreen.EQUALIZER,
                enter = slideInVertically(initialOffsetY = { it }, animationSpec = tween(350)) + fadeIn(),
                exit = slideOutVertically(targetOffsetY = { it }, animationSpec = tween(280)) + fadeOut()
            ) {
                EqualizerScreen(
                    equalizerState = equalizerState,
                    currentSong = currentSong,
                    onStateChange = { mainViewModel.updateEqualizer(it) },
                    onTriggerVoiceCommand = { showVoiceCommandSheet = true },
                    onBackClick = { mainViewModel.closeSubScreen() }
                )
            }

            AnimatedVisibility(
                visible = activeSubScreen == SubScreen.DOWNLOADS,
                enter = slideInVertically(initialOffsetY = { it }, animationSpec = tween(350)) + fadeIn(),
                exit = slideOutVertically(targetOffsetY = { it }, animationSpec = tween(280)) + fadeOut()
            ) {
                DownloadsScreen(
                    downloadedSongs = allSongs.take(6),
                    currentPlayingSong = currentSong,
                    isPlaying = isPlaying,
                    onSongClick = { mainViewModel.playSong(it, allSongs) },
                    onBackClick = { mainViewModel.closeSubScreen() }
                )
            }

            AnimatedVisibility(
                visible = activeSubScreen == SubScreen.SYNC_SESSION,
                enter = slideInVertically(initialOffsetY = { it }, animationSpec = tween(350)) + fadeIn(),
                exit = slideOutVertically(targetOffsetY = { it }, animationSpec = tween(280)) + fadeOut()
            ) {
                SyncSessionScreen(
                    currentSong = currentSyncSong,
                    isPlaying = isPlaying,
                    onPlayPauseClick = { mainViewModel.togglePlayPause() },
                    onLeaveSession = { mainViewModel.closeSubScreen() },
                    onBackClick = { mainViewModel.closeSubScreen() }
                )
            }

            AnimatedVisibility(
                visible = activeSubScreen == SubScreen.PLAYLIST_DETAIL && selectedPlaylist != null,
                enter = slideInVertically(initialOffsetY = { it }, animationSpec = tween(350)) + fadeIn(),
                exit = slideOutVertically(targetOffsetY = { it }, animationSpec = tween(280)) + fadeOut()
            ) {
                selectedPlaylist?.let { playlist ->
                    PlaylistDetailScreen(
                        playlist = playlist,
                        songs = playlistSongs,
                        currentPlayingSong = currentSong,
                        isPlaying = isPlaying,
                        onSongClick = { mainViewModel.playSong(it, playlistSongs) },
                        onPlayAll = {
                            if (playlistSongs.isNotEmpty()) {
                                mainViewModel.playSong(playlistSongs.first(), playlistSongs)
                            }
                        },
                        onShuffleAll = {
                            if (playlistSongs.isNotEmpty()) {
                                val shuffled = playlistSongs.shuffled()
                                mainViewModel.playSong(shuffled.first(), shuffled)
                            }
                        },
                        onFavoriteToggle = { mainViewModel.toggleFavorite(it) },
                        onBackClick = { mainViewModel.closeSubScreen() }
                    )
                }
            }

            AnimatedVisibility(
                visible = activeSubScreen == SubScreen.STATS,
                enter = slideInVertically(initialOffsetY = { it }, animationSpec = tween(350)) + fadeIn(),
                exit = slideOutVertically(targetOffsetY = { it }, animationSpec = tween(280)) + fadeOut()
            ) {
                ListeningStatsScreen(
                    stats = listeningStats,
                    currentPlayingSong = currentSong,
                    isPlaying = isPlaying,
                    onPlaySong = { song, queue -> mainViewModel.playSong(song, queue) },
                    onPlayAllTopSongs = { songs ->
                        if (songs.isNotEmpty()) {
                            mainViewModel.playSong(songs.first(), songs)
                        }
                    },
                    onBackClick = { mainViewModel.closeSubScreen() }
                )
            }

            AnimatedVisibility(
                visible = activeSubScreen == SubScreen.MOOD_RADIO,
                enter = slideInVertically(initialOffsetY = { it }, animationSpec = tween(350)) + fadeIn(),
                exit = slideOutVertically(targetOffsetY = { it }, animationSpec = tween(280)) + fadeOut()
            ) {
                val moodRadioVm: MoodRadioViewModel = viewModel()
                androidx.compose.runtime.LaunchedEffect(requestedVoiceMood) {
                    requestedVoiceMood?.let { mood ->
                        moodRadioVm.selectMood(mood)
                        moodRadioVm.generateMoodPlaylist(mood, allSongs) { playlist ->
                            if (playlist.isNotEmpty()) {
                                mainViewModel.playSong(playlist.first(), playlist)
                            }
                        }
                        requestedVoiceMood = null
                    }
                }
                MoodRadioScreen(
                    viewModel = moodRadioVm,
                    library = allSongs,
                    onSongClick = { song, playlist ->
                        mainViewModel.playSong(song, playlist)
                    },
                    onPlayAll = { playlist ->
                        if (playlist.isNotEmpty()) {
                            mainViewModel.playSong(playlist.first(), playlist)
                        }
                    },
                    onExportM3u = {
                        moodRadioVm.exportCurrentMoodPlaylist(context) { _, msg ->
                            scope.launch {
                                snackbarHostState.showSnackbar(msg)
                            }
                        }
                    },
                    onTriggerVoiceCommand = { showVoiceCommandSheet = true },
                    onBackClick = { mainViewModel.closeSubScreen() }
                )
            }
            
            AnimatedVisibility(
                visible = activeSubScreen == SubScreen.CROSSFADE,
                enter = slideInVertically(initialOffsetY = { it }, animationSpec = tween(350)) + fadeIn(),
                exit = slideOutVertically(targetOffsetY = { it }, animationSpec = tween(280)) + fadeOut()
            ) {
                CrossfadeSettingsScreen(
                    crossfadeEnabled = crossfadeEnabled,
                    crossfadeSeconds = crossfadeSeconds,
                    gaplessEnabled = gaplessPlayback,
                    onCrossfadeToggle = { mainViewModel.setCrossfadeEnabled(it) },
                    onCrossfadeSecondsChange = { mainViewModel.setCrossfadeSeconds(it) },
                    onGaplessToggle = { mainViewModel.setGaplessPlayback(it) },
                    onPreviewCrossfade = { mainViewModel.previewCrossfade(it) },
                    onBackClick = { mainViewModel.closeSubScreen() }
                )
            }
            
            AnimatedVisibility(
                visible = activeSubScreen == SubScreen.ARTIST_PROFILE && selectedArtist != null,
                enter = slideInVertically(initialOffsetY = { it }, animationSpec = tween(350)) + fadeIn(),
                exit = slideOutVertically(targetOffsetY = { it }, animationSpec = tween(280)) + fadeOut()
            ) {
                selectedArtist?.let { artist ->
                    ArtistProfileScreen(
                        artistName = artist.first,
                        artistArtworkUri = artist.second,
                        artistSongs = allSongs.filter { it.artist.contains(artist.first, ignoreCase = true) },
                        onSongClick = { mainViewModel.playSong(it, allSongs) },
                        onFavoriteToggle = { mainViewModel.toggleFavorite(it) },
                        onBackClick = { mainViewModel.closeSubScreen() }
                    )
                }
            }

            AnimatedVisibility(
                visible = activeSubScreen == SubScreen.DIAGNOSTICS,
                enter = slideInVertically(initialOffsetY = { it }, animationSpec = tween(350)) + fadeIn(),
                exit = slideOutVertically(targetOffsetY = { it }, animationSpec = tween(280)) + fadeOut()
            ) {
                com.example.ui.screens.settings.PlaybackDiagnosticsScreen(
                    healthStatus = mainViewModel.playbackHealthStatus.collectAsStateWithLifecycle().value,
                    recoveryCount = mainViewModel.playbackRecoveryCount.collectAsStateWithLifecycle().value,
                    currentSong = currentSong,
                    currentPosMs = progressMs,
                    durationMs = durationMs,
                    isPlaying = isPlaying,
                    logs = mainViewModel.getDiagnosticLogs(),
                    onResetPlayer = {
                        mainViewModel.resetPlayer()
                        scope.launch {
                            snackbarHostState.showSnackbar("ExoPlayer reset and audio buffers flushed.")
                        }
                    },
                    onDumpLogs = {
                        mainViewModel.dumpDiagnosticLogs()
                        scope.launch {
                            snackbarHostState.showSnackbar("Dumped diagnostic logs to Logcat.")
                        }
                    },
                    onBackClick = { mainViewModel.closeSubScreen() }
                )
            }
        }
    }

    if (showVoiceCommandSheet) {
        VoiceCommandSheet(
            voiceManager = mainViewModel.voiceCommandManager,
            currentEqualizerState = equalizerState,
            onCommandExecuted = { result ->
                mainViewModel.executeVoiceCommand(
                    result = result,
                    onOpenMoodRadio = { mood ->
                        showVoiceCommandSheet = false
                        requestedVoiceMood = mood
                        mainViewModel.openSubScreen(SubScreen.MOOD_RADIO)
                    },
                    onOpenEqualizer = {
                        showVoiceCommandSheet = false
                        mainViewModel.openSubScreen(SubScreen.EQUALIZER)
                    }
                )
                mainViewModel.recordVoiceCommand(mainViewModel.voiceCommandManager.recognizedText.value.ifBlank { "Voice Interaction" }, result)
                scope.launch {
                    val msg = when (result) {
                        is com.example.speech.VoiceCommandResult.MoodRadio -> "Starting ${result.mood} Mood Radio..."
                        is com.example.speech.VoiceCommandResult.Equalizer -> result.message
                        is com.example.speech.VoiceCommandResult.PlaybackAction -> result.message
                        is com.example.speech.VoiceCommandResult.Unrecognized -> result.suggestion
                    }
                    snackbarHostState.showSnackbar(msg)
                }
            },
            onDismiss = { showVoiceCommandSheet = false }
        )
    }
}
