package com.example.ui.components

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.core.graphics.drawable.toBitmap
import coil.ImageLoader
import coil.request.ImageRequest
import coil.request.SuccessResult
import com.example.core.model.Song
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlin.math.cos
import kotlin.math.sin

/**
 * Extracted color palette from song artwork.
 */
data class ArtworkPalette(
    val dominant: Color,
    val secondary: Color,
    val tertiary: Color,
    val surfaceDark: Color
) {
    companion object {
        val Default = ArtworkPalette(
            dominant = Color(0xFFD7BC83),    // Champagne Gold (Swarix accent)
            secondary = Color(0xFF3B5249),   // Ambient Pine
            tertiary = Color(0xFF283618),    // Deep Moss
            surfaceDark = Color(0xFF0B0D0C)  // Luxury Obsidian
        )
    }
}

/**
 * Engine that extracts dominant colors from album art bitmaps (remote URL, content:// or file://).
 */
object ArtworkColorExtractor {
    private val paletteCache = mutableMapOf<String, ArtworkPalette>()

    suspend fun extractPalette(context: Context, artworkUri: String?, songKey: String): ArtworkPalette {
        if (artworkUri.isNullOrBlank()) {
            return generateFallbackPalette(songKey)
        }

        paletteCache[artworkUri]?.let { return it }

        val bitmap = withContext(Dispatchers.IO) {
            try {
                if (artworkUri.startsWith("content://") || artworkUri.startsWith("file://")) {
                    context.contentResolver.openInputStream(Uri.parse(artworkUri))?.use { stream ->
                        val options = BitmapFactory.Options().apply {
                            inSampleSize = 4 // Downsample for fast memory-efficient analysis
                        }
                        BitmapFactory.decodeStream(stream, null, options)
                    }
                } else {
                    val loader = ImageLoader(context)
                    val request = ImageRequest.Builder(context)
                        .data(artworkUri)
                        .allowHardware(false) // Must allow software access to read pixels
                        .size(120, 120)
                        .build()
                    val result = loader.execute(request)
                    if (result is SuccessResult) {
                        result.drawable.toBitmap(120, 120, Bitmap.Config.ARGB_8888)
                    } else null
                }
            } catch (e: Exception) {
                null
            }
        }

        val palette = if (bitmap != null) {
            analyzeBitmapPixels(bitmap)
        } else {
            generateFallbackPalette(songKey)
        }

        paletteCache[artworkUri] = palette
        return palette
    }

    /**
     * Fast pixel analysis to find prominent hues with vibrant saturation and dark base.
     */
    private fun analyzeBitmapPixels(bitmap: Bitmap): ArtworkPalette {
        val width = bitmap.width
        val height = bitmap.height
        val step = 4 // Sample every 4th pixel for high speed
        val colorCounts = mutableMapOf<Int, Int>()

        for (x in 0 until width step step) {
            for (y in 0 until height step step) {
                val pixel = bitmap.getPixel(x, y)
                val alpha = (pixel shr 24) and 0xff
                if (alpha < 128) continue

                val r = (pixel shr 16) and 0xff
                val g = (pixel shr 8) and 0xff
                val b = pixel and 0xff

                // Convert to HSV for perceptual filtering
                val hsv = FloatArray(3)
                android.graphics.Color.RGBToHSV(r, g, b, hsv)
                val saturation = hsv[1]
                val brightness = hsv[2]

                // Skip washed-out white or near-total black
                if (brightness < 0.12f || brightness > 0.94f) continue
                if (saturation < 0.15f && brightness > 0.80f) continue

                // Quantize hue to 18-degree buckets (20 buckets total) to cluster similar hues
                val quantizedHue = ((hsv[0] / 18f).toInt() * 18).toFloat()
                val quantizedSat = ((saturation * 4f).toInt() / 4f).coerceIn(0.2f, 1f)
                val quantizedVal = ((brightness * 4f).toInt() / 4f).coerceIn(0.2f, 1f)

                val quantizedColor = android.graphics.Color.HSVToColor(floatArrayOf(quantizedHue, quantizedSat, quantizedVal))
                colorCounts[quantizedColor] = (colorCounts[quantizedColor] ?: 0) + 1
            }
        }

        if (colorCounts.isEmpty()) {
            return ArtworkPalette.Default
        }

        // Sort by frequency
        val sortedColors = colorCounts.entries
            .sortedByDescending { it.value }
            .map { Color(it.key) }

        val dominant = sortedColors.first()
        val secondary = sortedColors.getOrNull(1) ?: adjustHue(dominant, 45f)
        val tertiary = sortedColors.getOrNull(2) ?: adjustHue(dominant, -45f)

        // Generate deep obsidian/atmospheric dark background from dominant hue
        val domHsv = FloatArray(3)
        android.graphics.Color.colorToHSV(dominant.toArgb(), domHsv)
        val darkSurface = Color(android.graphics.Color.HSVToColor(floatArrayOf(domHsv[0], (domHsv[1] * 0.4f).coerceIn(0.1f, 0.35f), 0.08f)))

        return ArtworkPalette(
            dominant = dominant,
            secondary = secondary,
            tertiary = tertiary,
            surfaceDark = darkSurface
        )
    }

