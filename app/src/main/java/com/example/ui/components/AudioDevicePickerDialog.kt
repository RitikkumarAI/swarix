package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Cast
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.DirectionsCar
import androidx.compose.material.icons.filled.Headphones
import androidx.compose.material.icons.filled.PhoneAndroid
import androidx.compose.material.icons.filled.Speaker
import androidx.compose.material.icons.filled.Tv
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.LocalSwarixColors

data class AudioDevice(
    val id: String,
    val name: String,
    val description: String,
    val icon: ImageVector,
    val is4kCapable: Boolean = true
)

/**
 * Spotify Connect-style Device Picker modal.
 * Enables switching playback between local device, studio wireless headphones (LDAC),
 * Wi-Fi Hi-Fi speaker, 4K Smart TV Cast, and Android Auto.
 */
@Composable
fun AudioDevicePickerDialog(
    currentDeviceId: String,
    onSelectDevice: (AudioDevice) -> Unit,
    onDismiss: () -> Unit
) {
    val colors = LocalSwarixColors.current

    val devices = remember {
        listOf(
            AudioDevice(
                id = "phone",
                name = "This Phone",
                description = "Internal DAC • 4K Master (24-bit / 96kHz)",
                icon = Icons.Default.PhoneAndroid,
                is4kCapable = true
            ),
            AudioDevice(
                id = "headphones",
                name = "Studio Wireless Headphones",
                description = "Bluetooth LDAC • 990 kbps Hi-Res Audio",
                icon = Icons.Default.Headphones,
                is4kCapable = true
            ),
            AudioDevice(
                id = "hifi_living_room",
                name = "Living Room Hi-Fi",
                description = "Wi-Fi Lossless Streamer (AirPlay / DLNA)",
                icon = Icons.Default.Speaker,
                is4kCapable = true
            ),
            AudioDevice(
                id = "smart_tv",
                name = "4K Smart TV Cast",
                description = "Google Cast 4K Ultra HD Audio Passthrough",
                icon = Icons.Default.Tv,
                is4kCapable = true
            ),
            AudioDevice(
                id = "car",
                name = "CarPlay / Android Auto",
                description = "Automotive Wireless Audio Link",
                icon = Icons.Default.DirectionsCar,
                is4kCapable = false
            )
        )
    }

    var activeId by remember { mutableStateOf(currentDeviceId) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .clip(CircleShape)
                        .background(colors.primaryAccent.copy(alpha = 0.2f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Cast,
                        contentDescription = null,
                        tint = colors.primaryAccent,
                        modifier = Modifier.size(20.dp)
                    )
                }
                Column {
                    Text(
                        text = "Connect to a Device",
                        style = MaterialTheme.typography.titleLarge.copy(
                            fontWeight = FontWeight.Bold,
                            color = colors.primaryText
                        )
                    )
                    Text(
                        text = "Swarix Hi-Fi Audio Link",
                        style = MaterialTheme.typography.labelMedium.copy(
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
                devices.forEach { device ->
                    val isSelected = device.id == activeId

                    Surface(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(14.dp))
                            .border(
                                width = if (isSelected) 1.5.dp else 1.dp,
                                color = if (isSelected) colors.primaryAccent else colors.divider,
                                shape = RoundedCornerShape(14.dp)
                            )
                            .clickable {
                                activeId = device.id
                                onSelectDevice(device)
                            }
                            .testTag("device_option_${device.id}"),
                        color = if (isSelected) colors.primaryAccent.copy(alpha = 0.08f) else colors.surface
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(40.dp)
                                    .clip(CircleShape)
                                    .background(if (isSelected) colors.primaryAccent else colors.elevatedSurface),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = device.icon,
                                    contentDescription = device.name,
                                    tint = if (isSelected) colors.mainBackground else colors.primaryText,
                                    modifier = Modifier.size(22.dp)
                                )
                            }

                            Spacer(modifier = Modifier.width(12.dp))

                            Column(modifier = Modifier.weight(1f)) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(
                                        text = device.name,
                                        style = MaterialTheme.typography.bodyLarge.copy(
                                            fontWeight = FontWeight.SemiBold,
                                            color = colors.primaryText
                                        )
                                    )
                                    if (device.is4kCapable) {
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Box(
                                            modifier = Modifier
                                                .clip(RoundedCornerShape(4.dp))
                                                .background(colors.primaryAccent.copy(alpha = 0.2f))
                                                .padding(horizontal = 4.dp, vertical = 1.dp)
                                        ) {
                                            Text(
                                                text = "4K",
                                                style = MaterialTheme.typography.labelSmall.copy(
                                                    fontSize = 9.sp,
                                                    fontWeight = FontWeight.Bold,
                                                    color = colors.primaryAccent
                                                )
                                            )
                                        }
                                    }
                                }
                                Text(
                                    text = device.description,
                                    style = MaterialTheme.typography.labelMedium.copy(
                                        color = if (isSelected) colors.primaryAccent else colors.secondaryText
                                    )
                                )
                            }

                            if (isSelected) {
                                Box(
                                    modifier = Modifier
                                        .size(24.dp)
                                        .clip(CircleShape)
                                        .background(colors.primaryAccent),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Check,
                                        contentDescription = "Active",
                                        tint = colors.mainBackground,
                                        modifier = Modifier.size(16.dp)
                                    )
                                }
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            TextButton(
                onClick = onDismiss,
                modifier = Modifier.testTag("device_picker_done")
            ) {
                Text(
                    text = "Done",
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
