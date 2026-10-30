package com.example.ui.screens.ai

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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.PlaylistAdd
import androidx.compose.material.icons.filled.Send
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ElevatedButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
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
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.core.model.Song
import com.example.ui.components.SongRowItem
import com.example.ui.components.VoiceSearchDialog
import com.example.ui.theme.LocalSwarixColors

data class ChatMessage(
    val id: String = java.util.UUID.randomUUID().toString(),
    val senderIsUser: Boolean,
    val text: String,
    val recommendedSongs: List<Song> = emptyList(),
    val suggestedPlaylistTitle: String = ""
)

@Composable
fun SwarixAiScreen(
    catalogSongs: List<Song>,
    currentPlayingSong: Song?,
    isPlaying: Boolean,
    onPlayPlaylist: (List<Song>) -> Unit,
    onSongClick: (Song) -> Unit,
    onSavePlaylist: (title: String, songs: List<Song>) -> Unit,
    modifier: Modifier = Modifier
) {
    val colors = LocalSwarixColors.current
    var inputText by remember { mutableStateOf("") }
    var showVoiceDialog by remember { mutableStateOf(false) }

    val promptSuggestions = remember {
        listOf(
            "Make a playlist for late night drive",
            "Suggest songs like Arijit Singh",
            "Play some relaxing music",
            "Create a workout playlist",
            "Show trending songs",
            "Tell me about this artist"
        )
    }

    var messages by remember {
        mutableStateOf(
            listOf(
                ChatMessage(
                    senderIsUser = false,
                    text = "Hi Ritik! I'm Swarix AI, your personal music companion. Ask me to curate custom playlists, discover acoustics for any mood, or suggest songs based on your taste."
                )
            )
        )
    }

    fun handleUserPrompt(prompt: String) {
        if (prompt.isBlank()) return
        val userMsg = ChatMessage(senderIsUser = true, text = prompt)

        val lower = prompt.lowercase()
        val (responseTitle, responseSongs) = when {
            lower.contains("workout") || lower.contains("energy") -> {
                "High Energy Workout Mix" to catalogSongs.filter { it.mood == "Workout" || it.genre == "EDM" }.ifEmpty { catalogSongs.take(4) }
            }
            lower.contains("drive") || lower.contains("night") -> {
                "Late Night Drive Acoustics" to catalogSongs.filter { it.mood == "Chill" || it.mood == "Romance" }.ifEmpty { catalogSongs.take(4) }
            }
            lower.contains("arijit") -> {
                "Arijit Singh & Soulful Ballads" to catalogSongs.filter { it.artist.contains("Arijit", ignoreCase = true) }
            }
            lower.contains("relax") || lower.contains("chill") -> {
                "Calm & Serene Harmony" to catalogSongs.filter { it.mood == "Chill" || it.mood == "Focus" }
            }
            else -> {
                "Curated for You" to catalogSongs.shuffled().take(4)
            }
        }

        val aiMsg = ChatMessage(
            senderIsUser = false,
            text = "Here is what I curated for you based on \"$prompt\". Enjoy the acoustic flow!",
            recommendedSongs = responseSongs,
            suggestedPlaylistTitle = responseTitle
        )

        messages = messages + userMsg + aiMsg
        inputText = ""
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(colors.mainBackground)
            .systemBarsPadding()
    ) {
        // AI Header with golden emblem
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 16.dp, bottom = 8.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Swarix Golden Acoustic Emblem with radiant pulse
            com.example.ui.components.SwarixLogoMark(
                size = 56.dp,
                enableGlow = true
            )

            Spacer(modifier = Modifier.height(10.dp))

            Text(
                text = "Swarix AI",
                style = MaterialTheme.typography.titleLarge.copy(
                    fontWeight = FontWeight.Bold,
                    color = colors.primaryText
                )
            )
            Text(
                text = "Your Personal Music Companion",
                style = MaterialTheme.typography.bodyMedium.copy(
                    color = colors.primaryAccent
                )
            )
        }

        // Chat & Suggestions List
        LazyColumn(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            contentPadding = PaddingValues(vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            // Suggestion chips at top
            item {
                Text(
                    text = "Suggestions",
                    style = MaterialTheme.typography.labelMedium.copy(
                        color = colors.mutedText,
                        fontWeight = FontWeight.Bold
                    ),
                    modifier = Modifier.padding(bottom = 6.dp)
                )
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    promptSuggestions.chunked(2).forEach { rowPrompts ->
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            rowPrompts.forEach { prompt ->
                                Surface(
                                    modifier = Modifier
                                        .weight(1f)
                                        .clip(RoundedCornerShape(16.dp))
                                        .clickable { handleUserPrompt(prompt) }
                                        .testTag("ai_prompt_${prompt.take(10).lowercase()}"),
                                    color = colors.elevatedSurface
                                ) {
                                    Text(
                                        text = prompt,
                                        style = MaterialTheme.typography.labelSmall.copy(
                                            color = colors.primaryText,
                                            fontWeight = FontWeight.Medium
                                        ),
                                        modifier = Modifier.padding(10.dp)
                                    )
                                }
                            }
                        }
                    }
                }
            }

            items(messages) { msg ->
                if (msg.senderIsUser) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.End
                    ) {
                        Surface(
                            modifier = Modifier.clip(RoundedCornerShape(18.dp)),
                            color = colors.primaryAccent
                        ) {
                            Text(
                                text = msg.text,
                                style = MaterialTheme.typography.bodyMedium.copy(
                                    color = colors.mainBackground,
                                    fontWeight = FontWeight.Medium
                                ),
                                modifier = Modifier.padding(14.dp)
                            )
                        }
                    }
                } else {
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalAlignment = Alignment.Start
                    ) {
                        Surface(
                            modifier = Modifier.clip(RoundedCornerShape(18.dp)),
                            color = colors.elevatedSurface
                        ) {
                            Column(modifier = Modifier.padding(14.dp)) {
                                Text(
                                    text = msg.text,
                                    style = MaterialTheme.typography.bodyMedium.copy(
                                        color = colors.primaryText
                                    )
                                )

                                if (msg.recommendedSongs.isNotEmpty()) {
                                    Spacer(modifier = Modifier.height(12.dp))
                                    Text(
                                        text = msg.suggestedPlaylistTitle,
                                        style = MaterialTheme.typography.titleMedium.copy(
                                            fontWeight = FontWeight.Bold,
                                            color = colors.primaryAccent
                                        )
                                    )
                                    Spacer(modifier = Modifier.height(8.dp))

                                    msg.recommendedSongs.forEach { song ->
                                        SongRowItem(
                                            song = song,
                                            isPlaying = song.id == currentPlayingSong?.id && isPlaying,
                                            onSongClick = { onSongClick(song) }
                                        )
                                    }

                                    Spacer(modifier = Modifier.height(10.dp))
                                    Row(
                                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                                    ) {
                                        ElevatedButton(
                                            onClick = { onPlayPlaylist(msg.recommendedSongs) },
                                            colors = ButtonDefaults.elevatedButtonColors(
                                                containerColor = colors.primaryAccent,
                                                contentColor = colors.mainBackground
                                            ),
                                            shape = RoundedCornerShape(16.dp)
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.PlayArrow,
                                                contentDescription = null,
                                                modifier = Modifier.size(18.dp)
                                            )
                                            Spacer(modifier = Modifier.width(6.dp))
                                            Text("Play Now")
                                        }

                                        ElevatedButton(
                                            onClick = {
                                                onSavePlaylist(
                                                    msg.suggestedPlaylistTitle,
                                                    msg.recommendedSongs
                                                )
                                            },
                                            colors = ButtonDefaults.elevatedButtonColors(
                                                containerColor = colors.surface,
                                                contentColor = colors.primaryText
                                            ),
                                            shape = RoundedCornerShape(16.dp)
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.PlaylistAdd,
                                                contentDescription = null,
                                                modifier = Modifier.size(18.dp)
                                            )
                                            Spacer(modifier = Modifier.width(6.dp))
                                            Text("Save Playlist")
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        // Input Box
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            OutlinedTextField(
                value = inputText,
                onValueChange = { inputText = it },
                modifier = Modifier
                    .weight(1f)
                    .testTag("ai_input_field"),
                placeholder = { Text("Ask anything...", color = colors.mutedText) },
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

            Spacer(modifier = Modifier.width(6.dp))

            IconButton(
                onClick = { showVoiceDialog = true },
                modifier = Modifier
                    .size(44.dp)
                    .clip(CircleShape)
                    .background(colors.elevatedSurface)
                    .testTag("ai_voice_button")
            ) {
                Icon(
                    imageVector = Icons.Default.Mic,
                    contentDescription = "Voice Input",
                    tint = colors.primaryAccent,
                    modifier = Modifier.size(22.dp)
                )
            }

            Spacer(modifier = Modifier.width(6.dp))

            IconButton(
                onClick = { handleUserPrompt(inputText) },
                modifier = Modifier
                    .size(44.dp)
                    .clip(CircleShape)
                    .background(colors.primaryAccent)
                    .testTag("ai_send_button")
            ) {
                Icon(
                    imageVector = Icons.Default.Send,
                    contentDescription = "Send",
                    tint = colors.mainBackground,
                    modifier = Modifier.size(20.dp)
                )
            }
        }
    }

    if (showVoiceDialog) {
        VoiceSearchDialog(
            onDismiss = { showVoiceDialog = false },
            onVoiceResult = { spokenQuery ->
                showVoiceDialog = false
                inputText = spokenQuery
                handleUserPrompt(spokenQuery)
            }
        )
    }
}
