package com.example.ui.screens.stats

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
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
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Equalizer
import androidx.compose.material.icons.filled.Headphones
import androidx.compose.material.icons.filled.LocalFireDepartment
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Stars
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material.icons.filled.WorkspacePremium
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import coil.compose.AsyncImage
import com.example.core.model.GenreStats
import com.example.core.model.ListeningStats
import com.example.core.model.Song
import com.example.ui.components.bouncyClick
import com.example.ui.theme.AccentAmber
import com.example.ui.theme.AccentBlue
import com.example.ui.theme.AccentPurple
import com.example.ui.theme.AccentRose
import com.example.ui.theme.AccentTeal
import com.example.ui.theme.LocalSwarixColors

/**
 * Spotify Wrapped-style Listening Statistics Dashboard for Swarix.
 * Features:
 * - Animated Circular Progress Indicators for Total Listening Time & Goal
 * - Sound Persona / Audio Aura summary card with animated glow
 * - Top Most-Played Tracks (#1 Spotlight + Ranked 1-10 with direct play actions)
 * - Top Most-Played Artists with streaming shares & circular avatars
 * - Attractive weekly interactive activity bar chart with daily tap inspections
 * - Genre distribution visualizer with percentage breakdowns
 * - Interactive Wrapped Share Story Modal
 */
@Composable
fun ListeningStatsScreen(
    stats: ListeningStats,
    currentPlayingSong: Song? = null,
    isPlaying: Boolean = false,
    onPlaySong: (Song, List<Song>) -> Unit = { _, _ -> },
    onPlayAllTopSongs: (List<Song>) -> Unit = {},
    onBackClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val colors = LocalSwarixColors.current
    var selectedTab by remember { mutableIntStateOf(0) }
    val tabs = listOf("✨ Wrapped Story", "🎵 Top Tracks", "👑 Top Artists", "📊 Activity & Time")
    var showShareModal by remember { mutableStateOf(false) }

    // Intercept back navigation
    BackHandler {
        onBackClick()
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(colors.mainBackground)
            .systemBarsPadding()
    ) {
        // Wrapped Top Header
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                IconButton(
                    onClick = onBackClick,
                    modifier = Modifier.testTag("stats_back_button")
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Back",
                        tint = colors.primaryText
                    )
                }
                Spacer(modifier = Modifier.width(4.dp))
                Column {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = "SWARIX WRAPPED",
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.ExtraBold,
                                color = colors.primaryAccent,
                                letterSpacing = 1.2.sp
                            )
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Icon(
                            imageVector = Icons.Default.AutoAwesome,
                            contentDescription = null,
                            tint = colors.primaryAccent,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                    Text(
                        text = "2026 Sound Journey & Analytics",
                        style = MaterialTheme.typography.labelSmall.copy(color = colors.secondaryText)
                    )
                }
            }

            // Share Wrapped Button
            Surface(
                modifier = Modifier
                    .clip(RoundedCornerShape(18.dp))
                    .bouncyClick { showShareModal = true }
                    .testTag("share_wrapped_button"),
                color = colors.primaryAccent.copy(alpha = 0.15f)
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.Share,
                        contentDescription = "Share",
                        tint = colors.primaryAccent,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "Share",
                        style = MaterialTheme.typography.labelMedium.copy(
                            fontWeight = FontWeight.Bold,
                            color = colors.primaryAccent
                        )
                    )
                }
            }
        }

        // Navigation Tabs (Horizontal Scrollable Chips)
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 6.dp)
                .horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            tabs.forEachIndexed { index, title ->
                val isSelected = selectedTab == index
                FilterChip(
                    selected = isSelected,
                    onClick = { selectedTab = index },
                    label = {
                        Text(
                            text = title,
                            style = MaterialTheme.typography.labelMedium.copy(
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
                            )
                        )
                    },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = colors.primaryAccent,
                        selectedLabelColor = colors.mainBackground,
                        containerColor = colors.surface,
                        labelColor = colors.secondaryText
                    ),
                    border = FilterChipDefaults.filterChipBorder(
                        enabled = true,
                        selected = isSelected,
                        borderColor = if (isSelected) colors.primaryAccent else colors.divider
                    ),
                    shape = RoundedCornerShape(20.dp),
                    modifier = Modifier.testTag("wrapped_tab_$index")
                )
            }
        }

        // Main Tab Content
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            when (selectedTab) {
                0 -> renderWrappedStory(
                    stats = stats,
                    currentPlayingSong = currentPlayingSong,
                    isPlaying = isPlaying,
                    onPlaySong = onPlaySong,
                    onPlayAllTopSongs = onPlayAllTopSongs,
                    onOpenShare = { showShareModal = true }
                )
                1 -> renderTopTracks(
                    stats = stats,
                    currentPlayingSong = currentPlayingSong,
                    isPlaying = isPlaying,
                    onPlaySong = onPlaySong,
                    onPlayAllTopSongs = onPlayAllTopSongs
                )
                2 -> renderTopArtists(
                    stats = stats
                )
                3 -> renderActivityAndCharts(
                    stats = stats
                )
            }
        }
    }

    // Share Wrapped Modal
    if (showShareModal) {
        ShareWrappedModal(
            stats = stats,
            onDismiss = { showShareModal = false }
        )
    }
}

