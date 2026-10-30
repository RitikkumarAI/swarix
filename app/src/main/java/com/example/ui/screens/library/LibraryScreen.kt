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
import androidx.compose.material.icons.filled.GridView
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.SwapVert
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
        // Spotify Signature Library Header (Matching Image 3)
        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    // Profile Avatar (Ritik) with Online Dot
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(CircleShape)
                            .background(colors.elevatedSurface)
                            .border(1.dp, colors.primaryAccent.copy(alpha = 0.5f), CircleShape)
                    ) {
                        AsyncImage(
                            model = "https://images.unsplash.com/photo-1534528741775-53994a69daeb?w=200&auto=format&fit=crop&q=80",
                            contentDescription = "Profile",
                            contentScale = ContentScale.Crop,
                            modifier = Modifier.fillMaxSize()
                        )
                    }

                    Text(
                        text = "Your Library",
                        style = MaterialTheme.typography.titleLarge.copy(
                            fontWeight = FontWeight.Bold,
                            color = colors.primaryText,
                            fontSize = 24.sp
                        )
                    )
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(
                        onClick = { onSearchQueryClick?.invoke("") },
                        modifier = Modifier.testTag("btn_library_search")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Search,
                            contentDescription = "Search Library",
                            tint = colors.primaryText
                        )
                    }
                    IconButton(
                        onClick = { showCreateDialog = true },
                        modifier = Modifier.testTag("btn_library_add")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Add,
                            contentDescription = "Add Playlist",
                            tint = colors.primaryText
                        )
                    }
                }
            }
        }

        // Tabs: Playlists, Artists, Albums
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
                            selectedContainerColor = Color(0xFF1ED760),
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

        // Spotify Sort Row: ⇅ Recents
        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 10.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.SwapVert,
                        contentDescription = "Sort",
                        tint = colors.secondaryText,
                        modifier = Modifier.size(18.dp)
                    )
                    Text(
                        text = "Recents",
                        style = MaterialTheme.typography.labelMedium.copy(
                            fontWeight = FontWeight.SemiBold,
                            color = colors.primaryText
                        )
                    )
                }

                Icon(
                    imageVector = Icons.Default.GridView,
                    contentDescription = "View grid",
                    tint = colors.secondaryText,
                    modifier = Modifier.size(18.dp)
                )
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

                // Liked Songs Card (Spotify Purple Gradient matching Image 3)
                item {
                    Surface(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 4.dp)
                            .clickable(onClick = onLikedSongsClick)
                            .testTag("liked_songs_card"),
                        color = Color.Transparent
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(64.dp)
                                    .clip(RoundedCornerShape(6.dp))
                                    .background(
                                        Brush.linearGradient(
                                            listOf(
                                                Color(0xFF450AF5),
                                                Color(0xFF8E8EE5),
                                                Color(0xFFB4C8EC)
                                            )
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
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(
                                    text = "📌 Playlist • ${favoriteSongs.size} songs",
                                    style = MaterialTheme.typography.bodySmall.copy(
                                        color = colors.secondaryText
                                    )
                                )
                            }
                        }
                    }
                }

                // Custom Playlists List (Spotify Image 3 Style)
                items(playlists) { playlist ->
                    Surface(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 4.dp)
                            .clickable { onPlaylistClick(playlist) }
                            .testTag("playlist_item_${playlist.id}"),
                        color = Color.Transparent
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(64.dp)
                                    .clip(RoundedCornerShape(6.dp))
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
                                            .background(colors.elevatedSurface),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.MusicNote,
                                            contentDescription = null,
                                            tint = colors.primaryAccent
                                        )
                                    }
                                }
                            }
                            Spacer(modifier = Modifier.width(14.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = playlist.name,
                                    style = MaterialTheme.typography.titleMedium.copy(
                                        fontWeight = FontWeight.Bold,
                                        color = colors.primaryText
                                    ),
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(
                                    text = "Playlist • Ritik",
                                    style = MaterialTheme.typography.bodySmall.copy(
                                        color = colors.secondaryText
                                    )
                                )
                            }
                        }
                    }
                }

                // Followed Artists in Main Library (Image 3)
                items(artists.take(6)) { artist ->
                    Surface(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 4.dp)
                            .clickable { },
                        color = Color.Transparent
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(64.dp)
                                    .clip(CircleShape)
                                    .background(colors.surface)
                            ) {
                                AsyncImage(
                                    model = artist.artworkUri.ifBlank { "https://images.unsplash.com/photo-1507003211169-0a1dd7228f2d?w=300&auto=format&fit=crop&q=80" },
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
                                        fontWeight = FontWeight.Bold,
                                        color = colors.primaryText
                                    ),
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(
                                    text = "Artist",
                                    style = MaterialTheme.typography.bodySmall.copy(
                                        color = colors.secondaryText
                                    )
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
                            .padding(horizontal = 16.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(64.dp)
                                .clip(CircleShape)
                                .background(colors.surface)
                        ) {
                            AsyncImage(
                                model = artist.artworkUri.ifBlank { "https://images.unsplash.com/photo-1507003211169-0a1dd7228f2d?w=300&auto=format&fit=crop&q=80" },
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
                                    fontWeight = FontWeight.Bold,
                                    color = colors.primaryText
                                ),
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = "Artist",
                                style = MaterialTheme.typography.bodySmall.copy(
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
