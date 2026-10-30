package com.example.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.PieChart
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.LocalSwarixColors
import kotlin.math.cos
import kotlin.math.sin

/**
 * Genre Distribution Radar Chart Card for Dashboard.
 * Visualizes user's listening habits across AI-detected sub-genres using a polygon radar web chart.
 */
@Composable
fun GenreRadarChartCard(modifier: Modifier = Modifier) {
    val colors = LocalSwarixColors.current
    val primaryAccent = colors.primaryAccent
    val secondaryAccent = colors.secondaryAccent

    val genres = listOf(
        "Acoustic" to 0.85f,
        "Synthwave" to 0.70f,
        "Chillhop" to 0.90f,
        "Bollywood" to 0.75f,
        "Rock" to 0.60f,
        "Indie Folk" to 0.80f
    )

    Surface(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp)
            .clip(RoundedCornerShape(20.dp))
            .testTag("genre_radar_chart_card"),
        color = colors.elevatedSurface
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(18.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
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
                        imageVector = Icons.Default.PieChart,
                        contentDescription = null,
                        tint = primaryAccent,
                        modifier = Modifier.size(20.dp)
                    )
                    Text(
                        text = "AI Genre Distribution Radar",
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Bold,
                            color = colors.primaryText
                        )
                    )
                }
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = primaryAccent.copy(alpha = 0.16f)
                ) {
                    Text(
                        text = "Evolving Tastes",
                        style = MaterialTheme.typography.labelSmall.copy(
                            color = primaryAccent,
                            fontWeight = FontWeight.Bold
                        ),
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                    )
                }
            }

            Text(
                text = "Listening breakdown across AI-enriched niche sub-genres over the last 30 days.",
                style = MaterialTheme.typography.bodySmall.copy(color = colors.secondaryText)
            )

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(180.dp)
                    .clip(RoundedCornerShape(14.dp))
                    .background(colors.mainBackground)
                    .padding(16.dp),
                contentAlignment = Alignment.Center
            ) {
                Canvas(modifier = Modifier.fillMaxSize()) {
                    val centerX = size.width / 2f
                    val centerY = size.height / 2f
                    val maxRadius = (minOf(size.width, size.height) / 2f) * 0.75f
                    val numPoints = genres.size

                    // Draw concentric radar grid webs
                    val webLevels = listOf(0.33f, 0.66f, 1.0f)
                    for (level in webLevels) {
                        val webPath = Path()
                        val levelRadius = maxRadius * level
                        for (i in 0 until numPoints) {
                            val angle = (Math.PI * 2 * i / numPoints) - (Math.PI / 2)
                            val x = centerX + levelRadius * cos(angle).toFloat()
                            val y = centerY + levelRadius * sin(angle).toFloat()
                            if (i == 0) webPath.moveTo(x, y) else webPath.lineTo(x, y)
                        }
                        webPath.close()
                        drawPath(
                            path = webPath,
                            color = colors.divider.copy(alpha = 0.4f),
                            style = Stroke(width = 1.dp.toPx())
                        )
                    }

                    // Draw radial axis lines
                    for (i in 0 until numPoints) {
                        val angle = (Math.PI * 2 * i / numPoints) - (Math.PI / 2)
                        val x = centerX + maxRadius * cos(angle).toFloat()
                        val y = centerY + maxRadius * sin(angle).toFloat()
                        drawLine(
                            color = colors.divider.copy(alpha = 0.4f),
                            start = Offset(centerX, centerY),
                            end = Offset(x, y),
                            strokeWidth = 1.dp.toPx()
                        )
                    }

                    // Draw User Data Radar Polygon
                    val dataPath = Path()
                    for (i in 0 until numPoints) {
                        val value = genres[i].second
                        val angle = (Math.PI * 2 * i / numPoints) - (Math.PI / 2)
                        val radius = maxRadius * value
                        val x = centerX + radius * cos(angle).toFloat()
                        val y = centerY + radius * sin(angle).toFloat()
                        if (i == 0) dataPath.moveTo(x, y) else dataPath.lineTo(x, y)
                    }
                    dataPath.close()

                    // Fill polygon with alpha
                    drawPath(
                        path = dataPath,
                        color = primaryAccent.copy(alpha = 0.35f)
                    )

                    // Stroke polygon outline
                    drawPath(
                        path = dataPath,
                        color = primaryAccent,
                        style = Stroke(width = 2.5.dp.toPx())
                    )
                }
            }

            // Legend Chips Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                genres.take(4).forEach { (name, value) ->
                    Surface(
                        shape = RoundedCornerShape(6.dp),
                        color = colors.surface
                    ) {
                        Text(
                            text = "$name ${(value * 100).toInt()}%",
                            style = MaterialTheme.typography.labelSmall.copy(
                                color = colors.secondaryText,
                                fontSize = 10.sp
                            ),
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }
                }
            }
        }
    }
}