// -------------------------------------------------------------------------------------------------
// TAB 0: WRAPPED STORY (All-In-One Highlight Experience)
// -------------------------------------------------------------------------------------------------
private fun LazyListScope.renderWrappedStory(
    stats: ListeningStats,
    currentPlayingSong: Song?,
    isPlaying: Boolean,
    onPlaySong: (Song, List<Song>) -> Unit,
    onPlayAllTopSongs: (List<Song>) -> Unit,
    onOpenShare: () -> Unit
) {
    // 1. Sound Aura & Musical Personality Card
    item {
        SoundPersonalityCard(
            personality = stats.soundPersonality,
            description = stats.personalityDescription,
            topMood = stats.topMood,
            streakDays = stats.streakDays
        )
    }

    // 2. Animated Circular Progress Indicator (Listening Time & Annual Milestone)
    item {
        AnimatedListeningDialCard(
            totalMinutes = stats.totalMinutesListened,
            goalMinutes = stats.listeningGoalMinutes,
            totalTracks = stats.totalTracksPlayed,
            streakDays = stats.streakDays
        )
    }

    // 3. Top #1 Song Spotlight Card
    val topSong = stats.topSongs.firstOrNull()
    if (topSong != null) {
        item {
            TopSongSpotlightCard(
                song = topSong,
                isPlaying = topSong.id == currentPlayingSong?.id && isPlaying,
                onPlayClick = { onPlaySong(topSong, stats.topSongs) }
            )
        }
    }

    // 4. Top #1 Artist Spotlight Card
    val topArtist = stats.topArtists.firstOrNull()
    if (topArtist != null) {
        item {
            TopArtistSpotlightCard(
                artistName = topArtist.first,
                streamCount = topArtist.second,
                loyaltyPercentage = stats.artistLoyaltyPercentage,
                topSongTitle = topSong?.title ?: "Apna Bana Le"
            )
        }
    }

    // 5. Weekly Listening Activity Bar Chart
    item {
        WeeklyActivityChartCard(
            weeklyActivity = stats.weeklyActivityHours,
            peakHours = stats.peakListeningTime
        )
    }

    // 6. Genre Soundscape Distribution
    if (stats.topGenres.isNotEmpty()) {
        item {
            GenreDistributionCard(topGenres = stats.topGenres)
        }
    }

    // 7. Call To Action Banner: Share Wrapped 2026
    item {
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(20.dp))
                .bouncyClick(onClick = onOpenShare)
                .testTag("wrapped_share_banner"),
            color = Color(0xFF1E2620)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(
                        Brush.horizontalGradient(
                            listOf(
                                Color(0xFFD4AF37).copy(alpha = 0.25f),
                                Color(0xFF1DB954).copy(alpha = 0.15f)
                            )
                        )
                    )
                    .padding(20.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(text = "🎉", fontSize = 20.sp)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Ready to flex your Wrapped?",
                                style = MaterialTheme.typography.titleMedium.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White
                                )
                            )
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Generate and share your personalized 2026 Swarix Sound Story.",
                            style = MaterialTheme.typography.bodySmall.copy(
                                color = Color.White.copy(alpha = 0.8f)
                            )
                        )
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Box(
                        modifier = Modifier
                            .size(46.dp)
                            .clip(CircleShape)
                            .background(Color(0xFFD4AF37)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Share,
                            contentDescription = "Share",
                            tint = Color.Black,
                            modifier = Modifier.size(22.dp)
                        )
                    }
                }
            }
        }
    }
}

// -------------------------------------------------------------------------------------------------
// TAB 1: TOP TRACKS (Ranked 1 to 10 with direct playback)
// -------------------------------------------------------------------------------------------------
private fun LazyListScope.renderTopTracks(
    stats: ListeningStats,
    currentPlayingSong: Song?,
    isPlaying: Boolean,
    onPlaySong: (Song, List<Song>) -> Unit,
    onPlayAllTopSongs: (List<Song>) -> Unit
) {
    item {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = "Your Top Tracks",
                    style = MaterialTheme.typography.titleLarge.copy(
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                )
                Text(
                    text = "${stats.topSongs.size} tracks that defined your 2026",
                    style = MaterialTheme.typography.labelSmall.copy(color = Color(0xFFD4AF37))
                )
            }

            if (stats.topSongs.isNotEmpty()) {
                Button(
                    onClick = { onPlayAllTopSongs(stats.topSongs) },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFD4AF37)),
                    shape = RoundedCornerShape(20.dp),
                    contentPadding = PaddingValues(horizontal = 14.dp, vertical = 6.dp),
                    modifier = Modifier.testTag("play_top_tracks_button")
                ) {
                    Icon(
                        imageVector = Icons.Default.PlayArrow,
                        contentDescription = null,
                        tint = Color.Black,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "Play All",
                        style = MaterialTheme.typography.labelMedium.copy(
                            fontWeight = FontWeight.Bold,
                            color = Color.Black
                        )
                    )
                }
            }
        }
    }

    if (stats.topSongs.isEmpty()) {
        item {
            EmptySectionCard(message = "Play songs in Swarix to build your Top Tracks list!")
        }
    } else {
        itemsIndexed(stats.topSongs) { index, song ->
            val isCurrentSong = song.id == currentPlayingSong?.id
            TopTrackRowItem(
                rank = index + 1,
                song = song,
                isCurrent = isCurrentSong,
                isPlaying = isCurrentSong && isPlaying,
                onPlayClick = { onPlaySong(song, stats.topSongs) }
            )
        }
    }
}

