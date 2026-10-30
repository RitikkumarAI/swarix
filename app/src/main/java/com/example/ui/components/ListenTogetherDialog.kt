package com.example.ui.components

import android.graphics.Bitmap
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Group
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.core.listentogether.ListenTogetherManager
import com.example.ui.theme.LocalSwarixColors
import com.google.zxing.BarcodeFormat
import com.journeyapps.barcodescanner.BarcodeEncoder

@Composable
fun ListenTogetherDialog(
    currentTrackTitle: String,
    currentTrackArtist: String,
    activeSessionId: String?,
    onSessionStarted: (String) -> Unit,
    onSessionJoined: (String) -> Unit,
    onSessionLeft: () -> Unit,
    onDismiss: () -> Unit
) {
    val colors = LocalSwarixColors.current
    var sessionCodeInput by remember { mutableStateOf("") }
    var qrCodeBitmap by remember { mutableStateOf<Bitmap?>(null) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Icon(imageVector = Icons.Default.Group, contentDescription = null, tint = colors.primaryAccent)
                Text("Listen Together", color = colors.primaryText, fontWeight = FontWeight.Bold)
            }
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                if (activeSessionId == null) {
                    Text(
                        text = "Share your listening session with friends in real-time or join an active session.",
                        style = MaterialTheme.typography.bodyMedium.copy(color = colors.secondaryText)
                    )

                    Button(
                        onClick = {
                            val newId = ListenTogetherManager.createSession("Swarix User", currentTrackTitle, currentTrackArtist)
                            try {
                                val encoder = BarcodeEncoder()
                                qrCodeBitmap = encoder.encodeBitmap("swarix://session/$newId", BarcodeFormat.QR_CODE, 400, 400)
                            } catch (e: Exception) {
                                e.printStackTrace()
                            }
                            onSessionStarted(newId)
                        },
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.buttonColors(containerColor = colors.primaryAccent)
                    ) {
                        Text("Host New Session", color = colors.mainBackground, fontWeight = FontWeight.Bold)
                    }

                    HorizontalDivider(color = colors.divider)

                    OutlinedTextField(
                        value = sessionCodeInput,
                        onValueChange = { sessionCodeInput = it.uppercase() },
                        label = { Text("Enter Session Code") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )

                    Button(
                        onClick = {
                            if (sessionCodeInput.isNotBlank()) {
                                ListenTogetherManager.joinSession(sessionCodeInput) { success ->
                                    if (success) {
                                        onSessionJoined(sessionCodeInput)
                                    }
                                }
                            }
                        },
                        modifier = Modifier.fillMaxWidth(),
                        enabled = sessionCodeInput.isNotBlank()
                    ) {
                        Text("Join Session")
                    }
                } else {
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Text(
                            text = "Active Session Code:",
                            style = MaterialTheme.typography.bodyMedium.copy(color = colors.secondaryText)
                        )
                        Text(
                            text = activeSessionId,
                            style = MaterialTheme.typography.headlineLarge.copy(
                                color = colors.primaryAccent,
                                fontWeight = FontWeight.Bold
                            )
                        )

                        qrCodeBitmap?.let { bmp ->
                            Image(
                                bitmap = bmp.asImageBitmap(),
                                contentDescription = "Session QR Code",
                                modifier = Modifier
                                    .size(160.dp)
                                    .clip(RoundedCornerShape(12.dp))
                            )
                        }

                        Text(
                            text = "Now Playing: $currentTrackTitle by $currentTrackArtist",
                            style = MaterialTheme.typography.bodyMedium.copy(color = colors.primaryText)
                        )

                        Button(
                            onClick = {
                                ListenTogetherManager.leaveSession(activeSessionId)
                                onSessionLeft()
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = colors.error),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text("Leave Session", color = colors.primaryText, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("Close", color = colors.primaryAccent)
            }
        },
        containerColor = colors.surface
    )
}
