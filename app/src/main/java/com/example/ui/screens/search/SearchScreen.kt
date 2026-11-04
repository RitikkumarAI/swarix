package com.example.ui.screens.search

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
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.PlaylistAdd
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.core.model.Artist
import com.example.core.model.Playlist
import com.example.core.model.Song
import com.example.core.voice.VoiceSearchEngine
import com.example.core.voice.VoiceSearchResult
import com.example.ui.components.SongRowItem
import com.example.ui.components.VoiceSearchDialog
import com.example.ui.components.bouncyClick
import com.example.ui.theme.AccentAmber
import com.example.ui.theme.AccentBlue
import com.example.ui.theme.AccentPurple
import com.example.ui.theme.AccentRose
import com.example.ui.theme.AccentTeal
import com.example.ui.theme.LocalSwarixColors

data class MoodCategory(val name: String, val emoji: String, val color: Color)

@Composable
fun SearchScreen(
    allSongs: List<Song>,
    currentPlayingSong: Song?,
    isPlaying: Boolean,
    playlists: List<Playlist> = emptyList(),
    recentSearches: List<String> = emptyList(),
    onlineResults: List<Song> = emptyList(),
    isSearchingOnline: Boolean = false,
    onSongClick: (Song, List<Song>) -> Unit,
    onArtistClick: (String) -> Unit,
    onArtistProfileClick: ((String, String) -> Unit)? = null,
    onFavoriteToggle: (Song) -> Unit,
    onSavePlaylist: ((String, List<Song>) -> Unit)? = null,
    onSearch: (String) -> Unit,
    onClearRecentSearches: () -> Unit,
    modifier: Modifier = Modifier
) {
    val colors = LocalSwarixColors.current
    var searchQuery by remember { mutableStateOf("") }
    var selectedFilter by remember { mutableStateOf("All") }
    var showVoiceDialog by remember { mutableStateOf(false) }
    var voiceSearchResult by remember { mutableStateOf<VoiceSearchResult?>(null) }
    val filterTabs = listOf("All", "Songs", "Artists", "Albums", "Playlists")

    val moodCategories = remember {
        listOf(
            MoodCategory("Happy", "😊", AccentAmber),
            MoodCategory("Sad", "🌧️", AccentBlue),
            MoodCategory("Love", "❤️", AccentRose),
            MoodCategory("Focus", "🎯", AccentTeal),
            MoodCategory("Party", "✨", AccentPurple),
            MoodCategory("Devotional", "🪔", Color(0xFFD48B47))
        )
    }

    val trendingPicks = remember {
        listOf(
            Pair("India Top 50", "https://images.unsplash.com/photo-1514525253161-7a46d19cd819?w=400&auto=format&fit=crop&q=80"),
            Pair("Viral Hits", "https://images.unsplash.com/photo-1470225620780-dba8ba36b745?w=400&auto=format&fit=crop&q=80"),
            Pair("New Releases", "https://images.unsplash.com/photo-1492684223066-81342ee5ff30?w=400&auto=format&fit=crop&q=80")
        )
    }

    val popularArtists = remember {
        listOf(
            Artist("Arijit Singh", 58, "https://upload.wikimedia.org/wikipedia/commons/thumb/b/b7/Arijit_Singh_performance_at_Chandigarh_2025.jpg/500px-Arijit_Singh_performance_at_Chandigarh_2025.jpg"),
            Artist("Diljit Dosanjh", 32, "https://upload.wikimedia.org/wikipedia/commons/thumb/e/e2/Diljit_Dosanjh.jpg/500px-Diljit_Dosanjh.jpg"),
            Artist("Sidhu Moose Wala", 44, "https://upload.wikimedia.org/wikipedia/commons/thumb/7/7a/Sidhu_Moose_Wala_during_the_shooting_of_his_film_Moosa_Jatt_%28cropped%29.jpg/500px-Sidhu_Moose_Wala_during_the_shooting_of_his_film_Moosa_Jatt_%28cropped%29.jpg"),
            Artist("Shreya Ghoshal", 42, "https://upload.wikimedia.org/wikipedia/commons/thumb/a/a0/Shreya_Ghoshal_Behindwoods_Gold_Icons_Awards_2023_%28cropped%29.jpg/500px-Shreya_Ghoshal_Behindwoods_Gold_Icons_Awards_2023_%28cropped%29.jpg"),
            Artist("Pritam", 35, "https://upload.wikimedia.org/wikipedia/commons/thumb/7/7b/Pritam_at_the_Filmfare_Glamour_and_Style_Awards_2016.jpg/500px-Pritam_at_the_Filmfare_Glamour_and_Style_Awards_2016.jpg"),
            Artist("Sonu Nigam", 40, "https://upload.wikimedia.org/wikipedia/commons/thumb/6/69/Sonu_Nigam_in_2017.jpg/500px-Sonu_Nigam_in_2017.jpg"),
            Artist("AP Dhillon", 22, "https://upload.wikimedia.org/wikipedia/commons/thumb/9/95/AP_Dhillon_CA.jpg/500px-AP_Dhillon_CA.jpg"),
            Artist("Badshah", 30, "https://upload.wikimedia.org/wikipedia/commons/thumb/c/c5/Badshah_at_the_launch_of_his_song_%E2%80%98Heartless%E2%80%99_%28cropped%29.jpg/500px-Badshah_at_the_launch_of_his_song_%E2%80%98Heartless%E2%80%99_%28cropped%29.jpg"),
            Artist("Jubin Nautiyal", 28, "https://upload.wikimedia.org/wikipedia/commons/thumb/9/90/Jubin_Nauityal_at_the_Good_Homes_Awards_2015.jpg/500px-Jubin_Nauityal_at_the_Good_Homes_Awards_2015.jpg"),
            Artist("Neha Kakkar", 25, "https://upload.wikimedia.org/wikipedia/commons/thumb/8/87/Neha_Kakkar_in_2019.jpg/500px-Neha_Kakkar_in_2019.jpg"),
            Artist("Atif Aslam", 35, "https://upload.wikimedia.org/wikipedia/commons/thumb/2/2d/Atif_Aslam_at_Badlapur_%28cropped%29.jpg/500px-Atif_Aslam_at_Badlapur_%28cropped%29.jpg"),
            Artist("Taylor Swift", 45, "https://upload.wikimedia.org/wikipedia/commons/thumb/b/b1/Taylor_Swift_at_the_2023_MTV_Video_Music_Awards_4.jpg/500px-Taylor_Swift_at_the_2023_MTV_Video_Music_Awards_4.jpg"),
            Artist("Ed Sheeran", 38, "https://upload.wikimedia.org/wikipedia/commons/thumb/c/c1/Ed_Sheeran-6886_%28cropped%29.jpg/500px-Ed_Sheeran-6886_%28cropped%29.jpg"),
            Artist("The Weeknd", 34, "https://upload.wikimedia.org/wikipedia/commons/thumb/a/a0/The_Weeknd_Cannes_2023.png/500px-The_Weeknd_Cannes_2023.png"),
            Artist("Bruno Mars", 30, "https://upload.wikimedia.org/wikipedia/commons/thumb/8/8e/Bruno_Mars_2017.jpg/500px-Bruno_Mars_2017.jpg")
        )
    }

    val filteredSongs by remember(searchQuery, selectedFilter, allSongs) {
        derivedStateOf {
            if (searchQuery.isBlank()) {
                emptyList()
            } else {
                allSongs.filter { song ->
                    val query = searchQuery.trim().lowercase()
                    when (selectedFilter) {
                        "Songs" -> song.title.lowercase().contains(query)
                        "Artists" -> song.artist.lowercase().contains(query)
                        "Albums" -> song.album.lowercase().contains(query)
                        else -> song.title.lowercase().contains(query) ||
                                song.artist.lowercase().contains(query) ||
                                song.album.lowercase().contains(query)
                    }
                }
            }
        }
    }

    val matchingArtists by remember(searchQuery, allSongs, onlineResults, popularArtists) {
        derivedStateOf {
            if (searchQuery.isBlank()) emptyList()
            else {
                val q = searchQuery.trim().lowercase()
                val popularMatches = popularArtists.filter { it.name.lowercase().contains(q) || q.contains(it.name.lowercase()) }
                val combined = (allSongs + onlineResults).distinctBy { it.id }
                val dynamicMatches = combined.groupBy { it.artist }
                    .filter { (artistName, _) -> artistName.lowercase().contains(q) || q.contains(artistName.lowercase()) }
                    .map { (artistName, songs) ->
                        val verifiedPhoto = popularArtists.firstOrNull { it.name.equals(artistName, ignoreCase = true) }?.artworkUri
                        Artist(
                            name = artistName,
                            songCount = songs.size,
                            artworkUri = verifiedPhoto ?: songs.firstOrNull { it.artworkUri.isNotBlank() }?.artworkUri ?: ""
                        )
                    }
                (popularMatches + dynamicMatches).distinctBy { it.name.lowercase() }
            }
        }
    }

    val recentDisplaySongs = remember(allSongs, recentSearches) {
        allSongs.take(8)
    }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(colors.mainBackground),
        contentPadding = PaddingValues(bottom = 120.dp)
    ) {
        // Search Input Bar
        item {
            OutlinedTextField(
                value = searchQuery,
                onValueChange = {
                    searchQuery = it
                    onSearch(it)
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .statusBarsPadding()
                    .padding(horizontal = 16.dp, vertical = 12.dp)
                    .testTag("search_input_field"),
                placeholder = {
                    Text(
                        text = "What do you want to listen to?",
                        color = colors.mutedText
                    )
                },
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Default.Search,
                        contentDescription = "Search",
                        tint = colors.secondaryText
                    )
                },
                trailingIcon = {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(end = 4.dp)
                    ) {
                        if (searchQuery.isNotEmpty()) {
                            IconButton(onClick = {
                                searchQuery = ""
                                voiceSearchResult = null
                            }) {
                                Icon(
                                    imageVector = Icons.Default.Clear,
                                    contentDescription = "Clear",
                                    tint = colors.mutedText
                                )
                            }
                        }
                        IconButton(
                            onClick = { showVoiceDialog = true },
                            modifier = Modifier.testTag("btn_voice_search")
                        ) {
                            Icon(
                                imageVector = Icons.Default.Mic,
                                contentDescription = "Voice Search",
                                tint = colors.primaryAccent
                            )
                        }
                    }
                },
                shape = RoundedCornerShape(24.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedContainerColor = colors.elevatedSurface,
                    unfocusedContainerColor = colors.surface,
                    focusedBorderColor = colors.primaryAccent,
                    unfocusedBorderColor = colors.divider,
                    focusedTextColor = colors.primaryText,
                    unfocusedTextColor = colors.primaryText
                ),
                singleLine = true
            )
        }
        // Recent Searches (Spotify Image 2 Style)
        if (searchQuery.isBlank() && (recentSearches.isNotEmpty() || allSongs.isNotEmpty())) {
            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Recents",
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Bold,
                            color = colors.primaryText
                        )
                    )
                    if (recentSearches.isNotEmpty()) {
                        TextButton(onClick = onClearRecentSearches) {
                            Text("Clear all", color = colors.secondaryText)
                        }
                    }
                }
            }

            items(recentDisplaySongs) { song ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onSongClick(song, recentDisplaySongs) }
                        .padding(horizontal = 16.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(52.dp)
                            .clip(RoundedCornerShape(6.dp))
                            .background(colors.surface)
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
                                fontWeight = FontWeight.SemiBold,
                                color = if (song.id == currentPlayingSong?.id) Color(0xFF1ED760) else colors.primaryText
                            ),
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Text(
                            text = "Song • ${song.artist}",
                            style = MaterialTheme.typography.bodySmall.copy(
                                color = colors.secondaryText
                            ),
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                    IconButton(onClick = { onFavoriteToggle(song) }) {
                        Icon(
                            imageVector = if (song.isFavorite) Icons.Default.Favorite else Icons.Default.PlaylistAdd,
                            contentDescription = "Save",
                            tint = if (song.isFavorite) Color(0xFF1ED760) else colors.mutedText,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
            }
        }

        // Category Filter Chips
        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 4.dp)
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                filterTabs.forEach { tab ->
                    val isSelected = tab == selectedFilter
                    FilterChip(
                        selected = isSelected,
                        onClick = { selectedFilter = tab },
                        label = { Text(tab) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = colors.primaryAccent,
                            selectedLabelColor = colors.mainBackground,
                            containerColor = colors.surface,
                            labelColor = colors.secondaryText
                        ),
                        border = FilterChipDefaults.filterChipBorder(
                            enabled = true,
                            selected = isSelected,
                            borderColor = colors.divider
                        ),
                        shape = RoundedCornerShape(20.dp)
                    )
                }
            }
        }

        // AI Voice Search Companion Result Card
        if (voiceSearchResult != null) {
            val result = voiceSearchResult!!
            item {
                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 6.dp)
                        .clip(RoundedCornerShape(20.dp))
                        .testTag("voice_search_ai_card"),
                    color = colors.elevatedSurface
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(
                                Brush.horizontalGradient(
                                    listOf(
                                        colors.primaryAccent.copy(alpha = 0.22f),
                                        Color(0xFF1DB954).copy(alpha = 0.12f)
                                    )
                                )
                            )
                            .padding(16.dp)
                    ) {
                        Column {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Surface(
                                        shape = RoundedCornerShape(10.dp),
                                        color = colors.primaryAccent.copy(alpha = 0.2f)
                                    ) {
                                        Row(
                                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.Mic,
                                                contentDescription = null,
                                                tint = colors.primaryAccent,
                                                modifier = Modifier.size(14.dp)
                                            )
                                            Spacer(modifier = Modifier.width(4.dp))
                                            Text(
                                                text = "SWARIX AI VOICE",
                                                style = MaterialTheme.typography.labelSmall.copy(
                                                    fontWeight = FontWeight.Bold,
                                                    color = colors.primaryAccent,
                                                    fontSize = 10.sp
                                                )
                                            )
                                        }
                                    }

                                    Spacer(modifier = Modifier.width(8.dp))

                                    Text(
                                        text = "\"${result.spokenQuery}\"",
                                        style = MaterialTheme.typography.bodyMedium.copy(
                                            fontWeight = FontWeight.Bold,
                                            color = colors.primaryText
                                        ),
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                }

                                IconButton(
                                    onClick = { voiceSearchResult = null },
                                    modifier = Modifier.size(28.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Clear,
                                        contentDescription = "Dismiss",
                                        tint = colors.mutedText,
                                        modifier = Modifier.size(16.dp)
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(10.dp))

                            Text(
                                text = result.aiResponseSummary,
                                style = MaterialTheme.typography.bodyMedium.copy(
                                    color = colors.primaryText,
                                    lineHeight = 20.sp
                                )
                            )

                            if (result.matchedSongs.isNotEmpty()) {
                                Spacer(modifier = Modifier.height(12.dp))
                                Row(
                                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Surface(
                                        shape = RoundedCornerShape(16.dp),
                                        color = colors.primaryAccent,
                                        modifier = Modifier.bouncyClick {
                                            onSongClick(result.matchedSongs.first(), result.matchedSongs)
                                        }
                                    ) {
                                        Row(
                                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.PlayArrow,
                                                contentDescription = null,
                                                tint = colors.mainBackground,
                                                modifier = Modifier.size(16.dp)
                                            )
                                            Spacer(modifier = Modifier.width(4.dp))
                                            Text(
                                                text = "Play All (${result.matchedSongs.size})",
                                                style = MaterialTheme.typography.labelSmall.copy(
                                                    fontWeight = FontWeight.Bold,
                                                    color = colors.mainBackground
                                                )
                                            )
                                        }
                                    }

                                    val playlistTitle = result.suggestedPlaylistTitle
                                    if (onSavePlaylist != null && playlistTitle != null) {
                                        Surface(
                                            shape = RoundedCornerShape(16.dp),
                                            color = colors.surface,
                                            modifier = Modifier.bouncyClick {
                                                onSavePlaylist(playlistTitle, result.matchedSongs)
                                            }
                                        ) {
                                            Row(
                                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                                                verticalAlignment = Alignment.CenterVertically
                                            ) {
                                                Text(
                                                    text = "Save Playlist",
                                                    style = MaterialTheme.typography.labelSmall.copy(
                                                        fontWeight = FontWeight.SemiBold,
                                                        color = colors.primaryText
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
            }
        }

        val displaySongs = if (voiceSearchResult != null && selectedFilter == "All" && voiceSearchResult!!.matchedSongs.isNotEmpty()) {
            voiceSearchResult!!.matchedSongs
        } else if (onlineResults.isNotEmpty()) {
            (onlineResults + filteredSongs).distinctBy { it.id }
        } else {
            filteredSongs
        }

        if (searchQuery.isNotBlank()) {
            if (isSearchingOnline) {
                item {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        CircularProgressIndicator(
                            color = colors.primaryAccent,
                            modifier = Modifier.size(24.dp),
                            strokeWidth = 2.dp
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Text(
                            text = "Searching online music library...",
                            style = MaterialTheme.typography.bodyMedium.copy(color = colors.secondaryText)
                        )
                    }
                }
            }

            // Matching Artists Section with Circular Photos & Complete Discography Playlist Card
            if (matchingArtists.isNotEmpty()) {
                val dominantArtist = matchingArtists.first()
                val artistDiscography = displaySongs.filter { it.artist.contains(dominantArtist.name, ignoreCase = true) }

                item {
                    Text(
                        text = "Artist Spotlight",
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Bold,
                            color = colors.primaryText
                        ),
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                    )
                }

                items(matchingArtists.take(1)) { artist ->
                    val verifiedArt = artist.artworkUri.ifBlank {
                        popularArtists.find { it.name.equals(artist.name, ignoreCase = true) }?.artworkUri.orEmpty()
                    }
                    Surface(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                if (onArtistProfileClick != null) {
                                    onArtistProfileClick(artist.name, verifiedArt)
                                } else {
                                    onArtistClick(artist.name)
                                }
                            }
                            .padding(horizontal = 16.dp, vertical = 6.dp),
                        color = Color.Transparent
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(64.dp)
                                    .clip(CircleShape)
                                    .background(colors.surface)
                                    .border(2.dp, Color(0xFF1ED760), CircleShape)
                            ) {
                                AsyncImage(
                                    model = verifiedArt.ifBlank { "https://upload.wikimedia.org/wikipedia/commons/thumb/b/b7/Arijit_Singh_performance_at_Chandigarh_2025.jpg/500px-Arijit_Singh_performance_at_Chandigarh_2025.jpg" },
                                    contentDescription = artist.name,
                                    contentScale = ContentScale.Crop,
                                    modifier = Modifier.fillMaxSize()
                                )
                            }
                            Spacer(modifier = Modifier.width(14.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = artist.name,
                                    style = MaterialTheme.typography.titleLarge.copy(
                                        fontWeight = FontWeight.Bold,
                                        color = colors.primaryText
                                    ),
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                                Text(
                                    text = "Verified Artist • ${if (artistDiscography.isNotEmpty()) artistDiscography.size else artist.songCount} Songs Catalog",
                                    style = MaterialTheme.typography.bodySmall.copy(
                                        color = Color(0xFF1ED760)
                                    )
                                )
                            }
                        }
                    }
                }

                // Official Complete Artist Discography Playlist Card
                if (artistDiscography.isNotEmpty()) {
                    item {
                        Surface(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp, vertical = 8.dp)
                                .clip(RoundedCornerShape(20.dp)),
                            color = Color(0xFF0C1F14),
                            border = androidx.compose.foundation.BorderStroke(1.5.dp, Color(0xFF1ED760).copy(alpha = 0.6f))
                        ) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(56.dp)
                                            .clip(RoundedCornerShape(12.dp))
                                            .background(Color(0xFF1ED760).copy(alpha = 0.25f)),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.PlayArrow,
                                            contentDescription = null,
                                            tint = Color(0xFF1ED760),
                                            modifier = Modifier.size(32.dp)
                                        )
                                    }
                                    Spacer(modifier = Modifier.width(14.dp))
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            text = "This Is ${dominantArtist.name}",
                                            style = MaterialTheme.typography.titleMedium.copy(
                                                fontWeight = FontWeight.Bold,
                                                color = Color.White
                                            ),
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis
                                        )
                                        Text(
                                            text = "Complete Discography Playlist (Hit to Classic • ${artistDiscography.size} Tracks)",
                                            style = MaterialTheme.typography.bodySmall.copy(
                                                color = Color(0xFFE5C07B)
                                            ),
                                            maxLines = 2,
                                            overflow = TextOverflow.Ellipsis
                                        )
                                    }
                                }
                                Spacer(modifier = Modifier.height(14.dp))
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                                ) {
                                    Surface(
                                        modifier = Modifier
                                            .weight(1f)
                                            .clip(RoundedCornerShape(20.dp))
                                            .clickable { onSongClick(artistDiscography.first(), artistDiscography) },
                                        color = Color(0xFF1ED760),
                                        shape = RoundedCornerShape(20.dp)
                                    ) {
                                        Row(
                                            modifier = Modifier.padding(vertical = 10.dp),
                                            horizontalArrangement = Arrangement.Center,
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.PlayArrow,
                                                contentDescription = null,
                                                tint = Color.Black,
                                                modifier = Modifier.size(18.dp)
                                            )
                                            Spacer(modifier = Modifier.width(6.dp))
                                            Text(
                                                text = "Play All Songs",
                                                style = MaterialTheme.typography.labelMedium.copy(
                                                    color = Color.Black,
                                                    fontWeight = FontWeight.Bold
                                                )
                                            )
                                        }
                                    }
                                    if (onSavePlaylist != null) {
                                        Surface(
                                            modifier = Modifier
                                                .weight(1f)
                                                .clip(RoundedCornerShape(20.dp))
                                                .clickable {
                                                    onSavePlaylist("This Is ${dominantArtist.name}", artistDiscography)
                                                },
                                            color = Color(0xFF132A1C),
                                            border = androidx.compose.foundation.BorderStroke(1.2.dp, Color(0xFF1ED760).copy(alpha = 0.8f)),
                                            shape = RoundedCornerShape(20.dp)
                                        ) {
                                            Row(
                                                modifier = Modifier.padding(vertical = 10.dp),
                                                horizontalArrangement = Arrangement.Center,
                                                verticalAlignment = Alignment.CenterVertically
                                            ) {
                                                Icon(
                                                    imageVector = Icons.Default.PlaylistAdd,
                                                    contentDescription = null,
                                                    tint = Color(0xFF1ED760),
                                                    modifier = Modifier.size(18.dp)
                                                )
                                                Spacer(modifier = Modifier.width(6.dp))
                                                Text(
                                                    text = "Save Playlist",
                                                    style = MaterialTheme.typography.labelMedium.copy(
                                                        color = Color.White,
                                                        fontWeight = FontWeight.SemiBold
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
            }

            item {
                Text(
                    text = "Songs (${displaySongs.size})",
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.Bold,
                        color = colors.primaryText
                    ),
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp)
                )
            }

            if (displaySongs.isEmpty()) {
                item {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(40.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "No songs found for \"$searchQuery\"",
                            style = MaterialTheme.typography.titleMedium.copy(color = colors.secondaryText),
                            textAlign = TextAlign.Center
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = "Check spelling or search by artist or album",
                            style = MaterialTheme.typography.bodyMedium.copy(color = colors.mutedText),
                            textAlign = TextAlign.Center
                        )
                    }
                }
            } else {
                items(displaySongs) { song ->
                    SongRowItem(
                        song = song,
                        isPlaying = song.id == currentPlayingSong?.id && isPlaying,
                        onSongClick = { onSongClick(song, displaySongs) },
                        onFavoriteToggle = { onFavoriteToggle(song) }
                    )
                }
            }
        } else {
            // Default Discovery Screen

            // Trending Now
            item {
                Spacer(modifier = Modifier.height(12.dp))
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.TrendingUp,
                            contentDescription = null,
                            tint = colors.primaryAccent,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "Trending Now",
                            style = MaterialTheme.typography.titleLarge.copy(
                                fontWeight = FontWeight.Bold,
                                color = colors.primaryText
                            )
                        )
                    }
                }
            }

            item {
                LazyRow(
                    contentPadding = PaddingValues(horizontal = 16.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(trendingPicks) { (title, img) ->
                        Surface(
                            modifier = Modifier
                                .width(120.dp)
                                .height(130.dp)
                                .clip(RoundedCornerShape(16.dp))
                                .clickable {
                                    allSongs.firstOrNull()?.let { onSongClick(it, allSongs) }
                                }
                                .testTag("trending_${title.lowercase()}"),
                            color = colors.elevatedSurface
                        ) {
                            Column {
                                AsyncImage(
                                    model = img,
                                    contentDescription = title,
                                    contentScale = ContentScale.Crop,
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(90.dp)
                                )
                                Text(
                                    text = title,
                                    style = MaterialTheme.typography.labelMedium.copy(
                                        fontWeight = FontWeight.SemiBold,
                                        color = colors.primaryText
                                    ),
                                    modifier = Modifier.padding(6.dp),
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                        }
                    }
                }
            }

            // Browse by Mood
            item {
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = "Browse by Mood",
                    style = MaterialTheme.typography.titleLarge.copy(
                        fontWeight = FontWeight.Bold,
                        color = colors.primaryText
                    ),
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                )
            }

            item {
                LazyRow(
                    contentPadding = PaddingValues(horizontal = 16.dp),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    items(moodCategories) { mood ->
                        Surface(
                            modifier = Modifier
                                .size(width = 80.dp, height = 75.dp)
                                .clip(RoundedCornerShape(14.dp))
                                .clickable {
                                    searchQuery = mood.name
                                }
                                .testTag("search_mood_${mood.name.lowercase()}"),
                            color = colors.elevatedSurface
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .padding(6.dp),
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.Center
                            ) {
                                Text(text = mood.emoji, fontSize = 24.sp)
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = mood.name,
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        fontWeight = FontWeight.SemiBold,
                                        color = colors.primaryText
                                    )
                                )
                            }
                        }
                    }
                }
            }

            // Popular Artists
            item {
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = "Popular Artists",
                    style = MaterialTheme.typography.titleLarge.copy(
                        fontWeight = FontWeight.Bold,
                        color = colors.primaryText
                    ),
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                )
            }

            item {
                LazyRow(
                    contentPadding = PaddingValues(horizontal = 16.dp),
                    horizontalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    items(popularArtists) { artist ->
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            modifier = Modifier
                                .width(80.dp)
                                .clickable {
                                    if (onArtistProfileClick != null) {
                                        onArtistProfileClick(artist.name, artist.artworkUri)
                                    } else {
                                        onArtistClick(artist.name)
                                        searchQuery = artist.name
                                    }
                                }
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(72.dp)
                                    .clip(CircleShape)
                                    .background(colors.elevatedSurface)
                            ) {
                                AsyncImage(
                                    model = artist.artworkUri,
                                    contentDescription = artist.name,
                                    contentScale = ContentScale.Crop,
                                    modifier = Modifier.fillMaxSize()
                                )
                            }
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = artist.name,
                                style = MaterialTheme.typography.labelMedium.copy(
                                    fontWeight = FontWeight.Medium,
                                    color = colors.primaryText
                                ),
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }
                }
            }

            // Quick Picks
            item {
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = "Quick Picks",
                    style = MaterialTheme.typography.titleLarge.copy(
                        fontWeight = FontWeight.Bold,
                        color = colors.primaryText
                    ),
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                )
            }

            items(allSongs.take(5)) { song ->
                SongRowItem(
                    song = song,
                    isPlaying = song.id == currentPlayingSong?.id && isPlaying,
                    onSongClick = { onSongClick(song, allSongs) },
                    onFavoriteToggle = { onFavoriteToggle(song) }
                )
            }
        }
    }

    if (showVoiceDialog) {
        VoiceSearchDialog(
            onDismiss = { showVoiceDialog = false },
            onVoiceResult = { query ->
                searchQuery = query
                val result = VoiceSearchEngine.processVoiceQuery(query, allSongs, playlists)
                voiceSearchResult = result
                showVoiceDialog = false
                if (result.autoPlayCandidate != null) {
                    onSongClick(result.autoPlayCandidate, result.matchedSongs)
                }
            }
        )
    }
}