// -------------------------------------------------------------------------------------------------
// TAB 2: TOP ARTISTS
// -------------------------------------------------------------------------------------------------
private fun LazyListScope.renderTopArtists(stats: ListeningStats) {
    item {
        Column {
            Text(
                text = "Your Top Artists",
                style = MaterialTheme.typography.titleLarge.copy(
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
            )
            Text(
                text = "The creators soundtracking your world",
                style = MaterialTheme.typography.labelSmall.copy(color = Color(0xFFD4AF37))
            )
        }
    }

    // Artist Loyalty Circular Progress Dial
    item {
        ArtistLoyaltyDialCard(
            topArtistName = stats.topArtists.firstOrNull()?.first ?: "Artist",
            loyaltyPercentage = stats.artistLoyaltyPercentage
        )
    }

    itemsIndexed(stats.topArtists) { index, (artist, playCount) ->
        TopArtistRowItem(
            rank = index + 1,
            artist = artist,
            playCount = playCount,
            totalTracksPlayed = stats.totalTracksPlayed
        )
    }
}

// -------------------------------------------------------------------------------------------------
// TAB 3: ACTIVITY & CHARTS
// -------------------------------------------------------------------------------------------------
private fun LazyListScope.renderActivityAndCharts(stats: ListeningStats) {
    item {
        WeeklyActivityChartCard(
            weeklyActivity = stats.weeklyActivityHours,
            peakHours = stats.peakListeningTime
        )
    }

    item {
        AnimatedListeningDialCard(
            totalMinutes = stats.totalMinutesListened,
            goalMinutes = stats.listeningGoalMinutes,
            totalTracks = stats.totalTracksPlayed,
            streakDays = stats.streakDays
        )
    }

    if (stats.topGenres.isNotEmpty()) {
        item {
            GenreDistributionCard(topGenres = stats.topGenres)
        }
    }

    // Listening Milestones Grid
    item {
        MilestonesGrid(
            minutes = stats.totalMinutesListened,
            tracks = stats.totalTracksPlayed,
            streakDays = stats.streakDays
        )
    }
}

// -------------------------------------------------------------------------------------------------
// COMPONENT: ANIMATED CIRCULAR PROGRESS DIAL FOR LISTENING TIME
// -------------------------------------------------------------------------------------------------
@Composable
private fun AnimatedListeningDialCard(
    totalMinutes: Int,
    goalMinutes: Int,
    totalTracks: Int,
    streakDays: Int
) {
    val colors = LocalSwarixColors.current
    val progressRatio = (totalMinutes.toFloat() / goalMinutes.toFloat().coerceAtLeast(1f)).coerceIn(0f, 1f)

    var animationTriggered by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) {
        animationTriggered = true
    }

    val animatedProgress by animateFloatAsState(
        targetValue = if (animationTriggered) progressRatio else 0f,
        animationSpec = tween(durationMillis = 1400, easing = FastOutSlowInEasing),
        label = "listening_dial_progress"
    )

    val hoursListened = remember(totalMinutes) {
        String.format("%.1f", totalMinutes / 60f)
    }

    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(24.dp))
            .testTag("animated_listening_dial_card"),
        color = colors.elevatedSurface
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    Brush.verticalGradient(
                        listOf(
                            Color(0xFFD4AF37).copy(alpha = 0.12f),
                            colors.elevatedSurface
                        )
                    )
                )
                .padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "TOTAL LISTENING TIME",
                        style = MaterialTheme.typography.labelSmall.copy(
                            color = colors.primaryAccent,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 1.2.sp
                        )
                    )
                    Text(
                        text = "Annual Progress & Milestones",
                        style = MaterialTheme.typography.bodySmall.copy(color = colors.secondaryText)
                    )
                }

                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = colors.surface
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.WorkspacePremium,
                            contentDescription = null,
                            tint = Color(0xFFD4AF37),
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "Top 3%",
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontWeight = FontWeight.Bold,
                                color = colors.primaryText
                            )
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(18.dp))

            // Animated Circular Dial Canvas
            Box(
                modifier = Modifier.size(210.dp),
                contentAlignment = Alignment.Center
            ) {
                val primaryAccent = colors.primaryAccent
                val trackColor = colors.surface.copy(alpha = 0.7f)
                val secondaryGlow = AccentTeal

                Canvas(modifier = Modifier.fillMaxSize()) {
                    val strokeWidth = 16.dp.toPx()
                    val diameter = size.minDimension - strokeWidth
                    val topLeft = Offset((size.width - diameter) / 2, (size.height - diameter) / 2)
                    val arcSize = Size(diameter, diameter)

                    // 1. Background Track
                    drawArc(
                        color = trackColor,
                        startAngle = 135f,
                        sweepAngle = 270f,
                        useCenter = false,
                        topLeft = topLeft,
                        size = arcSize,
                        style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
                    )

                    // 2. Animated Progress Arc with rich Gradient
                    val sweep = animatedProgress * 270f
                    if (sweep > 0f) {
                        drawArc(
                            brush = Brush.sweepGradient(
                                0.0f to primaryAccent,
                                0.5f to Color(0xFFFFD700),
                                1.0f to secondaryGlow,
                                center = Offset(size.width / 2, size.height / 2)
                            ),
                            startAngle = 135f,
                            sweepAngle = sweep,
                            useCenter = false,
                            topLeft = topLeft,
                            size = arcSize,
                            style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
                        )
                    }
                }

                // Center Content
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = "$totalMinutes",
                        style = MaterialTheme.typography.displayMedium.copy(
                            fontWeight = FontWeight.ExtraBold,
                            color = colors.primaryText
                        )
                    )
                    Text(
                        text = "MINUTES",
                        style = MaterialTheme.typography.labelSmall.copy(
                            color = colors.primaryAccent,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 1.sp
                        )
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "≈ $hoursListened Hours",
                        style = MaterialTheme.typography.bodySmall.copy(
                            color = colors.secondaryText,
                            fontWeight = FontWeight.Medium
                        )
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Goal Progress Pill
            Surface(
                modifier = Modifier.clip(RoundedCornerShape(14.dp)),
                color = colors.surface
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.Timer,
                        contentDescription = null,
                        tint = colors.primaryAccent,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Goal: $goalMinutes mins (${(progressRatio * 100).toInt()}% completed)",
                        style = MaterialTheme.typography.labelMedium.copy(
                            color = colors.primaryText,
                            fontWeight = FontWeight.SemiBold
                        )
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Badges Row (Streak + Track Milestones)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Surface(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(14.dp)),
                    color = colors.surface
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.LocalFireDepartment,
                            contentDescription = null,
                            tint = AccentAmber,
                            modifier = Modifier.size(22.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Column {
                            Text(
                                text = "$streakDays Days",
                                style = MaterialTheme.typography.titleSmall.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = colors.primaryText
                                )
                            )
                            Text(
                                text = "Daily Streak",
                                style = MaterialTheme.typography.labelSmall.copy(color = colors.secondaryText)
                            )
                        }
                    }
                }

                Surface(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(14.dp)),
                    color = colors.surface
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Equalizer,
                            contentDescription = null,
                            tint = AccentTeal,
                            modifier = Modifier.size(22.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Column {
                            Text(
                                text = "$totalTracks Tracks",
                                style = MaterialTheme.typography.titleSmall.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = colors.primaryText
                                )
                            )
                            Text(
                                text = "Total Plays",
                                style = MaterialTheme.typography.labelSmall.copy(color = colors.secondaryText)
                            )
                        }
                    }
                }
            }
        }
    }
}

