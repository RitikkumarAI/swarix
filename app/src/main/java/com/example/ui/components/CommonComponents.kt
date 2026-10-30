package com.example.ui.components

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.LibraryMusic
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.SkipNext
import androidx.compose.material.icons.filled.DownloadDone
import androidx.compose.material.icons.filled.Wifi
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.ui.composed
import androidx.compose.ui.graphics.graphicsLayer
import coil.compose.AsyncImage
import com.example.core.model.Artist
import com.example.core.model.FriendActivity
import com.example.core.model.Song
import com.example.ui.theme.LocalSwarixColors
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.sin

@Composable
fun SwarixTopBar(
    title: String,
    onBackClick: (() -> Unit)? = null,
    actions: @Composable (() -> Unit)? = null
) {
    val swarixColors = LocalSwarixColors.current

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(56.dp)
            .padding(horizontal = 16.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            if (onBackClick != null) {
                IconButton(
                    onClick = onBackClick,
                    modifier = Modifier.testTag("topbar_back_button")
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Back",
                        tint = swarixColors.primaryText
                    )
                }
                Spacer(modifier = Modifier.width(8.dp))
            } else {
                // Stylized Swarix S emblem
                Box(
                    modifier = Modifier
                        .size(32.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(
                            Brush.linearGradient(
                                listOf(swarixColors.primaryAccent, swarixColors.secondaryAccent)
                            )
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "S",
                        color = swarixColors.mainBackground,
                        fontWeight = FontWeight.Bold,
                        fontSize = 20.sp
                    )
                }
                Spacer(modifier = Modifier.width(10.dp))
            }

            Text(
                text = title,
                style = MaterialTheme.typography.titleLarge.copy(
                    fontWeight = FontWeight.Bold,
                    color = swarixColors.primaryText
                ),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }

        if (actions != null) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                actions()
            }
        }
    }
}

enum class NavItem(val label: String, val icon: ImageVector) {
    HOME("Home", Icons.Default.Home),
    SEARCH("Search", Icons.Default.Search),
    AI("Swarix AI", Icons.Default.AutoAwesome),
    LIBRARY("Your Library", Icons.Default.LibraryMusic),
    SETTINGS("Settings", Icons.Default.Person)
}

@Composable
fun SwarixBottomNavigation(
    selectedItem: NavItem,
    onItemSelected: (NavItem) -> Unit,
    modifier: Modifier = Modifier
) {
    val colors = LocalSwarixColors.current

    NavigationBar(
        modifier = modifier.navigationBarsPadding(),
        containerColor = colors.mainBackground,
        tonalElevation = 8.dp
    ) {
        NavItem.entries.forEach { item ->
            val isSelected = item == selectedItem
            NavigationBarItem(
                selected = isSelected,
                onClick = { onItemSelected(item) },
                icon = {
                    Icon(
                        imageVector = item.icon,
                        contentDescription = item.label,
                        modifier = Modifier.size(24.dp)
                    )
                },
                label = {
                    Text(
                        text = item.label,
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                    )
                },
                colors = NavigationBarItemDefaults.colors(
                    selectedIconColor = colors.mainBackground,
                    selectedTextColor = colors.primaryAccent,
                    indicatorColor = colors.primaryAccent,
                    unselectedIconColor = colors.mutedText,
                    unselectedTextColor = colors.mutedText
                ),
                modifier = Modifier.testTag("nav_${item.name.lowercase()}")
            )
        }
    }
}

/**
 * Subtle status indicator component showing whether the current song is locally cached
 * or streaming, using a refined icon with an expressive bouncy spring entry animation.
 */
