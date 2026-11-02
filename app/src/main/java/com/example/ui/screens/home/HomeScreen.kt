package com.example.ui.screens.home

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
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Equalizer
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.core.model.Artist
import com.example.core.model.FriendActivity
import com.example.core.model.Playlist
import com.example.core.model.Song
import com.example.core.smartplaylist.DailyMix
import com.example.core.voice.VoiceSearchEngine
import com.example.ui.components.ArtistSpotlightSection
import com.example.ui.components.DailyMixesSection
import com.example.ui.components.FriendActivityTray
import com.example.ui.components.SongRowItem
import com.example.ui.components.TrendInsightsCard
import com.example.ui.components.VoiceSearchDialog
import com.example.ui.components.bouncyClick
import com.example.ui.theme.LocalSwarixColors

/**
 * Exact Home Screen matching Swarix UI Reference Images (Dark & Light).
 * Features:
 * - App Header with Swarix brand logo, notifications bell, and profile avatar
 * - Quick Search Bar shortcut with Mic icon
 * - Top Filter Chips: Music, Podcasts, Radio, Mood
 * - Hero Featured Card ("Good Evening Ritik / Let the music set the mood" with 4K Hi-Res Audio tag)
 * - Spotify-style Friend Activity Tray
 * - For Your Mood carousel
 * - AI-Driven Daily Mixes Smart Playlists
 * - Recently Played tracklist
 * - Popular Artists circular avatars
 * - Made for You playlists
 */
import androidx.compose.material.icons.filled.Sync

// ...