// -------------------------------------------------------------------------------------------------
// COMPONENT: SOUND PERSONALITY / AUDIO AURA CARD
// -------------------------------------------------------------------------------------------------
@Composable
private fun SoundPersonalityCard(
    personality: String,
    description: String,
    topMood: String,
    streakDays: Int
) {
    val colors = LocalSwarixColors.current

    val infiniteTransition = rememberInfiniteTransition(label = "aura_shimmer")
    val auraOffset by infiniteTransition.animateFloat(
        initialValue = -30f,
        targetValue = 30f,
        animationSpec = infiniteRepeatable(
            animation = tween(4000, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "aura_float"
    )

    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(24.dp))
            .testTag("sound_personality_card"),
        color = Color(0xFF141715)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    Brush.radialGradient(
                        colors = listOf(
                            Color(0xFFD4AF37).copy(alpha = 0.28f),
                            Color(0xFF8B5CF6).copy(alpha = 0.18f),
                            Color(0xFF141715)
                        ),
                        center = Offset(200f + auraOffset, 120f),
                        radius = 450f
                    )
                )
                .padding(22.dp)
        ) {
            Column {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = Color.Black.copy(alpha = 0.5f)
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(text = "✨", fontSize = 14.sp)
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "SOUND PERSONALITY",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFFD4AF37),
                                    letterSpacing = 1.sp
                                )
                            )
                        }
                    }

                    Text(
                        text = "Swarix 2026",
                        style = MaterialTheme.typography.labelSmall.copy(
                            color = Color.White.copy(alpha = 0.6f),
                            fontWeight = FontWeight.SemiBold
                        )
                    )
                }

                Spacer(modifier = Modifier.height(14.dp))

                Text(
                    text = personality,
                    style = MaterialTheme.typography.headlineMedium.copy(
                        fontWeight = FontWeight.ExtraBold,
                        color = Color.White,
                        letterSpacing = (-0.5).sp
                    )
                )

                Spacer(modifier = Modifier.height(6.dp))

                Text(
                    text = description,
                    style = MaterialTheme.typography.bodyMedium.copy(
                        color = Color.White.copy(alpha = 0.85f),
                        lineHeight = 20.sp
                    )
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Badges row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Surface(
                        shape = RoundedCornerShape(10.dp),
                        color = Color.White.copy(alpha = 0.1f)
                    ) {
                        Text(
                            text = "🌙 Peak: Night Owl",
                            style = MaterialTheme.typography.labelSmall.copy(
                                color = Color.White,
                                fontWeight = FontWeight.Medium
                            ),
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp)
                        )
                    }

                    Surface(
                        shape = RoundedCornerShape(10.dp),
                        color = Color.White.copy(alpha = 0.1f)
                    ) {
                        Text(
                            text = "❤️ Mood: $topMood",
                            style = MaterialTheme.typography.labelSmall.copy(
                                color = Color.White,
                                fontWeight = FontWeight.Medium
                            ),
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp)
                        )
                    }

                    Surface(
                        shape = RoundedCornerShape(10.dp),
                        color = Color.White.copy(alpha = 0.1f)
                    ) {
                        Text(
                            text = "🔥 $streakDays Days",
                            style = MaterialTheme.typography.labelSmall.copy(
                                color = Color(0xFFFFB74D),
                                fontWeight = FontWeight.Bold
                            ),
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp)
                        )
                    }
                }
            }
        }
    }
}

