package com.example.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.ui.theme.LocalSwarixColors

@Composable
fun TrendInsightsCard(modifier: Modifier = Modifier) {
    val colors = LocalSwarixColors.current
    val primaryAccent = colors.primaryAccent
    val secondaryAccent = colors.secondaryAccent

    Surface(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp)
            .clip(RoundedCornerShape(20.dp))
            .testTag("trend_insights_card"),
        color = colors.elevatedSurface
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(18.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
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
                        imageVector = Icons.Default.TrendingUp,
                        contentDescription = null,
                        tint = primaryAccent,
                        modifier = Modifier.size(20.dp)
                    )
                    Text(
                        text = "AI Mood Radio Trend Insights",
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
                        text = "+34% Activity",
                        style = MaterialTheme.typography.labelSmall.copy(
                            color = primaryAccent,
                            fontWeight = FontWeight.Bold
                        ),
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                    )
                }
            }

            Text(
                text = "Mood popularity correlates strongly with peak morning energy and evening focus sessions.",
                style = MaterialTheme.typography.bodySmall.copy(color = colors.secondaryText)
            )

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(110.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(colors.mainBackground)
                    .padding(12.dp)
            ) {
                Canvas(modifier = Modifier.fillMaxSize()) {
                    val w = size.width
                    val h = size.height

                    val pointsChill = listOf(
                        Offset(0f, h * 0.7f),
                        Offset(w * 0.25f, h * 0.5f),
                        Offset(w * 0.5f, h * 0.3f),
                        Offset(w * 0.75f, h * 0.6f),
                        Offset(w, h * 0.2f)
                    )

                    val pointsEnergy = listOf(
                        Offset(0f, h * 0.3f),
                        Offset(w * 0.25f, h * 0.2f),
                        Offset(w * 0.5f, h * 0.7f),
                        Offset(w * 0.75f, h * 0.4f),
                        Offset(w, h * 0.1f)
                    )

                    val pathChill = Path().apply {
                        moveTo(pointsChill.first().x, pointsChill.first().y)
                        for (p in pointsChill.drop(1)) {
                            lineTo(p.x, p.y)
                        }
                    }

                    val pathEnergy = Path().apply {
                        moveTo(pointsEnergy.first().x, pointsEnergy.first().y)
                        for (p in pointsEnergy.drop(1)) {
                            lineTo(p.x, p.y)
                        }
                    }

                    drawPath(
                        path = pathEnergy,
                        color = secondaryAccent,
                        style = Stroke(width = 3.dp.toPx())
                    )

                    drawPath(
                        path = pathChill,
                        color = primaryAccent,
                        style = Stroke(width = 3.dp.toPx())
                    )
                }
            }
        }
    }
}
