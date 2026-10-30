package com.example.ui.components

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Fill
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.LocalSwarixColors

/**
 * Swarix Master Brand Logo Mark.
 * Faithfully reproduces the luxury dual-wave acoustic ribbon 'S' emblem
 * from the Swarix design references.
 */
@Composable
fun SwarixLogoMark(
    modifier: Modifier = Modifier,
    size: Dp = 56.dp,
    tint: Color? = null,
    enableGlow: Boolean = true
) {
    val colors = LocalSwarixColors.current
    val accentColor = tint ?: colors.primaryAccent
    val secondaryColor = colors.primaryText

    val infiniteTransition = rememberInfiniteTransition(label = "swarix_logo_glow")
    val glowPulse by infiniteTransition.animateFloat(
        initialValue = 0.85f,
        targetValue = 1.15f,
        animationSpec = infiniteRepeatable(
            animation = tween(2200, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "glow_pulse"
    )

    Box(
        modifier = modifier
            .size(size)
            .testTag("swarix_logo_mark"),
        contentAlignment = Alignment.Center
    ) {
        if (enableGlow) {
            Box(
                modifier = Modifier
                    .size(size * 1.35f)
                    .clip(CircleShape)
                    .background(
                        Brush.radialGradient(
                            listOf(
                                accentColor.copy(alpha = 0.22f * glowPulse),
                                Color.Transparent
                            )
                        )
                    )
            )
        }

        androidx.compose.foundation.Image(
            painter = androidx.compose.ui.res.painterResource(id = com.example.R.drawable.swarix_logo),
            contentDescription = "Swarix Logo",
            contentScale = androidx.compose.ui.layout.ContentScale.Fit,
            modifier = Modifier
                .size(size)
                .clip(RoundedCornerShape(size * 0.22f))
        )
    }
}

/**
 * Floating squircle card badge featuring the golden Swarix logo,
 * exactly as seen in the onboarding reference screen.
 */
@Composable
fun SwarixLogoBadge(
    modifier: Modifier = Modifier,
    size: Dp = 80.dp
) {
    val colors = LocalSwarixColors.current

    Box(
        modifier = modifier
            .size(size)
            .shadow(16.dp, RoundedCornerShape(22.dp), ambientColor = colors.primaryAccent.copy(alpha = 0.3f))
            .clip(RoundedCornerShape(22.dp))
            .background(
                Brush.linearGradient(
                    listOf(
                        colors.elevatedSurface,
                        colors.surface,
                        colors.mainBackground
                    )
                )
            ),
        contentAlignment = Alignment.Center
    ) {
        // Inner acoustic frame
        Box(
            modifier = Modifier
                .size(size * 0.9f)
                .clip(RoundedCornerShape(18.dp))
                .background(Color.Transparent),
            contentAlignment = Alignment.Center
        ) {
            SwarixLogoMark(
                size = size * 0.58f,
                enableGlow = true
            )
        }
    }
}

/**
 * Full branded header with Logo, Name, and Tagline.
 */
@Composable
fun SwarixBrandHeader(
    modifier: Modifier = Modifier,
    showTagline: Boolean = true
) {
    val colors = LocalSwarixColors.current

    Column(
        modifier = modifier.testTag("swarix_brand_header"),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        SwarixLogoMark(
            size = 80.dp,
            enableGlow = true
        )

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = "Swarix",
            style = MaterialTheme.typography.displayMedium.copy(
                fontWeight = FontWeight.Bold,
                color = colors.primaryText,
                letterSpacing = 1.5.sp
            )
        )

        if (showTagline) {
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = "Sound for your story.",
                style = MaterialTheme.typography.titleMedium.copy(
                    color = colors.primaryAccent,
                    fontWeight = FontWeight.Medium,
                    letterSpacing = 0.5.sp
                )
            )
        }
    }
}
