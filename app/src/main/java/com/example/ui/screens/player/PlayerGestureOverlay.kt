package com.example.ui.screens.player

import android.content.Context
import android.media.AudioManager
import android.os.Build
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.detectDragGestures
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
import androidx.compose.material.icons.automirrored.filled.VolumeDown
import androidx.compose.material.icons.automirrored.filled.VolumeMute
import androidx.compose.material.icons.automirrored.filled.VolumeUp
import androidx.compose.material.icons.filled.SkipNext
import androidx.compose.material.icons.filled.SkipPrevious
import androidx.compose.material.icons.filled.VolumeOff
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.LocalSwarixColors
import kotlinx.coroutines.delay
import kotlin.math.abs

enum class SkipDirection {
    NEXT,
    PREVIOUS
}

/**
 * Controller class to manage audio volume via AudioManager.
 */
class VolumeController(private val context: Context) {
    private val audioManager = context.getSystemService(Context.AUDIO_SERVICE) as AudioManager

    val maxVolume: Int
        get() = audioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC).coerceAtLeast(1)

    val minVolume: Int
        get() = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            audioManager.getStreamMinVolume(AudioManager.STREAM_MUSIC)
        } else {
            0
        }

    val currentVolume: Int
        get() = audioManager.getStreamVolume(AudioManager.STREAM_MUSIC)

    fun changeVolumeBy(step: Int): Int {
        val current = currentVolume
        val target = (current + step).coerceIn(minVolume, maxVolume)
        if (target != current) {
            audioManager.setStreamVolume(AudioManager.STREAM_MUSIC, target, 0)
        }
        return target
    }
}

/**
 * Custom Modifier for detecting horizontal swipe gestures to skip tracks
 * and vertical swipe gestures to adjust volume.
 */
fun Modifier.playerSwipeGestures(
    onSkipNext: () -> Unit,
    onSkipPrevious: () -> Unit,
    onVolumeAdjust: (newVolume: Int, maxVolume: Int) -> Unit,
    onHorizontalDragOffset: (Float) -> Unit = {},
    onSkipDirectionHint: (SkipDirection?) -> Unit = {},
    thresholdDp: Float = 60f
): Modifier = pointerInput(Unit) {
    val thresholdPx = thresholdDp * density
    val volumeStepPx = 22f * density

    var totalDragX = 0f
    var totalDragY = 0f
    var accumulatedVolumeY = 0f
    var isDirectionLocked = false
    var isHorizontal = false

    detectDragGestures(
        onDragStart = {
            totalDragX = 0f
            totalDragY = 0f
            accumulatedVolumeY = 0f
            isDirectionLocked = false
            isHorizontal = false
            onHorizontalDragOffset(0f)
            onSkipDirectionHint(null)
        },
        onDrag = { change, dragAmount ->
            change.consume()
            totalDragX += dragAmount.x
            totalDragY += dragAmount.y

            if (!isDirectionLocked) {
                val absX = abs(totalDragX)
                val absY = abs(totalDragY)
                if (absX > 16f || absY > 16f) {
                    isDirectionLocked = true
                    isHorizontal = absX > absY * 1.15f
                }
            }

            if (isDirectionLocked) {
                if (isHorizontal) {
                    // Damped drag offset for turntable physical feedback
                    val dampedOffset = totalDragX * 0.35f
                    onHorizontalDragOffset(dampedOffset.coerceIn(-80f * density, 80f * density))

                    // Update live hint
                    if (totalDragX < -thresholdPx * 0.5f) {
                        onSkipDirectionHint(SkipDirection.NEXT)
                    } else if (totalDragX > thresholdPx * 0.5f) {
                        onSkipDirectionHint(SkipDirection.PREVIOUS)
                    } else {
                        onSkipDirectionHint(null)
                    }
                } else {
                    // Vertical drag for volume adjustment
                    // In Android screen coords: drag up is negative Y (increase volume), drag down is positive Y (decrease volume)
                    accumulatedVolumeY += dragAmount.y
                    if (accumulatedVolumeY <= -volumeStepPx) {
                        val steps = (abs(accumulatedVolumeY) / volumeStepPx).toInt().coerceAtLeast(1)
                        accumulatedVolumeY += steps * volumeStepPx
                        onVolumeAdjust(1 * steps, 0) // handled by controller
                    } else if (accumulatedVolumeY >= volumeStepPx) {
                        val steps = (accumulatedVolumeY / volumeStepPx).toInt().coerceAtLeast(1)
                        accumulatedVolumeY -= steps * volumeStepPx
                        onVolumeAdjust(-1 * steps, 0)
                    }
                }
            }
        },
        onDragEnd = {
            if (isDirectionLocked && isHorizontal) {
                if (totalDragX <= -thresholdPx) {
                    onSkipNext()
                } else if (totalDragX >= thresholdPx) {
                    onSkipPrevious()
                }
            }
            onHorizontalDragOffset(0f)
            onSkipDirectionHint(null)
            totalDragX = 0f
            totalDragY = 0f
            accumulatedVolumeY = 0f
            isDirectionLocked = false
        },
        onDragCancel = {
            onHorizontalDragOffset(0f)
            onSkipDirectionHint(null)
            totalDragX = 0f
            totalDragY = 0f
            accumulatedVolumeY = 0f
            isDirectionLocked = false
        }
    )
}

