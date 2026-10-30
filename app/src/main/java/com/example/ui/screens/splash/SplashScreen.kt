package com.example.ui.screens.splash

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.ui.theme.LocalSwarixColors
import kotlinx.coroutines.delay

/**
 * Exact cinematic Splash & Loading Screen from Swarix design reference.
 * Features the golden dawn mountain landscape backdrop, glowing Swarix 'S' monogram,
 * animated audio waveform indicator, and elegant typography.
 */
@Composable
fun SplashScreen(
    onTimeout: () -> Unit,
    modifier: Modifier = Modifier
) {
    val colors = LocalSwarixColors.current

    val logoScale = remember { Animatable(0.75f) }
    val logoAlpha = remember { Animatable(0f) }
    val contentAlpha = remember { Animatable(0f) }
    val progressAnim = remember { Animatable(0f) }

    val infiniteTransition = rememberInfiniteTransition(label = "pulse_glow")
    val pulseGlow by infiniteTransition.animateFloat(
        initialValue = 0.85f,
        targetValue = 1.15f,
        animationSpec = infiniteRepeatable(
            animation = tween(1800, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulse_scale"
    )

    LaunchedEffect(Unit) {
        logoScale.animateTo(
            targetValue = 1f,
            animationSpec = tween(900, easing = FastOutSlowInEasing)
        )
        logoAlpha.animateTo(
            targetValue = 1f,
            animationSpec = tween(600, easing = LinearEasing)
        )
        contentAlpha.animateTo(
            targetValue = 1f,
            animationSpec = tween(700, easing = LinearEasing)
        )
        progressAnim.animateTo(
            targetValue = 1f,
            animationSpec = tween(1500, easing = FastOutSlowInEasing)
        )
        delay(350)
        onTimeout()
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(colors.mainBackground)
            .clickable(onClick = onTimeout)
            .testTag("swarix_splash_screen")
    ) {
        // Misty dawn mountain landscape background
        AsyncImage(
            model = "https://images.unsplash.com/photo-1506744038136-46273834b3fb?w=1200&auto=format&fit=crop&q=80",
            contentDescription = null,
            contentScale = ContentScale.Crop,
            modifier = Modifier
                .fillMaxSize()
                .graphicsLayer { alpha = 0.35f }
        )

        // Subtle gradient overlay for readability & contrast
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        listOf(
                            colors.mainBackground.copy(alpha = 0.70f),
                            colors.mainBackground.copy(alpha = 0.85f),
                            colors.mainBackground.copy(alpha = 0.98f)
                        )
                    )
                )
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .systemBarsPadding()
                .padding(horizontal = 24.dp, vertical = 32.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Spacer(modifier = Modifier.height(40.dp))

            // Center Monogram & Title
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.graphicsLayer {
                    scaleX = logoScale.value
                    scaleY = logoScale.value
                    alpha = logoAlpha.value
                }
            ) {
                // Swarix Golden Monogram Emblem
                com.example.ui.components.SwarixLogoMark(
                    size = 92.dp,
                    enableGlow = true
                )

                Spacer(modifier = Modifier.height(24.dp))

                // App Brand Name
                Text(
                    text = "Swarix",
                    style = MaterialTheme.typography.displayMedium.copy(
                        fontWeight = FontWeight.Bold,
                        color = colors.primaryText,
                        letterSpacing = 1.5.sp
                    )
                )

                Spacer(modifier = Modifier.height(8.dp))

                // Brand Tagline
                Text(
                    text = "Sound for your story.",
                    style = MaterialTheme.typography.titleMedium.copy(
                        color = colors.primaryAccent,
                        fontWeight = FontWeight.Medium,
                        letterSpacing = 0.5.sp
                    )
                )

                Spacer(modifier = Modifier.height(6.dp))

                Text(
                    text = "Your Music. Deeper.",
                    style = MaterialTheme.typography.bodyMedium.copy(
                        color = colors.secondaryText,
                        letterSpacing = 0.3.sp
                    )
                )
            }

            // Bottom Waveform Progress & Tagline
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier
                    .fillMaxWidth()
                    .graphicsLayer { alpha = contentAlpha.value }
            ) {
                // Animated Loading Waveform
                Row(
                    modifier = Modifier.height(24.dp),
                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    val barCount = 18
                    for (i in 0 until barCount) {
                        val barHeight = (8 + (14 * kotlin.math.sin((i * 0.5f) + progressAnim.value * 6.28f).coerceAtLeast(0.1f))).dp
                        Box(
                            modifier = Modifier
                                .width(3.dp)
                                .height(barHeight)
                                .clip(RoundedCornerShape(1.5.dp))
                                .background(
                                    if (i.toFloat() / barCount <= progressAnim.value) {
                                        colors.primaryAccent
                                    } else {
                                        colors.divider
                                    }
                                )
                        )
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))

                Text(
                    text = "Explore  •  Create  •  Feel",
                    style = MaterialTheme.typography.labelMedium.copy(
                        color = colors.mutedText,
                        letterSpacing = 2.sp,
                        fontWeight = FontWeight.Medium
                    ),
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Bottom home indicator bar capsule
                Box(
                    modifier = Modifier
                        .width(72.dp)
                        .height(4.dp)
                        .clip(RoundedCornerShape(2.dp))
                        .background(colors.mutedText.copy(alpha = 0.4f))
                )
            }
        }
    }
}
