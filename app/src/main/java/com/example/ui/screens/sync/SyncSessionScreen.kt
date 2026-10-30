package com.example.ui.screens.sync

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.core.model.Song
import com.example.ui.theme.LocalSwarixColors

@Composable
fun SyncSessionScreen(
    currentSong: Song?,
    isPlaying: Boolean,
    onPlayPauseClick: () -> Unit,
    onLeaveSession: () -> Unit,
    onBackClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val colors = LocalSwarixColors.current

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(colors.mainBackground)
            .padding(16.dp)
            .systemBarsPadding()
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onBackClick) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back", tint = colors.primaryText)
            }
            Text("Sync Session", style = MaterialTheme.typography.titleLarge.copy(color = colors.primaryText, fontWeight = FontWeight.Bold))
        }

        Spacer(modifier = Modifier.height(24.dp))

        Surface(
            modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(16.dp)),
            color = colors.elevatedSurface
        ) {
            Column(modifier = Modifier.padding(16.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                Icon(Icons.Default.MusicNote, null, modifier = Modifier.size(64.dp), tint = colors.primaryAccent)
                Text(currentSong?.title ?: "No song playing", style = MaterialTheme.typography.headlineSmall.copy(color = colors.primaryText))
                Text(currentSong?.artist ?: "Artist", style = MaterialTheme.typography.bodyLarge.copy(color = colors.secondaryText))
                
                Spacer(modifier = Modifier.height(16.dp))
                
                IconButton(onClick = onPlayPauseClick, modifier = Modifier.size(64.dp).background(colors.primaryAccent, CircleShape)) {
                    Icon(if (isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow, "Play/Pause", tint = colors.mainBackground, modifier = Modifier.size(32.dp))
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))
        Button(onClick = onLeaveSession, modifier = Modifier.fillMaxWidth(), colors = ButtonDefaults.buttonColors(containerColor = colors.primaryAccent)) {
            Text("Leave Session", color = colors.mainBackground)
        }
    }
}
