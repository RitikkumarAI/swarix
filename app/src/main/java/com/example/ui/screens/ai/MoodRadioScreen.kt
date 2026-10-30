package com.example.ui.screens.ai

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
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
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.CloudDone
import androidx.compose.material.icons.filled.FileDownload
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Radio
import androidx.compose.ui.platform.LocalContext
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
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
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.AsyncImage
import com.example.core.model.Song
import com.example.ui.components.bouncyClick
import com.example.ui.theme.LocalSwarixColors
import com.example.ui.viewmodels.MoodRadioViewModel

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun MoodRadioScreen(
    viewModel: MoodRadioViewModel,
    library: List<Song>,
    onSongClick: (Song, List<Song>) -> Unit = { _, _ -> },
    onPlayAll: (List<Song>) -> Unit = {},
    onExportM3u: (() -> Unit)? = null,
    onTriggerVoiceCommand: () -> Unit = {},
    onBackClick: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    val colors = LocalSwarixColors.current
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val activeMood by viewModel.activeMood.collectAsStateWithLifecycle()

    val availableMoods = listOf("Chill", "Energy", "Relax", "Focus", "Party", "Romance", "Workout")

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(colors.mainBackground)
            .systemBarsPadding()
    ) {
        // Top App Bar
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                IconButton(
                    onClick = onBackClick,
                    modifier = Modifier.testTag("btn_mood_radio_back")
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Back",
                        tint = colors.primaryText
                    )
                }
                Spacer(modifier = Modifier.width(6.dp))
                Column {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Radio,
                            contentDescription = null,
                            tint = colors.primaryAccent,
                            modifier = Modifier.size(20.dp)
                        )
                        Text(
                            text = "Mood Radio",
                            style = MaterialTheme.typography.titleLarge.copy(
                                fontWeight = FontWeight.Bold,
                                color = colors.primaryText
                            )
                        )
                    }
                    Text(
                        text = "AI-curated stations matching your vibe",
                        style = MaterialTheme.typography.labelSmall.copy(
                            color = colors.mutedText
                        )
                    )
                }
            }

            // Voice Command Button
            IconButton(
                onClick = onTriggerVoiceCommand,
                modifier = Modifier
                    .size(44.dp)
                    .clip(CircleShape)
                    .background(colors.primaryAccent.copy(alpha = 0.16f))
                    .testTag("btn_voice_mood_radio")
            ) {
                Icon(
                    imageVector = Icons.Default.Mic,
                    contentDescription = "Voice Commands",
                    tint = colors.primaryAccent,
                    modifier = Modifier.size(22.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Mood Selection Chips
        Column(modifier = Modifier.padding(horizontal = 16.dp)) {
            Text(
                text = "SELECT YOUR VIBE",
                style = MaterialTheme.typography.labelSmall.copy(
                    color = colors.primaryAccent,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.sp
                )
            )

            Spacer(modifier = Modifier.height(8.dp))

            FlowRow(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                availableMoods.forEach { mood ->
                    val isSelected = activeMood == mood
                    Surface(
                        modifier = Modifier
                            .clip(RoundedCornerShape(20.dp))
                            .clickable {
                                viewModel.selectMood(mood)
                                viewModel.generateMoodPlaylist(mood, library)
                            }
                            .testTag("mood_chip_${mood.lowercase()}"),
                        color = if (isSelected) colors.primaryAccent else colors.surface,
                        border = androidx.compose.foundation.BorderStroke(
                            1.dp,
                            if (isSelected) colors.primaryAccent else colors.divider
                        )
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Icon(
                                imageVector = if (isSelected) Icons.Default.AutoAwesome else Icons.Default.Radio,
                                contentDescription = null,
                                tint = if (isSelected) colors.mainBackground else colors.mutedText,
                                modifier = Modifier.size(14.dp)
                            )
                            Text(
                                text = mood,
                                style = MaterialTheme.typography.labelMedium.copy(
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                    color = if (isSelected) colors.mainBackground else colors.primaryText
                                )
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Action Buttons Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Button(
                    onClick = { viewModel.generateMoodPlaylist(activeMood, library) },
                    modifier = Modifier
                        .weight(1f)
                        .height(46.dp)
                        .testTag("btn_generate_mood_radio"),
                    colors = ButtonDefaults.buttonColors(containerColor = colors.primaryAccent),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.AutoAwesome,
                        contentDescription = null,
                        tint = colors.mainBackground,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "Generate $activeMood Station",
                        fontWeight = FontWeight.Bold,
                        color = colors.mainBackground,
                        fontSize = 13.sp
                    )
                }

                if (uiState is MoodRadioViewModel.UiState.Success) {
                    val songs = (uiState as MoodRadioViewModel.UiState.Success).songs
                    if (songs.isNotEmpty()) {
                        OutlinedButton(
                            onClick = { onPlayAll(songs) },
                            modifier = Modifier
                                .height(46.dp)
                                .testTag("btn_play_all_mood_radio"),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.PlayArrow,
                                contentDescription = null,
                                tint = colors.primaryAccent,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Play", color = colors.primaryText, fontSize = 13.sp)
                        }

                        if (onExportM3u != null) {
                            OutlinedButton(
                                onClick = onExportM3u,
                                modifier = Modifier
                                    .height(46.dp)
                                    .testTag("btn_export_m3u_playlist"),
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.FileDownload,
                                    contentDescription = "Export M3U Playlist",
                                    tint = colors.primaryAccent,
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Export M3U", color = colors.primaryText, fontSize = 13.sp)
                            }
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // Playlist Tracks List / Loading / Error
        when (val state = uiState) {
            is MoodRadioViewModel.UiState.Idle -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(24.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Radio,
                            contentDescription = null,
                            tint = colors.mutedText,
                            modifier = Modifier.size(48.dp)
                        )
                        Text(
                            text = "Choose a vibe above or say \"Play Focus music\"",
                            style = MaterialTheme.typography.bodyMedium.copy(
                                color = colors.mutedText
                            ),
                            textAlign = androidx.compose.ui.text.style.TextAlign.Center
                        )
                    }
                }
            }

            is MoodRadioViewModel.UiState.Loading -> {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        CircularProgressIndicator(
                            color = colors.primaryAccent,
                            strokeWidth = 3.dp
                        )
                        Text(
                            text = "Curating your $activeMood radio station...",
                            style = MaterialTheme.typography.bodyMedium.copy(
                                color = colors.secondaryText
                            )
                        )
                    }
                }
            }

            is MoodRadioViewModel.UiState.Success -> {
                Column(modifier = Modifier.fillMaxSize()) {
                    // Header Status Info
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 6.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "${state.songs.size} TRACKS • ${state.mood.uppercase()} STATION",
                            style = MaterialTheme.typography.labelSmall.copy(
                                color = colors.mutedText,
                                fontWeight = FontWeight.Bold,
                                letterSpacing = 0.5.sp
                            )
                        )

                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            if (onExportM3u != null) {
                                Surface(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(6.dp))
                                        .clickable { onExportM3u() }
                                        .testTag("btn_export_m3u_badge"),
                                    color = colors.primaryAccent.copy(alpha = 0.16f)
                                ) {
                                    Row(
                                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(3.dp)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.FileDownload,
                                            contentDescription = "Export M3U Playlist",
                                            tint = colors.primaryAccent,
                                            modifier = Modifier.size(11.dp)
                                        )
                                        Text(
                                            text = "EXPORT M3U",
                                            style = MaterialTheme.typography.labelSmall.copy(
                                                fontSize = 9.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = colors.primaryAccent
                                            )
                                        )
                                    }
                                }
                            }

                            // DataStore Cache Badge
                            Surface(
                                modifier = Modifier.clip(RoundedCornerShape(6.dp)),
                                color = if (state.isFromCache) colors.secondaryAccent.copy(alpha = 0.16f) else colors.primaryAccent.copy(alpha = 0.16f)
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                                ) {
                                    Icon(
                                        imageVector = if (state.isFromCache) Icons.Default.CloudDone else Icons.Default.AutoAwesome,
                                        contentDescription = null,
                                        tint = if (state.isFromCache) colors.secondaryAccent else colors.primaryAccent,
                                        modifier = Modifier.size(12.dp)
                                    )
                                    Text(
                                        text = if (state.isFromCache) "OFFLINE DATASTORE CACHE" else "GEMINI LIVE AI",
                                        style = MaterialTheme.typography.labelSmall.copy(
                                            fontSize = 9.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = if (state.isFromCache) colors.secondaryAccent else colors.primaryAccent
                                        )
                                    )
                                }
                            }
                        }
                    }

                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        itemsIndexed(state.songs, key = { _, s -> s.id }) { index, song ->
                            Surface(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(12.dp))
                                    .clickable { onSongClick(song, state.songs) }
                                    .testTag("mood_song_${song.id}"),
                                color = colors.surface
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(10.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    // Index / Play indicator
                                    Text(
                                        text = "${index + 1}",
                                        style = MaterialTheme.typography.labelMedium.copy(
                                            color = colors.mutedText,
                                            fontWeight = FontWeight.Bold
                                        ),
                                        modifier = Modifier.width(24.dp)
                                    )

                                    // Artwork
                                    Box(
                                        modifier = Modifier
                                            .size(46.dp)
                                            .clip(RoundedCornerShape(8.dp))
                                            .background(colors.elevatedSurface)
                                    ) {
                                        if (song.artworkUri.isNotBlank()) {
                                            AsyncImage(
                                                model = song.artworkUri,
                                                contentDescription = song.title,
                                                contentScale = ContentScale.Crop,
                                                modifier = Modifier.fillMaxSize()
                                            )
                                        } else {
                                            Icon(
                                                imageVector = Icons.Default.Radio,
                                                contentDescription = null,
                                                tint = colors.primaryAccent,
                                                modifier = Modifier
                                                    .size(24.dp)
                                                    .align(Alignment.Center)
                                            )
                                        }
                                    }

                                    Spacer(modifier = Modifier.width(12.dp))

                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            text = song.title,
                                            style = MaterialTheme.typography.bodyLarge.copy(
                                                fontWeight = FontWeight.SemiBold,
                                                color = colors.primaryText
                                            ),
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis
                                        )
                                        Text(
                                            text = "${song.artist} • ${song.genre.ifBlank { song.mood }}",
                                            style = MaterialTheme.typography.bodySmall.copy(
                                                color = colors.secondaryText
                                            ),
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis
                                        )
                                    }

                                    IconButton(
                                        onClick = { onSongClick(song, state.songs) },
                                        modifier = Modifier.size(40.dp)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.PlayArrow,
                                            contentDescription = "Play Track",
                                            tint = colors.primaryAccent,
                                            modifier = Modifier.size(24.dp)
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }

            is MoodRadioViewModel.UiState.Error -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(24.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "Error: ${state.message}",
                        color = colors.error,
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            }
        }
    }
}
