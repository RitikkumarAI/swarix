package com.example.ui.components

import android.Manifest
import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Equalizer
import androidx.compose.material.icons.filled.GraphicEq
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.MicOff
import androidx.compose.material.icons.filled.Radio
import androidx.compose.material.icons.filled.Send
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.core.model.EqualizerState
import com.example.speech.VoiceCommandManager
import com.example.speech.VoiceCommandResult
import com.example.ui.theme.LocalSwarixColors

/**
 * Modern Spotify-grade Voice Command Sheet for triggering Mood Radio and Adjusting Equalizer settings.
 */
@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun VoiceCommandSheet(
    voiceManager: VoiceCommandManager,
    currentEqualizerState: EqualizerState,
    onCommandExecuted: (VoiceCommandResult) -> Unit,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val colors = LocalSwarixColors.current
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    val isListening by voiceManager.isListening.collectAsStateWithLifecycle()
    val partialText by voiceManager.partialText.collectAsStateWithLifecycle()
    val recognizedText by voiceManager.recognizedText.collectAsStateWithLifecycle()
    val statusMessage by voiceManager.statusMessage.collectAsStateWithLifecycle()
    val audioRms by voiceManager.audioRms.collectAsStateWithLifecycle()
    val isProcessing by voiceManager.isProcessing.collectAsStateWithLifecycle()
    val commandResult by voiceManager.commandResult.collectAsStateWithLifecycle()

    var textInput by remember { mutableStateOf("") }
    var hasRecordPermission by remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.RECORD_AUDIO
            ) == PackageManager.PERMISSION_GRANTED
        )
    }

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        hasRecordPermission = isGranted
        if (isGranted) {
            voiceManager.startListening(currentEqualizerState, onCommandExecuted)
        }
    }

    // Auto-start listening if permission granted
    LaunchedEffect(Unit) {
        if (hasRecordPermission) {
            voiceManager.startListening(currentEqualizerState, onCommandExecuted)
        } else {
            permissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
        }
    }

    DisposableEffect(Unit) {
        onDispose {
            voiceManager.stopListening()
        }
    }

    // Pulsing animation for listening state
    val infiniteTransition = rememberInfiniteTransition(label = "mic_pulse")
    val pulseScale by infiniteTransition.animateFloat(
        initialValue = 1.0f,
        targetValue = 1.25f,
        animationSpec = infiniteRepeatable(
            animation = tween(900, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulse_scale"
    )

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = colors.elevatedSurface,
        modifier = modifier.testTag("voice_command_sheet")
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 8.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Header Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.AutoAwesome,
                        contentDescription = null,
                        tint = colors.primaryAccent,
                        modifier = Modifier.size(20.dp)
                    )
                    Text(
                        text = "Swarix Voice Assistant",
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Bold,
                            color = colors.primaryText
                        )
                    )
                }

                IconButton(
                    onClick = onDismiss,
                    modifier = Modifier.size(48.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "Close",
                        tint = colors.mutedText
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Animated Microphone Button Stage
            Box(
                modifier = Modifier
                    .size(130.dp),
                contentAlignment = Alignment.Center
            ) {
                // Outer Ripple Ring
                if (isListening) {
                    val dynamicScale = (pulseScale + (audioRms / 15f)).coerceIn(1.0f, 1.45f)
                    Box(
                        modifier = Modifier
                            .size(120.dp)
                            .scale(dynamicScale)
                            .clip(CircleShape)
                            .background(colors.primaryAccent.copy(alpha = 0.15f))
                    )
                    Box(
                        modifier = Modifier
                            .size(96.dp)
                            .scale(pulseScale)
                            .clip(CircleShape)
                            .background(colors.primaryAccent.copy(alpha = 0.25f))
                    )
                }

                Surface(
                    modifier = Modifier
                        .size(76.dp)
                        .clip(CircleShape)
                        .clickable {
                            if (!hasRecordPermission) {
                                permissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
                            } else if (isListening) {
                                voiceManager.stopListening()
                            } else {
                                voiceManager.startListening(currentEqualizerState, onCommandExecuted)
                            }
                        }
                        .testTag("btn_voice_record"),
                    shape = CircleShape,
                    color = if (isListening) colors.primaryAccent else colors.surface,
                    border = androidx.compose.foundation.BorderStroke(
                        2.dp,
                        if (isListening) colors.primaryAccent else colors.divider
                    )
                ) {
                    Box(
                        modifier = Modifier.fillMaxWidth(),
                        contentAlignment = Alignment.Center
                    ) {
                        if (isProcessing) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(32.dp),
                                color = if (isListening) colors.mainBackground else colors.primaryAccent,
                                strokeWidth = 3.dp
                            )
                        } else {
                            Icon(
                                imageVector = if (isListening) Icons.Default.Mic else Icons.Default.MicOff,
                                contentDescription = "Toggle Recording",
                                tint = if (isListening) colors.mainBackground else colors.primaryAccent,
                                modifier = Modifier.size(36.dp)
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Status & Live Transcript Box
            Text(
                text = if (isListening) "Listening... speak now" else statusMessage,
                style = MaterialTheme.typography.labelMedium.copy(
                    color = if (isListening) colors.primaryAccent else colors.mutedText,
                    fontWeight = FontWeight.Medium
                ),
                textAlign = TextAlign.Center
            )

            val displaySpoken = if (isListening && partialText.isNotBlank()) {
                "\"$partialText\""
            } else if (recognizedText.isNotBlank()) {
                "\"$recognizedText\""
            } else {
                ""
            }

            if (displaySpoken.isNotBlank()) {
                Spacer(modifier = Modifier.height(8.dp))
                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(colors.surface.copy(alpha = 0.6f))
                        .padding(horizontal = 14.dp, vertical = 10.dp),
                    color = Color.Transparent
                ) {
                    Text(
                        text = displaySpoken,
                        style = MaterialTheme.typography.bodyLarge.copy(
                            fontWeight = FontWeight.SemiBold,
                            color = colors.primaryText
                        ),
                        textAlign = TextAlign.Center
                    )
                }
            }

            // Command Result Action Pill
            AnimatedVisibility(visible = commandResult != null) {
                commandResult?.let { res ->
                    Spacer(modifier = Modifier.height(12.dp))
                    Surface(
                        modifier = Modifier
                            .clip(RoundedCornerShape(10.dp)),
                        color = when (res) {
                            is VoiceCommandResult.MoodRadio -> colors.primaryAccent.copy(alpha = 0.18f)
                            is VoiceCommandResult.Equalizer -> colors.secondaryAccent.copy(alpha = 0.18f)
                            is VoiceCommandResult.PlaybackAction -> colors.elevatedSurface
                            is VoiceCommandResult.Unrecognized -> colors.error.copy(alpha = 0.15f)
                        }
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Icon(
                                imageVector = when (res) {
                                    is VoiceCommandResult.MoodRadio -> Icons.Default.Radio
                                    is VoiceCommandResult.Equalizer -> Icons.Default.Equalizer
                                    is VoiceCommandResult.PlaybackAction -> Icons.Default.GraphicEq
                                    is VoiceCommandResult.Unrecognized -> Icons.Default.AutoAwesome
                                },
                                contentDescription = null,
                                tint = when (res) {
                                    is VoiceCommandResult.MoodRadio -> colors.primaryAccent
                                    is VoiceCommandResult.Equalizer -> colors.secondaryAccent
                                    is VoiceCommandResult.PlaybackAction -> colors.primaryAccent
                                    is VoiceCommandResult.Unrecognized -> colors.error
                                },
                                modifier = Modifier.size(18.dp)
                            )
                            Text(
                                text = when (res) {
                                    is VoiceCommandResult.MoodRadio -> "Mood Radio: ${res.mood}"
                                    is VoiceCommandResult.Equalizer -> res.message
                                    is VoiceCommandResult.PlaybackAction -> res.message
                                    is VoiceCommandResult.Unrecognized -> res.suggestion
                                },
                                style = MaterialTheme.typography.bodyMedium.copy(
                                    fontWeight = FontWeight.Medium,
                                    color = colors.primaryText
                                )
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Quick Voice Suggestion Chips
            Text(
                text = "TRY SAYING",
                style = MaterialTheme.typography.labelSmall.copy(
                    color = colors.mutedText,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.sp
                ),
                modifier = Modifier.align(Alignment.Start)
            )

            Spacer(modifier = Modifier.height(8.dp))

            FlowRow(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                val suggestions = listOf(
                    "Play Focus music",
                    "Boost bass",
                    "Acoustic preset",
                    "Chill mood radio",
                    "Vocal booster",
                    "Energy playlist",
                    "Increase treble",
                    "Reset equalizer"
                )

                suggestions.forEach { suggestion ->
                    Surface(
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .clickable {
                                textInput = suggestion
                                voiceManager.processSpokenCommand(
                                    suggestion,
                                    currentEqualizerState,
                                    onCommandExecuted
                                )
                            }
                            .testTag("suggestion_${suggestion.lowercase().replace(" ", "_")}"),
                        color = colors.surface,
                        border = androidx.compose.foundation.BorderStroke(1.dp, colors.divider)
                    ) {
                        Text(
                            text = suggestion,
                            style = MaterialTheme.typography.labelSmall.copy(
                                color = colors.secondaryText,
                                fontSize = 11.sp
                            ),
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Manual Text Input Fallback (for typing voice commands or testing)
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                OutlinedTextField(
                    value = textInput,
                    onValueChange = { textInput = it },
                    placeholder = {
                        Text(
                            "Type command or ask AI...",
                            color = colors.mutedText,
                            fontSize = 13.sp
                        )
                    },
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = colors.primaryText,
                        unfocusedTextColor = colors.primaryText,
                        focusedBorderColor = colors.primaryAccent,
                        unfocusedBorderColor = colors.divider,
                        focusedContainerColor = colors.surface,
                        unfocusedContainerColor = colors.surface
                    ),
                    shape = RoundedCornerShape(12.dp),
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Send),
                    keyboardActions = KeyboardActions(
                        onSend = {
                            if (textInput.isNotBlank()) {
                                voiceManager.processSpokenCommand(
                                    textInput,
                                    currentEqualizerState,
                                    onCommandExecuted
                                )
                            }
                        }
                    ),
                    modifier = Modifier
                        .weight(1f)
                        .testTag("input_voice_command")
                )

                Spacer(modifier = Modifier.width(8.dp))

                IconButton(
                    onClick = {
                        if (textInput.isNotBlank()) {
                            voiceManager.processSpokenCommand(
                                textInput,
                                currentEqualizerState,
                                onCommandExecuted
                            )
                        }
                    },
                    modifier = Modifier
                        .size(48.dp)
                        .clip(CircleShape)
                        .background(colors.primaryAccent)
                        .testTag("btn_voice_send_text")
                ) {
                    Icon(
                        imageVector = Icons.Default.Send,
                        contentDescription = "Send",
                        tint = colors.mainBackground,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}