// -------------------------------------------------------------------------------------------------
// COMPONENT: TOP #1 SONG SPOTLIGHT CARD
// -------------------------------------------------------------------------------------------------
@Composable
private fun TopSongSpotlightCard(
    song: Song,
    isPlaying: Boolean,
    onPlayClick: () -> Unit
) {
    val colors = LocalSwarixColors.current

    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(22.dp))
            .testTag("top_song_spotlight_card"),
        color = colors.elevatedSurface
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(18.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(text = "👑", fontSize = 18.sp)
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "YOUR #1 TRACK OF 2026",
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontWeight = FontWeight.Bold,
                            color = colors.primaryAccent,
                            letterSpacing = 1.sp
                        )
                    )
                }

                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = colors.primaryAccent.copy(alpha = 0.15f)
                ) {
                    Text(
                        text = "${song.playCount} plays",
                        style = MaterialTheme.typography.labelSmall.copy(
                            color = colors.primaryAccent,
                            fontWeight = FontWeight.Bold
                        ),
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(80.dp)
                        .clip(RoundedCornerShape(16.dp))
                ) {
                    AsyncImage(
                        model = song.artworkUri,
                        contentDescription = song.title,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize()
                    )
                }

                Spacer(modifier = Modifier.width(14.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = song.title,
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Bold,
                            color = colors.primaryText
                        ),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = song.artist,
                        style = MaterialTheme.typography.bodyMedium.copy(color = colors.secondaryText),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = colors.surface
                    ) {
                        Text(
                            text = "4K Hi-Res Studio",
                            style = MaterialTheme.typography.labelSmall.copy(
                                color = colors.primaryAccent,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold
                            ),
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.width(8.dp))

                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .clip(CircleShape)
                        .background(colors.primaryAccent)
                        .bouncyClick(onClick = onPlayClick),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = if (isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                        contentDescription = "Play/Pause",
                        tint = colors.mainBackground,
                        modifier = Modifier.size(28.dp)
                    )
                }
            }
        }
    }
}

// -------------------------------------------------------------------------------------------------
// COMPONENT: TOP #1 ARTIST SPOTLIGHT CARD
// -------------------------------------------------------------------------------------------------
@Composable
private fun TopArtistSpotlightCard(
    artistName: String,
    streamCount: Int,
    loyaltyPercentage: Int,
    topSongTitle: String
) {
    val colors = LocalSwarixColors.current

    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(22.dp))
            .testTag("top_artist_spotlight_card"),
        color = colors.elevatedSurface
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(18.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(76.dp)
                    .clip(CircleShape)
                    .border(2.5.dp, colors.primaryAccent, CircleShape)
            ) {
                AsyncImage(
                    model = "https://images.unsplash.com/photo-1507003211169-0a1dd7228f2d?w=300&auto=format&fit=crop&q=80",
                    contentDescription = artistName,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize()
                )
            }

            Spacer(modifier = Modifier.width(16.dp))

            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = "TOP ARTIST",
                        style = MaterialTheme.typography.labelSmall.copy(
                            color = colors.primaryAccent,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 1.sp
                        )
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Icon(
                        imageVector = Icons.Default.Stars,
                        contentDescription = null,
                        tint = colors.primaryAccent,
                        modifier = Modifier.size(14.dp)
                    )
                }
                Text(
                    text = artistName,
                    style = MaterialTheme.typography.titleLarge.copy(
                        fontWeight = FontWeight.ExtraBold,
                        color = colors.primaryText
                    ),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = "$streamCount streams • $loyaltyPercentage% of total listening",
                    style = MaterialTheme.typography.bodySmall.copy(color = colors.secondaryText)
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = "Most played: \"$topSongTitle\"",
                    style = MaterialTheme.typography.labelSmall.copy(
                        color = colors.mutedText,
                        fontStyle = androidx.compose.ui.text.font.FontStyle.Italic
                    ),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
}

// -------------------------------------------------------------------------------------------------
// COMPONENT: ARTIST LOYALTY CIRCULAR DIAL CARD
// -------------------------------------------------------------------------------------------------
@Composable
private fun ArtistLoyaltyDialCard(
    topArtistName: String,
    loyaltyPercentage: Int
) {
    val colors = LocalSwarixColors.current
    var animatedTrigger by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) { animatedTrigger = true }

    val animProgress by animateFloatAsState(
        targetValue = if (animatedTrigger) loyaltyPercentage / 100f else 0f,
        animationSpec = tween(1200, easing = FastOutSlowInEasing),
        label = "loyalty_progress"
    )

    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(20.dp)),
        color = colors.elevatedSurface
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(18.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier.size(86.dp),
                contentAlignment = Alignment.Center
            ) {
                val accent = colors.primaryAccent
                val track = colors.surface

                Canvas(modifier = Modifier.fillMaxSize()) {
                    val stroke = 8.dp.toPx()
                    val diameter = size.minDimension - stroke
                    val topLeft = Offset((size.width - diameter) / 2, (size.height - diameter) / 2)
                    val arcSize = Size(diameter, diameter)

                    drawArc(
                        color = track,
                        startAngle = 0f,
                        sweepAngle = 360f,
                        useCenter = false,
                        topLeft = topLeft,
                        size = arcSize,
                        style = Stroke(width = stroke)
                    )

                    drawArc(
                        brush = Brush.sweepGradient(
                            0f to accent,
                            0.7f to Color(0xFFFFD700),
                            1f to AccentTeal
                        ),
                        startAngle = -90f,
                        sweepAngle = animProgress * 360f,
                        useCenter = false,
                        topLeft = topLeft,
                        size = arcSize,
                        style = Stroke(width = stroke, cap = StrokeCap.Round)
                    )
                }

                Text(
                    text = "$loyaltyPercentage%",
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.ExtraBold,
                        color = colors.primaryText
                    )
                )
            }

            Spacer(modifier = Modifier.width(16.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "Artist Devotion",
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.Bold,
                        color = colors.primaryText
                    )
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = "$loyaltyPercentage% of all your stream time in 2026 was dedicated to $topArtistName.",
                    style = MaterialTheme.typography.bodySmall.copy(color = colors.secondaryText)
                )
            }
        }
    }
}

