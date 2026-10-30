package com.example.ui.screens.settings

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.CompareArrows
import androidx.compose.material.icons.automirrored.filled.VolumeUp
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.GraphicEq
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Shuffle
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.LocalSwarixColors
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.math.sin

@Composable
fun CrossfadeSettingsScreen(
    crossfadeEnabled: Boolean,
    crossfadeSeconds: Int,
    gaplessEnabled: Boolean,
    onCrossfadeToggle: (Boolean) -> Unit,
    onCrossfadeSecondsChange: (Int) -> Unit,
    onGaplessToggle: (Boolean) -> Unit,
    onPreviewCrossfade: (Int) -> Unit,
    onBackClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val colors = LocalSwarixColors.current
    val scope = rememberCoroutineScope()
    var isPreviewing by remember { mutableStateOf(false) }

    // Wave animation for visualizer
    val pulseAnim = remember { Animatable(0f) }
    LaunchedEffect(isPreviewing) {
        if (isPreviewing) {
            pulseAnim.animateTo(
                targetValue = 1f,
                animationSpec = infiniteRepeatable(
                    animation = tween(1200, easing = LinearEasing),
                    repeatMode = RepeatMode.Restart
                )
            )
        } else {
            pulseAnim.snapTo(0f)
        }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(colors.mainBackground)
            .systemBarsPadding()
    ) {
        // Top Navigation Bar
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(
                onClick = onBackClick,
                modifier = Modifier.testTag("crossfade_back_button")
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "Back",
                    tint = colors.primaryText
                )
            }
            Spacer(modifier = Modifier.width(4.dp))
            Column {
                Text(
                    text = "Crossfade",
                    style = MaterialTheme.typography.titleLarge.copy(
                        fontWeight = FontWeight.Bold,
                        color = colors.primaryText
                    )
                )
                Text(
                    text = "Audio & Playback Transitions",
                    style = MaterialTheme.typography.labelMedium.copy(
                        color = colors.mutedText
                    )
                )
            }
        }

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 8.dp, bottom = 32.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Master Switch Card
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = colors.elevatedSurface)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 18.dp, vertical = 16.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.weight(1f)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(44.dp)
                                    .clip(CircleShape)
                                    .background(
                                        if (crossfadeEnabled) colors.primaryAccent.copy(alpha = 0.18f)
                                        else colors.divider.copy(alpha = 0.4f)
                                    ),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.AutoMirrored.Filled.CompareArrows,
                                    contentDescription = null,
                                    tint = if (crossfadeEnabled) colors.primaryAccent else colors.mutedText,
                                    modifier = Modifier.size(24.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(14.dp))
                            Column {
                                Text(
                                    text = "Crossfade Tracks",
                                    style = MaterialTheme.typography.titleMedium.copy(
                                        fontWeight = FontWeight.Bold,
                                        color = colors.primaryText
                                    )
                                )
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(
                                    text = if (crossfadeEnabled) "Enabled (${crossfadeSeconds}s overlap)" else "Disabled (Gapless mode)",
                                    style = MaterialTheme.typography.bodySmall.copy(
                                        color = if (crossfadeEnabled) colors.primaryAccent else colors.secondaryText
                                    )
                                )
                            }
                        }

                        Switch(
                            checked = crossfadeEnabled,
                            onCheckedChange = onCrossfadeToggle,
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = colors.mainBackground,
                                checkedTrackColor = colors.primaryAccent,
                                uncheckedThumbColor = colors.mutedText,
                                uncheckedTrackColor = colors.surface
                            ),
                            modifier = Modifier.testTag("crossfade_switch")
                        )
                    }
                }
            }

            // Interactive Crossfade Overlap Visualization Diagram
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = colors.surface)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(18.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.GraphicEq,
                                    contentDescription = null,
                                    tint = colors.primaryAccent,
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "BLEND DYNAMICS",
                                    style = MaterialTheme.typography.labelMedium.copy(
                                        fontWeight = FontWeight.Bold,
                                        color = colors.primaryAccent,
                                        letterSpacing = 1.sp
                                    )
                                )
                            }

                            // Overlap duration pill
                            Surface(
                                shape = RoundedCornerShape(12.dp),
                                color = if (crossfadeEnabled && crossfadeSeconds > 0) colors.primaryAccent.copy(alpha = 0.16f) else colors.divider.copy(alpha = 0.5f)
                            ) {
                                Text(
                                    text = if (crossfadeEnabled && crossfadeSeconds > 0) "${crossfadeSeconds}s Overlap" else "0s (No Fade)",
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        fontWeight = FontWeight.Bold,
                                        color = if (crossfadeEnabled && crossfadeSeconds > 0) colors.primaryAccent else colors.mutedText
                                    ),
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        // Custom Waveform Overlap Canvas
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(120.dp)
                                .clip(RoundedCornerShape(14.dp))
                                .background(colors.mainBackground)
                                .border(1.dp, colors.divider.copy(alpha = 0.5f), RoundedCornerShape(14.dp))
                        ) {
                            val accentColor = colors.primaryAccent
                            val secondaryAccent = colors.secondaryAccent
                            val dividerColor = colors.divider
                            val isFading = crossfadeEnabled && crossfadeSeconds > 0
                            val overlapFraction = if (isFading) (crossfadeSeconds / 12f).coerceIn(0.1f, 1f) else 0f

                            Canvas(modifier = Modifier.fillMaxSize()) {
                                val w = size.width
                                val h = size.height

                                // Draw subtle background grid lines
                                val midY = h / 2f
                                drawLine(
                                    color = dividerColor.copy(alpha = 0.3f),
                                    start = Offset(0f, midY),
                                    end = Offset(w, midY),
                                    strokeWidth = 1f
                                )

                                if (isFading) {
                                    // Highlight overlap zone in the middle
                                    val overlapWidth = w * (0.3f + 0.45f * overlapFraction)
                                    val overlapLeft = (w - overlapWidth) / 2f
                                    val overlapRight = overlapLeft + overlapWidth

                                    // Overlap shaded background
                                    drawRect(
                                        brush = Brush.horizontalGradient(
                                            colors = listOf(
                                                accentColor.copy(alpha = 0.05f),
                                                accentColor.copy(alpha = 0.18f),
                                                secondaryAccent.copy(alpha = 0.05f)
                                            ),
                                            startX = overlapLeft,
                                            endX = overlapRight
                                        ),
                                        topLeft = Offset(overlapLeft, 0f),
                                        size = androidx.compose.ui.geometry.Size(overlapWidth, h)
                                    )

                                    // Overlap boundary dashed lines
                                    drawLine(
                                        color = accentColor.copy(alpha = 0.4f),
                                        start = Offset(overlapLeft, 0f),
                                        end = Offset(overlapLeft, h),
                                        strokeWidth = 1.5f
                                    )
                                    drawLine(
                                        color = secondaryAccent.copy(alpha = 0.4f),
                                        start = Offset(overlapRight, 0f),
                                        end = Offset(overlapRight, h),
                                        strokeWidth = 1.5f
                                    )

                                    // Track A fade-out curve (descending from left to right)
                                    val pathA = Path().apply {
                                        moveTo(0f, 16f)
                                        cubicTo(
                                            overlapLeft, 16f,
                                            midY, midY,
                                            overlapRight, h - 16f
                                        )
                                        lineTo(w, h - 16f)
                                    }
                                    drawPath(
                                        path = pathA,
                                        color = accentColor,
                                        style = Stroke(width = 3.5f, cap = StrokeCap.Round)
                                    )

                                    // Track B fade-in curve (ascending from left to right)
                                    val pathB = Path().apply {
                                        moveTo(0f, h - 16f)
                                        lineTo(overlapLeft, h - 16f)
                                        cubicTo(
                                            midY, midY,
                                            overlapRight, 16f,
                                            w, 16f
                                        )
                                    }
                                    drawPath(
                                        path = pathB,
                                        color = secondaryAccent,
                                        style = Stroke(width = 3.5f, cap = StrokeCap.Round)
                                    )

                                    // Overlapping audio frequency bars in the overlap zone
                                    val barCount = 14
                                    val barSpacing = overlapWidth / (barCount + 1)
                                    for (i in 1..barCount) {
                                        val barX = overlapLeft + i * barSpacing
                                        val wavePhase = pulseAnim.value * 2 * Math.PI.toFloat()
                                        val waveHeight = (20f + 25f * sin(i * 0.7f + wavePhase)).coerceIn(12f, h - 28f)
                                        val barTop = (h - waveHeight) / 2f
                                        drawLine(
                                            color = if (i % 2 == 0) accentColor.copy(alpha = 0.5f) else secondaryAccent.copy(alpha = 0.5f),
                                            start = Offset(barX, barTop),
                                            end = Offset(barX, barTop + waveHeight),
                                            strokeWidth = 2.5f,
                                            cap = StrokeCap.Round
                                        )
                                    }
                                } else {
                                    // Instant cut line representation
                                    val splitX = w / 2f
                                    drawLine(
                                        color = accentColor,
                                        start = Offset(0f, 18f),
                                        end = Offset(splitX - 4f, 18f),
                                        strokeWidth = 3f,
                                        cap = StrokeCap.Round
                                    )
                                    drawLine(
                                        color = dividerColor,
                                        start = Offset(splitX, 10f),
                                        end = Offset(splitX, h - 10f),
                                        strokeWidth = 2f
                                    )
                                    drawLine(
                                        color = secondaryAccent,
                                        start = Offset(splitX + 4f, 18f),
                                        end = Offset(w, 18f),
                                        strokeWidth = 3f,
                                        cap = StrokeCap.Round
                                    )
                                }
                            }

                            // Track labels inside canvas
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 12.dp, vertical = 8.dp)
                                    .align(Alignment.BottomCenter),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(
                                    text = "Track 1 (Fade Out)",
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        color = colors.primaryAccent,
                                        fontWeight = FontWeight.SemiBold
                                    )
                                )
                                Text(
                                    text = "Track 2 (Fade In)",
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        color = colors.secondaryAccent,
                                        fontWeight = FontWeight.SemiBold
                                    )
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(10.dp))
                        Text(
                            text = if (crossfadeEnabled && crossfadeSeconds > 0)
                                "As Track 1 ends, its volume gently fades while Track 2 ramps up over $crossfadeSeconds seconds for uninterrupted flow."
                            else
                                "Tracks play with standard boundary cuts or gapless transition.",
                            style = MaterialTheme.typography.bodySmall.copy(color = colors.secondaryText)
                        )
                    }
                }
            }

            // DJ Transition Profiles ('Club', 'Smooth', 'Sudden')
            item {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 8.dp)
                        .clip(RoundedCornerShape(20.dp))
                        .background(colors.surface)
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text(
                        text = "DJ TRANSITION PROFILES",
                        style = MaterialTheme.typography.labelMedium.copy(
                            fontWeight = FontWeight.Bold,
                            color = colors.primaryAccent,
                            letterSpacing = 1.sp
                        )
                    )
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        val profiles = listOf(
                            Triple("Club", 8, "Logarithmic Curve"),
                            Triple("Smooth", 5, "Cosine Fade"),
                            Triple("Sudden", 2, "Rapid Cut")
                        )
                        profiles.forEach { (name, sec, curveDesc) ->
                            val isSelected = crossfadeEnabled && crossfadeSeconds == sec
                            Surface(
                                modifier = Modifier
                                    .weight(1f)
                                    .clip(RoundedCornerShape(12.dp))
                                    .clickable {
                                        onCrossfadeToggle(true)
                                        onCrossfadeSecondsChange(sec)
                                    },
                                color = if (isSelected) colors.primaryAccent.copy(alpha = 0.25f) else colors.elevatedSurface,
                                border = if (isSelected) androidx.compose.foundation.BorderStroke(1.5.dp, colors.primaryAccent) else null
                            ) {
                                Column(
                                    modifier = Modifier.padding(10.dp),
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                    verticalArrangement = Arrangement.spacedBy(4.dp)
                                ) {
                                    Text(
                                        text = name,
                                        style = MaterialTheme.typography.bodyMedium.copy(
                                            fontWeight = FontWeight.Bold,
                                            color = if (isSelected) colors.primaryAccent else colors.primaryText
                                        )
                                    )
                                    Text(
                                        text = "${sec}s • $curveDesc",
                                        style = MaterialTheme.typography.labelSmall.copy(
                                            color = colors.mutedText,
                                            fontSize = 9.sp
                                        ),
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // Duration Slider & Preset Section
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = colors.elevatedSurface)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(18.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "OVERLAP DURATION",
                                style = MaterialTheme.typography.labelMedium.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = colors.mutedText,
                                    letterSpacing = 1.sp
                                )
                            )

                            Text(
                                text = if (crossfadeEnabled) "${crossfadeSeconds}s" else "Off",
                                style = MaterialTheme.typography.headlineSmall.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = if (crossfadeEnabled) colors.primaryAccent else colors.mutedText
                                )
                            )
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        // Duration Slider
                        Slider(
                            value = if (crossfadeEnabled) crossfadeSeconds.toFloat() else 0f,
                            onValueChange = { newValue ->
                                if (!crossfadeEnabled) {
                                    onCrossfadeToggle(true)
                                }
                                onCrossfadeSecondsChange(newValue.toInt())
                            },
                            valueRange = 0f..12f,
                            steps = 11,
                            enabled = crossfadeEnabled,
                            colors = SliderDefaults.colors(
                                thumbColor = colors.primaryAccent,
                                activeTrackColor = colors.primaryAccent,
                                inactiveTrackColor = colors.divider.copy(alpha = 0.5f),
                                activeTickColor = colors.mainBackground,
                                inactiveTickColor = colors.mutedText
                            ),
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("crossfade_slider")
                        )

                        // Slider labels (0s, 6s, 12s)
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("0s (Off)", style = MaterialTheme.typography.labelSmall.copy(color = colors.mutedText))
                            Text("6s", style = MaterialTheme.typography.labelSmall.copy(color = colors.mutedText))
                            Text("12s (Max)", style = MaterialTheme.typography.labelSmall.copy(color = colors.mutedText))
                        }

                        Spacer(modifier = Modifier.height(18.dp))

                        // Quick Presets Row
                        Text(
                            text = "Quick Presets",
                            style = MaterialTheme.typography.labelMedium.copy(
                                fontWeight = FontWeight.SemiBold,
                                color = colors.primaryText
                            )
                        )
                        Spacer(modifier = Modifier.height(8.dp))

                        val presets = listOf(
                            0 to "Off (0s)",
                            3 to "Smooth (3s)",
                            5 to "Standard (5s)",
                            8 to "DJ Blend (8s)",
                            12 to "Party (12s)"
                        )

                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .horizontalScroll(rememberScrollState()),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            presets.forEach { (sec, label) ->
                                val isSelected = crossfadeEnabled && crossfadeSeconds == sec
                                FilterChip(
                                    selected = isSelected,
                                    onClick = {
                                        if (sec == 0) {
                                            onCrossfadeSecondsChange(0)
                                            onCrossfadeToggle(false)
                                        } else {
                                            if (!crossfadeEnabled) {
                                                onCrossfadeToggle(true)
                                            }
                                            onCrossfadeSecondsChange(sec)
                                        }
                                    },
                                    label = {
                                        Text(
                                            text = label,
                                            style = MaterialTheme.typography.labelMedium.copy(
                                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                                            )
                                        )
                                    },
                                    leadingIcon = if (isSelected) {
                                        {
                                            Icon(
                                                imageVector = Icons.Default.Check,
                                                contentDescription = null,
                                                modifier = Modifier.size(16.dp)
                                            )
                                        }
                                    } else null,
                                    colors = FilterChipDefaults.filterChipColors(
                                        selectedContainerColor = colors.primaryAccent.copy(alpha = 0.2f),
                                        selectedLabelColor = colors.primaryAccent,
                                        selectedLeadingIconColor = colors.primaryAccent,
                                        containerColor = colors.surface,
                                        labelColor = colors.primaryText
                                    ),
                                    border = FilterChipDefaults.filterChipBorder(
                                        enabled = true,
                                        selected = isSelected,
                                        borderColor = if (isSelected) colors.primaryAccent else colors.divider
                                    ),
                                    modifier = Modifier.testTag("crossfade_preset_${sec}s")
                                )
                            }
                        }
                    }
                }
            }

            // Test Crossfade Live Audition Button
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = colors.elevatedSurface)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(18.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.VolumeUp,
                                contentDescription = null,
                                tint = colors.primaryAccent,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Audition Transition",
                                style = MaterialTheme.typography.titleMedium.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = colors.primaryText
                                )
                            )
                        }

                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Audition the volume attenuation curve with currently loaded track.",
                            style = MaterialTheme.typography.bodySmall.copy(color = colors.secondaryText)
                        )

                        Spacer(modifier = Modifier.height(14.dp))

                        Button(
                            onClick = {
                                if (!isPreviewing) {
                                    isPreviewing = true
                                    onPreviewCrossfade(if (crossfadeEnabled) crossfadeSeconds else 5)
                                    scope.launch {
                                        delay(((if (crossfadeEnabled) crossfadeSeconds else 5) * 1000L).coerceIn(1000L, 3000L))
                                        isPreviewing = false
                                    }
                                }
                            },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (isPreviewing) colors.secondaryAccent else colors.primaryAccent,
                                contentColor = colors.mainBackground
                            ),
                            shape = RoundedCornerShape(14.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(48.dp)
                                .testTag("crossfade_preview_button")
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = if (isPreviewing) Icons.Default.AutoAwesome else Icons.Default.PlayArrow,
                                    contentDescription = null,
                                    modifier = Modifier.size(20.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = if (isPreviewing) "Auditioning Crossfade Blend..." else "Test Crossfade Overlap",
                                    fontWeight = FontWeight.Bold,
                                    style = MaterialTheme.typography.bodyMedium
                                )
                            }
                        }
                    }
                }
            }

            // Related Transition Options: Gapless Playback & Automix
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = colors.elevatedSurface)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 18.dp, vertical = 14.dp)
                    ) {
                        Text(
                            text = "COMPLEMENTARY PLAYBACK MODES",
                            style = MaterialTheme.typography.labelMedium.copy(
                                fontWeight = FontWeight.Bold,
                                color = colors.mutedText,
                                letterSpacing = 1.sp
                            )
                        )

                        Spacer(modifier = Modifier.height(12.dp))

                        // Gapless Playback
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { onGaplessToggle(!gaplessEnabled) }
                                .padding(vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = "Gapless Playback",
                                    style = MaterialTheme.typography.bodyLarge.copy(
                                        fontWeight = FontWeight.SemiBold,
                                        color = colors.primaryText
                                    )
                                )
                                Text(
                                    text = "Eliminates silence between live or concept album tracks.",
                                    style = MaterialTheme.typography.bodySmall.copy(color = colors.secondaryText)
                                )
                            }

                            Switch(
                                checked = gaplessEnabled,
                                onCheckedChange = onGaplessToggle,
                                colors = SwitchDefaults.colors(
                                    checkedThumbColor = colors.mainBackground,
                                    checkedTrackColor = colors.primaryAccent,
                                    uncheckedThumbColor = colors.mutedText,
                                    uncheckedTrackColor = colors.surface
                                ),
                                modifier = Modifier.testTag("gapless_switch")
                            )
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        // Automix Indicator
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(
                                        text = "Automix Beat Blending",
                                        style = MaterialTheme.typography.bodyLarge.copy(
                                            fontWeight = FontWeight.SemiBold,
                                            color = colors.primaryText
                                        )
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Surface(
                                        shape = RoundedCornerShape(6.dp),
                                        color = colors.primaryAccent.copy(alpha = 0.15f)
                                    ) {
                                        Text(
                                            text = "4K ACTIVE",
                                            style = MaterialTheme.typography.labelSmall.copy(
                                                color = colors.primaryAccent,
                                                fontWeight = FontWeight.Bold
                                            ),
                                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                        )
                                    }
                                }
                                Text(
                                    text = "Intelligently aligns tempo and beat structure on crossfaded transitions.",
                                    style = MaterialTheme.typography.bodySmall.copy(color = colors.secondaryText)
                                )
                            }

                            Icon(
                                imageVector = Icons.Default.AutoAwesome,
                                contentDescription = null,
                                tint = colors.primaryAccent,
                                modifier = Modifier.size(22.dp)
                            )
                        }
                    }
                }
            }

            // Specs / Tips Card
            item {
                Surface(
                    shape = RoundedCornerShape(16.dp),
                    color = colors.surface,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(14.dp),
                        verticalAlignment = Alignment.Top
                    ) {
                        Icon(
                            imageVector = Icons.Default.Info,
                            contentDescription = null,
                            tint = colors.mutedText,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(
                            text = "Crossfade is rendered natively with 32-bit floating-point audio processing, maintaining 4K Lossless FLAC clarity and dynamic range throughout the entire overlap transition.",
                            style = MaterialTheme.typography.bodySmall.copy(
                                color = colors.mutedText,
                                lineHeight = 18.sp
                            )
                        )
                    }
                }
            }
        }
    }
}