@Composable
fun PlaybackSourceStatusIndicator(
    isLocallyCached: Boolean,
    modifier: Modifier = Modifier,
    songId: String = "",
    compact: Boolean = false,
    showLabel: Boolean = !compact,
    onClick: (() -> Unit)? = null
) {
    val colors = LocalSwarixColors.current
    var showInfoDialog by remember { mutableStateOf(false) }

    // Expressive bouncy spring entry animation triggered on track or source change
    val bounceScale = remember { Animatable(0.35f) }

    LaunchedEffect(key1 = songId, key2 = isLocallyCached) {
        bounceScale.snapTo(0.35f)
        bounceScale.animateTo(
            targetValue = 1f,
            animationSpec = spring(
                dampingRatio = Spring.DampingRatioMediumBouncy,
                stiffness = Spring.StiffnessLow
            )
        )
    }

    val icon = if (isLocallyCached) Icons.Default.DownloadDone else Icons.Default.Wifi
    val label = if (isLocallyCached) "Cached" else "Streaming"
    val accentColor = if (isLocallyCached) colors.success else colors.primaryAccent
    val contentDesc = if (isLocallyCached) "Locally cached track" else "Streaming network track"

    Box(
        modifier = modifier
            .graphicsLayer {
                scaleX = bounceScale.value
                scaleY = bounceScale.value
            }
            .clip(RoundedCornerShape(20.dp))
            .background(accentColor.copy(alpha = 0.14f))
            .clickable {
                if (onClick != null) {
                    onClick()
                } else {
                    showInfoDialog = true
                }
            }
            .padding(
                horizontal = if (compact) 6.dp else 8.dp,
                vertical = if (compact) 2.dp else 4.dp
            )
            .testTag(if (isLocallyCached) "cached_indicator_${songId}" else "audio_source_status_indicator"),
        contentAlignment = Alignment.Center
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Icon(
                imageVector = icon,
                contentDescription = contentDesc,
                tint = accentColor,
                modifier = Modifier.size(if (compact) 12.dp else 14.dp)
            )

            if (showLabel) {
                Text(
                    text = label,
                    style = MaterialTheme.typography.labelSmall.copy(
                        fontSize = if (compact) 10.sp else 11.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = accentColor,
                        letterSpacing = 0.3.sp
                    )
                )
            }
        }
    }

    if (showInfoDialog) {
        AlertDialog(
            onDismissRequest = { showInfoDialog = false },
            title = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = accentColor,
                        modifier = Modifier.size(22.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = if (isLocallyCached) "Local Cache Audio" else "Streaming Audio",
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Bold,
                            color = colors.primaryText
                        )
                    )
                }
            },
            text = {
                Text(
                    text = if (isLocallyCached) {
                        "This audio track is stored locally on this device. It plays instantly with lossless audio quality and zero data usage."
                    } else {
                        "This track is being streamed in high quality from the Swarix music catalog over your internet connection."
                    },
                    style = MaterialTheme.typography.bodyMedium.copy(
                        color = colors.secondaryText
                    )
                )
            },
            confirmButton = {
                TextButton(onClick = { showInfoDialog = false }) {
                    Text("Got it", color = colors.primaryAccent)
                }
            },
            containerColor = colors.elevatedSurface,
            shape = RoundedCornerShape(18.dp)
        )
    }
}