// -------------------------------------------------------------------------------------------------
// COMPONENT: ATTRACTIVE WEEKLY LISTENING ACTIVITY BAR CHART
// -------------------------------------------------------------------------------------------------
@Composable
private fun WeeklyActivityChartCard(
    weeklyActivity: List<Pair<String, Float>>,
    peakHours: String
) {
    val colors = LocalSwarixColors.current
    var selectedDayIndex by remember { mutableIntStateOf(5) } // Default Saturday (peak)
    val maxHours = remember(weeklyActivity) {
        weeklyActivity.maxOfOrNull { it.second }?.coerceAtLeast(1f) ?: 5f
    }

    var animateBars by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) {
        animateBars = true
    }

    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(22.dp))
            .testTag("weekly_activity_chart_card"),
        color = colors.elevatedSurface
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(18.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "LISTENING ACTIVITY",
                        style = MaterialTheme.typography.labelSmall.copy(
                            color = colors.primaryAccent,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 1.sp
                        )
                    )
                    Text(
                        text = "Weekly Playback Trends",
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Bold,
                            color = colors.primaryText
                        )
                    )
                }

                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = colors.surface
                ) {
                    Text(
                        text = "Peak: $peakHours",
                        style = MaterialTheme.typography.labelSmall.copy(
                            color = colors.primaryAccent,
                            fontWeight = FontWeight.SemiBold
                        ),
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Interactive Bar Chart Display
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(150.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Bottom
            ) {
                weeklyActivity.forEachIndexed { index, (day, hours) ->
                    val isSelected = index == selectedDayIndex
                    val heightRatio = (hours / maxHours).coerceIn(0.12f, 1f)

                    val animHeight by animateFloatAsState(
                        targetValue = if (animateBars) heightRatio else 0.05f,
                        animationSpec = tween(
                            durationMillis = 800 + index * 60,
                            easing = FastOutSlowInEasing
                        ),
                        label = "bar_height_$index"
                    )

                    Column(
                        modifier = Modifier
                            .weight(1f)
                            .bouncyClick { selectedDayIndex = index },
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Bottom
                    ) {
                        // Tooltip / Hour label on active bar
                        if (isSelected) {
                            Text(
                                text = "${String.format("%.1f", hours)}h",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontWeight = FontWeight.ExtraBold,
                                    color = colors.primaryAccent,
                                    fontSize = 10.sp
                                )
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                        }

                        // Bar
                        Box(
                            modifier = Modifier
                                .width(22.dp)
                                .height((100 * animHeight).dp)
                                .clip(RoundedCornerShape(topStart = 8.dp, topEnd = 8.dp))
                                .background(
                                    if (isSelected) {
                                        Brush.verticalGradient(
                                            listOf(
                                                colors.primaryAccent,
                                                Color(0xFFFFB300)
                                            )
                                        )
                                    } else {
                                        Brush.verticalGradient(
                                            listOf(
                                                colors.surface.copy(alpha = 0.9f),
                                                colors.surface.copy(alpha = 0.5f)
                                            )
                                        )
                                    }
                                )
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        Text(
                            text = day,
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                color = if (isSelected) colors.primaryAccent else colors.mutedText,
                                fontSize = 11.sp
                            )
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Selected Day Inspection Summary
            val selectedDay = weeklyActivity.getOrNull(selectedDayIndex)
            if (selectedDay != null) {
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    color = colors.surface
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 12.dp, vertical = 8.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "${selectedDay.first}: ${String.format("%.1f", selectedDay.second)} hours listened",
                            style = MaterialTheme.typography.bodySmall.copy(
                                fontWeight = FontWeight.SemiBold,
                                color = colors.primaryText
                            )
                        )
                        Text(
                            text = if (selectedDayIndex == 5) "🔥 Peak Activity" else "Regular Groove",
                            style = MaterialTheme.typography.labelSmall.copy(
                                color = if (selectedDayIndex == 5) AccentAmber else colors.mutedText,
                                fontWeight = FontWeight.Bold
                            )
                        )
                    }
                }
            }
        }
    }
}

// -------------------------------------------------------------------------------------------------
// COMPONENT: GENRE SOUNDSCAPE DISTRIBUTION CARD
// -------------------------------------------------------------------------------------------------
@Composable
private fun GenreDistributionCard(topGenres: List<GenreStats>) {
    val colors = LocalSwarixColors.current
    val genreColors = listOf(colors.primaryAccent, AccentPurple, AccentRose, AccentTeal, AccentBlue)

    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(22.dp))
            .testTag("genre_distribution_card"),
        color = colors.elevatedSurface
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(18.dp)
        ) {
            Text(
                text = "GENRE SOUNDSCAPE",
                style = MaterialTheme.typography.labelSmall.copy(
                    color = colors.primaryAccent,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.sp
                )
            )
            Text(
                text = "Your Top Musical Spaces",
                style = MaterialTheme.typography.titleMedium.copy(
                    fontWeight = FontWeight.Bold,
                    color = colors.primaryText
                )
            )

            Spacer(modifier = Modifier.height(14.dp))

            // Stacked Multi-Color Segmented Bar
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(12.dp)
                    .clip(RoundedCornerShape(6.dp))
            ) {
                topGenres.forEachIndexed { index, genreStat ->
                    val segColor = genreColors.getOrElse(index) { colors.primaryAccent }
                    Box(
                        modifier = Modifier
                            .weight(genreStat.percentage.toFloat().coerceAtLeast(1f))
                            .fillMaxSize()
                            .background(segColor)
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Genre Chips & Percentage breakdown
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                topGenres.forEachIndexed { index, genreStat ->
                    val segColor = genreColors.getOrElse(index) { colors.primaryAccent }
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(10.dp)
                                    .clip(CircleShape)
                                    .background(segColor)
                            )
                            Spacer(modifier = Modifier.width(10.dp))
                            Text(
                                text = genreStat.genre,
                                style = MaterialTheme.typography.bodyMedium.copy(
                                    fontWeight = FontWeight.SemiBold,
                                    color = colors.primaryText
                                )
                            )
                        }

                        Text(
                            text = "${genreStat.percentage}% (${genreStat.trackCount} tracks)",
                            style = MaterialTheme.typography.labelMedium.copy(
                                fontWeight = FontWeight.Bold,
                                color = segColor
                            )
                        )
                    }
                }
            }
        }
    }
}