/**
 * Visual Feedback Overlay for Volume adjustment.
 * Displays volume icon, animated level bar, and percentage.
 */
@Composable
fun VolumeFeedbackOverlay(
    visible: Boolean,
    currentVolume: Int,
    maxVolume: Int,
    modifier: Modifier = Modifier
) {
    val colors = LocalSwarixColors.current
    val fraction = if (maxVolume > 0) (currentVolume.toFloat() / maxVolume.toFloat()).coerceIn(0f, 1f) else 0f
    val percentage = (fraction * 100).toInt()

    val volumeIcon: ImageVector = when {
        currentVolume == 0 -> Icons.Default.VolumeOff
        fraction < 0.35f -> Icons.AutoMirrored.Filled.VolumeMute
        fraction < 0.70f -> Icons.AutoMirrored.Filled.VolumeDown
        else -> Icons.AutoMirrored.Filled.VolumeUp
    }

    AnimatedVisibility(
        visible = visible,
        enter = fadeIn(spring(stiffness = Spring.StiffnessMedium)) +
                scaleIn(initialScale = 0.85f, animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy)),
        exit = fadeOut(spring(stiffness = Spring.StiffnessLow)) +
                scaleOut(targetScale = 0.9f),
        modifier = modifier
    ) {
        Surface(
            modifier = Modifier
                .shadow(24.dp, RoundedCornerShape(24.dp), ambientColor = colors.primaryAccent.copy(alpha = 0.25f))
                .clip(RoundedCornerShape(24.dp))
                .border(1.2.dp, colors.primaryAccent.copy(alpha = 0.45f), RoundedCornerShape(24.dp))
                .testTag("volume_feedback_overlay"),
            color = colors.elevatedSurface.copy(alpha = 0.94f)
        ) {
            Row(
                modifier = Modifier
                    .padding(horizontal = 20.dp, vertical = 14.dp)
                    .width(220.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                // Icon with subtle glow background
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                        .background(colors.primaryAccent.copy(alpha = 0.15f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = volumeIcon,
                        contentDescription = "Volume",
                        tint = colors.primaryAccent,
                        modifier = Modifier.size(22.dp)
                    )
                }

                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Volume",
                            style = MaterialTheme.typography.labelSmall.copy(
                                color = colors.secondaryText,
                                fontWeight = FontWeight.SemiBold,
                                letterSpacing = 0.5.sp
                            )
                        )
                        Text(
                            text = "$percentage%",
                            style = MaterialTheme.typography.labelSmall.copy(
                                color = colors.primaryAccent,
                                fontWeight = FontWeight.Bold
                            )
                        )
                    }

                    // Volume progress bar
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(6.dp)
                            .clip(RoundedCornerShape(3.dp))
                            .background(colors.divider)
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth(fraction)
                                .height(6.dp)
                                .clip(RoundedCornerShape(3.dp))
                                .background(
                                    Brush.horizontalGradient(
                                        listOf(colors.primaryAccent, colors.secondaryAccent)
                                    )
                                )
                        )
                    }
                }
            }
        }
    }
}

/**
 * Visual Feedback Overlay for Track Skip gesture.
 * Informs the user they are skipping forward or backward with directional iconography.
 */
@Composable
fun TrackSkipFeedbackOverlay(
    visible: Boolean,
    direction: SkipDirection?,
    modifier: Modifier = Modifier
) {
    if (direction == null) return
    val colors = LocalSwarixColors.current

    val isNext = direction == SkipDirection.NEXT
    val label = if (isNext) "Next Track" else "Previous Track"
    val icon = if (isNext) Icons.Default.SkipNext else Icons.Default.SkipPrevious

    AnimatedVisibility(
        visible = visible,
        enter = fadeIn(spring(stiffness = Spring.StiffnessMedium)) +
                slideInVertically(
                    initialOffsetY = { -it / 2 },
                    animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy)
                ) +
                scaleIn(initialScale = 0.85f),
        exit = fadeOut(spring(stiffness = Spring.StiffnessLow)) +
                slideOutVertically(targetOffsetY = { -it / 3 }) +
                scaleOut(targetScale = 0.9f),
        modifier = modifier
    ) {
        Surface(
            modifier = Modifier
                .shadow(20.dp, RoundedCornerShape(20.dp), ambientColor = colors.primaryAccent.copy(alpha = 0.3f))
                .clip(RoundedCornerShape(20.dp))
                .border(1.2.dp, colors.primaryAccent.copy(alpha = 0.5f), RoundedCornerShape(20.dp))
                .testTag("track_skip_feedback_overlay"),
            color = colors.elevatedSurface.copy(alpha = 0.94f)
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 18.dp, vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(34.dp)
                        .clip(CircleShape)
                        .background(colors.primaryAccent.copy(alpha = 0.18f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = icon,
                        contentDescription = label,
                        tint = colors.primaryAccent,
                        modifier = Modifier.size(20.dp)
                    )
                }
                Text(
                    text = label,
                    style = MaterialTheme.typography.titleSmall.copy(
                        color = colors.primaryText,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 0.3.sp
                    )
                )
            }
        }
    }
}