@Composable
fun MiniPlayer(
    song: Song?,
    nextSong: Song?, // Add nextSong
    isPlaying: Boolean,
    progressMs: Long,
    durationMs: Long,
    onPlayPauseClick: () -> Unit,
    onNextClick: () -> Unit,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    if (song == null) return
    val colors = LocalSwarixColors.current
    val haptics = rememberHapticFeedback()
    val view = LocalView.current
    val progress = if (durationMs > 0) (progressMs.toFloat() / durationMs.toFloat()).coerceIn(0f, 1f) else 0f
    
    var showPreview by remember { mutableStateOf(false) }

    Column {
        if (showPreview && nextSong != null) {
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp)
                    .clip(RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp)),
                color = colors.elevatedSurface,
                tonalElevation = 4.dp
            ) {
                Row(
                    modifier = Modifier.padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Up Next: ", style = MaterialTheme.typography.labelMedium.copy(color = colors.secondaryText))
                    Text(nextSong.title, style = MaterialTheme.typography.labelMedium.copy(color = colors.primaryText, fontWeight = FontWeight.Bold))
                }
            }
        }

        Surface(
            modifier = modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 6.dp)
                .clip(RoundedCornerShape(16.dp))
                .pointerInput(Unit) {
                    detectDragGestures(
                        onDrag = { change, dragAmount ->
                            change.consume()
                            if (dragAmount.y < -10) {
                                showPreview = true
                            } else if (dragAmount.y > 10) {
                                showPreview = false
                            } else if (Math.abs(dragAmount.x) > 20) {
                                if (dragAmount.x > 0) onNextClick() else onNextClick()
                            }
                        }
                    )
                }
                .clickable(onClick = onClick)
                .testTag("mini_player"),
            color = colors.elevatedSurface,
            tonalElevation = 6.dp
        ) {
            Column {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 12.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Album artwork thumbnail
                    val artScale by animateFloatAsState(targetValue = 1f, animationSpec = tween(500), label = "art_scale")
                    Box(
                        modifier = Modifier
                            .size(46.dp)
                            .graphicsLayer {
                                scaleX = artScale
                                scaleY = artScale
                            }
                            .clip(RoundedCornerShape(10.dp))
                            .background(colors.surface)
                    ) {
                        if (song.artworkUri.isNotEmpty()) {
                            AsyncImage(
                                model = song.artworkUri,
                                contentDescription = song.title,
                                contentScale = ContentScale.Crop,
                                modifier = Modifier.fillMaxSize()
                            )
                        } else {
                            Box(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .background(colors.primaryAccent),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = song.title.take(1).uppercase(),
                                    fontWeight = FontWeight.Bold,
                                    color = colors.mainBackground
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.width(12.dp))

                    // Song Info
                    Column(
                        modifier = Modifier.weight(1f)
                    ) {
                        Text(
                            text = song.title,
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.SemiBold,
                                color = colors.primaryText
                            ),
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Text(
                                text = song.artist,
                                style = MaterialTheme.typography.bodyMedium.copy(
                                    color = colors.secondaryText
                                ),
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                                modifier = Modifier.weight(1f, fill = false)
                            )
                            PlaybackSourceStatusIndicator(
                                isLocallyCached = song.isLocal || song.contentUri.startsWith("content://") || song.contentUri.startsWith("file://"),
                                songId = song.id,
                                compact = true,
                                showLabel = false
                            )
                        }
                    }

                    // Controls
                    IconButton(
                        onClick = {
                            HapticFeedbackHelper.triggerClick(view)
                            onPlayPauseClick()
                        },
                        modifier = Modifier
                            .size(38.dp)
                            .testTag("mini_player_play_pause")
                    ) {
                        Icon(
                            imageVector = if (isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                            contentDescription = if (isPlaying) "Pause" else "Play",
                            tint = colors.primaryText,
                            modifier = Modifier.size(26.dp)
                        )
                    }

                    IconButton(
                        onClick = {
                            HapticFeedbackHelper.triggerClick(view)
                            onNextClick()
                        },
                        modifier = Modifier
                            .size(38.dp)
                            .testTag("mini_player_next")
                    ) {
                        Icon(
                            imageVector = Icons.Default.SkipNext,
                            contentDescription = "Next",
                            tint = colors.secondaryText,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                }

                // Fine progress bar
                LinearProgressIndicator(
                    progress = { progress },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(2.5.dp),
                    color = colors.primaryAccent,
                    trackColor = colors.divider
                )
            }
        }
    }
}

@Composable
fun SongRowItem(
    song: Song,
    isPlaying: Boolean,
    onSongClick: () -> Unit,
    onFavoriteToggle: (() -> Unit)? = null,
    onMoreClick: (() -> Unit)? = null,
    isCached: Boolean = false,
    onCacheClick: (() -> Unit)? = null,
    isMetadataCached: Boolean = true,
    isLyricsCached: Boolean = true,
    isMoodCached: Boolean = true,
    modifier: Modifier = Modifier
) {
    val colors = LocalSwarixColors.current
    val cached = isCached || song.isLocal || song.contentUri.startsWith("content://") || song.contentUri.startsWith("file://")

    Row(
        modifier = modifier
            .fillMaxWidth()
            .clickable(onClick = onSongClick)
            .padding(horizontal = 16.dp, vertical = 8.dp)
            .testTag("song_item_${song.id}"),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Thumbnail
        Box(
            modifier = Modifier
                .size(48.dp)
                .clip(RoundedCornerShape(10.dp))
                .background(colors.elevatedSurface),
            contentAlignment = Alignment.Center
        ) {
            if (song.artworkUri.isNotEmpty()) {
                AsyncImage(
                    model = song.artworkUri,
                    contentDescription = song.title,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize()
                )
            } else {
                Text(
                    text = song.title.take(1).uppercase(),
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.Bold,
                        color = colors.primaryAccent
                    )
                )
            }

            if (isPlaying) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color.Black.copy(alpha = 0.45f)),
                    contentAlignment = Alignment.Center
                ) {
                    AudioVisualizer(color = colors.primaryAccent, barCount = 3)
                }
            }
        }

        Spacer(modifier = Modifier.width(12.dp))

        // Title and Artist
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = song.title,
                style = MaterialTheme.typography.bodyLarge.copy(
                    fontWeight = FontWeight.SemiBold,
                    color = if (isPlaying) colors.primaryAccent else colors.primaryText
                ),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Text(
                    text = "${song.artist} • ${song.album.ifEmpty { "Single" }}",
                    style = MaterialTheme.typography.bodyMedium.copy(
                        color = colors.secondaryText
                    ),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f, fill = false)
                )
                PlaybackSourceStatusIndicator(
                    isLocallyCached = cached,
                    songId = song.id,
                    compact = true,
                    showLabel = cached,
                    onClick = onCacheClick
                )
            }
            Spacer(modifier = Modifier.height(3.dp))
            // Visual Cache Status Indicators for Metadata, Lyrics, and Mood Tags (Offline Confidence)
            Row(
                horizontalArrangement = Arrangement.spacedBy(4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                CacheStatusBadge(label = "Meta", isCached = isMetadataCached, testTag = "cache_status_meta_${song.id}")
                CacheStatusBadge(label = "Lyrics", isCached = isLyricsCached, testTag = "cache_status_lyrics_${song.id}")
                CacheStatusBadge(label = "Mood", isCached = isMoodCached, testTag = "cache_status_mood_${song.id}")
            }
        }

        Text(
            text = song.formattedDuration,
            style = MaterialTheme.typography.bodyMedium.copy(
                color = colors.mutedText
            ),
            modifier = Modifier.padding(horizontal = 6.dp)
        )

        if (onFavoriteToggle != null) {
            IconButton(
                onClick = onFavoriteToggle,
                modifier = Modifier.size(36.dp)
            ) {
                Icon(
                    imageVector = if (song.isFavorite) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                    contentDescription = "Favorite",
                    tint = if (song.isFavorite) colors.primaryAccent else colors.mutedText,
                    modifier = Modifier.size(20.dp)
                )
            }
        }

        if (onMoreClick != null) {
            IconButton(
                onClick = onMoreClick,
                modifier = Modifier.size(36.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.MoreVert,
                    contentDescription = "More",
                    tint = colors.mutedText,
                    modifier = Modifier.size(20.dp)
                )
            }
        }
    }
}