// -------------------------------------------------------------------------------------------------
// COMPONENT: TOP TRACK ROW ITEM
// -------------------------------------------------------------------------------------------------
@Composable
private fun TopTrackRowItem(
    rank: Int,
    song: Song,
    isCurrent: Boolean,
    isPlaying: Boolean,
    onPlayClick: () -> Unit
) {
    val colors = LocalSwarixColors.current

    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .bouncyClick(onClick = onPlayClick)
            .testTag("top_track_item_$rank"),
        color = if (isCurrent) colors.primaryAccent.copy(alpha = 0.12f) else colors.surface
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Rank Number & Medal
            Box(
                modifier = Modifier.width(36.dp),
                contentAlignment = Alignment.CenterStart
            ) {
                when (rank) {
                    1 -> Text(text = "🥇", fontSize = 20.sp)
                    2 -> Text(text = "🥈", fontSize = 18.sp)
                    3 -> Text(text = "🥉", fontSize = 18.sp)
                    else -> Text(
                        text = "#$rank",
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Bold,
                            color = colors.mutedText
                        )
                    )
                }
            }

            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(RoundedCornerShape(10.dp))
            ) {
                AsyncImage(
                    model = song.artworkUri,
                    contentDescription = song.title,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize()
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = song.title,
                    style = MaterialTheme.typography.bodyLarge.copy(
                        fontWeight = FontWeight.Bold,
                        color = if (isCurrent) colors.primaryAccent else colors.primaryText
                    ),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = "${song.artist} • ${song.playCount} plays",
                    style = MaterialTheme.typography.bodySmall.copy(color = colors.secondaryText),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }

            IconButton(
                onClick = onPlayClick,
                modifier = Modifier.size(36.dp)
            ) {
                Icon(
                    imageVector = if (isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                    contentDescription = "Play Track",
                    tint = if (isCurrent) colors.primaryAccent else colors.primaryText,
                    modifier = Modifier.size(22.dp)
                )
            }
        }
    }
}

// -------------------------------------------------------------------------------------------------
// COMPONENT: TOP ARTIST ROW ITEM
// -------------------------------------------------------------------------------------------------
@Composable
private fun TopArtistRowItem(
    rank: Int,
    artist: String,
    playCount: Int,
    totalTracksPlayed: Int
) {
    val colors = LocalSwarixColors.current
    val pct = ((playCount.toFloat() / totalTracksPlayed.toFloat().coerceAtLeast(1f)) * 100).toInt()

    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .testTag("top_artist_item_$rank"),
        color = colors.surface
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier.width(36.dp),
                contentAlignment = Alignment.CenterStart
            ) {
                when (rank) {
                    1 -> Text(text = "🥇", fontSize = 20.sp)
                    2 -> Text(text = "🥈", fontSize = 18.sp)
                    3 -> Text(text = "🥉", fontSize = 18.sp)
                    else -> Text(
                        text = "#$rank",
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Bold,
                            color = colors.mutedText
                        )
                    )
                }
            }

            Box(
                modifier = Modifier
                    .size(46.dp)
                    .clip(CircleShape)
                    .background(colors.primaryAccent.copy(alpha = 0.2f)),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = artist.firstOrNull()?.toString() ?: "A",
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.Bold,
                        color = colors.primaryAccent
                    )
                )
            }

            Spacer(modifier = Modifier.width(14.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = artist,
                    style = MaterialTheme.typography.bodyLarge.copy(
                        fontWeight = FontWeight.Bold,
                        color = colors.primaryText
                    ),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = "$playCount streams • $pct% of your listening",
                    style = MaterialTheme.typography.bodySmall.copy(color = colors.secondaryText)
                )
            }

            Icon(
                imageVector = Icons.Default.Stars,
                contentDescription = null,
                tint = if (rank == 1) colors.primaryAccent else colors.divider,
                modifier = Modifier.size(20.dp)
            )
        }
    }
}

// -------------------------------------------------------------------------------------------------
// COMPONENT: MILESTONES GRID
// -------------------------------------------------------------------------------------------------
@Composable
private fun MilestonesGrid(
    minutes: Int,
    tracks: Int,
    streakDays: Int
) {
    val colors = LocalSwarixColors.current

    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        Text(
            text = "2026 LISTENING MILESTONES",
            style = MaterialTheme.typography.labelSmall.copy(
                color = colors.primaryAccent,
                fontWeight = FontWeight.Bold,
                letterSpacing = 1.sp
            )
        )

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            MilestoneCard(
                modifier = Modifier.weight(1f),
                emoji = "⏱️",
                title = "${minutes / 60}h ${minutes % 60}m",
                subtitle = "Total Playtime"
            )
            MilestoneCard(
                modifier = Modifier.weight(1f),
                emoji = "🎧",
                title = "$tracks Tracks",
                subtitle = "Unique Streams"
            )
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            MilestoneCard(
                modifier = Modifier.weight(1f),
                emoji = "🔥",
                title = "$streakDays Days",
                subtitle = "Listening Streak"
            )
            MilestoneCard(
                modifier = Modifier.weight(1f),
                emoji = "💿",
                title = "4K Hi-Res",
                subtitle = "Audiophile Studio"
            )
        }
    }
}

