package com.example.ui.screens.library

import androidx.compose.foundation.background
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ElevatedButton
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
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
import com.example.core.model.Playlist
import com.example.core.model.Song
import com.example.ui.components.SongRowItem
import com.example.ui.theme.AccentPurple
import com.example.ui.theme.LocalSwarixColors

@Composable
fun LibraryScreen(
    allSongs: List<Song>,
    favoriteSongs: List<Song>,
    cachedSongIds: Set<String>,
    onCacheToggle: (Song) -> Unit,
    playlists: List<Playlist>,
    currentPlayingSong: Song?,
    isPlaying: Boolean,
    onSongClick: (Song) -> Unit,
    onPlaylistClick: (Playlist) -> Unit,
    onLikedSongsClick: () -> Unit,
    onCreatePlaylist: (name: String, description: String) -> Unit,
    onRescanLibrary: () -> Unit,
    onFavoriteToggle: (Song) -> Unit,
    recentSearches: List<String> = emptyList(),
    onSearchQueryClick: ((String) -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    val colors = LocalSwarixColors.current
    var selectedTab by remember { mutableStateOf("Playlists") }
    val tabs = listOf("Playlists", "Smart Folders", "Songs", "Artists", "Albums")

    val smartFolders = remember(allSongs) {
        allSongs.groupBy { song ->
            val genre = if (song.genre.isNotBlank() && song.genre != "Unknown") song.genre else "Indie Acoustic"
            val mood = if (song.mood.isNotBlank() && song.mood != "Unknown") song.mood else "Chill"
            "$genre ($mood)"
        }.map { (folderName, songs) ->
            Triple(folderName, songs, songs.firstOrNull()?.artworkUri ?: "")
        }.sortedByDescending { it.second.size }
    }

    val artists = remember(allSongs) {
        allSongs.groupBy { it.artist }.map { (name, songs) ->
            Artist(name, songs.size, songs.firstOrNull()?.artworkUri ?: "")
        }
    }

    val albums = remember(allSongs) {
        allSongs.groupBy { it.album.ifEmpty { "Single" } }
            .map { (name, songs) ->
                Triple(name, songs.firstOrNull()?.artist ?: "", songs.firstOrNull()?.artworkUri ?: "")
            }
    }

    var showCreateDialog by remember { mutableStateOf(false) }
    var newPlaylistName by remember { mutableStateOf("") }
    var newPlaylistDesc by remember { mutableStateOf("") }

    var selectedSmartTag by remember { mutableStateOf<String?>(null) }
    val smartTags = remember(allSongs) {
        listOf("All Tags") + allSongs.mapNotNull { if (it.genre.isNotBlank() && it.genre != "Unknown") "${it.genre} • ${it.mood}" else null }.distinct().take(8)
    }
    val displayedSongs = remember(allSongs, selectedSmartTag) {
        if (selectedSmartTag == null) allSongs
        else allSongs.filter { "${it.genre} • ${it.mood}" == selectedSmartTag }
    }

    if (showCreateDialog) {
        AlertDialog(
            onDismissRequest = { showCreateDialog = false },
            title = {
                Text(
                    text = "Create Playlist",
                    style = MaterialTheme.typography.titleLarge.copy(color = colors.primaryText)
                )
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    OutlinedTextField(
                        value = newPlaylistName,
                        onValueChange = { newPlaylistName = it },
                        label = { Text("Playlist Name") },
                        singleLine = true,
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("playlist_name_input")
                    )
                    OutlinedTextField(
                        value = newPlaylistDesc,
                        onValueChange = { newPlaylistDesc = it },
                        label = { Text("Description (Optional)") },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (newPlaylistName.isNotBlank()) {
                            onCreatePlaylist(newPlaylistName.trim(), newPlaylistDesc.trim())
                            newPlaylistName = ""
                            newPlaylistDesc = ""
                            showCreateDialog = false
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = colors.primaryAccent),
                    modifier = Modifier.testTag("confirm_create_playlist")
                ) {
                    Text("Create", color = colors.mainBackground)
                }
            },
            dismissButton = {
                TextButton(onClick = { showCreateDialog = false }) {
                    Text("Cancel", color = colors.secondaryText)
                }
            },
            containerColor = colors.elevatedSurface
        )
    }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(colors.mainBackground),
        contentPadding = PaddingValues(bottom = 120.dp)
    ) {
        // Title & Rescan
        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Your Library",
                    style = MaterialTheme.typography.displayMedium.copy(
                        fontWeight = FontWeight.Bold,
                        color = colors.primaryText,
                        fontSize = 26.sp
                    )
                )

                IconButton(
                    onClick = onRescanLibrary,
                    modifier = Modifier.testTag("btn_rescan_library")
                ) {
                    Icon(
                        imageVector = Icons.Default.Refresh,
                        contentDescription = "Rescan",
                        tint = colors.primaryAccent
                    )
                }
                
                // Add File Import Button
                IconButton(
                    onClick = { /* TODO: Trigger File Picker */ },
                    modifier = Modifier.testTag("btn_import_file")
                ) {
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = "Import",
                        tint = colors.primaryAccent
                    )
                }
            }
        }

        // Recent Searches & Trends
        if (recentSearches.isNotEmpty()) {
            item {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = "RECENT SEARCHES & TRENDS",
                        style = MaterialTheme.typography.labelSmall.copy(
                            color = colors.primaryAccent,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 0.5.sp
                        )
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        recentSearches.forEach { query ->
                            Surface(
                                shape = RoundedCornerShape(16.dp),
                                color = colors.elevatedSurface,
                                modifier = Modifier
                                    .clickable { onSearchQueryClick?.invoke(query) }
                                    .testTag("recent_search_chip_$query")
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.History,
                                        contentDescription = null,
                                        tint = colors.secondaryText,
                                        modifier = Modifier.size(12.dp)
                                    )
                                    Text(
                                        text = query,
                                        style = MaterialTheme.typography.labelSmall.copy(
                                            color = colors.primaryText,
                                            fontSize = 11.sp
                                        )
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        // Tabs: Playlists, Songs, Artists, Albums
        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 4.dp)
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                tabs.forEach { tab ->
                    val isSelected = tab == selectedTab
                    FilterChip(
                        selected = isSelected,
                        onClick = { selectedTab = tab },
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

        when (selectedTab) {
            "Playlists" -> {
                // + Create Playlist Button
                item {
                    Spacer(modifier = Modifier.height(10.dp))
                    Surface(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 6.dp)
                            .clip(RoundedCornerShape(16.dp))
                            .clickable { showCreateDialog = true }
                            .testTag("btn_create_playlist"),
                        color = colors.elevatedSurface
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp, vertical = 14.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(44.dp)
                                    .clip(CircleShape)
                                    .background(colors.primaryAccent.copy(alpha = 0.2f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Add,
                                    contentDescription = "Create",
                                    tint = colors.primaryAccent
                                )
                            }
                            Spacer(modifier = Modifier.width(14.dp))
                            Text(
                                text = "Create Playlist",
                                style = MaterialTheme.typography.titleMedium.copy(
                                    fontWeight = FontWeight.SemiBold,
                                    color = colors.primaryText
                                )
                            )
                        }
                    }
                }

                // Liked Songs Card (Purple Gradient Icon as in Image 1 Screen 10)
                item {
                    Surface(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 6.dp)
                            .clip(RoundedCornerShape(16.dp))
                            .clickable(onClick = onLikedSongsClick)
                            .testTag("liked_songs_card"),
                        color = colors.surface
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(56.dp)
                                    .clip(RoundedCornerShape(14.dp))
                                    .background(
                                        Brush.linearGradient(
                                            listOf(AccentPurple, Color(0xFF5A4488))
                                        )
                                    ),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Favorite,
                                    contentDescription = "Liked",
                                    tint = Color.White,
                                    modifier = Modifier.size(28.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(14.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = "Liked Songs",
                                    style = MaterialTheme.typography.titleMedium.copy(
                                        fontWeight = FontWeight.Bold,
                                        color = colors.primaryText
                                    )
                                )
                                Text(
                                    text = "${favoriteSongs.size} songs",
                                    style = MaterialTheme.typography.bodyMedium.copy(
                                        color = colors.secondaryText
                                    )
                                )
                            }
                            IconButton(onClick = {}) {
                                Icon(
                                    imageVector = Icons.Default.MoreVert,
                                    contentDescription = "More",
                                    tint = colors.mutedText
                                )
                            }
                        }
                    }
                }

                // Custom Playlists List
                items(playlists) { playlist ->
                    Surface(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 4.dp)
                            .clip(RoundedCornerShape(14.dp))
                            .clickable { onPlaylistClick(playlist) }
                            .testTag("playlist_item_${playlist.id}"),
                        color = colors.surface
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(10.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(52.dp)
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(colors.elevatedSurface)
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
                                            fontWeight = FontWeight.Bold,
                                            color = colors.mainBackground,
                                            fontSize = 20.sp
                                        )
                                    }
                                }
                            }
                            Spacer(modifier = Modifier.width(14.dp))
                            Column(modifier = Modifier.weight(1f)) {
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
                                    text = playlist.description.ifEmpty { "Playlist" },
                                    style = MaterialTheme.typography.bodyMedium.copy(
                                        color = colors.secondaryText
                                    ),
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                            IconButton(onClick = {}) {
                                Icon(
                                    imageVector = Icons.Default.MoreVert,
                                    contentDescription = "Options",
                                    tint = colors.mutedText
                                )
                            }
                        }
                    }
                }
            }

            "Smart Folders" -> {
                item {
                    Text(
                        text = "AI Smart Folders (${smartFolders.size})",
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Bold,
                            color = colors.primaryText
                        ),
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                    )
                }

                items(smartFolders) { (folderName, songsInFolder, artwork) ->
                    Surface(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 6.dp)
                            .clip(RoundedCornerShape(16.dp))
                            .clickable {
                                if (songsInFolder.isNotEmpty()) {
                                    onSongClick(songsInFolder.first())
                                }
                            }
                            .testTag("smart_folder_item_$folderName"),
                        color = colors.elevatedSurface
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(48.dp)
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(colors.primaryAccent.copy(alpha = 0.2f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Folder,
                                    contentDescription = "Folder",
                                    tint = colors.primaryAccent,
                                    modifier = Modifier.size(24.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(14.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = folderName,
                                    style = MaterialTheme.typography.titleMedium.copy(
                                        fontWeight = FontWeight.SemiBold,
                                        color = colors.primaryText
                                    ),
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                                Text(
                                    text = "Auto-grouped • ${songsInFolder.size} tracks",
                                    style = MaterialTheme.typography.bodySmall.copy(
                                        color = colors.secondaryText
                                    )
                                )
                            }
                        }
                    }
                }
            }

            "Songs" -> {
                item {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 8.dp)
                    ) {
                        Text(
                            text = "All Tracks (${displayedSongs.size})",
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.Bold,
                                color = colors.primaryText
                            )
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        if (smartTags.size > 1) {
                            Text(
                                text = "AI SMART TAGS & SUB-GENRES",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    color = colors.primaryAccent,
                                    fontWeight = FontWeight.Bold,
                                    letterSpacing = 0.5.sp
                                )
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .horizontalScroll(rememberScrollState()),
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                smartTags.forEach { tag ->
                                    val isSelected = selectedSmartTag == tag || (tag == "All Tags" && selectedSmartTag == null)
                                    FilterChip(
                                        selected = isSelected,
                                        onClick = { selectedSmartTag = if (tag == "All Tags") null else tag },
                                        label = { Text(tag, fontSize = 11.sp) },
                                        colors = FilterChipDefaults.filterChipColors(
                                            selectedContainerColor = colors.primaryAccent.copy(alpha = 0.25f),
                                            selectedLabelColor = colors.primaryAccent
                                        )
                                    )
                                }
                            }
                        }
                    }
                }

                items(displayedSongs) { song ->
                    SongRowItem(
                        song = song,
                        isPlaying = song.id == currentPlayingSong?.id && isPlaying,
                        onSongClick = { onSongClick(song) },
                        onFavoriteToggle = { onFavoriteToggle(song) },
                        isCached = cachedSongIds.contains(song.id),
                        onCacheClick = { onCacheToggle(song) },
                        isMetadataCached = song.genre.isNotBlank() && song.mood.isNotBlank(),
                        isLyricsCached = true,
                        isMoodCached = true
                    )
                }
            }

            "Artists" -> {
                items(artists) { artist ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {}
                            .padding(horizontal = 16.dp, vertical = 10.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(50.dp)
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
                        Spacer(modifier = Modifier.width(14.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = artist.name,
                                style = MaterialTheme.typography.titleMedium.copy(
                                    fontWeight = FontWeight.SemiBold,
                                    color = colors.primaryText
                                )
                            )
                            Text(
                                text = "${artist.songCount} songs",
                                style = MaterialTheme.typography.bodyMedium.copy(
                                    color = colors.secondaryText
                                )
                            )
                        }
                    }
                }
            }

            "Albums" -> {
                items(albums) { (albumName, artistName, art) ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {}
                            .padding(horizontal = 16.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(50.dp)
                                .clip(RoundedCornerShape(10.dp))
                                .background(colors.elevatedSurface)
                        ) {
                            AsyncImage(
                                model = art,
                                contentDescription = albumName,
                                contentScale = ContentScale.Crop,
                                modifier = Modifier.fillMaxSize()
                            )
                        }
                        Spacer(modifier = Modifier.width(14.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = albumName,
                                style = MaterialTheme.typography.titleMedium.copy(
                                    fontWeight = FontWeight.SemiBold,
                                    color = colors.primaryText
                                )
                            )
                            Text(
                                text = artistName,
                                style = MaterialTheme.typography.bodyMedium.copy(
                                    color = colors.secondaryText
                                )
                            )
                        }
                    }
                }
            }
        }
    }
}