@Composable
fun AudioVisualizer(
    color: Color,
    modifier: Modifier = Modifier,
    barCount: Int = 4
) {
    val transition = rememberInfiniteTransition(label = "eq_bars")
    val heights = (0 until barCount).map { index ->
        val duration = 300 + index * 120
        transition.animateFloat(
            initialValue = 0.2f,
            targetValue = 1f,
            animationSpec = infiniteRepeatable(
                animation = tween(duration, easing = FastOutSlowInEasing),
                repeatMode = RepeatMode.Reverse
            ),
            label = "bar_$index"
        )
    }

    Row(
        modifier = modifier.height(18.dp),
        horizontalArrangement = Arrangement.spacedBy(2.dp),
        verticalAlignment = Alignment.Bottom
    ) {
        heights.forEach { anim ->
            Box(
                modifier = Modifier
                    .width(3.dp)
                    .height((18 * anim.value).dp)
                    .clip(RoundedCornerShape(1.5.dp))
                    .background(color)
            )
        }
    }
}

@Composable
fun RotaryKnob(
    value: Int, // 0 to 100
    onValueChange: (Int) -> Unit,
    label: String,
    modifier: Modifier = Modifier
) {
    val colors = LocalSwarixColors.current
    var currentAngle by remember(value) {
        mutableFloatStateOf((value.toFloat() / 100f) * 270f - 135f)
    }

    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            modifier = Modifier
                .size(76.dp)
                .pointerInput(Unit) {
                    detectDragGestures { change, dragAmount ->
                        change.consume()
                        val delta = -dragAmount.y * 0.8f + dragAmount.x * 0.4f
                        val newValue = (value + delta.toInt()).coerceIn(0, 100)
                        onValueChange(newValue)
                    }
                },
            contentAlignment = Alignment.Center
        ) {
            Canvas(modifier = Modifier.fillMaxSize()) {
                val center = Offset(size.width / 2f, size.height / 2f)
                val radius = size.minDimension / 2f - 8.dp.toPx()

                // Background track arc
                drawArc(
                    color = colors.divider,
                    startAngle = 135f,
                    sweepAngle = 270f,
                    useCenter = false,
                    style = Stroke(width = 6.dp.toPx(), cap = StrokeCap.Round)
                )

                // Active accent arc
                val activeSweep = (value.toFloat() / 100f) * 270f
                drawArc(
                    color = colors.primaryAccent,
                    startAngle = 135f,
                    sweepAngle = activeSweep,
                    useCenter = false,
                    style = Stroke(width = 6.dp.toPx(), cap = StrokeCap.Round)
                )

                // Center knob dial
                drawCircle(
                    color = colors.elevatedSurface,
                    radius = radius - 6.dp.toPx()
                )

                // Indicator dot on knob
                val angleRad = Math.toRadians((135f + activeSweep).toDouble())
                val dotRadius = radius - 10.dp.toPx()
                val dotCenter = Offset(
                    (center.x + dotRadius * cos(angleRad)).toFloat(),
                    (center.y + dotRadius * sin(angleRad)).toFloat()
                )
                drawCircle(
                    color = colors.primaryAccent,
                    radius = 3.dp.toPx(),
                    center = dotCenter
                )
            }

            Text(
                text = "$value%",
                style = MaterialTheme.typography.labelSmall.copy(
                    fontWeight = FontWeight.Bold,
                    color = colors.primaryText
                )
            )
        }

        Spacer(modifier = Modifier.height(4.dp))

        Text(
            text = label,
            style = MaterialTheme.typography.labelMedium.copy(
                fontWeight = FontWeight.Medium,
                color = colors.secondaryText
            )
        )
    }
}