@Composable
private fun MilestoneCard(
    modifier: Modifier = Modifier,
    emoji: String,
    title: String,
    subtitle: String
) {
    val colors = LocalSwarixColors.current
    Surface(
        modifier = modifier.clip(RoundedCornerShape(16.dp)),
        color = colors.elevatedSurface
    ) {
        Row(
            modifier = Modifier.padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(text = emoji, fontSize = 22.sp)
            Spacer(modifier = Modifier.width(10.dp))
            Column {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleSmall.copy(
                        fontWeight = FontWeight.Bold,
                        color = colors.primaryText
                    )
                )
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.labelSmall.copy(color = colors.secondaryText)
                )
            }
        }
    }
}

// -------------------------------------------------------------------------------------------------
// COMPONENT: EMPTY STATE
// -------------------------------------------------------------------------------------------------
@Composable
private fun EmptySectionCard(message: String) {
    val colors = LocalSwarixColors.current
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp)),
        color = colors.surface
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                imageVector = Icons.Default.MusicNote,
                contentDescription = null,
                tint = colors.mutedText,
                modifier = Modifier.size(36.dp)
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = message,
                style = MaterialTheme.typography.bodyMedium.copy(color = colors.secondaryText),
                textAlign = TextAlign.Center
            )
        }
    }
}

// -------------------------------------------------------------------------------------------------
// COMPONENT: SHARE WRAPPED STORY MODAL
// -------------------------------------------------------------------------------------------------
@Composable
private fun ShareWrappedModal(
    stats: ListeningStats,
    onDismiss: () -> Unit
) {
    var isCopied by remember { mutableStateOf(false) }

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(26.dp))
                .testTag("share_wrapped_modal"),
            color = Color(0xFF0F1210)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(
                        Brush.verticalGradient(
                            listOf(
                                Color(0xFFD4AF37).copy(alpha = 0.25f),
                                Color(0xFF141A16),
                                Color(0xFF0F1210)
                            )
                        )
                    )
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Header badge
                Surface(
                    shape = RoundedCornerShape(14.dp),
                    color = Color.Black.copy(alpha = 0.6f)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(text = "🎵", fontSize = 14.sp)
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "SWARIX WRAPPED 2026",
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontWeight = FontWeight.ExtraBold,
                                color = Color(0xFFD4AF37),
                                letterSpacing = 1.2.sp
                            )
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Sound Persona Title
                Text(
                    text = stats.soundPersonality,
                    style = MaterialTheme.typography.headlineMedium.copy(
                        fontWeight = FontWeight.ExtraBold,
                        color = Color.White
                    ),
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(6.dp))

                Text(
                    text = "\"${stats.personalityDescription}\"",
                    style = MaterialTheme.typography.bodySmall.copy(
                        color = Color.White.copy(alpha = 0.8f),
                        textAlign = TextAlign.Center
                    )
                )

                Spacer(modifier = Modifier.height(18.dp))

                // Wrapped Snapshot Card
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(18.dp),
                    color = Color.White.copy(alpha = 0.08f)
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = "Minutes Listened",
                                style = MaterialTheme.typography.bodySmall.copy(color = Color.White.copy(alpha = 0.7f))
                            )
                            Text(
                                text = "${stats.totalMinutesListened} mins",
                                style = MaterialTheme.typography.bodyMedium.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFFD4AF37)
                                )
                            )
                        }

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = "Top Track",
                                style = MaterialTheme.typography.bodySmall.copy(color = Color.White.copy(alpha = 0.7f))
                            )
                            Text(
                                text = stats.topSongs.firstOrNull()?.title ?: "Apna Bana Le",
                                style = MaterialTheme.typography.bodyMedium.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White
                                ),
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = "Top Artist",
                                style = MaterialTheme.typography.bodySmall.copy(color = Color.White.copy(alpha = 0.7f))
                            )
                            Text(
                                text = stats.topArtists.firstOrNull()?.first ?: "Arijit Singh",
                                style = MaterialTheme.typography.bodyMedium.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White
                                ),
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = "Daily Streak",
                                style = MaterialTheme.typography.bodySmall.copy(color = Color.White.copy(alpha = 0.7f))
                            )
                            Text(
                                text = "${stats.streakDays} Days 🔥",
                                style = MaterialTheme.typography.bodyMedium.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFFFFB74D)
                                )
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))

                Button(
                    onClick = {
                        isCopied = true
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp),
                    shape = RoundedCornerShape(24.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFD4AF37))
                ) {
                    Icon(
                        imageVector = Icons.Default.Share,
                        contentDescription = null,
                        tint = Color.Black,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = if (isCopied) "Saved to Clipboard!" else "Share Wrapped Card",
                        style = MaterialTheme.typography.labelLarge.copy(
                            fontWeight = FontWeight.Bold,
                            color = Color.Black
                        )
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                IconButton(onClick = onDismiss) {
                    Text(
                        text = "Close",
                        style = MaterialTheme.typography.labelMedium.copy(
                            color = Color.White.copy(alpha = 0.6f)
                        )
                    )
                }
            }
        }
    }
}
