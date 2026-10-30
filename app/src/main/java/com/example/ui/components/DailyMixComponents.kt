package com.example.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.core.model.Song
import com.example.core.smartplaylist.DailyMix
import com.example.ui.theme.LocalSwarixColors

/**
 * Spotify Premium-style Daily Mix Card with AI mood badge and quick play button.
 */
@Composable
fun DailyMixCard(
    mix: DailyMix,
    onMixClick: (DailyMix) -> Unit,
    onPlayMix: (DailyMix) -> Unit,
    modifier: Modifier = Modifier
) {
    val colors = LocalSwarixColors.current

    Surface(
        modifier = modifier
            .width(168.dp)
            .height(230.dp)
            .shadow(12.dp, RoundedCornerShape(20.dp), ambientColor = mix.gradientStart.copy(alpha = 0.35f))
            .clip(RoundedCornerShape(20.dp))
            .border(1.dp, colors.divider.copy(alpha = 0.6f), RoundedCornerShape(20.dp))
            .bouncyClick { onMixClick(mix) }
            .testTag("daily_mix_card_${mix.mixNumber}"),
        color = colors.elevatedSurface
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            // Dual-tone dynamic background gradient
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        Brush.verticalGradient(
                            listOf(
                                mix.gradientStart.copy(alpha = 0.85f),
                                mix.gradientEnd.copy(alpha = 0.95f),
                                colors.mainBackground
                            )
                        )
                    )
            )

            // Subtle vinyl groove artwork watermark
            Canvas(
                modifier = Modifier
                    .size(140.dp)
                    .align(Alignment.TopEnd)
            ) {
                val center = Offset(size.width * 0.8f, size.height * 0.2f)
                listOf(0.9f, 0.7f, 0.5f, 0.3f).forEach { scale ->
                    drawCircle(
                        color = Color.White.copy(alpha = 0.08f),
                        radius = (size.minDimension / 2f) * scale,
                        center = center,
                        style = Stroke(width = 1.dp.toPx())
                    )
                }
            }

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(14.dp),
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                // Top: AI Sparkle Tag + Mood Pill
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.AutoAwesome,
                                contentDescription = "AI Curated",
                                tint = Color.White,
                                modifier = Modifier.size(13.dp)
                            )
                            Text(
                                text = "AI MIX",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    color = Color.White,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 9.sp,
                                    letterSpacing = 1.sp
                                )
                            )
                        }

                        // Mood pill
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(6.dp))
                                .background(Color.Black.copy(alpha = 0.35f))
                                .padding(horizontal = 6.dp, vertical = 2.dp)
                        ) {
                            Text(
                                text = mix.moodTag,
                                style = MaterialTheme.typography.labelSmall.copy(
                                    color = Color.White,
                                    fontWeight = FontWeight.SemiBold,
                                    fontSize = 10.sp
                                )
                            )
                        }
                    }

                    Text(
                        text = mix.title,
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Bold,
                            color = Color.White,
                            fontSize = 17.sp,
                            letterSpacing = 0.2.sp
                        )
                    )
                }

                // Center Image Collage Thumbnail
                Box(
                    modifier = Modifier
                        .size(76.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(Color.Black.copy(alpha = 0.25f))
                        .border(1.dp, Color.White.copy(alpha = 0.2f), RoundedCornerShape(12.dp))
                ) {
                    if (mix.coverUri.isNotEmpty()) {
                        AsyncImage(
                            model = mix.coverUri,
                            contentDescription = mix.title,
                            contentScale = ContentScale.Crop,
                            modifier = Modifier.fillMaxSize()
                        )
                    }
                }

                // Bottom: Subtitle & Quick Play Circle
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.Bottom
                ) {
                    Text(
                        text = mix.subtitle,
                        style = MaterialTheme.typography.bodySmall.copy(
                            color = Color.White.copy(alpha = 0.85f),
                            fontSize = 11.sp,
                            lineHeight = 15.sp
                        ),
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f).padding(end = 8.dp)
                    )

                    // Quick Play FAB
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .shadow(8.dp, CircleShape)
                            .clip(CircleShape)
                            .background(colors.primaryAccent)
                            .bouncyClick { onPlayMix(mix) }
                            .testTag("play_mix_${mix.mixNumber}"),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.PlayArrow,
                            contentDescription = "Play Mix",
                            tint = colors.mainBackground,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
            }
        }
    }
}

/**
 * Section container for Daily Mixes with AI badge and header.
 */
@Composable
fun DailyMixesSection(
    dailyMixes: List<DailyMix>,
    onMixClick: (DailyMix) -> Unit,
    onPlayMix: (DailyMix) -> Unit,
    onRegenerateClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    if (dailyMixes.isEmpty()) return
    val colors = LocalSwarixColors.current

    Column(modifier = modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 6.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Text(
                        text = "Your Daily Mixes",
                        style = MaterialTheme.typography.titleLarge.copy(
                            fontWeight = FontWeight.Bold,
                            color = colors.primaryText,
                            letterSpacing = 0.3.sp
                        )
                    )
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(6.dp))
                            .background(colors.primaryAccent.copy(alpha = 0.16f))
                            .padding(horizontal = 6.dp, vertical = 2.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(3.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.AutoAwesome,
                                contentDescription = null,
                                tint = colors.primaryAccent,
                                modifier = Modifier.size(11.dp)
                            )
                            Text(
                                text = "AI Curated",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    color = colors.primaryAccent,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 10.sp
                                )
                            )
                        }
                    }
                }
                Text(
                    text = "Tuned to your moods, genres & repeat plays",
                    style = MaterialTheme.typography.bodySmall.copy(
                        color = colors.secondaryText,
                        fontSize = 12.sp
                    )
                )
            }

            TextButton(
                onClick = onRegenerateClick,
                modifier = Modifier.testTag("btn_regenerate_mixes")
            ) {
                Text(
                    text = "Refresh",
                    color = colors.primaryAccent,
                    style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.SemiBold)
                )
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        LazyRow(
            contentPadding = PaddingValues(horizontal = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            items(dailyMixes) { mix ->
                DailyMixCard(
                    mix = mix,
                    onMixClick = onMixClick,
                    onPlayMix = onPlayMix
                )
            }
        }
    }
}
