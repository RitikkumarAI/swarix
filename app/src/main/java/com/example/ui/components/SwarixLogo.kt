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

        Canvas(modifier = Modifier.size(size)) {
            val w = this.size.width
            val h = this.size.height
            val scaleX = w / 108f
            val scaleY = h / 108f

            // Upper wave ribbon of 'S'
            val topPath = Path().apply {
                moveTo(40f * scaleX, 49f * scaleY)
                cubicTo(37f * scaleX, 42f * scaleY, 42f * scaleX, 32f * scaleY, 52f * scaleX, 28f * scaleY)
                cubicTo(64f * scaleX, 24f * scaleY, 74f * scaleX, 27f * scaleY, 77f * scaleX, 35f * scaleY)
                cubicTo(79f * scaleX, 42f * scaleY, 75f * scaleX, 50f * scaleY, 67f * scaleX, 54f * scaleY)
                cubicTo(61f * scaleX, 57f * scaleY, 54f * scaleX, 60f * scaleY, 48f * scaleX, 63f * scaleY)
                lineTo(42f * scaleX, 57f * scaleY)
                cubicTo(49f * scaleX, 53f * scaleY, 58f * scaleX, 49f * scaleY, 63f * scaleX, 46f * scaleY)
                cubicTo(68f * scaleX, 43f * scaleY, 69f * scaleX, 38f * scaleY, 67f * scaleX, 35f * scaleY)
                cubicTo(65f * scaleX, 31f * scaleY, 59f * scaleX, 29f * scaleY, 52f * scaleX, 32f * scaleY)
                cubicTo(46f * scaleX, 35f * scaleY, 44f * scaleX, 40f * scaleY, 46f * scaleX, 45f * scaleY)
                close()
            }
            drawPath(
                path = topPath,
                brush = Brush.linearGradient(
                    colors = listOf(secondaryColor, accentColor, colors.secondaryAccent),
                    start = Offset(30f * scaleX, 20f * scaleY),
                    end = Offset(80f * scaleX, 60f * scaleY)
                ),
                style = Fill
            )

            // Lower wave ribbon of 'S'
            val bottomPath = Path().apply {
                moveTo(68f * scaleX, 59f * scaleY)
                cubicTo(71f * scaleX, 66f * scaleY, 66f * scaleX, 76f * scaleY, 56f * scaleX, 80f * scaleY)
                cubicTo(44f * scaleX, 84f * scaleY, 34f * scaleX, 81f * scaleY, 31f * scaleX, 73f * scaleY)
                cubicTo(29f * scaleX, 66f * scaleY, 33f * scaleX, 58f * scaleY, 41f * scaleX, 54f * scaleY)
                cubicTo(47f * scaleX, 51f * scaleY, 54f * scaleX, 48f * scaleY, 60f * scaleX, 45f * scaleY)
                lineTo(66f * scaleX, 51f * scaleY)
                cubicTo(59f * scaleX, 55f * scaleY, 50f * scaleX, 59f * scaleY, 45f * scaleX, 62f * scaleY)
                cubicTo(40f * scaleX, 65f * scaleY, 39f * scaleX, 70f * scaleY, 41f * scaleX, 73f * scaleY)
                cubicTo(43f * scaleX, 77f * scaleY, 49f * scaleX, 79f * scaleY, 56f * scaleX, 76f * scaleY)
                cubicTo(62f * scaleX, 73f * scaleY, 64f * scaleX, 68f * scaleY, 62f * scaleX, 63f * scaleY)
                close()
            }
            drawPath(
                path = bottomPath,
                brush = Brush.linearGradient(
                    colors = listOf(accentColor, colors.secondaryAccent, secondaryColor),
                    start = Offset(70f * scaleX, 50f * scaleY),
                    end = Offset(30f * scaleX, 85f * scaleY)
                ),
                style = Fill
            )

            // Connecting acoustic pulse line
            val centerPath = Path().apply {
                moveTo(46f * scaleX, 51f * scaleY)
                lineTo(60f * scaleX, 57f * scaleY)
                lineTo(59f * scaleX, 62f * scaleY)
                lineTo(48f * scaleX, 53f * scaleY)
                close()
            }
            drawPath(
                path = centerPath,
                color = secondaryColor.copy(alpha = 0.9f),
                style = Fill
            )

            // Satellite acoustic dots
            drawCircle(
                color = accentColor,
                radius = 2.4f * scaleX,
                center = Offset(77f * scaleX, 41f * scaleY)
            )
            drawCircle(
                color = colors.secondaryAccent,
                radius = 2.4f * scaleX,
                center = Offset(31f * scaleX, 67f * scaleY)
            )
        }
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
