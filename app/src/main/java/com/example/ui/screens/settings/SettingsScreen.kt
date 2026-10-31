package com.example.ui.screens.settings

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.BatteryFull
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.Equalizer
import androidx.compose.material.icons.filled.GraphicEq
import androidx.compose.material.icons.filled.Help
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.OpenInNew
import androidx.compose.material.icons.filled.Public
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.PlayCircle
import androidx.compose.material.icons.filled.Storage
import androidx.compose.material.icons.filled.SystemUpdate
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.RadioButtonDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.ui.components.SleepTimerDialog
import com.example.ui.theme.LocalSwarixColors
import androidx.compose.material.icons.automirrored.filled.CompareArrows
import androidx.compose.material.icons.filled.Vibration
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import com.example.core.model.Song
import com.example.playback.HapticIntensity
import com.example.ui.theme.ThemeMode

@Composable
fun SettingsScreen(
    currentTheme: ThemeMode,
    audioQuality: String,
    crossfadeEnabled: Boolean = true,
    crossfadeSeconds: Int = 5,
    sleepTimerMinutesRemaining: Int = 0,
    sleepTimerSecondsRemaining: Int = 0,
    isSleepTimerFading: Boolean = false,
    sleepTimerFadeSeconds: Int = 60,
    language: String = "en",
    region: String = "Global",
    bassHapticsEnabled: Boolean = true,
    bassHapticsIntensity: HapticIntensity = HapticIntensity.MEDIUM,
    onBassHapticsToggle: (Boolean) -> Unit = {},
    onBassHapticsIntensityChange: (HapticIntensity) -> Unit = {},
    onThemeChange: (ThemeMode) -> Unit,
    onAudioQualityChange: (String) -> Unit,
    onOpenEqualizer: () -> Unit,
    onOpenCrossfade: () -> Unit,
    onOpenDownloads: () -> Unit,
    onSetSleepTimer: (minutes: Int, fadeSeconds: Int, isEndOfTrack: Boolean) -> Unit,
    onCancelSleepTimer: () -> Unit = {},
    onAddSleepTimerMinutes: (Int) -> Unit = {},
    onClearHistory: () -> Unit,
    onLanguageChange: (String) -> Unit,
    onRegionChange: (String) -> Unit,
    batterySaverEnabled: Boolean,
    onBatterySaverToggle: (Boolean) -> Unit,
    batteryLevel: Int = 100,
    isLowBattery: Boolean = false,
    isBatterySaverEngaged: Boolean = false,
    offlineModeEnabled: Boolean,
    onOfflineModeToggle: (Boolean) -> Unit,
    onCheckForUpdates: () -> Unit = {},
    library: List<Song> = emptyList(),
    voiceCommandHistory: List<com.example.ui.MainViewModel.VoiceCommandHistoryItem> = emptyList(),
    onRetriggerVoiceCommand: (com.example.ui.MainViewModel.VoiceCommandHistoryItem) -> Unit = {},
    onClearVoiceCommandHistory: () -> Unit = {},
    playbackHealthStatus: String = "Healthy (Connected)",
    playbackRecoveryCount: Int = 0,
    onResetPlayer: () -> Unit = {},
    onOpenDiagnostics: () -> Unit = {},
    onBackClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val colors = LocalSwarixColors.current
    val uriHandler = LocalUriHandler.current

    var showThemeDialog by remember { mutableStateOf(false) }
    var showQualityDialog by remember { mutableStateOf(false) }
    var showLanguageDialog by remember { mutableStateOf(false) }
    var showRegionDialog by remember { mutableStateOf(false) }
    var showSleepTimerDialog by remember { mutableStateOf(false) }
    var showWakeUpDialog by remember { mutableStateOf(false) }
    var showAboutDialog by remember { mutableStateOf(false) }
    var showClearHistoryDialog by remember { mutableStateOf(false) }
    var showVoiceHistoryDialog by remember { mutableStateOf(false) }

    if (showWakeUpDialog) {
        com.example.ui.components.MusicWakeUpDialog(
            library = library,
            onDismiss = { showWakeUpDialog = false }
        )
    }

    if (showVoiceHistoryDialog) {
        AlertDialog(
            onDismissRequest = { showVoiceHistoryDialog = false },
            title = { Text("Voice Command History", color = colors.primaryText) },
            text = {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(300.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    if (voiceCommandHistory.isEmpty()) {
                        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                            Text("No voice commands recorded yet.", color = colors.mutedText)
                        }
                    } else {
                        androidx.compose.foundation.lazy.LazyColumn(
                            verticalArrangement = Arrangement.spacedBy(6.dp),
                            modifier = Modifier.fillMaxSize()
                        ) {
                            items(voiceCommandHistory.size) { index ->
                                val item = voiceCommandHistory[index]
                                Surface(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clip(RoundedCornerShape(10.dp))
                                        .clickable {
                                            onRetriggerVoiceCommand(item)
                                            showVoiceHistoryDialog = false
                                        },
                                    color = colors.elevatedSurface
                                ) {
                                    Column(modifier = Modifier.padding(10.dp)) {
                                        Text(
                                            text = "\"${item.spokenText}\"",
                                            style = MaterialTheme.typography.bodyMedium.copy(
                                                fontWeight = FontWeight.Bold,
                                                color = colors.primaryAccent
                                            )
                                        )
                                        Spacer(modifier = Modifier.height(2.dp))
                                        Text(
                                            text = item.resultMessage,
                                            style = MaterialTheme.typography.bodySmall.copy(
                                                color = colors.primaryText
                                            )
                                        )
                                        Spacer(modifier = Modifier.height(2.dp))
                                        Text(
                                            text = "Tap to re-trigger • ${java.text.SimpleDateFormat("HH:mm", java.util.Locale.getDefault()).format(java.util.Date(item.timestamp))}",
                                            style = MaterialTheme.typography.labelSmall.copy(
                                                color = colors.mutedText,
                                                fontSize = 10.sp
                                            )
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showVoiceHistoryDialog = false }) {
                    Text("Close", color = colors.primaryAccent)
                }
            },
            dismissButton = {
                if (voiceCommandHistory.isNotEmpty()) {
                    TextButton(onClick = { onClearVoiceCommandHistory() }) {
                        Text("Clear History", color = Color.Red)
                    }
                }
            },
            containerColor = colors.surface
        )
    }

    var showVoiceHelpDialog by remember { mutableStateOf(false) }

    if (showVoiceHelpDialog) {
        AlertDialog(
            onDismissRequest = { showVoiceHelpDialog = false },
            title = {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Icon(imageVector = Icons.Default.Help, contentDescription = null, tint = colors.primaryAccent)
                    Text("Voice Commands Guide", color = colors.primaryText, fontWeight = FontWeight.Bold)
                }
            },
            text = {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(350.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    androidx.compose.foundation.lazy.LazyColumn(
                        verticalArrangement = Arrangement.spacedBy(10.dp),
                        modifier = Modifier.fillMaxSize()
                    ) {
                        item {
                            VoiceHelpCategory(
                                title = "1. Playback Control",
                                commands = listOf(
                                    "\"Play\" or \"Resume\" - Resumes music playback",
                                    "\"Pause\" or \"Stop\" - Pauses current track",
                                    "\"Next\" or \"Skip\" - Skips to next song",
                                    "\"Previous\" or \"Back\" - Plays previous song",
                                    "\"Smart Shuffle\" - Triggers AI time-of-day shuffle"
                                )
                            )
                        }
                        item {
                            VoiceHelpCategory(
                                title = "2. Mood Radio Stations",
                                commands = listOf(
                                    "\"Play Chill music\" - Starts chill lo-fi station",
                                    "\"Start Focus station\" - Plays deep work playlist",
                                    "\"Workout energy\" - High-tempo workout station",
                                    "\"Relax music\" - Calm meditation tunes",
                                    "\"Party mode\" - Upbeat party dance tracks"
                                )
                            )
                        }
                        item {
                            VoiceHelpCategory(
                                title = "3. Equalizer Presets",
                                commands = listOf(
                                    "\"Boost bass\" / \"Heavy bass\" - Max sub-bass boost",
                                    "\"Acoustic preset\" - Warm string clarity",
                                    "\"Vocal boost\" - Prominent 1-4kHz vocal presence",
                                    "\"Rock preset\" / \"Electronic mode\" - Genre tuning",
                                    "\"Reset equalizer\" - Flat studio curve"
                                )
                            )
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showVoiceHelpDialog = false }) {
                    Text("Got It", color = colors.primaryAccent, fontWeight = FontWeight.Bold)
                }
            },
            containerColor = colors.surface
        )
    }

    // Theme Dialog
    if (showThemeDialog) {
        AlertDialog(
            onDismissRequest = { showThemeDialog = false },
            title = { Text("Choose Theme", color = colors.primaryText) },
            text = {
                Column {
                    ThemeMode.entries.forEach { mode ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    onThemeChange(mode)
                                    showThemeDialog = false
                                }
                                .padding(vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            RadioButton(
                                selected = currentTheme == mode,
                                onClick = {
                                    onThemeChange(mode)
                                    showThemeDialog = false
                                },
                                colors = RadioButtonDefaults.colors(selectedColor = colors.primaryAccent)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = when (mode) {
                                    ThemeMode.DARK -> "Dark Theme (Default)"
                                    ThemeMode.LIGHT -> "Light Theme"
                                    ThemeMode.SYSTEM -> "Follow System"
                                },
                                color = colors.primaryText
                            )
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showThemeDialog = false }) {
                    Text("Close", color = colors.primaryAccent)
                }
            },
            containerColor = colors.elevatedSurface
        )
    }

    // Audio Quality Dialog
    if (showQualityDialog) {
        val qualities = listOf(
            "8K Ultra Studio Master (Lossless Hi-Res)",
            "4K Premium Master (320 kbps)",
            "Very High (320 kbps)",
            "High (256 kbps)",
            "Standard (128 kbps)"
        )
        AlertDialog(
            onDismissRequest = { showQualityDialog = false },
            title = { Text("Audio Streaming Quality", color = colors.primaryText) },
            text = {
                Column {
                    qualities.forEach { q ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    onAudioQualityChange(q)
                                    showQualityDialog = false
                                }
                                .padding(vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            RadioButton(
                                selected = audioQuality == q,
                                onClick = {
                                    onAudioQualityChange(q)
                                    showQualityDialog = false
                                },
                                colors = RadioButtonDefaults.colors(selectedColor = colors.primaryAccent)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(text = q, color = colors.primaryText)
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showQualityDialog = false }) {
                    Text("Close", color = colors.primaryAccent)
                }
            },
            containerColor = colors.elevatedSurface
        )
    }

    // Sleep Timer Dialog with Gradual Volume Fade-Out
    if (showSleepTimerDialog) {
        SleepTimerDialog(
            currentMinutes = sleepTimerMinutesRemaining,
            secondsRemaining = sleepTimerSecondsRemaining,
            isFadingOut = isSleepTimerFading,
            onSetSleepTimer = onSetSleepTimer,
            onCancelTimer = onCancelSleepTimer,
            onAddMinutes = onAddSleepTimerMinutes,
            onDismiss = { showSleepTimerDialog = false }
        )
    }

    // Clear History Dialog
    if (showClearHistoryDialog) {
        AlertDialog(
            onDismissRequest = { showClearHistoryDialog = false },
            title = { Text("Clear Listening History?", color = colors.primaryText) },
            text = {
                Text(
                    text = "This will remove your recently played songs and listening statistics from the device.",
                    color = colors.secondaryText
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        onClearHistory()
                        showClearHistoryDialog = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = colors.error)
                ) {
                    Text("Clear", color = colors.primaryText)
                }
            },
            dismissButton = {
                TextButton(onClick = { showClearHistoryDialog = false }) {
                    Text("Cancel", color = colors.secondaryText)
                }
            },
            containerColor = colors.elevatedSurface
        )
    }

    // About Swarix Dialog
    if (showAboutDialog) {
        AlertDialog(
            onDismissRequest = { showAboutDialog = false },
            title = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    com.example.ui.components.SwarixLogoMark(
                        size = 32.dp,
                        enableGlow = false
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Text("Swarix", color = colors.primaryText, fontWeight = FontWeight.Bold)
                }
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text(
                        text = "Version 1.4 (Release)",
                        style = MaterialTheme.typography.labelLarge.copy(color = colors.primaryAccent)
                    )
                    Text(
                        text = "Tagline: Sound for Every Story.",
                        style = MaterialTheme.typography.bodyMedium.copy(color = colors.primaryText)
                    )
                    Text(
                        text = "Built with Kotlin, Jetpack Compose, Material 3, AndroidX Media3 ExoPlayer, and Room Database. Designed for 8K ultra high fidelity audio and personalized listening without paywalls.",
                        style = MaterialTheme.typography.bodyMedium.copy(color = colors.secondaryText)
                    )

                    Spacer(modifier = Modifier.height(2.dp))

                    // Developer & Maintainer Clickable Card
                    Surface(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .clickable {
                                uriHandler.openUri("https://ritikkumarai.vercel.app/")
                            },
                        color = colors.primaryAccent.copy(alpha = 0.12f),
                        border = androidx.compose.foundation.BorderStroke(1.dp, colors.primaryAccent.copy(alpha = 0.35f))
                    ) {
                        Row(
                            modifier = Modifier.padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = "Maintained & Developed by",
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        color = colors.mutedText,
                                        fontWeight = FontWeight.Medium
                                    )
                                )
                                Text(
                                    text = "Ritik Kumar",
                                    style = MaterialTheme.typography.titleMedium.copy(
                                        color = colors.primaryAccent,
                                        fontWeight = FontWeight.Bold
                                    )
                                )
                                Text(
                                    text = "ritikkumarai.vercel.app ↗",
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        color = colors.secondaryText
                                    )
                                )
                            }
                            Icon(
                                imageVector = Icons.Default.OpenInNew,
                                contentDescription = "Open Portfolio",
                                tint = colors.primaryAccent,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showAboutDialog = false }) {
                    Text("Close", color = colors.primaryAccent)
                }
            },
            containerColor = colors.elevatedSurface
        )
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(colors.mainBackground)
            .systemBarsPadding()
    ) {
        // Header
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(
                onClick = onBackClick,
                modifier = Modifier.testTag("settings_back_button")
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "Back",
                    tint = colors.primaryText
                )
            }
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = "Settings",
                style = MaterialTheme.typography.titleLarge.copy(
                    fontWeight = FontWeight.Bold,
                    color = colors.primaryText
                )
            )
        }

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // Profile Card (from Image 1 Screen 14)
            item {
                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(18.dp)),
                    color = colors.elevatedSurface
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(56.dp)
                                .clip(CircleShape)
                                .background(colors.primaryAccent.copy(alpha = 0.2f)),
                            contentAlignment = Alignment.Center
                        ) {
                            AsyncImage(
                                model = "https://images.unsplash.com/photo-1535713875002-d1d0cf377fde?w=200&auto=format&fit=crop&q=80",
                                contentDescription = "Ritik Profile",
                                contentScale = ContentScale.Crop,
                                modifier = Modifier
                                    .fillMaxSize()
                                    .clip(CircleShape)
                            )
                        }
                        Spacer(modifier = Modifier.width(14.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Ritik",
                                style = MaterialTheme.typography.titleMedium.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = colors.primaryText
                                )
                            )
                            Text(
                                text = "ritik@example.com",
                                style = MaterialTheme.typography.bodyMedium.copy(
                                    color = colors.secondaryText
                                )
                            )
                        }
                        IconButton(onClick = {}) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                                contentDescription = null,
                                tint = colors.mutedText
                            )
                        }
                    }
                }
            }

            // Setting Items
            item {
                SettingsSectionHeader("AUDIO & PLAYBACK")
            }

            item {
                SettingsRow(
                    icon = Icons.Default.GraphicEq,
                    title = "Audio Quality",
                    value = audioQuality,
                    onClick = { showQualityDialog = true }
                )
            }

            item {
                SettingsRow(
                    icon = Icons.Default.Equalizer,
                    title = "Equalizer",
                    value = "Custom",
                    onClick = onOpenEqualizer
                )
            }

            item {
                SettingsRow(
                    icon = Icons.AutoMirrored.Filled.CompareArrows,
                    title = "Crossfade",
                    value = if (crossfadeEnabled && crossfadeSeconds > 0) "${crossfadeSeconds}s" else "Off",
                    onClick = onOpenCrossfade
                )
            }

            item {
                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(14.dp)),
                    color = colors.surface
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 14.dp, vertical = 14.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.Vibration,
                                contentDescription = null,
                                tint = if (bassHapticsEnabled) colors.primaryAccent else colors.secondaryAccent,
                                modifier = Modifier.size(22.dp)
                            )
                            Spacer(modifier = Modifier.width(14.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = "Bass Haptic Feedback",
                                    style = MaterialTheme.typography.bodyLarge.copy(
                                        fontWeight = FontWeight.Medium,
                                        color = colors.primaryText
                                    )
                                )
                                Text(
                                    text = "Tactile vibration synced with low-end kicks",
                                    style = MaterialTheme.typography.bodySmall.copy(
                                        color = colors.mutedText
                                    )
                                )
                            }
                            Switch(
                                checked = bassHapticsEnabled,
                                onCheckedChange = onBassHapticsToggle,
                                colors = SwitchDefaults.colors(
                                    checkedThumbColor = colors.mainBackground,
                                    checkedTrackColor = colors.primaryAccent,
                                    uncheckedThumbColor = colors.mutedText,
                                    uncheckedTrackColor = colors.elevatedSurface
                                ),
                                modifier = Modifier.testTag("switch_bass_haptics")
                            )
                        }

                        if (isBatterySaverEngaged) {
                            Spacer(modifier = Modifier.height(8.dp))
                            Surface(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(8.dp)),
                                color = colors.secondaryAccent.copy(alpha = 0.16f)
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.BatteryFull,
                                        contentDescription = null,
                                        tint = colors.secondaryAccent,
                                        modifier = Modifier.size(14.dp)
                                    )
                                    Text(
                                        text = "Haptics suspended to save energy (Battery Saver active)",
                                        style = MaterialTheme.typography.labelSmall.copy(
                                            color = colors.secondaryAccent,
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.Medium
                                        )
                                    )
                                }
                            }
                        }

                        if (bassHapticsEnabled) {
                            Spacer(modifier = Modifier.height(12.dp))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                listOf(
                                    HapticIntensity.LOW to "Subtle",
                                    HapticIntensity.MEDIUM to "Balanced",
                                    HapticIntensity.HIGH to "Powerful"
                                ).forEach { (intensity, label) ->
                                    val selected = bassHapticsIntensity == intensity
                                    Surface(
                                        modifier = Modifier
                                            .weight(1f)
                                            .clip(RoundedCornerShape(10.dp))
                                            .clickable { onBassHapticsIntensityChange(intensity) }
                                            .testTag("haptic_intensity_${label.lowercase()}"),
                                        color = if (selected) colors.primaryAccent else colors.elevatedSurface
                                    ) {
                                        Box(
                                            modifier = Modifier.padding(vertical = 8.dp),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Text(
                                                text = label,
                                                style = MaterialTheme.typography.labelSmall.copy(
                                                    fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal,
                                                    color = if (selected) colors.mainBackground else colors.primaryText
                                                )
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }

            item {
                val timerSubtitle = if (sleepTimerSecondsRemaining > 0) {
                    if (sleepTimerSecondsRemaining >= 999) {
                        "Active • End of track (gradual fade)"
                    } else {
                        val m = sleepTimerSecondsRemaining / 60
                        val s = sleepTimerSecondsRemaining % 60
                        if (isSleepTimerFading) "Fading out volume now..." else "Active • ${m}m ${s}s (fade ${sleepTimerFadeSeconds}s)"
                    }
                } else {
                    "Off (fades out volume)"
                }

                SettingsRow(
                    icon = Icons.Default.Timer,
                    title = "Sleep Timer",
                    value = timerSubtitle,
                    onClick = { showSleepTimerDialog = true }
                )
            }

            item {
                SettingsRow(
                    icon = Icons.Default.PlayCircle,
                    title = "Music Wake-up Alarm",
                    value = "Gentle volume ramp up",
                    onClick = { showWakeUpDialog = true }
                )
            }

            item {
                SettingsRow(
                    icon = Icons.Default.Download,
                    title = "Downloads & Offline",
                    value = "Manage",
                    onClick = onOpenDownloads
                )
            }

            item {
                SettingsSectionHeader("APPEARANCE")
            }

            item {
                SettingsRow(
                    icon = Icons.Default.DarkMode,
                    title = "Theme",
                    value = when (currentTheme) {
                        ThemeMode.DARK -> "Dark"
                        ThemeMode.LIGHT -> "Light"
                        ThemeMode.SYSTEM -> "System"
                    },
                    onClick = { showThemeDialog = true }
                )
            }

            item {
                SettingsRow(
                    icon = Icons.Default.Language,
                    title = "Language",
                    value = if (language == "hi") "Hindi" else "English",
                    onClick = { showLanguageDialog = true }
                )
            }

            item {
                SettingsRow(
                    icon = Icons.Default.Public,
                    title = "Music Discovery Region",
                    value = region,
                    onClick = { showRegionDialog = true }
                )
            }

            item {
                SettingsSectionHeader("DATA & PRIVACY")
            }

            item {
                SettingsRow(
                    icon = Icons.Default.Delete,
                    title = "Clear Listening History",
                    value = "Reset stats",
                    onClick = { showClearHistoryDialog = true }
                )
            }

            item {
                SettingsRow(
                    icon = Icons.Default.Lock,
                    title = "Privacy & Security",
                    value = "",
                    onClick = {}
                )
            }
            
            item {
                SettingsSectionHeader("POWER & BATTERY")
            }

            item {
                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .testTag("battery_status_card"),
                    color = colors.surface
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 14.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.BatteryFull,
                                contentDescription = null,
                                tint = if (isLowBattery) colors.error else colors.primaryAccent,
                                modifier = Modifier.size(24.dp)
                            )
                            Column {
                                Text(
                                    text = "Device Battery: $batteryLevel%",
                                    style = MaterialTheme.typography.bodyMedium.copy(
                                        fontWeight = FontWeight.Bold,
                                        color = colors.primaryText
                                    )
                                )
                                Text(
                                    text = if (isLowBattery) {
                                        "Low Battery (< 20%) • Energy saver active"
                                    } else if (isBatterySaverEngaged) {
                                        "Battery Saver active • AI throttled & haptics paused"
                                    } else {
                                        "Normal • Auto-saves below 20%"
                                    },
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        color = if (isLowBattery) colors.error else colors.mutedText
                                    )
                                )
                            }
                        }

                        if (isBatterySaverEngaged) {
                            Surface(
                                modifier = Modifier.clip(RoundedCornerShape(6.dp)),
                                color = (if (isLowBattery) colors.error else colors.primaryAccent).copy(alpha = 0.16f)
                            ) {
                                Text(
                                    text = if (isLowBattery) "LOW (<20%)" else "SAVER ACTIVE",
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 9.sp,
                                        color = if (isLowBattery) colors.error else colors.primaryAccent
                                    )
                                )
                            }
                        }
                    }
                }
            }

            item {
                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .clickable { onBatterySaverToggle(!batterySaverEnabled) }
                        .testTag("row_battery_saver"),
                    color = colors.surface
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 14.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(12.dp),
                            modifier = Modifier.weight(1f)
                        ) {
                            Icon(
                                imageVector = Icons.Default.BatteryFull,
                                contentDescription = null,
                                tint = colors.primaryAccent,
                                modifier = Modifier.size(22.dp)
                            )
                            Column {
                                Text(
                                    text = "Battery Saver",
                                    style = MaterialTheme.typography.bodyMedium.copy(
                                        fontWeight = FontWeight.Medium,
                                        color = colors.primaryText
                                    )
                                )
                                Text(
                                    text = if (isBatterySaverEngaged) {
                                        if (batterySaverEnabled) "Active (manual toggle) • AI metadata throttled & haptics paused"
                                        else if (isLowBattery) "Auto-active (< 20% battery) • AI & haptics paused"
                                        else "Active (System Power Saver) • AI & haptics paused"
                                    } else {
                                        "Off • Automatically throttles AI & haptics below 20%"
                                    },
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        color = colors.mutedText
                                    )
                                )
                            }
                        }
                        Switch(
                            checked = batterySaverEnabled,
                            onCheckedChange = { onBatterySaverToggle(it) },
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = colors.mainBackground,
                                checkedTrackColor = colors.primaryAccent,
                                uncheckedThumbColor = colors.mutedText,
                                uncheckedTrackColor = colors.elevatedSurface
                            ),
                            modifier = Modifier.testTag("switch_battery_saver")
                        )
                    }
                }
            }

            item {
                SettingsRow(
                    icon = Icons.Default.Download,
                    title = "Offline Mode",
                    value = if (offlineModeEnabled) "On" else "Off",
                    onClick = { onOfflineModeToggle(!offlineModeEnabled) }
                )
            }

            item {
                SettingsSectionHeader("VOICE ASSISTANT")
            }

            item {
                SettingsRow(
                    icon = Icons.Default.GraphicEq,
                    title = "Voice Command History",
                    value = "${voiceCommandHistory.size} items",
                    onClick = { showVoiceHistoryDialog = true }
                )
            }

            item {
                SettingsSectionHeader("MEDIA3 PLAYBACK HEALTH & DIAGNOSTICS")
            }

            item {
                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .testTag("playback_health_status_card"),
                    color = colors.surface
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(14.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Audio Engine Connection",
                                style = MaterialTheme.typography.bodyMedium.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = colors.primaryText
                                )
                            )
                            Surface(
                                shape = RoundedCornerShape(6.dp),
                                color = colors.primaryAccent.copy(alpha = 0.16f)
                            ) {
                                Text(
                                    text = playbackHealthStatus,
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        color = colors.primaryAccent,
                                        fontWeight = FontWeight.Bold
                                    ),
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                                )
                            }
                        }

                        Text(
                            text = "Auto-Recoveries Executed: $playbackRecoveryCount",
                            style = MaterialTheme.typography.bodySmall.copy(color = colors.secondaryText)
                        )

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Button(
                                onClick = onResetPlayer,
                                colors = ButtonDefaults.buttonColors(containerColor = colors.secondaryAccent),
                                modifier = Modifier
                                    .weight(1f)
                                    .testTag("btn_reset_player")
                            ) {
                                Text("Reset Player", color = colors.mainBackground, fontWeight = FontWeight.Bold)
                            }
                            androidx.compose.material3.OutlinedButton(
                                onClick = onOpenDiagnostics,
                                modifier = Modifier
                                    .weight(1f)
                                    .testTag("btn_open_diagnostics")
                            ) {
                                Text("View Diagnostics", color = colors.primaryText)
                            }
                        }
                    }
                }
            }

            item {
                SettingsSectionHeader("ABOUT")
            }

            item {
                SettingsRow(
                    icon = Icons.Default.Person,
                    title = "Maintainer & Developer",
                    value = "Ritik Kumar ↗",
                    onClick = {
                        uriHandler.openUri("https://ritikkumarai.vercel.app/")
                    }
                )
            }

            item {
                SettingsRow(
                    icon = Icons.Default.SystemUpdate,
                    title = "Check for Updates",
                    value = "v1.4",
                    onClick = onCheckForUpdates
                )
            }

            item {
                SettingsRow(
                    icon = Icons.Default.Info,
                    title = "About Swarix",
                    value = "v1.4",
                    onClick = { showAboutDialog = true }
                )
            }
        }
    }
}

