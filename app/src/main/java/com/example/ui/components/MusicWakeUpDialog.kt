package com.example.ui.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.RadioButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.core.model.Song
import com.example.playback.MusicWakeUpManager
import com.example.ui.theme.LocalSwarixColors

@Composable
fun MusicWakeUpDialog(
    library: List<Song>,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    val colors = LocalSwarixColors.current

    var selectedMood by remember { mutableStateOf("Chill") }
    var rampSeconds by remember { mutableIntStateOf(60) }

    val moods = listOf("Chill", "Energy", "Relax", "Focus", "Workout", "Party")
    val durations = listOf(
        "30 Seconds" to 30,
        "1 Minute" to 60,
        "2 Minutes" to 120,
        "5 Minutes" to 300
    )

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = "Music Wake-up Alarm",
                style = MaterialTheme.typography.titleLarge.copy(
                    fontWeight = FontWeight.Bold,
                    color = colors.primaryText
                )
            )
        },
        text = {
            Column(modifier = Modifier.fillMaxWidth()) {
                Text(
                    text = "Gently ramp up volume over time using a Mood Radio playlist as your alarm.",
                    style = MaterialTheme.typography.bodyMedium.copy(color = colors.secondaryText)
                )
                Spacer(modifier = Modifier.height(12.dp))

                Text(
                    text = "Select Alarm Mood:",
                    style = MaterialTheme.typography.labelLarge.copy(
                        fontWeight = FontWeight.Bold,
                        color = colors.primaryAccent
                    )
                )
                Spacer(modifier = Modifier.height(4.dp))

                moods.chunked(3).forEach { rowMoods ->
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        rowMoods.forEach { mood ->
                            Row(
                                modifier = Modifier
                                    .weight(1f)
                                    .clickable { selectedMood = mood }
                                    .padding(vertical = 4.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                RadioButton(
                                    selected = selectedMood == mood,
                                    onClick = { selectedMood = mood },
                                    colors = RadioButtonDefaults.colors(selectedColor = colors.primaryAccent)
                                )
                                Text(
                                    text = mood,
                                    style = MaterialTheme.typography.bodyMedium.copy(color = colors.primaryText),
                                    fontSize = 14.sp
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    text = "Volume Ramp-Up Duration:",
                    style = MaterialTheme.typography.labelLarge.copy(
                        fontWeight = FontWeight.Bold,
                        color = colors.primaryAccent
                    )
                )
                Spacer(modifier = Modifier.height(4.dp))

                durations.forEach { (label, secs) ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { rampSeconds = secs }
                            .padding(vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        RadioButton(
                            selected = rampSeconds == secs,
                            onClick = { rampSeconds = secs },
                            colors = RadioButtonDefaults.colors(selectedColor = colors.primaryAccent)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = label,
                            style = MaterialTheme.typography.bodyMedium.copy(color = colors.primaryText)
                        )
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    // Filter library or create playlist for selected mood
                    val playlist = library.filter {
                        it.mood.contains(selectedMood, ignoreCase = true) ||
                        it.genre.contains(selectedMood, ignoreCase = true)
                    }.ifEmpty { library.shuffled().take(10) }

                    MusicWakeUpManager.startWakeUpAlarm(
                        context = context,
                        playlist = playlist,
                        rampDurationSeconds = rampSeconds
                    )
                    onDismiss()
                },
                colors = ButtonDefaults.buttonColors(containerColor = colors.primaryAccent)
            ) {
                Text("Test Wake-up Now", color = colors.mainBackground, fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel", color = colors.secondaryText)
            }
        },
        containerColor = colors.elevatedSurface
    )
}
