package com.example.ui.screens.artist

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Shuffle
import androidx.compose.material.icons.filled.Verified
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import com.example.core.model.Artist
import com.example.core.model.Song
import com.example.ui.components.SongRowItem
import com.example.ui.components.bouncyClick
import com.example.ui.theme.LocalSwarixColors

@Composable
fun ArtistProfileScreen(
    artistName: String,
    artistArtworkUri: String,
    artistSongs: List<Song>,
    onSongClick: (Song) -> Unit,
    onFavoriteToggle: (Song) -> Unit,
    onPlayAll: (List<Song>) -> Unit = {},
    onShuffleAll: (List<Song>) -> Unit = {},
    onRelatedArtistClick: (String, String) -> Unit = { _, _ -> },
    onBackClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val colors = LocalSwarixColors.current
    val listState = rememberLazyListState()
    var selectedTab by remember { mutableStateOf("Popular") }
    val artistViewModel: ArtistViewModel = viewModel()
    val biographyState by artistViewModel.biographyState.collectAsStateWithLifecycle()

    val fallbackArtists = remember {
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

    val relatedArtists = remember(artistName) {
        fallbackArtists.filter { !it.name.equals(artistName, ignoreCase = true) }
    }

    // Group songs into albums
    val albums = remember(artistSongs) {
        artistSongs
            .filter { it.album.isNotBlank() }
            .groupBy { it.album }
            .map { (albumTitle, tracks) ->
                Triple(albumTitle, tracks.first().artworkUri, tracks)
            }
    }

    val singlesAndEPs = remember(artistSongs) {
        artistSongs.filter { it.album.isBlank() || it.album.contains("Single", ignoreCase = true) || it.album.contains("Ep", ignoreCase = true) }
    }

    LaunchedEffect(selectedTab) {
        if (selectedTab == "Biography" && biographyState is BiographyUiState.Idle) {
            artistViewModel.fetchBiography(artistName)
        }
    }

    val scrollOffset = remember { derivedStateOf { listState.firstVisibleItemScrollOffset } }
    val headerAlpha by animateFloatAsState(targetValue = (1f - (scrollOffset.value / 400f)).coerceIn(0f, 1f), label = "header_alpha")

    Box(modifier = modifier.fillMaxSize().background(colors.mainBackground)) {
        LazyColumn(
            state = listState,
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(bottom = 120.dp)
        ) {
            item {
                // Collapsible Header
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(340.dp)
                        .graphicsLayer { alpha = headerAlpha }
                ) {
                    AsyncImage(
                        model = artistArtworkUri.ifBlank {
                            fallbackArtists.find { it.name.equals(artistName, ignoreCase = true) }?.artworkUri.orEmpty()
                        },
                        contentDescription = artistName,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize()
                    )
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(Brush.verticalGradient(listOf(Color.Transparent, Color.Black.copy(alpha = 0.5f), colors.mainBackground)))
                    )
                    Column(
                        modifier = Modifier
                            .align(Alignment.BottomStart)
                            .padding(horizontal = 20.dp, vertical = 16.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Verified,
                                contentDescription = "Verified Artist",
                                tint = Color(0xFF1ED760),
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "Verified Artist",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    color = Color(0xFF1ED760),
                                    fontWeight = FontWeight.SemiBold
                                )
                            )
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = artistName,
                            style = MaterialTheme.typography.headlineLarge.copy(
                                fontWeight = FontWeight.ExtraBold,
                                color = Color.White
                            ),
                            maxLines = 2,
                            overflow = TextOverflow.Ellipsis
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = "${artistSongs.size} verified catalog songs",
                            style = MaterialTheme.typography.bodyMedium.copy(color = Color(0xFFE5C07B))
                        )
                    }
                }
            }

            // Play All & Shuffle Buttons
            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp, vertical = 12.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Play Button
                    Surface(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(24.dp))
                            .bouncyClick {
                                if (artistSongs.isNotEmpty()) {
                                    onPlayAll(artistSongs)
                                }
                            },
                        color = Color(0xFF1ED760),
                        shape = RoundedCornerShape(24.dp)
                    ) {
                        Row(
                            modifier = Modifier.padding(vertical = 12.dp),
                            horizontalArrangement = Arrangement.Center,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.PlayArrow,
                                contentDescription = null,
                                tint = Color.Black,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Play",
                                style = MaterialTheme.typography.titleMedium.copy(
                                    color = Color.Black,
                                    fontWeight = FontWeight.Bold
                                )
                            )
                        }
                    }

                    // Shuffle Button
                    Surface(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(24.dp))
                            .bouncyClick {
                                if (artistSongs.isNotEmpty()) {
                                    onShuffleAll(artistSongs)
                                }
                            },
                        color = colors.elevatedSurface,
                        border = androidx.compose.foundation.BorderStroke(1.2.dp, colors.divider),
                        shape = RoundedCornerShape(24.dp)
                    ) {
                        Row(
                            modifier = Modifier.padding(vertical = 12.dp),
                            horizontalArrangement = Arrangement.Center,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.Shuffle,
                                contentDescription = null,
                                tint = colors.primaryText,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Shuffle",
                                style = MaterialTheme.typography.titleMedium.copy(
                                    color = colors.primaryText,
                                    fontWeight = FontWeight.SemiBold
                                )
                            )
                        }
                    }
                }
            }

            // Tab Selection: Popular / Discography / Albums / Bio
            item {
                TabRow(
                    selectedTabIndex = when (selectedTab) {
                        "Popular" -> 0
                        "Albums" -> 1
                        "Discography" -> 2
                        else -> 3
                    },
                    containerColor = colors.mainBackground,
                    contentColor = colors.primaryAccent
                ) {
                    Tab(
                        selected = selectedTab == "Popular",
                        onClick = { selectedTab = "Popular" },
                        text = { Text("Popular", fontWeight = if (selectedTab == "Popular") FontWeight.Bold else FontWeight.Normal) }
                    )
                    Tab(
                        selected = selectedTab == "Albums",
                        onClick = { selectedTab = "Albums" },
                        text = { Text("Albums", fontWeight = if (selectedTab == "Albums") FontWeight.Bold else FontWeight.Normal) }
                    )
                    Tab(
                        selected = selectedTab == "Discography",
                        onClick = { selectedTab = "Discography" },
                        text = { Text("All Songs", fontWeight = if (selectedTab == "Discography") FontWeight.Bold else FontWeight.Normal) }
                    )
                    Tab(
                        selected = selectedTab == "Biography",
                        onClick = { selectedTab = "Biography" },
                        text = { Text("About", fontWeight = if (selectedTab == "Biography") FontWeight.Bold else FontWeight.Normal) }
                    )
                }
                Spacer(modifier = Modifier.height(10.dp))
            }

            when (selectedTab) {
                "Popular" -> {
                    item {
                        Text(
                            text = "Popular Songs",
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.Bold,
                                color = colors.primaryText
                            ),
                            modifier = Modifier.padding(horizontal = 20.dp, vertical = 8.dp)
                        )
                    }

                    items(artistSongs.take(8)) { song ->
                        SongRowItem(
                            song = song,
                            isPlaying = false,
                            onSongClick = { onSongClick(song) },
                            onFavoriteToggle = { onFavoriteToggle(song) }
                        )
                    }

                    // Albums Row Section
                    if (albums.isNotEmpty()) {
                        item {
                            Spacer(modifier = Modifier.height(16.dp))
                            Text(
                                text = "Albums & Compilations",
                                style = MaterialTheme.typography.titleMedium.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = colors.primaryText
                                ),
                                modifier = Modifier.padding(horizontal = 20.dp, vertical = 8.dp)
                            )
                        }

                        item {
                            LazyRow(
                                contentPadding = PaddingValues(horizontal = 20.dp),
                                horizontalArrangement = Arrangement.spacedBy(14.dp)
                            ) {
                                items(albums) { (albumTitle, artwork, tracks) ->
                                    Surface(
                                        modifier = Modifier
                                            .width(140.dp)
                                            .clip(RoundedCornerShape(12.dp))
                                            .bouncyClick {
                                                if (tracks.isNotEmpty()) {
                                                    onSongClick(tracks.first())
                                                }
                                            },
                                        color = colors.elevatedSurface
                                    ) {
                                        Column(modifier = Modifier.padding(10.dp)) {
                                            Box(
                                                modifier = Modifier
                                                    .size(120.dp)
                                                    .clip(RoundedCornerShape(8.dp))
                                                    .background(colors.surface)
                                            ) {
                                                AsyncImage(
                                                    model = artwork,
                                                    contentDescription = albumTitle,
                                                    contentScale = ContentScale.Crop,
                                                    modifier = Modifier.fillMaxSize()
                                                )
                                            }
                                            Spacer(modifier = Modifier.height(8.dp))
                                            Text(
                                                text = albumTitle,
                                                style = MaterialTheme.typography.bodyMedium.copy(
                                                    fontWeight = FontWeight.SemiBold,
                                                    color = colors.primaryText
                                                ),
                                                maxLines = 1,
                                                overflow = TextOverflow.Ellipsis
                                            )
                                            Text(
                                                text = "${tracks.size} tracks",
                                                style = MaterialTheme.typography.bodySmall.copy(
                                                    color = colors.secondaryText,
                                                    fontSize = 11.sp
                                                )
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }

                    // Singles & EPs
                    if (singlesAndEPs.isNotEmpty()) {
                        item {
                            Spacer(modifier = Modifier.height(16.dp))
                            Text(
                                text = "Singles & Collaborations",
                                style = MaterialTheme.typography.titleMedium.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = colors.primaryText
                                ),
                                modifier = Modifier.padding(horizontal = 20.dp, vertical = 8.dp)
                            )
                        }

                        items(singlesAndEPs.take(5)) { song ->
                            SongRowItem(
                                song = song,
                                isPlaying = false,
                                onSongClick = { onSongClick(song) },
                                onFavoriteToggle = { onFavoriteToggle(song) }
                            )
                        }
                    }

                    // Related Artists
                    item {
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = "Fans Also Like",
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.Bold,
                                color = colors.primaryText
                            ),
                            modifier = Modifier.padding(horizontal = 20.dp, vertical = 8.dp)
                        )
                    }

                    item {
                        LazyRow(
                            contentPadding = PaddingValues(horizontal = 20.dp),
                            horizontalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            items(relatedArtists) { relArtist ->
                                Column(
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                    modifier = Modifier
                                        .width(86.dp)
                                        .bouncyClick {
                                            onRelatedArtistClick(relArtist.name, relArtist.artworkUri)
                                        }
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(76.dp)
                                            .clip(CircleShape)
                                            .background(colors.elevatedSurface)
                                            .border(1.5.dp, colors.divider, CircleShape)
                                    ) {
                                        AsyncImage(
                                            model = relArtist.artworkUri,
                                            contentDescription = relArtist.name,
                                            contentScale = ContentScale.Crop,
                                            modifier = Modifier.fillMaxSize()
                                        )
                                    }
                                    Spacer(modifier = Modifier.height(6.dp))
                                    Text(
                                        text = relArtist.name,
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
                }

                "Albums" -> {
                    if (albums.isEmpty()) {
                        item {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(40.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = "No standalone albums found for this artist.",
                                    style = MaterialTheme.typography.bodyMedium.copy(color = colors.secondaryText)
                                )
                            }
                        }
                    } else {
                        items(albums) { (albumTitle, artwork, tracks) ->
                            Surface(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 20.dp, vertical = 6.dp)
                                    .clip(RoundedCornerShape(14.dp))
                                    .bouncyClick {
                                        if (tracks.isNotEmpty()) {
                                            onSongClick(tracks.first())
                                        }
                                    },
                                color = colors.elevatedSurface
                            ) {
                                Row(
                                    modifier = Modifier.padding(12.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(60.dp)
                                            .clip(RoundedCornerShape(10.dp))
                                    ) {
                                        AsyncImage(
                                            model = artwork,
                                            contentDescription = albumTitle,
                                            contentScale = ContentScale.Crop,
                                            modifier = Modifier.fillMaxSize()
                                        )
                                    }
                                    Spacer(modifier = Modifier.width(14.dp))
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            text = albumTitle,
                                            style = MaterialTheme.typography.titleMedium.copy(
                                                fontWeight = FontWeight.Bold,
                                                color = colors.primaryText
                                            ),
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis
                                        )
                                        Text(
                                            text = "${tracks.size} songs • Official Release",
                                            style = MaterialTheme.typography.bodySmall.copy(color = colors.secondaryText)
                                        )
                                    }
                                    IconButton(onClick = { if (tracks.isNotEmpty()) onPlayAll(tracks) }) {
                                        Icon(
                                            imageVector = Icons.Default.PlayArrow,
                                            contentDescription = "Play Album",
                                            tint = Color(0xFF1ED760)
                                        )
                                    }
                                }
                            }
                        }
                    }
                }

                "Discography" -> {
                    items(artistSongs) { song ->
                        SongRowItem(
                            song = song,
                            isPlaying = false,
                            onSongClick = { onSongClick(song) },
                            onFavoriteToggle = { onFavoriteToggle(song) }
                        )
                    }
                }

                "Biography" -> {
                    item {
                        when (val state = biographyState) {
                            is BiographyUiState.Idle, is BiographyUiState.Loading -> {
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(32.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    CircularProgressIndicator(color = colors.primaryAccent)
                                }
                            }
                            is BiographyUiState.Success -> {
                                Text(
                                    text = state.biography,
                                    style = MaterialTheme.typography.bodyMedium.copy(
                                        color = colors.primaryText,
                                        lineHeight = 22.sp
                                    ),
                                    modifier = Modifier.padding(20.dp)
                                )
                            }
                            is BiographyUiState.Error -> {
                                Text(
                                    text = state.message,
                                    style = MaterialTheme.typography.bodyMedium.copy(color = MaterialTheme.colorScheme.error),
                                    modifier = Modifier.padding(20.dp)
                                )
                            }
                        }
                    }
                }
            }
        }

        // Floating Back Button
        IconButton(
            onClick = onBackClick,
            modifier = Modifier
                .padding(top = 40.dp, start = 16.dp)
                .clip(CircleShape)
                .background(Color.Black.copy(alpha = 0.5f))
                .testTag("artist_profile_back")
        ) {
            Icon(
                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                contentDescription = "Back",
                tint = Color.White
            )
        }
    }
}