@Composable
fun SettingsSectionHeader(title: String) {
    val colors = LocalSwarixColors.current
    Text(
        text = title,
        style = MaterialTheme.typography.labelSmall.copy(
            color = colors.primaryAccent,
            fontWeight = FontWeight.Bold,
            letterSpacing = 1.sp
        ),
        modifier = Modifier.padding(top = 12.dp, bottom = 4.dp, start = 4.dp)
    )
}

@Composable
fun SettingsRow(
    icon: ImageVector,
    title: String,
    value: String = "",
    onClick: () -> Unit
) {
    val colors = LocalSwarixColors.current

    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .clickable(onClick = onClick)
            .testTag("setting_${title.take(10).lowercase()}"),
        color = colors.surface
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp, vertical = 14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = colors.secondaryAccent,
                modifier = Modifier.size(22.dp)
            )
            Spacer(modifier = Modifier.width(14.dp))
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.bodyLarge.copy(
                        fontWeight = FontWeight.Medium,
                        color = colors.primaryText
                    ),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                if (value.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = value,
                        style = MaterialTheme.typography.bodyMedium.copy(
                            color = colors.mutedText,
                            fontSize = 13.sp
                        ),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }
            Spacer(modifier = Modifier.width(8.dp))
            Icon(
                imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                contentDescription = null,
                tint = colors.mutedText,
                modifier = Modifier.size(20.dp)
            )
        }
    }
}

@Composable
fun VoiceHelpCategory(
    title: String,
    commands: List<String>
) {
    val colors = LocalSwarixColors.current
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(colors.elevatedSurface.copy(alpha = 0.5f))
            .padding(12.dp),
        verticalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.titleSmall.copy(
                color = colors.primaryAccent,
                fontWeight = FontWeight.Bold
            )
        )
        commands.forEach { cmd ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.Top
            ) {
                Text(
                    text = "• ",
                    style = MaterialTheme.typography.bodyMedium.copy(color = colors.secondaryAccent)
                )
                Text(
                    text = cmd,
                    style = MaterialTheme.typography.bodyMedium.copy(color = colors.primaryText)
                )
            }
        }
    }
}
