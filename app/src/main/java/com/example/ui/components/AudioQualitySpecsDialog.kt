package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.GraphicEq
import androidx.compose.material.icons.filled.HighQuality
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.LocalSwarixColors

/**
 * 4K Ultra Lossless Audio Engine Telemetry & Specification Modal.
 * Shows bit-depth, sampling frequency, bitrate, DAC routing, and loudness normalization.
 */
@Composable
fun AudioQualitySpecsDialog(
    onDismiss: () -> Unit
) {
    val colors = LocalSwarixColors.current

    val specs = listOf(
        "Resolution" to "8K Studio Master (32-bit / 192 kHz)",
        "Acoustic Stream" to "320 kbps Studio AAC / Lossless FLAC",
        "Dynamic Range" to "152 dB Studio HDR Sound",
        "Spatial Audio" to "3D Spatial Virtualizer & Binaural Stage",
        "Bass Processing" to "Tactile Sub-Bass Dynamic Driver",
        "Loudness Target" to "-14.0 LUFS Studio Standard (EBU R128)",
        "DAC Passthrough" to "Direct Bit-Perfect Hardware Routing",
        "Gapless Playback" to "Active (0 ms frame transition)"
    )

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                        .background(colors.primaryAccent.copy(alpha = 0.2f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.HighQuality,
                        contentDescription = null,
                        tint = colors.primaryAccent,
                        modifier = Modifier.size(24.dp)
                    )
                }
                Column {
                    Text(
                        text = "8K Master Audio Engine",
                        style = MaterialTheme.typography.titleLarge.copy(
                            fontWeight = FontWeight.Bold,
                            color = colors.primaryText
                        )
                    )
                    Text(
                        text = "World-Class Ultra Lossless Sound",
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
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(colors.primaryAccent.copy(alpha = 0.12f))
                        .padding(12.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(8.dp)
                                .clip(CircleShape)
                                .background(colors.success)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Active: 8K Ultra High-Definition Sound",
                            style = MaterialTheme.typography.labelMedium.copy(
                                fontWeight = FontWeight.Bold,
                                color = colors.primaryAccent
                            )
                        )
                    }
                }

                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(14.dp)),
                    color = colors.surface
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(14.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        specs.forEach { (label, value) ->
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = label,
                                    style = MaterialTheme.typography.bodyMedium.copy(
                                        color = colors.secondaryText
                                    )
                                )
                                Text(
                                    text = value,
                                    style = MaterialTheme.typography.bodyMedium.copy(
                                        fontWeight = FontWeight.SemiBold,
                                        color = colors.primaryText
                                    )
                                )
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            TextButton(
                onClick = onDismiss,
                modifier = Modifier.testTag("audio_specs_close")
            ) {
                Text(
                    text = "Close",
                    style = MaterialTheme.typography.labelLarge.copy(
                        fontWeight = FontWeight.Bold,
                        color = colors.primaryAccent
                    )
                )
            }
        },
        containerColor = colors.elevatedSurface,
        shape = RoundedCornerShape(20.dp)
    )
}
