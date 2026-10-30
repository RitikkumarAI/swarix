package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Bedtime
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.GraphicEq
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.LocalSwarixColors

/**
 * Spotify Premium-grade Sleep Timer modal dialog with gradual volume fade-out controls.
 */
@Composable
fun SleepTimerDialog(
    currentMinutes: Int,
    secondsRemaining: Int = 0,
    isFadingOut: Boolean = false,
    onSelectMinutes: (Int) -> Unit = {},
    onSetSleepTimer: (minutes: Int, fadeSeconds: Int, isEndOfTrack: Boolean) -> Unit = { m, f, e -> onSelectMinutes(m) },
    onCancelTimer: () -> Unit = {},
    onAddMinutes: (Int) -> Unit = {},
    onDismiss: () -> Unit
) {
    val colors = LocalSwarixColors.current

    val timerOptions = remember {
        listOf(
            5 to "5 min",
            15 to "15 min",
            30 to "30 min",
            45 to "45 min",
            60 to "1 hour",
            999 to "End of track"
        )
    }

    val fadeOptions = remember {
        listOf(
            30 to "30 sec",
            60 to "1 min",
            120 to "2 min",
            0 to "No fade"
        )
    }

    var selectedMinutes by remember { mutableIntStateOf(if (currentMinutes > 0) currentMinutes else 30) }
    var selectedFadeSeconds by remember { mutableIntStateOf(60) }

    val isTimerActive = secondsRemaining > 0

    fun formatRemaining(sec: Int): String {
        if (sec >= 999) return "End of track"
        val m = sec / 60
        val s = sec % 60
        return "%d:%02d".format(m, s)
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                        .background(colors.primaryAccent.copy(alpha = 0.2f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Bedtime,
                        contentDescription = null,
                        tint = colors.primaryAccent,
                        modifier = Modifier.size(22.dp)
                    )
                }
                Column {
                    Text(
                        text = "Sleep Timer",
                        style = MaterialTheme.typography.titleLarge.copy(
                            fontWeight = FontWeight.Bold,
                            color = colors.primaryText
                        )
                    )
                    Text(
                        text = "Gradually fades volume before stopping playback",
                        style = MaterialTheme.typography.labelSmall.copy(
                            color = colors.secondaryText
                        )
                    )
                }
            }
        },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                // Active Timer Status Card
                if (isTimerActive) {
                    Surface(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(14.dp))
                            .border(1.2.dp, colors.primaryAccent.copy(alpha = 0.5f), RoundedCornerShape(14.dp)),
                        color = colors.primaryAccent.copy(alpha = 0.12f)
                    ) {
                        Column(
                            modifier = Modifier.padding(14.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Timer,
                                        contentDescription = null,
                                        tint = colors.primaryAccent,
                                        modifier = Modifier.size(18.dp)
                                    )
                                    Text(
                                        text = if (isFadingOut) "Fading Out Volume..." else "Timer Active",
                                        style = MaterialTheme.typography.labelLarge.copy(
                                            color = colors.primaryAccent,
                                            fontWeight = FontWeight.Bold
                                        )
                                    )
                                }

                                Text(
                                    text = formatRemaining(secondsRemaining),
                                    style = MaterialTheme.typography.titleMedium.copy(
                                        color = colors.primaryText,
                                        fontWeight = FontWeight.Bold,
                                        letterSpacing = 1.sp
                                    )
                                )
                            }

                            // Quick extension and cancel buttons
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                OutlinedButton(
                                    onClick = { onAddMinutes(5) },
                                    modifier = Modifier.weight(1f),
                                    shape = RoundedCornerShape(8.dp),
                                    colors = ButtonDefaults.outlinedButtonColors(contentColor = colors.primaryAccent)
                                ) {
                                    Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(14.dp))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("+5m", fontSize = 12.sp)
                                }

                                OutlinedButton(
                                    onClick = { onAddMinutes(15) },
                                    modifier = Modifier.weight(1f),
                                    shape = RoundedCornerShape(8.dp),
                                    colors = ButtonDefaults.outlinedButtonColors(contentColor = colors.primaryAccent)
                                ) {
                                    Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(14.dp))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("+15m", fontSize = 12.sp)
                                }

                                Button(
                                    onClick = {
                                        onCancelTimer()
                                        onDismiss()
                                    },
                                    modifier = Modifier.weight(1.1f),
                                    shape = RoundedCornerShape(8.dp),
                                    colors = ButtonDefaults.buttonColors(containerColor = colors.error.copy(alpha = 0.85f))
                                ) {
                                    Icon(Icons.Default.Stop, contentDescription = null, modifier = Modifier.size(14.dp))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("Turn off", fontSize = 12.sp, color = colors.primaryText)
                                }
                            }
                        }
                    }
                }

                // Section 1: Duration Selection
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text(
                        text = "TIMER DURATION",
                        style = MaterialTheme.typography.labelSmall.copy(
                            color = colors.mutedText,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 0.5.sp
                        )
                    )

                    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        timerOptions.chunked(3).forEach { rowOptions ->
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                rowOptions.forEach { (mins, label) ->
                                    val isSelected = (selectedMinutes == mins)
                                    Surface(
                                        modifier = Modifier
                                            .weight(1f)
                                            .clip(RoundedCornerShape(10.dp))
                                            .border(
                                                1.dp,
                                                if (isSelected) colors.primaryAccent else colors.divider,
                                                RoundedCornerShape(10.dp)
                                            )
                                            .clickable { selectedMinutes = mins }
                                            .testTag("sleep_timer_opt_$mins"),
                                        color = if (isSelected) colors.primaryAccent.copy(alpha = 0.15f) else colors.surface
                                    ) {
                                        Text(
                                            text = label,
                                            style = MaterialTheme.typography.bodyMedium.copy(
                                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                                color = if (isSelected) colors.primaryAccent else colors.primaryText
                                            ),
                                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 10.dp),
                                            textAlign = androidx.compose.ui.text.style.TextAlign.Center
                                        )
                                    }
                                }
                            }
                        }
                    }
                }

                // Section 2: Gradual Volume Fade-Out Window
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "GRADUAL VOLUME FADE-OUT",
                            style = MaterialTheme.typography.labelSmall.copy(
                                color = colors.mutedText,
                                fontWeight = FontWeight.Bold,
                                letterSpacing = 0.5.sp
                            )
                        )
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.GraphicEq,
                                contentDescription = null,
                                tint = colors.primaryAccent,
                                modifier = Modifier.size(13.dp)
                            )
                            Text(
                                text = "Gentle curve",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    color = colors.primaryAccent,
                                    fontSize = 10.sp
                                )
                            )
                        }
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        fadeOptions.forEach { (sec, label) ->
                            val isSelected = (selectedFadeSeconds == sec)
                            Surface(
                                modifier = Modifier
                                    .weight(1f)
                                    .clip(RoundedCornerShape(10.dp))
                                    .border(
                                        1.dp,
                                        if (isSelected) colors.primaryAccent else colors.divider,
                                        RoundedCornerShape(10.dp)
                                    )
                                    .clickable { selectedFadeSeconds = sec }
                                    .testTag("fade_opt_$sec"),
                                color = if (isSelected) colors.primaryAccent.copy(alpha = 0.15f) else colors.surface
                            ) {
                                Text(
                                    text = label,
                                    style = MaterialTheme.typography.bodySmall.copy(
                                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                        color = if (isSelected) colors.primaryAccent else colors.primaryText,
                                        fontSize = 11.sp
                                    ),
                                    modifier = Modifier.padding(horizontal = 4.dp, vertical = 8.dp),
                                    textAlign = androidx.compose.ui.text.style.TextAlign.Center
                                )
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val isEnd = (selectedMinutes == 999)
                    onSetSleepTimer(selectedMinutes, selectedFadeSeconds, isEnd)
                    onDismiss()
                },
                colors = ButtonDefaults.buttonColors(containerColor = colors.primaryAccent),
                shape = RoundedCornerShape(10.dp),
                modifier = Modifier.testTag("btn_confirm_sleep_timer")
            ) {
                Text(
                    text = "Start Sleep Timer",
                    color = colors.mainBackground,
                    fontWeight = FontWeight.Bold
                )
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel", color = colors.secondaryText)
            }
        },
        containerColor = colors.elevatedSurface,
        shape = RoundedCornerShape(22.dp)
    )
}
