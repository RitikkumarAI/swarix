package com.example.ui.screens.onboarding

import android.Manifest
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
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
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.ui.components.bouncyClick
import com.example.ui.theme.LocalSwarixColors

data class MoodOption(val name: String, val subtitle: String, val imageUrl: String)

/**
 * Exact Startup & Onboarding screens from Swarix design reference:
 * Step 0: "Music that understands you. Explore. Feel. Create. All in Swarix." with layered artwork, dot indicators, and circular next button.
 * Step 1: "What music moves you? Choose a few so we can personalize your experience." with 3x3 mood grid & Continue button.
 */
@Composable
fun OnboardingScreen(
    onFinishOnboarding: (selectedMoods: Set<String>) -> Unit,
    modifier: Modifier = Modifier
) {
    var step by remember { mutableIntStateOf(0) }
    val colors = LocalSwarixColors.current

    val moods = remember {
        listOf(
            MoodOption("Romance", "Love & Ballads", "https://images.unsplash.com/photo-1518495973542-4542c06a5843?w=500&auto=format&fit=crop&q=80"),
            MoodOption("Chill", "Calm & Ambient", "https://images.unsplash.com/photo-1507525428034-b723cf961d3e?w=500&auto=format&fit=crop&q=80"),
            MoodOption("Workout", "Energy & Beats", "https://images.unsplash.com/photo-1517838277536-f5f99be501cd?w=500&auto=format&fit=crop&q=80"),
            MoodOption("Devotional", "Peace & Solace", "https://images.unsplash.com/photo-1506744038136-46273834b3fb?w=500&auto=format&fit=crop&q=80"),
            MoodOption("Party", "Dance & Club", "https://images.unsplash.com/photo-1492684223066-81342ee5ff30?w=500&auto=format&fit=crop&q=80"),
            MoodOption("Focus", "Deep Work & Study", "https://images.unsplash.com/photo-1434030216411-0b793f4b4173?w=500&auto=format&fit=crop&q=80"),
            MoodOption("Sad", "Healing Melodies", "https://images.unsplash.com/photo-1516589178581-6cd7833ae3b2?w=500&auto=format&fit=crop&q=80"),
            MoodOption("Indie", "Raw & Acoustic", "https://images.unsplash.com/photo-1511671782779-c97d3d27a1d4?w=500&auto=format&fit=crop&q=80"),
            MoodOption("Old Hits", "Timeless Classics", "https://images.unsplash.com/photo-1539185441755-769473a23570?w=500&auto=format&fit=crop&q=80")
        )
    }
    var selectedMoods by remember { mutableStateOf(setOf("Romance", "Chill", "Focus")) }

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions()
    ) {
        onFinishOnboarding(selectedMoods)
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(colors.mainBackground)
            .systemBarsPadding()
    ) {
        AnimatedContent(
            targetState = step,
            transitionSpec = { fadeIn(tween(400)) togetherWith fadeOut(tween(300)) },
            label = "onboarding_step"
        ) { targetStep ->
            when (targetStep) {
                0 -> {
                    // Step 0: Welcome Screen from Reference Image 1 Screen 2
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(horizontal = 24.dp, vertical = 16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.SpaceBetween
                    ) {
                        // Top Branding
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(top = 8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            com.example.ui.components.SwarixLogoMark(
                                size = 36.dp,
                                enableGlow = false
                            )
                            Spacer(modifier = Modifier.width(10.dp))
                            Text(
                                text = "Swarix",
                                style = MaterialTheme.typography.titleLarge.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = colors.primaryText,
                                    letterSpacing = 0.5.sp
                                )
                            )
                        }

                        // Hero Visual with Layered Angled Cards (as in reference image)
                        Box(
                            modifier = Modifier
                                .size(310.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            // Back Layered Card (Turntable vinyl)
                            Box(
                                modifier = Modifier
                                    .size(230.dp)
                                    .rotate(-10f)
                                    .clip(RoundedCornerShape(28.dp))
                                    .background(colors.surface)
                                    .border(1.dp, colors.divider, RoundedCornerShape(28.dp))
                            ) {
                                AsyncImage(
                                    model = "https://images.unsplash.com/photo-1539185441755-769473a23570?w=500&auto=format&fit=crop&q=80",
                                    contentDescription = null,
                                    contentScale = ContentScale.Crop,
                                    modifier = Modifier.fillMaxSize()
                                )
                            }

                            // Front Main Card (Listener with headphones)
                            Box(
                                modifier = Modifier
                                    .size(245.dp)
                                    .rotate(5f)
                                    .clip(RoundedCornerShape(28.dp))
                                    .background(colors.elevatedSurface)
                                    .border(1.5.dp, colors.primaryAccent.copy(alpha = 0.6f), RoundedCornerShape(28.dp))
                            ) {
                                AsyncImage(
                                    model = "https://images.unsplash.com/photo-1511671782779-c97d3d27a1d4?w=600&auto=format&fit=crop&q=80",
                                    contentDescription = "Swarix Music",
                                    contentScale = ContentScale.Crop,
                                    modifier = Modifier.fillMaxSize()
                                )
                                Box(
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .background(
                                            Brush.verticalGradient(
                                                listOf(Color.Transparent, Color.Black.copy(alpha = 0.65f))
                                            )
                                        )
                                )
                                // Golden Swarix floating emblem badge
                                com.example.ui.components.SwarixLogoBadge(
                                    size = 48.dp,
                                    modifier = Modifier
                                        .padding(14.dp)
                                        .align(Alignment.BottomEnd)
                                )
                            }
                        }

                        // Title & Subtitle from Reference
                        Column(
                            horizontalAlignment = Alignment.Start,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                text = "Music that\nunderstands you.",
                                style = MaterialTheme.typography.displaySmall.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = colors.primaryText,
                                    lineHeight = 42.sp
                                )
                            )
                            Spacer(modifier = Modifier.height(10.dp))
                            Text(
                                text = "Explore. Feel. Create. All in Swarix.",
                                style = MaterialTheme.typography.bodyLarge.copy(
                                    color = colors.secondaryText,
                                    fontSize = 17.sp
                                )
                            )
                        }

                        // Bottom Controls Row: Skip | Dot Pager | Circular Next Arrow
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 12.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            TextButton(
                                onClick = {
                                    requestPermissionsAndFinish(permissionLauncher) {
                                        onFinishOnboarding(selectedMoods)
                                    }
                                }
                            ) {
                                Text(
                                    text = "Skip",
                                    style = MaterialTheme.typography.bodyLarge.copy(
                                        color = colors.mutedText,
                                        fontWeight = FontWeight.Medium
                                    )
                                )
                            }

                            // Dot Pager Indicator (• • • •)
                            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                Box(
                                    modifier = Modifier
                                        .size(width = 20.dp, height = 6.dp)
                                        .clip(RoundedCornerShape(3.dp))
                                        .background(colors.primaryAccent)
                                )
                                Box(
                                    modifier = Modifier
                                        .size(6.dp)
                                        .clip(CircleShape)
                                        .background(colors.divider)
                                )
                                Box(
                                    modifier = Modifier
                                        .size(6.dp)
                                        .clip(CircleShape)
                                        .background(colors.divider)
                                )
                            }

                            // Circular Next Arrow Button
                            Box(
                                modifier = Modifier
                                    .size(56.dp)
                                    .clip(CircleShape)
                                    .background(colors.primaryAccent)
                                    .bouncyClick { step = 1 }
                                    .testTag("onboarding_next_arrow"),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                                    contentDescription = "Next",
                                    tint = colors.mainBackground,
                                    modifier = Modifier.size(26.dp)
                                )
                            }
                        }
                    }
                }
                1 -> {
                    // Step 1: Mood & Genre Selection Grid from Reference Image 1 Screen 3
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(horizontal = 20.dp, vertical = 16.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "What music\nmoves you?",
                                style = MaterialTheme.typography.headlineMedium.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = colors.primaryText,
                                    lineHeight = 36.sp
                                )
                            )
                            TextButton(
                                onClick = {
                                    requestPermissionsAndFinish(permissionLauncher) {
                                        onFinishOnboarding(selectedMoods)
                                    }
                                }
                            ) {
                                Text("Skip", color = colors.mutedText)
                            }
                        }

                        Text(
                            text = "Choose a few so we can personalize your experience.",
                            style = MaterialTheme.typography.bodyMedium.copy(color = colors.secondaryText),
                            modifier = Modifier.padding(top = 6.dp, bottom = 18.dp)
                        )

                        LazyVerticalGrid(
                            columns = GridCells.Fixed(3),
                            modifier = Modifier
                                .weight(1f)
                                .fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(10.dp),
                            verticalArrangement = Arrangement.spacedBy(10.dp),
                            contentPadding = PaddingValues(bottom = 14.dp)
                        ) {
                            items(moods) { mood ->
                                val isSelected = selectedMoods.contains(mood.name)
                                Surface(
                                    modifier = Modifier
                                        .height(115.dp)
                                        .clip(RoundedCornerShape(16.dp))
                                        .border(
                                            width = if (isSelected) 2.dp else 1.dp,
                                            color = if (isSelected) colors.primaryAccent else colors.divider,
                                            shape = RoundedCornerShape(16.dp)
                                        )
                                        .bouncyClick {
                                            selectedMoods = if (isSelected) {
                                                selectedMoods - mood.name
                                            } else {
                                                selectedMoods + mood.name
                                            }
                                        }
                                        .testTag("mood_tile_${mood.name.lowercase()}"),
                                    color = colors.elevatedSurface
                                ) {
                                    Box {
                                        AsyncImage(
                                            model = mood.imageUrl,
                                            contentDescription = mood.name,
                                            contentScale = ContentScale.Crop,
                                            modifier = Modifier.fillMaxSize()
                                        )
                                        Box(
                                            modifier = Modifier
                                                .fillMaxSize()
                                                .background(
                                                    Brush.verticalGradient(
                                                        listOf(
                                                            Color.Black.copy(alpha = 0.25f),
                                                            Color.Black.copy(alpha = 0.85f)
                                                        )
                                                    )
                                                )
                                        )
                                        if (isSelected) {
                                            Box(
                                                modifier = Modifier
                                                    .padding(6.dp)
                                                    .size(20.dp)
                                                    .clip(CircleShape)
                                                    .background(colors.primaryAccent)
                                                    .align(Alignment.TopEnd),
                                                contentAlignment = Alignment.Center
                                            ) {
                                                Icon(
                                                    Icons.Default.Check,
                                                    contentDescription = "Selected",
                                                    tint = colors.mainBackground,
                                                    modifier = Modifier.size(14.dp)
                                                )
                                            }
                                        }
                                        Text(
                                            text = mood.name,
                                            style = MaterialTheme.typography.labelLarge.copy(
                                                fontWeight = FontWeight.Bold,
                                                color = Color.White
                                            ),
                                            modifier = Modifier
                                                .align(Alignment.BottomStart)
                                                .padding(8.dp)
                                        )
                                    }
                                }
                            }
                        }

                        Button(
                            onClick = {
                                requestPermissionsAndFinish(permissionLauncher) {
                                    onFinishOnboarding(selectedMoods)
                                }
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(56.dp)
                                .testTag("onboarding_continue"),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = colors.primaryAccent,
                                contentColor = colors.mainBackground
                            ),
                            shape = RoundedCornerShape(18.dp)
                        ) {
                            Text(
                                text = "Continue",
                                style = MaterialTheme.typography.titleMedium.copy(
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 18.sp
                                )
                            )
                        }
                    }
                }
            }
        }
    }
}

private fun requestPermissionsAndFinish(
    permissionLauncher: androidx.activity.result.ActivityResultLauncher<Array<String>>,
    onFinish: () -> Unit
) {
    val permissions = mutableListOf<String>()
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
        permissions.add(Manifest.permission.READ_MEDIA_AUDIO)
        permissions.add(Manifest.permission.POST_NOTIFICATIONS)
    } else {
        permissions.add(Manifest.permission.READ_EXTERNAL_STORAGE)
    }
    try {
        permissionLauncher.launch(permissions.toTypedArray())
    } catch (e: Exception) {
        onFinish()
    }
}
