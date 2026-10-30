package com.example.ui.screens.queue

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
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.ClearAll
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.DragHandle
import androidx.compose.material.icons.filled.Radio
import androidx.compose.material.icons.filled.Shuffle
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
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
import coil.compose.AsyncImage
import com.example.core.model.Song
import com.example.ui.components.AudioVisualizer
import com.example.ui.components.bouncyClick
import com.example.ui.theme.LocalSwarixColors

/**
 * Spotify Premium-grade Play Queue Screen matching Reference Image 1 Screen 9.
 * Includes:
 * - Queue summary (track count & duration)
 * - Now Playing card with live audio visualizer
 * - Autoplay Similar Songs radio toggle
 * - Clear Queue, Shuffle Queue & Gemini Smart Shuffle actions
 * - Next in Queue track list with drag handles and track removal
 */
@Composable
fun PlayQueueScreen(
    currentSong: Song?,
    queue: List<Song>,
    queueIndex: Int,
    isPlaying: Boolean,
    onSongClick: (Song) -> Unit,
    onRemoveFromQueue: (Int) -> Unit,
    onClearQueue: () -> Unit,
    onShuffleQueue: () -> Unit,
    onSmartShuffle: (() -> Unit)? = null,
    isSmartShuffling: Boolean = false,
    onBackClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val colors = LocalSwarixColors.current
    var isAutoplayEnabled by remember { mutableStateOf(true) }

    val totalDurationMs = queue.sumOf { it.durationMs }
    val totalHours = totalDurationMs / 3600000
    val totalMins = (totalDurationMs % 3600000) / 60000
    val durationSummary = if (totalHours > 0) {
        "${totalHours}h ${totalMins}m"
    } else {
        "${totalMins} minutes"
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
                modifier = Modifier.testTag("queue_back_button")
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "Back",
                    tint = colors.primaryText
                )
            }
            Spacer(modifier = Modifier.width(8.dp))
            Column {
                Text(
                    text = "Play Queue",
                    style = MaterialTheme.typography.titleLarge.copy(
                        fontWeight = FontWeight.Bold,
                        color = colors.primaryText
                    )
                )
                Text(
                    text = "${queue.size} songs • $durationSummary",
                    style = MaterialTheme.typography.labelMedium.copy(
                        color = colors.secondaryText
                    )
                )
            }
        }

        // Action Buttons: Smart Shuffle, Shuffle & Clear Queue
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 6.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // AI Smart Shuffle Button
            Button(
                onClick = { onSmartShuffle?.invoke() },
                enabled = !isSmartShuffling && queue.size >= 2,
                colors = ButtonDefaults.buttonColors(
                    containerColor = colors.primaryAccent,
                    contentColor = colors.mainBackground,
                    disabledContainerColor = colors.surface,
                    disabledContentColor = colors.mutedText
                ),
                shape = RoundedCornerShape(18.dp),
                modifier = Modifier
                    .weight(1.3f)
                    .testTag("btn_smart_shuffle")
            ) {
                if (isSmartShuffling) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(16.dp),
                        strokeWidth = 2.dp,
                        color = colors.mainBackground
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "AI Flow...",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold
                    )
                } else {
                    Icon(
                        imageVector = Icons.Default.AutoAwesome,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "Smart Shuffle",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            // Standard Shuffle
            OutlinedButton(
                onClick = onShuffleQueue,
                colors = ButtonDefaults.outlinedButtonColors(contentColor = colors.primaryText),
                shape = RoundedCornerShape(18.dp),
                modifier = Modifier
                    .weight(1f)
                    .testTag("btn_shuffle_queue")
            ) {
                Icon(
                    imageVector = Icons.Default.Shuffle,
                    contentDescription = null,
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = "Shuffle",
                    fontSize = 12.sp
                )
            }

            // Clear Queue Button
            OutlinedButton(
                onClick = onClearQueue,
                colors = ButtonDefaults.outlinedButtonColors(contentColor = colors.secondaryText),
                shape = RoundedCornerShape(18.dp),
                modifier = Modifier.testTag("btn_clear_queue")
            ) {
                Icon(
                    imageVector = Icons.Default.ClearAll,
                    contentDescription = "Clear Queue",
                    modifier = Modifier.size(16.dp)
                )
            }
        }

        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp),
            contentPadding = PaddingValues(top = 10.dp, bottom = 80.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // Now Playing Section
            if (currentSong != null) {
                item {
                    Text(
                        text = "Now Playing",
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Bold,
                            color = colors.primaryAccent
                        ),
                        modifier = Modifier.padding(vertical = 4.dp)
                    )

                    Surface(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(16.dp)),
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
                                    .size(54.dp)
                                    .clip(RoundedCornerShape(10.dp))
                                    .background(colors.surface)
                            ) {
                                AsyncImage(
                                    model = currentSong.artworkUri,
                                    contentDescription = currentSong.title,
                                    contentScale = ContentScale.Crop,
                                    modifier = Modifier.fillMaxSize()
                                )
                            }
                            Spacer(modifier = Modifier.width(12.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = currentSong.title,
                                    style = MaterialTheme.typography.titleMedium.copy(
                                        fontWeight = FontWeight.Bold,
                                        color = colors.primaryText
                                    ),
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                                Text(
                                    text = currentSong.artist,
                                    style = MaterialTheme.typography.bodyMedium.copy(
                                        color = colors.secondaryText
                                    ),
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                            if (isPlaying) {
                                AudioVisualizer(color = colors.primaryAccent, barCount = 4)
                            }
                        }
                    }
                }
            }

            // Spotify Autoplay Similar Content Toggle
            item {
                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 6.dp)
                        .clip(RoundedCornerShape(14.dp)),
                    color = colors.surface
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 14.dp, vertical = 10.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.weight(1f)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Radio,
                                contentDescription = null,
                                tint = colors.primaryAccent,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(10.dp))
                            Column {
                                Text(
                                    text = "Autoplay Similar Songs",
                                    style = MaterialTheme.typography.bodyLarge.copy(
                                        fontWeight = FontWeight.SemiBold,
                                        color = colors.primaryText
                                    )
                                )
                                Text(
                                    text = "Keep the music going when queue ends",
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        color = colors.secondaryText
                                    )
                                )
                            }
                        }

                        Switch(
                            checked = isAutoplayEnabled,
                            onCheckedChange = { isAutoplayEnabled = it },
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = colors.mainBackground,
                                checkedTrackColor = colors.primaryAccent,
                                uncheckedThumbColor = colors.secondaryText,
                                uncheckedTrackColor = colors.elevatedSurface
                            )
                        )
                    }
                }
            }

            // Next in Queue Section
            item {
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = "Next in Queue",
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.Bold,
                        color = colors.primaryText
                    ),
                    modifier = Modifier.padding(vertical = 4.dp)
                )
            }

            val upcoming = queue.filterIndexed { idx, _ -> idx != queueIndex }
            if (upcoming.isEmpty()) {
                item {
                    Text(
                        text = "Queue is empty. Songs will autoplay based on your music taste.",
                        style = MaterialTheme.typography.bodyMedium.copy(color = colors.mutedText),
                        modifier = Modifier.padding(vertical = 20.dp)
                    )
                }
            } else {
                itemsIndexed(queue) { index, song ->
                    if (index != queueIndex) {
                        Surface(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(12.dp))
                                .bouncyClick { onSongClick(song) },
                            color = colors.surface
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 12.dp, vertical = 8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(46.dp)
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(colors.elevatedSurface)
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
                                            color = colors.primaryText
                                        ),
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                    Text(
                                        text = song.artist,
                                        style = MaterialTheme.typography.bodyMedium.copy(
                                            color = colors.secondaryText
                                        ),
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                }

                                IconButton(
                                    onClick = { onRemoveFromQueue(index) },
                                    modifier = Modifier.size(32.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Close,
                                        contentDescription = "Remove",
                                        tint = colors.mutedText,
                                        modifier = Modifier.size(18.dp)
                                    )
                                }

                                Icon(
                                    imageVector = Icons.Default.DragHandle,
                                    contentDescription = "Reorder",
                                    tint = colors.divider,
                                    modifier = Modifier.padding(start = 4.dp)
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