@Composable
fun HomeScreen(
    allSongs: List<Song>,
    recentlyPlayed: List<Song>,
    playlists: List<Playlist>,
    topArtists: List<Artist>,
    friendActivity: List<FriendActivity>,
    currentPlayingSong: Song?,
    isPlaying: Boolean,
    onSongClick: (Song) -> Unit,
    onPlaylistClick: (Playlist) -> Unit,
    onFriendClick: (FriendActivity) -> Unit,
    onArtistSpotlightClick: (String, String) -> Unit,
    onOpenStats: () -> Unit,
    onArtistClick: (String) -> Unit,
    onMoodClick: (String) -> Unit,
    onFavoriteToggle: (Song) -> Unit,
    onNotificationsClick: () -> Unit,
    onJoinSyncSession: () -> Unit,
    onSearchShortcutClick: (() -> Unit)? = null,
    dailyMixes: List<DailyMix> = emptyList(),
    onDailyMixClick: (DailyMix) -> Unit = {},
    onPlayDailyMix: (DailyMix) -> Unit = {},
    onRegenerateDailyMixes: () -> Unit = {},
    onRescanLibrary: (() -> Unit)? = null,
    userName: String = "Ritik Kumar",
    userAvatar: String = "https://images.unsplash.com/photo-1535713875002-d1d0cf377fde?w=200&auto=format&fit=crop&q=80",
    onProfileClick: (() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    val colors = LocalSwarixColors.current
    var selectedCategoryTab by remember { mutableStateOf("Music") }
    var showVoiceDialog by remember { mutableStateOf(false) }
    val categories = listOf("Music", "Podcasts", "Radio", "Mood")

    val popularArtists = remember {
        listOf(
            Artist("Sidhu Moose Wala", 44, "https://upload.wikimedia.org/wikipedia/commons/thumb/7/7a/Sidhu_Moose_Wala_during_the_shooting_of_his_film_Moosa_Jatt_%28cropped%29.jpg/500px-Sidhu_Moose_Wala_during_the_shooting_of_his_film_Moosa_Jatt_%28cropped%29.jpg"),
            Artist("Arijit Singh", 58, "https://upload.wikimedia.org/wikipedia/commons/thumb/b/b7/Arijit_Singh_performance_at_Chandigarh_2025.jpg/500px-Arijit_Singh_performance_at_Chandigarh_2025.jpg"),
            Artist("Karan Aujla", 36, "https://upload.wikimedia.org/wikipedia/commons/thumb/7/76/Karan_Aujla_2020.jpg/500px-Karan_Aujla_2020.jpg"),
            Artist("Diljit Dosanjh", 32, "https://upload.wikimedia.org/wikipedia/commons/e/e2/Diljit_Dosanjh.jpg"),
            Artist("Masoom Sharma", 26, "https://c.saavncdn.com/233/4-5-Pistol-Haryanvi-2023-20230502053127-500x500.jpg"),
            Artist("Shreya Ghoshal", 42, "https://upload.wikimedia.org/wikipedia/commons/thumb/a/a0/Shreya_Ghoshal_Behindwoods_Gold_Icons_Awards_2023_%28cropped%29.jpg/500px-Shreya_Ghoshal_Behindwoods_Gold_Icons_Awards_2023_%28cropped%29.jpg"),
            Artist("Jubin Nautiyal", 28, "https://upload.wikimedia.org/wikipedia/commons/thumb/9/90/Jubin_Nauityal_at_the_Good_Homes_Awards_2015.jpg/500px-Jubin_Nauityal_at_the_Good_Homes_Awards_2015.jpg"),
            Artist("AP Dhillon", 22, "https://upload.wikimedia.org/wikipedia/commons/thumb/9/95/AP_Dhillon_CA.jpg/500px-AP_Dhillon_CA.jpg"),
            Artist("Badshah", 30, "https://upload.wikimedia.org/wikipedia/commons/thumb/c/c5/Badshah_at_the_launch_of_his_song_%E2%80%98Heartless%E2%80%99_%28cropped%29.jpg/500px-Badshah_at_the_launch_of_his_song_%E2%80%98Heartless%E2%80%99_%28cropped%29.jpg"),
            Artist("Atif Aslam", 35, "https://upload.wikimedia.org/wikipedia/commons/thumb/2/2d/Atif_Aslam_at_Badlapur_%28cropped%29.jpg/500px-Atif_Aslam_at_Badlapur_%28cropped%29.jpg")
        )
    }

    val moodCards = remember {
        listOf(
            Triple("Chill Evening", "Let go and relax", "https://images.unsplash.com/photo-1518495973542-4542c06a5843?w=500&auto=format&fit=crop&q=80"),
            Triple("Workout", "High bpm energy", "https://images.unsplash.com/photo-1517838277536-f5f99be501cd?w=500&auto=format&fit=crop&q=80"),
            Triple("Deep Focus", "Ambient & study", "https://images.unsplash.com/photo-1434030216411-0b793f4b4173?w=500&auto=format&fit=crop&q=80"),
            Triple("Romance", "Warm love ballads", "https://images.unsplash.com/photo-1516589178581-6cd7833ae3b2?w=500&auto=format&fit=crop&q=80")
        )
    }

    val offlineSongs = remember(allSongs) { allSongs.filter { it.isLocal } }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(colors.mainBackground),
        contentPadding = PaddingValues(bottom = 120.dp)
    ) {
        // App Header with Branding, Notification Bell, and Profile Avatar
        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .statusBarsPadding()
                    .padding(horizontal = 16.dp, vertical = 10.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    com.example.ui.components.SwarixLogoMark(
                        size = 32.dp,
                        enableGlow = false
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Text(
                        text = "Swarix",
                        style = MaterialTheme.typography.titleLarge.copy(
                            fontWeight = FontWeight.Bold,
                            color = colors.primaryText,
                            letterSpacing = 0.5.sp
                        )
                    )
                }

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    IconButton(onClick = onJoinSyncSession) {
                        Icon(
                            imageVector = Icons.Default.Sync,
                            contentDescription = "Sync Session",
                            tint = colors.primaryAccent
                        )
                    }

                    BadgedBox(
                        badge = { Badge { Text("2") } }
                    ) {
                        IconButton(onClick = onNotificationsClick) {
                            Icon(
                                imageVector = Icons.Default.Notifications,
                                contentDescription = "Notifications",
                                tint = colors.primaryText
                            )
                        }
                    }

                    // Profile Avatar (User Chosen Avatar)
                    Box(
                        modifier = Modifier
                            .size(34.dp)
                            .clip(CircleShape)
                            .background(colors.elevatedSurface)
                            .border(1.5.dp, Color(0xFF1ED760), CircleShape)
                            .clickable { onProfileClick?.invoke() }
                    ) {
                        AsyncImage(
                            model = userAvatar,
                            contentDescription = "Profile",
                            contentScale = ContentScale.Crop,
                            modifier = Modifier.fillMaxSize()
                        )
                    }
                }
            }
        }

        // Quick Search Bar Shortcut (as in Image 2 Screen 2)
        item {
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 6.dp)
                    .clip(RoundedCornerShape(24.dp))
                    .clickable { onSearchShortcutClick?.invoke() }
                    .testTag("home_quick_search_bar"),
                color = colors.surface,
                shape = RoundedCornerShape(24.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, colors.divider)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.Search,
                        contentDescription = "Search",
                        tint = colors.mutedText,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Text(
                        text = "Search songs, artists, albums...",
                        style = MaterialTheme.typography.bodyMedium.copy(
                            color = colors.mutedText
                        ),
                        modifier = Modifier.weight(1f)
                    )
                    Icon(
                        imageVector = Icons.Default.Mic,
                        contentDescription = "Voice search",
                        tint = colors.mutedText,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
        }


        // Spotify-style Filter Chips (All, Music, Podcasts, Offline)
        item {
            val spotifyFilters = listOf("All", "Music", "Podcasts", "Offline")
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 6.dp)
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                spotifyFilters.forEach { tab ->
                    val isSelected = tab == selectedCategoryTab || (selectedCategoryTab !in spotifyFilters && tab == "All")
                    FilterChip(
                        selected = isSelected,
                        onClick = { selectedCategoryTab = tab },
                        label = {
                            Text(
                                text = tab,
                                style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.SemiBold)
                            )
                        },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = Color(0xFF1ED760), // Spotify Signature Green
                            selectedLabelColor = Color.Black,
                            containerColor = colors.surface,
                            labelColor = colors.primaryText
                        ),
                        border = FilterChipDefaults.filterChipBorder(
                            enabled = true,
                            selected = isSelected,
                            borderColor = if (isSelected) Color(0xFF1ED760) else colors.divider
                        ),
                        shape = RoundedCornerShape(20.dp)
                    )
                }
            }
        }

        // Offline Mode Banner when "Offline" tab is selected
        if (selectedCategoryTab == "Offline") {
            item {
                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 8.dp),
                    shape = RoundedCornerShape(16.dp),
                    color = Color(0xFF1B2E23),
                    border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF1ED760).copy(alpha = 0.4f))
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(32.dp)
                                    .clip(CircleShape)
                                    .background(Color(0xFF1ED760)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.PlayArrow,
                                    contentDescription = null,
                                    tint = Color.Black,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = "Offline Mode Enabled",
                                    style = MaterialTheme.typography.titleMedium.copy(
                                        fontWeight = FontWeight.Bold,
                                        color = Color.White
                                    )
                                )
                                Text(
                                    text = "Playing without internet • ${offlineSongs.size} offline tracks available",
                                    style = MaterialTheme.typography.bodySmall.copy(
                                        color = Color(0xFF1ED760)
                                    )
                                )
                            }
                        }
                        if (onRescanLibrary != null) {
                            Spacer(modifier = Modifier.height(12.dp))
                            Surface(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(20.dp))
                                    .clickable { onRescanLibrary() },
                                color = Color(0xFF1ED760),
                                shape = RoundedCornerShape(20.dp)
                            ) {
                                Text(
                                    text = "Scan Phone Storage for Music",
                                    style = MaterialTheme.typography.labelMedium.copy(
                                        color = Color.Black,
                                        fontWeight = FontWeight.Bold
                                    ),
                                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp)
                                )
                            }
                        }
                    }
                }
            }
        }

        // Spotify Signature 2-Column Quick Access Grid (8 items matching Spotify Home)
        item {
            val quickItems = remember(recentlyPlayed, allSongs, selectedCategoryTab) {
                val pool = if (selectedCategoryTab == "Offline") {
                    allSongs.filter { it.isLocal }
                } else if (recentlyPlayed.isNotEmpty()) {
                    recentlyPlayed
                } else {
                    allSongs
                }
                pool.take(8)
            }

            if (quickItems.isNotEmpty()) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 6.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    val rows = quickItems.chunked(2)
                    rows.forEach { pair ->
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            pair.forEach { itemSong ->
                                Surface(
                                    modifier = Modifier
                                        .weight(1f)
                                        .height(56.dp)
                                        .clip(RoundedCornerShape(6.dp))
                                        .clickable { onSongClick(itemSong) }
                                        .testTag("quick_item_${itemSong.id}"),
                                    color = colors.surface,
                                    shape = RoundedCornerShape(6.dp)
                                ) {
                                    Row(
                                        modifier = Modifier.fillMaxSize(),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        AsyncImage(
                                            model = itemSong.artworkUri.ifBlank { "https://images.unsplash.com/photo-1514525253161-7a46d19cd819?w=200&auto=format&fit=crop&q=80" },
                                            contentDescription = itemSong.title,
                                            contentScale = ContentScale.Crop,
                                            modifier = Modifier
                                                .size(56.dp)
                                                .clip(RoundedCornerShape(topStart = 6.dp, bottomStart = 6.dp))
                                        )
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Text(
                                            text = itemSong.title,
                                            style = MaterialTheme.typography.labelMedium.copy(
                                                fontWeight = FontWeight.Bold,
                                                color = colors.primaryText,
                                                fontSize = 12.sp
                                            ),
                                            maxLines = 2,
                                            overflow = TextOverflow.Ellipsis,
                                            modifier = Modifier
                                                .weight(1f)
                                                .padding(end = 6.dp)
                                        )
                                    }
                                }
                            }
                            if (pair.size == 1) {
                                Spacer(modifier = Modifier.weight(1f))
                            }
                        }
                    }
                }
            }
        }

        if (selectedCategoryTab == "Offline") {
            item {
                Spacer(modifier = Modifier.height(14.dp))
                SectionTitle(title = "Device Storage Songs (${offlineSongs.size})", onSeeAllClick = null)
            }
            if (offlineSongs.isEmpty()) {
                item {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 24.dp, vertical = 24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "No Local Audio Files Found",
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.Bold,
                                color = colors.primaryText
                            )
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Download or copy MP3 songs to your phone, then tap 'Scan Phone Storage for Music' above.",
                            style = MaterialTheme.typography.bodySmall.copy(
                                color = colors.secondaryText,
                                textAlign = androidx.compose.ui.text.style.TextAlign.Center
                            )
                        )
                    }
                }
            } else {
                items(offlineSongs) { song ->
                    SongRowItem(
                        song = song,
                        isPlaying = song.id == currentPlayingSong?.id && isPlaying,
                        onSongClick = { onSongClick(song) },
                        onFavoriteToggle = { onFavoriteToggle(song) }
                    )
                }
            }
        } else {
            // Section: It's New Music Friday! (Horizontal Scroll Cards like Spotify Image 1)
            item {
                Spacer(modifier = Modifier.height(14.dp))
                SectionTitle(title = "It's New Music Friday!", onSeeAllClick = {})
            }

        item {
            val newMusicList = remember(allSongs) {
                if (allSongs.isNotEmpty()) allSongs.shuffled().take(6) else emptyList()
            }
            LazyRow(
                contentPadding = PaddingValues(horizontal = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                items(newMusicList) { song ->
                    Column(
                        modifier = Modifier
                            .width(148.dp)
                            .bouncyClick { onSongClick(song) }
                            .testTag("new_friday_${song.id}")
                    ) {
                        Box(
                            modifier = Modifier
                                .size(148.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .background(colors.surface)
                        ) {
                            AsyncImage(
                                model = song.artworkUri,
                                contentDescription = song.title,
                                contentScale = ContentScale.Crop,
                                modifier = Modifier.fillMaxSize()
                            )
                            // Spotify logo badge top-left
                            Box(
                                modifier = Modifier
                                    .padding(8.dp)
                                    .size(20.dp)
                                    .clip(CircleShape)
                                    .background(Color.Black.copy(alpha = 0.6f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Equalizer,
                                    contentDescription = null,
                                    tint = Color(0xFF1ED760),
                                    modifier = Modifier.size(12.dp)
                                )
                            }
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = song.title,
                            style = MaterialTheme.typography.bodyMedium.copy(
                                fontWeight = FontWeight.Bold,
                                color = colors.primaryText
                            ),
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Text(
                            text = song.artist,
                            style = MaterialTheme.typography.bodySmall.copy(
                                color = colors.secondaryText
                            ),
                            maxLines = 2,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }
            }
        }

        // Section: Jump Back In / Recently Played
        item {
            Spacer(modifier = Modifier.height(18.dp))
            SectionTitle(title = "Jump Back In", onSeeAllClick = {})
        }

        item {
            val jumpBackList = remember(recentlyPlayed, allSongs) {
                val list = if (recentlyPlayed.isNotEmpty()) recentlyPlayed else allSongs
                list.take(10)
            }
            LazyRow(
                contentPadding = PaddingValues(horizontal = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                items(jumpBackList) { song ->
                    Column(
                        modifier = Modifier
                            .width(136.dp)
                            .bouncyClick { onSongClick(song) }
                            .testTag("jump_back_${song.id}")
                    ) {
                        Box(
                            modifier = Modifier
                                .size(136.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .background(colors.surface)
                        ) {
                            AsyncImage(
                                model = song.artworkUri,
                                contentDescription = song.title,
                                contentScale = ContentScale.Crop,
                                modifier = Modifier.fillMaxSize()
                            )
                        }
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = song.title,
                            style = MaterialTheme.typography.bodyMedium.copy(
                                fontWeight = FontWeight.SemiBold,
                                color = colors.primaryText
                            ),
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Text(
                            text = "Song • ${song.artist}",
                            style = MaterialTheme.typography.bodySmall.copy(
                                color = colors.secondaryText,
                                fontSize = 11.sp
                            ),
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }
            }
        }

        // Artist Spotlight
        item {
            ArtistSpotlightSection(
                artists = topArtists,
                onArtistClick = onArtistSpotlightClick
            )
        }

        // Spotify-style Social Friend Activity Tray
        item {
            FriendActivityTray(
                friends = friendActivity,
                onFriendClick = onFriendClick
            )
        }

        // Spotify-style Listening Stats Summary Banner
        item {
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp)
                    .clip(RoundedCornerShape(18.dp))
                    .bouncyClick(onClick = onOpenStats)
                    .testTag("home_stats_banner"),
                color = colors.elevatedSurface
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(14.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(44.dp)
                            .clip(CircleShape)
                            .background(colors.primaryAccent.copy(alpha = 0.2f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "📊",
                            fontSize = 20.sp
                        )
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Your Listening Analytics",
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.Bold,
                                color = colors.primaryText
                            )
                        )
                        Text(
                            text = "Explore top genres, artists & playback history",
                            style = MaterialTheme.typography.bodyMedium.copy(
                                color = colors.secondaryText
                            )
                        )
                    }
                    Text(
                        text = "View",
                        style = MaterialTheme.typography.labelLarge.copy(
                            fontWeight = FontWeight.Bold,
                            color = colors.primaryAccent
                        )
                    )
                }
            }
        }

        // For Your Mood
        item {
            SectionTitle(title = "For Your Mood", onSeeAllClick = { onMoodClick("Chill") })
        }

        item {
            LazyRow(
                contentPadding = PaddingValues(horizontal = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(moodCards) { (title, subtitle, img) ->
                    Surface(
                        modifier = Modifier
                            .width(140.dp)
                            .height(140.dp)
                            .clip(RoundedCornerShape(18.dp))
                            .bouncyClick { onMoodClick(title) }
                            .testTag("mood_card_${title.lowercase()}"),
                        color = colors.elevatedSurface
                    ) {
                        Box {
                            AsyncImage(
                                model = img,
                                contentDescription = title,
                                contentScale = ContentScale.Crop,
                                modifier = Modifier.fillMaxSize()
                            )
                            Box(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .background(
                                        Brush.verticalGradient(
                                            listOf(
                                                Color.Transparent,
                                                Color.Black.copy(alpha = 0.85f)
                                            )
                                        )
                                    )
                            )
                            Column(
                                modifier = Modifier
                                    .align(Alignment.BottomStart)
                                    .padding(10.dp)
                            ) {
                                Text(
                                    text = title,
                                    style = MaterialTheme.typography.titleMedium.copy(
                                        fontWeight = FontWeight.Bold,
                                        color = Color.White
                                    ),
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                                Text(
                                    text = subtitle,
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        color = Color.White.copy(alpha = 0.75f)
                                    ),
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                        }
                    }
                }
            }
        }

        // Spotify-style AI Daily Mixes Smart Playlists
        if (dailyMixes.isNotEmpty()) {
            item {
                Spacer(modifier = Modifier.height(18.dp))
                DailyMixesSection(
                    dailyMixes = dailyMixes,
                    onMixClick = onDailyMixClick,
                    onPlayMix = onPlayDailyMix,
                    onRegenerateClick = onRegenerateDailyMixes
                )
            }
        }

        // Recently Played
        item {
            Spacer(modifier = Modifier.height(16.dp))
            SectionTitle(title = "Recently Played", onSeeAllClick = {})
        }

        val displayRecent = if (recentlyPlayed.isNotEmpty()) recentlyPlayed else allSongs.take(4)
        items(displayRecent) { song ->
            SongRowItem(
                song = song,
                isPlaying = song.id == currentPlayingSong?.id && isPlaying,
                onSongClick = { onSongClick(song) },
                onFavoriteToggle = { onFavoriteToggle(song) }
            )
        }

        // Popular Artists
        item {
            Spacer(modifier = Modifier.height(16.dp))
            SectionTitle(title = "Popular Artists", onSeeAllClick = {})
        }

        item {
            LazyRow(
                contentPadding = PaddingValues(horizontal = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                items(popularArtists) { artist ->
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier
                            .width(86.dp)
                            .bouncyClick { onArtistClick(artist.name) }
                            .testTag("artist_${artist.name.lowercase()}")
                    ) {
                        Box(
                            modifier = Modifier
                                .size(78.dp)
                                .clip(CircleShape)
                                .background(colors.elevatedSurface)
                                .border(1.5.dp, colors.divider, CircleShape)
                        ) {
                            AsyncImage(
                                model = artist.artworkUri,
                                contentDescription = artist.name,
                                contentScale = ContentScale.Crop,
                                modifier = Modifier.fillMaxSize()
                            )
                        }
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = artist.name,
                            style = MaterialTheme.typography.bodyMedium.copy(
                                fontWeight = FontWeight.SemiBold,
                                color = colors.primaryText
                            ),
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Text(
                            text = "Artist",
                            style = MaterialTheme.typography.bodySmall.copy(
                                color = colors.secondaryText,
                                fontSize = 11.sp
                            )
                        )
                    }
                }
            }
        }

        // Recommended Playlists
        item {
            Spacer(modifier = Modifier.height(16.dp))
            SectionTitle(title = "Made for You", onSeeAllClick = {})
        }

        item {
            LazyRow(
                contentPadding = PaddingValues(horizontal = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                items(playlists) { playlist ->
                    Surface(
                        modifier = Modifier
                            .width(150.dp)
                            .clip(RoundedCornerShape(18.dp))
                            .bouncyClick { onPlaylistClick(playlist) }
                            .testTag("playlist_card_${playlist.id}"),
                        color = colors.elevatedSurface
                    ) {
                        Column {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(140.dp)
                                    .background(colors.surface)
                            ) {
                                if (playlist.coverUri.isNotEmpty()) {
                                    AsyncImage(
                                        model = playlist.coverUri,
                                        contentDescription = playlist.name,
                                        contentScale = ContentScale.Crop,
                                        modifier = Modifier.fillMaxSize()
                                    )
                                } else {
                                    Box(
                                        modifier = Modifier
                                            .fillMaxSize()
                                            .background(colors.primaryAccent),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(
                                            text = playlist.name.take(1),
                                            style = MaterialTheme.typography.displayMedium.copy(
                                                color = colors.mainBackground,
                                                fontWeight = FontWeight.Bold
                                            )
                                        )
                                    }
                                }
                            }
                            Column(modifier = Modifier.padding(10.dp)) {
                                Text(
                                    text = playlist.name,
                                    style = MaterialTheme.typography.titleMedium.copy(
                                        fontWeight = FontWeight.SemiBold,
                                        color = colors.primaryText
                                    ),
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                                Text(
                                    text = playlist.description.ifEmpty { "Curated by Swarix" },
                                    style = MaterialTheme.typography.bodyMedium.copy(
                                        color = colors.secondaryText
                                    ),
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                        }
                    }
                }
            }
        }
        }
    }

    if (showVoiceDialog) {
        VoiceSearchDialog(
            onDismiss = { showVoiceDialog = false },
            onVoiceResult = { spokenQuery ->
                showVoiceDialog = false
                val result = VoiceSearchEngine.processVoiceQuery(spokenQuery, allSongs)
                if (result.matchedSongs.isNotEmpty()) {
                    onSongClick(result.matchedSongs.first())
                } else {
                    onSearchShortcutClick?.invoke()
                }
            }
        )
    }
}

@Composable
fun SectionTitle(
    title: String,
    onSeeAllClick: (() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    val colors = LocalSwarixColors.current

    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.titleLarge.copy(
                fontWeight = FontWeight.Bold,
                color = colors.primaryText
            )
        )
        if (onSeeAllClick != null) {
            Text(
                text = "See all",
                style = MaterialTheme.typography.labelLarge.copy(
                    fontWeight = FontWeight.SemiBold,
                    color = colors.primaryAccent
                ),
                modifier = Modifier.clickable(onClick = onSeeAllClick)
            )
        }
    }
}
