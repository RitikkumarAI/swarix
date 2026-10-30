package com.example.ui.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.platform.LocalView
import com.example.ui.theme.LocalSwarixColors
import kotlinx.coroutines.delay
import kotlin.math.sin

@Composable
fun VisualWaveformComponent(
    isPlaying: Boolean,
    bassHapticsEnabled: Boolean = true,
    modifier: Modifier = Modifier
) {
    val colors = LocalSwarixColors.current
    val view = LocalView.current
    val haptics = rememberHapticFeedback()

    val infiniteTransition = rememberInfiniteTransition(label = "waveform_anim")
    val phase by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 2f * Math.PI.toFloat(),
        animationSpec = infiniteRepeatable(
            animation = tween(1200, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "phase"
    )

    LaunchedEffect(isPlaying, bassHapticsEnabled) {
        if (isPlaying && bassHapticsEnabled) {
            while (true) {
                delay(180L)
                val simulatedAmplitude = (0.5f + 0.5f * sin(phase * 3f)).toFloat()
                if (simulatedAmplitude > 0.70f) {
                    HapticFeedbackHelper.triggerVolumeChange(view)
                }
            }
        }
    }

    Canvas(modifier = modifier) {
        val barCount = 32
        val totalWidth = size.width
        val totalHeight = size.height
        val barWidth = (totalWidth / (barCount * 1.8f)).coerceAtLeast(3f)
        val spacing = barWidth * 0.8f

        val brush = Brush.verticalGradient(
            colors = listOf(colors.primaryAccent, colors.secondaryAccent)
        )

        for (i in 0 until barCount) {
            val x = i * (barWidth + spacing) + spacing
            val frequencyFactor = sin((i.toFloat() / barCount) * Math.PI.toFloat() + phase)
            val dynamicAmplitude = if (isPlaying) {
                (0.2f + 0.8f * kotlin.math.abs(frequencyFactor)).coerceIn(0.1f, 1f)
            } else {
                0.15f
            }
            val barHeight = totalHeight * dynamicAmplitude

            drawRoundRect(
                brush = brush,
                topLeft = Offset(x, totalHeight - barHeight),
                size = Size(barWidth, barHeight),
                cornerRadius = CornerRadius(barWidth / 2f, barWidth / 2f)
            )
        }
    }
}
