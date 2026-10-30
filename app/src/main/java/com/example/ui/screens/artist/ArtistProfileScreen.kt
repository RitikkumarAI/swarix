package com.example.ui.screens.artist

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import com.example.core.model.Song
import com.example.ui.components.SongRowItem
import com.example.ui.theme.LocalSwarixColors

@Composable
fun ArtistProfileScreen(
    artistName: String,
    artistArtworkUri: String,
    artistSongs: List<Song>,
    onSongClick: (Song) -> Unit,
    onFavoriteToggle: (Song) -> Unit,
    onBackClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val colors = LocalSwarixColors.current
    val listState = rememberLazyListState()
    var selectedTab by remember { mutableStateOf("Discography") }
    val artistViewModel: ArtistViewModel = viewModel()
    val biographyState by artistViewModel.biographyState.collectAsStateWithLifecycle()
    
    LaunchedEffect(selectedTab) {
        if (selectedTab == "Biography" && biographyState is BiographyUiState.Idle) {
            artistViewModel.fetchBiography(artistName)
        }
    }
    
    // Animated scroll offset for header
    val scrollOffset = remember { derivedStateOf { listState.firstVisibleItemScrollOffset } }
    val headerAlpha by animateFloatAsState(targetValue = (1f - (scrollOffset.value / 400f)).coerceIn(0f, 1f), label = "header_alpha")

    Box(modifier = modifier.fillMaxSize().background(colors.mainBackground)) {
        LazyColumn(
            state = listState,
            modifier = Modifier.fillMaxSize()
        ) {
            item {
                // Collapsible Header
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(350.dp)
                        .graphicsLayer { alpha = headerAlpha }
                ) {
                    AsyncImage(
                        model = artistArtworkUri,
                        contentDescription = artistName,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize()
                    )
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(Brush.verticalGradient(listOf(Color.Transparent, colors.mainBackground)))
                    )
                    Column(
                        modifier = Modifier
                            .align(Alignment.BottomStart)
                            .padding(16.dp)
                    ) {
                        Text(
                            text = artistName,
                            style = MaterialTheme.typography.displayMedium.copy(
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                        )
                        Text(
                            text = "${artistSongs.size} songs in catalog",
                            style = MaterialTheme.typography.bodyLarge.copy(color = Color.White.copy(alpha = 0.7f))
                        )
                    }
                }
            }

            item {
                Text(
                    text = "Top Tracks",
                    style = MaterialTheme.typography.titleLarge.copy(
                        fontWeight = FontWeight.Bold,
                        color = colors.primaryText
                    ),
                    modifier = Modifier.padding(16.dp)
                )
            }

            items(artistSongs.take(5)) { song ->
                SongRowItem(
                    song = song,
                    isPlaying = false,
                    onSongClick = { onSongClick(song) },
                    onFavoriteToggle = { onFavoriteToggle(song) }
                )
            }
            
            item {
                Spacer(modifier = Modifier.height(16.dp))
                // Tab Selection
                TabRow(selectedTabIndex = if (selectedTab == "Discography") 0 else 1, containerColor = colors.mainBackground) {
                    Tab(selected = selectedTab == "Discography", onClick = { selectedTab = "Discography" }, text = { Text("Discography") })
                    Tab(selected = selectedTab == "Biography", onClick = { selectedTab = "Biography" }, text = { Text("Biography") })
                }
                
                when (val state = biographyState) {
                    is BiographyUiState.Idle, is BiographyUiState.Loading -> {
                        Text(
                            text = "Fetching biography...",
                            style = MaterialTheme.typography.bodyMedium.copy(color = colors.secondaryText),
                            modifier = Modifier.padding(16.dp)
                        )
                    }
                    is BiographyUiState.Success -> {
                        Text(
                            text = state.biography,
                            style = MaterialTheme.typography.bodyMedium.copy(color = colors.primaryText),
                            modifier = Modifier.padding(16.dp)
                        )
                    }
                    is BiographyUiState.Error -> {
                        Text(
                            text = state.message,
                            style = MaterialTheme.typography.bodyMedium.copy(color = MaterialTheme.colorScheme.error),
                            modifier = Modifier.padding(16.dp)
                        )
                    }
                }
            }
            
            if (selectedTab == "Discography") {
                items(artistSongs) { song ->
                    SongRowItem(
                        song = song,
                        isPlaying = false,
                        onSongClick = { onSongClick(song) },
                        onFavoriteToggle = { onFavoriteToggle(song) }
                    )
                }
            }
        }
        
        // Back Button
        IconButton(
            onClick = onBackClick,
            modifier = Modifier
                .padding(top = 40.dp, start = 16.dp)
                .clip(CircleShape)
                .background(Color.Black.copy(alpha = 0.3f))
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
