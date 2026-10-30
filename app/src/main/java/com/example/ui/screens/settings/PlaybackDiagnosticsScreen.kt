package com.example.ui.screens.settings

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.BugReport
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.ListAlt
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.core.model.Song
import com.example.playback.PlaybackDiagnosticLogger
import com.example.ui.theme.LocalSwarixColors
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * Playback Diagnostics Screen.
 * Displays real-time ExoPlayer state, buffering metrics, active audio track metadata,
 * and a live diagnostic log feed captured by PlaybackDiagnosticLogger to troubleshoot audio output issues.
 */
@Composable
fun PlaybackDiagnosticsScreen(
    healthStatus: String,
    recoveryCount: Int,
    currentSong: Song?,
    currentPosMs: Long,
    durationMs: Long,
    isPlaying: Boolean,
    logs: List<PlaybackDiagnosticLogger.DiagnosticEntry>,
    onResetPlayer: () -> Unit,
    onDumpLogs: () -> Unit,
    onBackClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val colors = LocalSwarixColors.current
    val dateFormat = remember { SimpleDateFormat("HH:mm:ss.SSS", Locale.US) }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(colors.mainBackground)
            .systemBarsPadding()
            .testTag("playback_diagnostics_screen")
    ) {
        // Top Navigation Bar
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(
                onClick = onBackClick,
                modifier = Modifier.testTag("diagnostics_back_button")
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "Back",
                    tint = colors.primaryText
                )
            }
            Spacer(modifier = Modifier.width(8.dp))
            Icon(
                imageVector = Icons.Default.BugReport,
                contentDescription = null,
                tint = colors.primaryAccent,
                modifier = Modifier.size(22.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = "Playback Diagnostics",
                style = MaterialTheme.typography.titleLarge.copy(
                    fontWeight = FontWeight.Bold,
                    color = colors.primaryText
                )
            )
        }

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            // 1. Audio Engine State & Health Card
            item {
                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(16.dp)),
                    color = colors.elevatedSurface
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Info,
                                contentDescription = null,
                                tint = colors.primaryAccent,
                                modifier = Modifier.size(18.dp)
                            )
                            Text(
                                text = "ExoPlayer Health & Audio Session",
                                style = MaterialTheme.typography.titleMedium.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = colors.primaryText
                                )
                            )
                        }

                        DiagnosticRow("Health Status", healthStatus, colors.primaryAccent)
                        DiagnosticRow("Playback State", if (isPlaying) "PLAYING" else "PAUSED/IDLE", colors.primaryText)
                        DiagnosticRow("Auto-Recoveries Executed", "$recoveryCount", colors.secondaryText)
                        DiagnosticRow("AudioAttributes", "USAGE_MEDIA (ContentType: MUSIC)", colors.secondaryText)
                        DiagnosticRow("Audio Focus", "Granted (Managed by AudioFocusManager)", colors.primaryAccent)

                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(top = 8.dp),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Button(
                                onClick = onResetPlayer,
                                colors = ButtonDefaults.buttonColors(containerColor = colors.secondaryAccent),
                                modifier = Modifier.weight(1f)
                            ) {
                                Text("Reset Player", color = colors.mainBackground, fontWeight = FontWeight.Bold)
                            }
                            OutlinedButton(
                                onClick = onDumpLogs,
                                modifier = Modifier.weight(1f)
                            ) {
                                Text("Dump Logcat", color = colors.primaryText)
                            }
                        }
                    }
                }
            }

            // 2. Active Audio Track Metadata Card
            item {
                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(16.dp)),
                    color = colors.elevatedSurface
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(
                            text = "Active Track Metadata",
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.Bold,
                                color = colors.primaryText
                            )
                        )

                        if (currentSong != null) {
                            DiagnosticRow("Title", currentSong.title, colors.primaryText)
                            DiagnosticRow("Artist", currentSong.artist, colors.secondaryText)
                            DiagnosticRow("Album", currentSong.album.ifBlank { "Single" }, colors.secondaryText)
                            DiagnosticRow("Content URI", currentSong.contentUri, colors.mutedText)
                            DiagnosticRow("Progress / Duration", "${currentPosMs / 1000}s / ${durationMs / 1000}s", colors.primaryAccent)
                        } else {
                            Text(
                                text = "No active audio track playing.",
                                style = MaterialTheme.typography.bodyMedium.copy(color = colors.mutedText)
                            )
                        }
                    }
                }
            }

            // 3. Real-time Log Feed Header
            item {
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
                            imageVector = Icons.Default.ListAlt,
                            contentDescription = null,
                            tint = colors.primaryAccent,
                            modifier = Modifier.size(18.dp)
                        )
                        Text(
                            text = "Real-Time Diagnostic Log Feed (${logs.size})",
                            style = MaterialTheme.typography.titleSmall.copy(
                                fontWeight = FontWeight.Bold,
                                color = colors.primaryText
                            )
                        )
                    }
                    IconButton(onClick = onDumpLogs) {
                        Icon(
                            imageVector = Icons.Default.Refresh,
                            contentDescription = "Refresh Dump",
                            tint = colors.primaryAccent
                        )
                    }
                }
            }

            // 4. Real-time Log Feed Items
            if (logs.isEmpty()) {
                item {
                    Text(
                        text = "No diagnostic events captured yet.",
                        style = MaterialTheme.typography.bodySmall.copy(color = colors.mutedText),
                        modifier = Modifier.padding(horizontal = 8.dp)
                    )
                }
            } else {
                items(logs.reversed()) { entry ->
                    Surface(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(10.dp)),
                        color = colors.surface
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(10.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(
                                    text = entry.category,
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        color = if (entry.category.contains("ERROR")) colors.error else colors.primaryAccent,
                                        fontWeight = FontWeight.Bold
                                    )
                                )
                                Text(
                                    text = dateFormat.format(Date(entry.timestampMs)),
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        color = colors.mutedText,
                                        fontSize = 10.sp
                                    )
                                )
                            }
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = entry.details,
                                style = MaterialTheme.typography.bodySmall.copy(
                                    color = colors.primaryText,
                                    fontFamily = FontFamily.Monospace,
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

@Composable
private fun DiagnosticRow(label: String, value: String, valueColor: androidx.compose.ui.graphics.Color) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall.copy(color = LocalSwarixColors.current.secondaryText)
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodySmall.copy(
                fontWeight = FontWeight.SemiBold,
                color = valueColor
            )
        )
    }
}