/**
 * Expressive bouncy spring press interaction modifier
 */
fun Modifier.bouncyClick(
    scaleDown: Float = 0.94f,
    onClick: () -> Unit
): Modifier = composed {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val scale by animateFloatAsState(
        targetValue = if (isPressed) scaleDown else 1f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessLow
        ),
        label = "bouncy_scale"
    )

    this
        .graphicsLayer {
            scaleX = scale
            scaleY = scale
        }
        .clickable(
            interactionSource = interactionSource,
            indication = null,
            onClick = onClick
        )
}

/**
 * Spotify-style Social Friend Activity Carousel
 */
@Composable
fun FriendActivityTray(
    friends: List<FriendActivity>,
    onFriendClick: (FriendActivity) -> Unit,
    modifier: Modifier = Modifier
) {
    val colors = LocalSwarixColors.current

    Column(modifier = modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 6.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(10.dp)
                        .clip(CircleShape)
                        .background(colors.success)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Friend Activity",
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.Bold,
                        color = colors.primaryText
                    )
                )
            }
            Text(
                text = "${friends.count { it.isListeningNow }} active",
                style = MaterialTheme.typography.labelSmall.copy(
                    color = colors.success,
                    fontWeight = FontWeight.SemiBold
                )
            )
        }

        LazyRow(
            contentPadding = PaddingValues(horizontal = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(friends, key = { it.id }) { friend ->
                Surface(
                    modifier = Modifier
                        .width(200.dp)
                        .clip(RoundedCornerShape(16.dp))
                        .bouncyClick { onFriendClick(friend) }
                        .testTag("friend_card_${friend.id}"),
                    color = colors.elevatedSurface
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(10.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Friend Avatar with active status badge
                        Box(modifier = Modifier.size(46.dp)) {
                            Box(
                                modifier = Modifier
                                    .size(42.dp)
                                    .clip(CircleShape)
                                    .background(colors.surface)
                            ) {
                                AsyncImage(
                                    model = friend.avatarUrl,
                                    contentDescription = friend.friendName,
                                    contentScale = ContentScale.Crop,
                                    modifier = Modifier.fillMaxSize()
                                )
                            }
                            if (friend.isListeningNow) {
                                Box(
                                    modifier = Modifier
                                        .size(12.dp)
                                        .clip(CircleShape)
                                        .background(colors.success)
                                        .align(Alignment.BottomEnd)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.width(10.dp))

                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = friend.friendName,
                                style = MaterialTheme.typography.labelLarge.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = colors.primaryText
                                ),
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                            Text(
                                text = friend.songTitle,
                                style = MaterialTheme.typography.labelSmall.copy(
                                    color = colors.primaryAccent,
                                    fontWeight = FontWeight.SemiBold
                                ),
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                            Text(
                                text = friend.artistName,
                                style = MaterialTheme.typography.labelSmall.copy(
                                    color = colors.secondaryText
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
}

/**
 * Dynamic artist spotlight carousel.
 */
@Composable
fun ArtistSpotlightSection(
    artists: List<Artist>,
    onArtistClick: (String, String) -> Unit,
    modifier: Modifier = Modifier
) {
    val colors = LocalSwarixColors.current

    Column(modifier = modifier.fillMaxWidth()) {
        Text(
            text = "Artist Spotlight",
            style = MaterialTheme.typography.titleLarge.copy(
                fontWeight = FontWeight.Bold,
                color = colors.primaryText
            ),
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp)
        )

        LazyRow(
            contentPadding = PaddingValues(horizontal = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            items(artists) { artist ->
                Surface(
                    modifier = Modifier
                        .width(160.dp)
                        .clip(RoundedCornerShape(16.dp))
                        .bouncyClick { onArtistClick(artist.name, artist.artworkUri) }
                        .testTag("artist_spotlight_${artist.name.lowercase().replace(" ", "_")}"),
                    color = colors.surface
                ) {
                    Column(
                        modifier = Modifier.padding(12.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Box(
                            modifier = Modifier
                                .size(100.dp)
                                .clip(CircleShape)
                                .background(colors.elevatedSurface)
                        ) {
                            AsyncImage(
                                model = artist.artworkUri,
                                contentDescription = artist.name,
                                contentScale = ContentScale.Crop,
                                modifier = Modifier.fillMaxSize()
                            )
                        }
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text = artist.name,
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.Bold,
                                color = colors.primaryText
                            ),
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Text(
                            text = "${artist.songCount} songs",
                            style = MaterialTheme.typography.bodySmall.copy(
                                color = colors.secondaryText
                            )
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun CacheStatusBadge(
    label: String,
    isCached: Boolean,
    testTag: String
) {
    val colors = LocalSwarixColors.current
    Surface(
        modifier = Modifier
            .clip(RoundedCornerShape(4.dp))
            .testTag(testTag),
        color = if (isCached) colors.success.copy(alpha = 0.15f) else colors.mutedText.copy(alpha = 0.10f)
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall.copy(
                fontSize = 8.sp,
                fontWeight = FontWeight.Bold,
                color = if (isCached) colors.success else colors.mutedText
            ),
            modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp)
        )
    }
}