    private fun adjustHue(color: Color, deltaHue: Float): Color {
        val hsv = FloatArray(3)
        android.graphics.Color.colorToHSV(color.toArgb(), hsv)
        hsv[0] = (hsv[0] + deltaHue + 360f) % 360f
        return Color(android.graphics.Color.HSVToColor(hsv))
    }

    private fun generateFallbackPalette(songKey: String): ArtworkPalette {
        val hash = songKey.hashCode()
        val baseHue = ((hash and 0xFFFF) % 360).toFloat()
        val dominant = Color(android.graphics.Color.HSVToColor(floatArrayOf(baseHue, 0.65f, 0.85f)))
        val secondary = Color(android.graphics.Color.HSVToColor(floatArrayOf((baseHue + 40f) % 360f, 0.55f, 0.70f)))
        val tertiary = Color(android.graphics.Color.HSVToColor(floatArrayOf((baseHue + 160f) % 360f, 0.45f, 0.60f)))
        val surfaceDark = Color(android.graphics.Color.HSVToColor(floatArrayOf(baseHue, 0.25f, 0.07f)))

        return ArtworkPalette(dominant, secondary, tertiary, surfaceDark)
    }
}

/**
 * Hook to asynchronously extract and remember the artwork color palette for a song.
 */
@Composable
fun rememberArtworkPalette(song: Song?): ArtworkPalette {
    val context = LocalContext.current
    var palette by remember { mutableStateOf(ArtworkPalette.Default) }

    LaunchedEffect(song?.id, song?.artworkUri) {
        if (song != null) {
            val key = "${song.id}_${song.title}"
            val extracted = ArtworkColorExtractor.extractPalette(context, song.artworkUri, key)
            palette = extracted
        }
    }

    return palette
}

/**
 * Dynamic, fluid animated gradient background.
 * Uses the extracted colors to generate living, breathing auroral light meshes
 * that slowly drift and orbit behind the player controls.
 */
@Composable
fun DynamicAnimatedGradientBackground(
    palette: ArtworkPalette,
    isPlaying: Boolean = true,
    modifier: Modifier = Modifier
) {
    // Smooth crossfade transition between song palettes
    val animDominant by animateColorAsState(
        targetValue = palette.dominant,
        animationSpec = tween(1200, easing = FastOutSlowInEasing),
        label = "anim_dominant"
    )
    val animSecondary by animateColorAsState(
        targetValue = palette.secondary,
        animationSpec = tween(1200, easing = FastOutSlowInEasing),
        label = "anim_secondary"
    )
    val animTertiary by animateColorAsState(
        targetValue = palette.tertiary,
        animationSpec = tween(1200, easing = FastOutSlowInEasing),
        label = "anim_tertiary"
    )
    val animDarkSurface by animateColorAsState(
        targetValue = palette.surfaceDark,
        animationSpec = tween(1200, easing = FastOutSlowInEasing),
        label = "anim_dark_surface"
    )

    // Orbital drifting phase for alive motion
    val infiniteTransition = rememberInfiniteTransition(label = "ambient_drift")
    val phase by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 6.28318f,
        animationSpec = infiniteRepeatable(
            animation = tween(if (isPlaying) 18000 else 32000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "drift_phase"
    )

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(animDarkSurface)
            .testTag("dynamic_gradient_background")
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val w = size.width
            val h = size.height

            // Node 1: Dominant aura drifting near top-left
            val center1X = w * (0.35f + 0.18f * cos(phase))
            val center1Y = h * (0.28f + 0.12f * sin(phase))
            val radius1 = (w * 0.85f) * (0.92f + 0.08f * sin(phase * 1.5f))

            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(
                        animDominant.copy(alpha = 0.45f),
                        animDominant.copy(alpha = 0.22f),
                        Color.Transparent
                    ),
                    center = Offset(center1X, center1Y),
                    radius = radius1
                ),
                center = Offset(center1X, center1Y),
                radius = radius1
            )

            // Node 2: Secondary accent drifting near bottom-right
            val center2X = w * (0.65f - 0.20f * sin(phase * 0.8f))
            val center2Y = h * (0.60f + 0.15f * cos(phase * 0.8f))
            val radius2 = (w * 0.80f) * (0.90f + 0.10f * cos(phase * 1.2f))

            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(
                        animSecondary.copy(alpha = 0.38f),
                        animSecondary.copy(alpha = 0.18f),
                        Color.Transparent
                    ),
                    center = Offset(center2X, center2Y),
                    radius = radius2
                ),
                center = Offset(center2X, center2Y),
                radius = radius2
            )

            // Node 3: Tertiary tone gently swaying centrally
            val center3X = w * (0.50f + 0.12f * sin(phase * 1.2f))
            val center3Y = h * (0.45f - 0.14f * cos(phase * 1.1f))
            val radius3 = w * 0.65f

            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(
                        animTertiary.copy(alpha = 0.30f),
                        Color.Transparent
                    ),
                    center = Offset(center3X, center3Y),
                    radius = radius3
                ),
                center = Offset(center3X, center3Y),
                radius = radius3
            )
        }

        // Luxury atmospheric overlay with vertical vignette for pristine text contrast
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            animDarkSurface.copy(alpha = 0.55f),
                            animDarkSurface.copy(alpha = 0.40f),
                            animDarkSurface.copy(alpha = 0.75f),
                            animDarkSurface.copy(alpha = 0.94f)
                        )
                    )
                )
        )
    }
}
