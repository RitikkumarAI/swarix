package com.example.ui.screens.player

import android.content.Intent
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode as AnimRepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.filled.SwapHoriz
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.unit.IntOffset
import kotlinx.coroutines.delay
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.QueueMusic
import androidx.compose.material.icons.filled.Bedtime
import androidx.compose.material.icons.filled.Cast
import androidx.compose.material.icons.filled.Devices
import androidx.compose.material.icons.filled.Group
import androidx.compose.material.icons.filled.Equalizer
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.HighQuality
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Repeat
import androidx.compose.material.icons.filled.RepeatOne
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Shuffle
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material.icons.filled.SkipNext
import androidx.compose.material.icons.filled.SkipPrevious
import androidx.compose.material.icons.filled.Subtitles
import androidx.compose.material.icons.filled.Vibration
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ElevatedButton
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.core.model.Song
import com.example.playback.RepeatMode
import com.google.firebase.Firebase
import com.google.firebase.ai.ai
import kotlinx.coroutines.withContext
import kotlinx.coroutines.Dispatchers
import org.json.JSONArray
import com.example.ui.components.AudioDevicePickerDialog
import com.example.ui.components.AudioQualitySpecsDialog
import com.example.ui.components.PlaybackSourceStatusIndicator
import com.example.ui.components.SleepTimerDialog
import com.example.ui.components.bouncyClick
import com.example.ui.theme.LocalSwarixColors

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NowPlayingScreen(
    song: Song?,
    isPlaying: Boolean,
    progressMs: Long,
    durationMs: Long,
    isShuffle: Boolean,
    repeatMode: RepeatMode,
    onPlayPauseClick: () -> Unit,
    onPreviousClick: () -> Unit,
    onNextClick: () -> Unit,
    onSeek: (Long) -> Unit,
    onShuffleToggle: () -> Unit,
    onRepeatToggle: () -> Unit,
    onFavoriteToggle: (Song) -> Unit,
    onCollapse: () -> Unit,
    onOpenQueue: () -> Unit,
    onOpenLyrics: () -> Unit,
    onOpenEqualizer: () -> Unit,
    sleepTimerSecondsRemaining: Int = 0,
    isSleepTimerFading: Boolean = false,
    onSetSleepTimer: ((minutes: Int, fadeSeconds: Int, isEndOfTrack: Boolean) -> Unit)? = null,
    onCancelSleepTimer: (() -> Unit)? = null,
    onAddSleepTimerMinutes: ((Int) -> Unit)? = null,
    bassHapticsEnabled: Boolean = true,
    onToggleBassHaptics: (() -> Unit)? = null,
    onSmartShuffle: (() -> Unit)? = null,
    allSongs: List<Song> = emptyList(),
    onSongSelected: ((Song) -> Unit)? = null,
    isBatterySaverEngaged: Boolean = false,
    modifier: Modifier = Modifier
) {
    if (song == null) return
    val context = LocalContext.current
    val colors = LocalSwarixColors.current

    var isSeeking by remember { mutableStateOf(false) }
    var seekPosition by remember { mutableFloatStateOf(0f) }
    var showMenu by remember { mutableStateOf(false) }
    var showDevicePicker by remember { mutableStateOf(false) }
    var showSleepTimer by remember { mutableStateOf(false) }
    var showQualitySpecs by remember { mutableStateOf(false) }
    var currentDeviceId by remember { mutableStateOf("phone") }
    var currentSleepMinutes by remember { mutableIntStateOf(0) }
    var showListenTogetherDialog by remember { mutableStateOf(false) }
    var activeListenSessionId by remember { mutableStateOf<String?>(null) }

    // Gemini AI Recommendations state
    var aiRecommendations by remember(song.id) { mutableStateOf<List<Song>>(emptyList()) }
    var isLoadingRecommendations by remember(song.id) { mutableStateOf(false) }

    LaunchedEffect(song.id) {
        if (allSongs.isNotEmpty()) {
            isLoadingRecommendations = true
            try {
                val model = Firebase.ai.generativeModel("gemini-3.8-flash")
                val librarySnippet = allSongs.take(25).map { "${it.id}: '${it.title}' by ${it.artist} (${it.genre}, mood: ${it.mood})" }.joinToString("\n")
                val prompt = """
                    Current song: '${song.title}' by ${song.artist} (Genre: '${song.genre}', Mood: '${song.mood}').
                    From the following local library, pick EXACTLY 3 song IDs that match the sonic characteristics and mood of the current song.
                    Return ONLY a valid JSON array of 3 string IDs, e.g. ["id1", "id2", "id3"]:
                    $librarySnippet
                """.trimIndent()

                val response = withContext(Dispatchers.IO) {
                    model.generateContent(prompt)
                }
                val text = response.text ?: ""
                val jsonStart = text.indexOf('[')
                val jsonEnd = text.lastIndexOf(']')
                if (jsonStart != -1 && jsonEnd != -1 && jsonEnd > jsonStart) {
                    val jsonArray = JSONArray(text.substring(jsonStart, jsonEnd + 1))
                    val ids = mutableListOf<String>()
                    for (i in 0 until jsonArray.length()) {
                        ids.add(jsonArray.getString(i))
                    }
                    val matched = allSongs.filter { ids.contains(it.id) && it.id != song.id }
                    aiRecommendations = if (matched.isNotEmpty()) matched.take(3) else allSongs.filter { it.id != song.id }.shuffled().take(3)
                } else {
                    aiRecommendations = allSongs.filter { it.id != song.id }.shuffled().take(3)
                }
            } catch (e: Exception) {
                android.util.Log.w("NowPlayingAI", "Gemini recommendations failed: ${e.message}")
                aiRecommendations = allSongs.filter { it.id != song.id }.shuffled().take(3)
            }
            isLoadingRecommendations = false
        }
    }

    // Audio Volume Controller & State
    val volumeController = remember(context) { VolumeController(context) }
    var currentVolume by remember { mutableIntStateOf(volumeController.currentVolume) }
    val maxVolume = remember { volumeController.maxVolume }
    var showVolumeOverlay by remember { mutableStateOf(false) }
    var volumeHideTrigger by remember { mutableIntStateOf(0) }

    // Track Skip Gesture Feedback State
    var liveSkipHint by remember { mutableStateOf<SkipDirection?>(null) }
    var activeSkipFeedback by remember { mutableStateOf<SkipDirection?>(null) }
    var skipHideTrigger by remember { mutableIntStateOf(0) }

    // Physical drag offset for turntable disc
    var horizontalVinylOffset by remember { mutableFloatStateOf(0f) }
    val animatedVinylOffsetX by androidx.compose.animation.core.animateFloatAsState(
        targetValue = horizontalVinylOffset,
        animationSpec = androidx.compose.animation.core.spring(
            dampingRatio = androidx.compose.animation.core.Spring.DampingRatioMediumBouncy,
            stiffness = androidx.compose.animation.core.Spring.StiffnessMedium
        ),
        label = "vinyl_offset_x"
    )

    LaunchedEffect(volumeHideTrigger) {
        if (volumeHideTrigger > 0) {
            showVolumeOverlay = true
            delay(1250)
            showVolumeOverlay = false
        }
    }

    LaunchedEffect(skipHideTrigger) {
        if (skipHideTrigger > 0) {
            delay(1100)
            activeSkipFeedback = null
        }
    }

    val handleSkipNext: () -> Unit = {
        activeSkipFeedback = SkipDirection.NEXT
        skipHideTrigger++
        onNextClick()
    }

    val handleSkipPrevious: () -> Unit = {
        activeSkipFeedback = SkipDirection.PREVIOUS
        skipHideTrigger++
        onPreviousClick()
    }

    val handleVolumeAdjust: (Int, Int) -> Unit = { step, _ ->
        val newVol = volumeController.changeVolumeBy(step)
        currentVolume = newVol
        volumeHideTrigger++
    }

    val infiniteTransition = rememberInfiniteTransition(label = "vinyl_spin")
    val rotation by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(12000, easing = LinearEasing),
            repeatMode = AnimRepeatMode.Restart
        ),
        label = "vinyl_rotation"
    )

    val currentMs = if (isSeeking) seekPosition.toLong() else progressMs
    val totalMs = if (durationMs > 0) durationMs else song.durationMs.coerceAtLeast(1L)
    val sliderValue = (currentMs.toFloat() / totalMs.toFloat()).coerceIn(0f, 1f)

    fun formatTime(ms: Long): String {
        val totalSec = (ms / 1000).coerceAtLeast(0)
        val m = totalSec / 60
        val s = totalSec % 60
        return "%d:%02d".format(m, s)
    }

    val vinylScale by androidx.compose.animation.core.animateFloatAsState(
        targetValue = if (isPlaying) 1.02f else 0.96f,
        animationSpec = androidx.compose.animation.core.spring(
            dampingRatio = androidx.compose.animation.core.Spring.DampingRatioMediumBouncy,
            stiffness = androidx.compose.animation.core.Spring.StiffnessLow
        ),
        label = "vinyl_scale"
    )

    // Extract dynamic dominant color palette from currently playing album art
    val artworkPalette = com.example.ui.components.rememberArtworkPalette(song)

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(artworkPalette.surfaceDark)
    ) {
        // 1. Dynamic Blurred Album Artwork Layer for Glassmorphism (Throttled during battery saver)
        if (song.artworkUri.isNotBlank()) {
            AsyncImage(
                model = song.artworkUri,
                contentDescription = null,
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .fillMaxSize()
                    .blur(if (isBatterySaverEngaged) 8.dp else 50.dp)
                    .graphicsLayer {
                        alpha = if (isBatterySaverEngaged) 0.20f else 0.40f
                        scaleX = 1.25f
                        scaleY = 1.25f
                    }
            )
        }

        // 2. Dynamic Animated Gradient Background Engine from Album Art with Dominant Tinting
        com.example.ui.components.DynamicAnimatedGradientBackground(
            palette = artworkPalette,
            isPlaying = isPlaying,
            modifier = Modifier.fillMaxSize()
        )

        // 3. Dynamic Glassmorphic Tint & Frost Overlay
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            artworkPalette.surfaceDark.copy(alpha = 0.40f),
                            artworkPalette.dominant.copy(alpha = 0.15f),
                            artworkPalette.surfaceDark.copy(alpha = 0.75f)
                        )
                    )
                )
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .systemBarsPadding()
                .padding(horizontal = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            // Top Bar
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(
                    onClick = onCollapse,
                    modifier = Modifier.testTag("now_playing_collapse")
                ) {
                    Icon(
                        imageVector = Icons.Default.KeyboardArrowDown,
                        contentDescription = "Collapse",
                        tint = Color.White,
                        modifier = Modifier.size(32.dp)
                    )
                }

                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = "PLAYING FROM",
                        style = MaterialTheme.typography.labelSmall.copy(
                            color = Color.White.copy(alpha = 0.70f),
                            letterSpacing = 1.sp
                        )
                    )
                    Text(
                        text = if (song.isFavorite) "Liked Songs" else (song.album.ifEmpty { "Swarix Music" }),
                        style = MaterialTheme.typography.labelLarge.copy(
                            fontWeight = FontWeight.SemiBold,
                            color = Color.White
                        )
                    )
                }

                Box {
                    IconButton(onClick = { showMenu = !showMenu }) {
                        Icon(
                            imageVector = Icons.Default.MoreVert,
                            contentDescription = "Options",
                            tint = Color.White
                        )
                    }
                    DropdownMenu(
                        expanded = showMenu,
                        onDismissRequest = { showMenu = false }
                    ) {
                        DropdownMenuItem(
                            text = { Text("Smart Shuffle (Gemini AI)") },
                            leadingIcon = { Icon(Icons.Default.AutoAwesome, contentDescription = null, tint = colors.primaryAccent) },
                            onClick = {
                                showMenu = false
                                onSmartShuffle?.invoke()
                            }
                        )
                        DropdownMenuItem(
                            text = { Text(if (bassHapticsEnabled) "Bass Haptics: ON" else "Bass Haptics: OFF") },
                            leadingIcon = { Icon(Icons.Default.Vibration, contentDescription = null, tint = if (bassHapticsEnabled) colors.primaryAccent else colors.mutedText) },
                            onClick = {
                                showMenu = false
                                onToggleBassHaptics?.invoke()
                            }
                        )
                        DropdownMenuItem(
                            text = { Text("4K Master Audio Specs") },
                            leadingIcon = { Icon(Icons.Default.HighQuality, contentDescription = null) },
                            onClick = {
                                showMenu = false
                                showQualitySpecs = true
                            }
                        )
                        DropdownMenuItem(
                            text = { Text("Sleep Timer") },
                            leadingIcon = { Icon(Icons.Default.Bedtime, contentDescription = null) },
                            onClick = {
                                showMenu = false
                                showSleepTimer = true
                            }
                        )
                        DropdownMenuItem(
                            text = { Text("Open Equalizer") },
                            leadingIcon = { Icon(Icons.Default.Equalizer, contentDescription = null) },
                            onClick = {
                                showMenu = false
                                onOpenEqualizer()
                            }
                        )
                        DropdownMenuItem(
                            text = { Text("Share Track") },
                            leadingIcon = { Icon(Icons.Default.Share, contentDescription = null) },
                            onClick = {
                                showMenu = false
                                val sendIntent = Intent().apply {
                                    action = Intent.ACTION_SEND
                                    putExtra(Intent.EXTRA_TEXT, "Listening to ${song.title} by ${song.artist} on Swarix!")
                                    type = "text/plain"
                                }
                                context.startActivity(Intent.createChooser(sendIntent, "Share Track"))
                            }
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(6.dp))

            // Gesture-sensitive turntable & vinyl listening stage
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp)
                    .playerSwipeGestures(
                        onSkipNext = handleSkipNext,
                        onSkipPrevious = handleSkipPrevious,
                        onVolumeAdjust = handleVolumeAdjust,
                        onHorizontalDragOffset = { horizontalVinylOffset = it },
                        onSkipDirectionHint = { liveSkipHint = it }
                    )
                    .testTag("now_playing_gesture_area"),
                contentAlignment = Alignment.Center
            ) {
                // Realistic Turntable Vinyl Disc Visual from Reference Image 1 Screen 6
                Box(
                    modifier = Modifier
                        .offset { IntOffset(animatedVinylOffsetX.toInt(), 0) }
                        .size(270.dp)
                        .graphicsLayer {
                            scaleX = vinylScale
                            scaleY = vinylScale
                        }
                        .clip(CircleShape)
                        .background(Color(0xFF0D0F0D))
                        .border(2.dp, colors.divider, CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    // Vinyl grooves canvas
                    Canvas(
                        modifier = Modifier
                            .fillMaxSize()
                            .rotate(if (isPlaying) rotation else 0f)
                    ) {
                        val center = Offset(size.width / 2f, size.height / 2f)
                        val maxRadius = size.minDimension / 2f

                        // Subtle vinyl groove rings
                        listOf(0.96f, 0.90f, 0.84f, 0.78f, 0.72f, 0.66f).forEach { scale ->
                            drawCircle(
                                color = Color(0xFF222622),
                                radius = maxRadius * scale,
                                center = center,
                                style = Stroke(width = 1.2.dp.toPx())
                            )
                        }
                    }

                    // Center circular album artwork
                    Box(
                        modifier = Modifier
                            .size(170.dp)
                            .clip(CircleShape)
                            .rotate(if (isPlaying) rotation else 0f)
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
                            Box(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .background(colors.primaryAccent),
                                contentAlignment = Alignment.Center
                            ) {
                                com.example.ui.components.SwarixLogoMark(
                                    size = 72.dp,
                                    tint = colors.mainBackground,
                                    enableGlow = false
                                )
                            }
                        }

                        // Turntable center spindle hole
                        Box(
                            modifier = Modifier
                                .size(22.dp)
                                .clip(CircleShape)
                                .background(Color(0xFF0B0D0C))
                                .border(1.dp, Color(0xFF30352F), CircleShape)
                        )
                    }
                }

                // Live gesture hint during drag
                if (liveSkipHint != null && activeSkipFeedback == null) {
                    TrackSkipFeedbackOverlay(
                        visible = true,
                        direction = liveSkipHint,
                        modifier = Modifier.align(Alignment.Center)
                    )
                }

                // Completed track skip visual feedback overlay
                TrackSkipFeedbackOverlay(
                    visible = activeSkipFeedback != null,
                    direction = activeSkipFeedback,
                    modifier = Modifier.align(Alignment.Center)
                )

                // Volume adjustment visual feedback overlay HUD
                VolumeFeedbackOverlay(
                    visible = showVolumeOverlay,
                    currentVolume = currentVolume,
                    maxVolume = maxVolume,
                    modifier = Modifier.align(Alignment.Center)
                )
            }

            // Subtle Gesture Affordance Hint
            Row(
                modifier = Modifier.padding(vertical = 2.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.SwapHoriz,
                    contentDescription = null,
                    tint = colors.mutedText.copy(alpha = 0.65f),
                    modifier = Modifier.size(14.dp)
                )
                Text(
                    text = "Swipe ↔ to skip track  •  ↕ for volume",
                    style = MaterialTheme.typography.labelSmall.copy(
                        color = colors.mutedText.copy(alpha = 0.70f),
                        fontSize = 11.sp,
                        letterSpacing = 0.3.sp
                    )
                )
            }

            // Frosted Glass Morphic Container for Song Details & Controls
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 6.dp)
                    .clip(RoundedCornerShape(26.dp))
                    .border(
                        width = 1.dp,
                        brush = Brush.verticalGradient(
                            listOf(
                                Color.White.copy(alpha = 0.20f),
                                artworkPalette.dominant.copy(alpha = 0.30f),
                                Color.White.copy(alpha = 0.05f)
                            )
                        ),
                        shape = RoundedCornerShape(26.dp)
                    ),
                color = artworkPalette.surfaceDark.copy(alpha = 0.50f)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 14.dp, vertical = 12.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    // Song Info & Favorite
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = song.title,
                                style = MaterialTheme.typography.headlineMedium.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White
                                ),
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Text(
                                    text = song.artist,
                                    style = MaterialTheme.typography.titleMedium.copy(
                                        color = Color.White.copy(alpha = 0.85f),
                                        fontWeight = FontWeight.Medium
                                    ),
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis,
                                    modifier = Modifier.weight(1f, fill = false)
                                )
                                PlaybackSourceStatusIndicator(
                                    isLocallyCached = song.isLocal || song.contentUri.startsWith("content://") || song.contentUri.startsWith("file://"),
                                    songId = song.id,
                                    compact = false
                                )
                            }

                            Spacer(modifier = Modifier.height(6.dp))

                            // 8K Ultra Quality, DSP Master Sound, and Tactile Bass Haptics Pills
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Surface(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(6.dp))
                                        .clickable { showQualitySpecs = true }
                                        .testTag("4k_quality_badge"),
                                    color = colors.primaryAccent.copy(alpha = 0.20f),
                                    border = androidx.compose.foundation.BorderStroke(1.dp, colors.primaryAccent.copy(alpha = 0.40f))
                                ) {
                                    Row(
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.HighQuality,
                                            contentDescription = null,
                                            tint = colors.primaryAccent,
                                            modifier = Modifier.size(13.dp)
                                        )
                                        Text(
                                            text = "8K ULTRA",
                                            style = MaterialTheme.typography.labelSmall.copy(
                                                fontSize = 10.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = colors.primaryAccent,
                                                letterSpacing = 0.5.sp
                                            )
                                        )
                                    }
                                }

                                Surface(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(6.dp))
                                        .clickable { onOpenEqualizer() }
                                        .testTag("dsp_sound_pill"),
                                    color = Color(0xFF4A8B87).copy(alpha = 0.25f),
                                    border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF4A8B87).copy(alpha = 0.60f))
                                ) {
                                    Row(
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.GraphicEq,
                                            contentDescription = "DSP Equalizer",
                                            tint = Color(0xFF6ED0C9),
                                            modifier = Modifier.size(13.dp)
                                        )
                                        Text(
                                            text = "DSP SOUND",
                                            style = MaterialTheme.typography.labelSmall.copy(
                                                fontSize = 10.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = Color(0xFF6ED0C9),
                                                letterSpacing = 0.5.sp
                                            )
                                        )
                                    }
                                }

                                Surface(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(6.dp))
                                        .clickable { onToggleBassHaptics?.invoke() }
                                        .testTag("tactile_bass_badge"),
                                    color = if (bassHapticsEnabled) colors.primaryAccent.copy(alpha = 0.20f) else Color.White.copy(alpha = 0.08f),
                                    border = androidx.compose.foundation.BorderStroke(1.dp, if (bassHapticsEnabled) colors.primaryAccent.copy(alpha = 0.40f) else Color.White.copy(alpha = 0.15f))
                                ) {
                                    Row(
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Vibration,
                                            contentDescription = "Bass Haptics",
                                            tint = if (bassHapticsEnabled) colors.primaryAccent else Color.White.copy(alpha = 0.65f),
                                            modifier = Modifier.size(13.dp)
                                        )
                                        Text(
                                            text = if (bassHapticsEnabled) "BASS ON" else "BASS OFF",
                                            style = MaterialTheme.typography.labelSmall.copy(
                                                fontSize = 10.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = if (bassHapticsEnabled) colors.primaryAccent else Color.White.copy(alpha = 0.65f),
                                                letterSpacing = 0.5.sp
                                            )
                                        )
                                    }
                                }
                            }
                        }

                        IconButton(
                            onClick = { onFavoriteToggle(song) },
                            modifier = Modifier.size(48.dp)
                        ) {
                            Icon(
                                imageVector = if (song.isFavorite) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                                contentDescription = "Favorite",
                                tint = if (song.isFavorite) colors.primaryAccent else Color.White.copy(alpha = 0.75f),
                                modifier = Modifier.size(28.dp)
                            )
                        }
                    }

                    // High-Fidelity Visual Waveform Component with Real-Time Frequency-Dependent Haptic Feedback
                    com.example.ui.components.VisualWaveformComponent(
                        isPlaying = isPlaying,
                        bassHapticsEnabled = bassHapticsEnabled,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(52.dp)
                            .padding(vertical = 4.dp)
                            .testTag("visual_waveform_component")
                    )

                    // Progress Slider
                    Column(modifier = Modifier.fillMaxWidth()) {
                        Slider(
                            value = sliderValue,
                            onValueChange = {
                                isSeeking = true
                                seekPosition = it * totalMs
                            },
                            onValueChangeFinished = {
                                onSeek(seekPosition.toLong())
                                isSeeking = false
                            },
                            colors = SliderDefaults.colors(
                                thumbColor = colors.primaryAccent,
                                activeTrackColor = colors.primaryAccent,
                                inactiveTrackColor = Color.White.copy(alpha = 0.22f)
                            ),
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("now_playing_slider")
                        )

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = formatTime(currentMs),
                                style = MaterialTheme.typography.labelSmall.copy(
                                    color = Color.White.copy(alpha = 0.75f),
                                    fontWeight = FontWeight.Medium
                                )
                            )
                            Text(
                                text = formatTime(totalMs),
                                style = MaterialTheme.typography.labelSmall.copy(
                                    color = Color.White.copy(alpha = 0.75f),
                                    fontWeight = FontWeight.Medium
                                )
                            )
                        }
                    }

                    // Playback Controls
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Shuffle
                        IconButton(
                            onClick = onShuffleToggle,
                            modifier = Modifier.testTag("btn_shuffle")
                        ) {
                            Icon(
                                imageVector = Icons.Default.Shuffle,
                                contentDescription = "Shuffle",
                                tint = if (isShuffle) colors.primaryAccent else Color.White.copy(alpha = 0.55f),
                                modifier = Modifier.size(24.dp)
                            )
                        }

                        // Previous
                        IconButton(
                            onClick = onPreviousClick,
                            modifier = Modifier
                                .size(48.dp)
                                .testTag("btn_prev")
                        ) {
                            Icon(
                                imageVector = Icons.Default.SkipPrevious,
                                contentDescription = "Previous",
                                tint = Color.White,
                                modifier = Modifier.size(34.dp)
                            )
                        }

                        // Big Play / Pause Circular Button
                        Box(
                            modifier = Modifier
                                .size(68.dp)
                                .clip(CircleShape)
                                .background(colors.primaryAccent)
                                .bouncyClick(onClick = onPlayPauseClick)
                                .testTag("btn_play_pause"),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = if (isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                                contentDescription = if (isPlaying) "Pause" else "Play",
                                tint = Color(0xFF121513),
                                modifier = Modifier.size(38.dp)
                            )
                        }

                        // Next
                        IconButton(
                            onClick = onNextClick,
                            modifier = Modifier
                                .size(48.dp)
                                .testTag("btn_next")
                        ) {
                            Icon(
                                imageVector = Icons.Default.SkipNext,
                                contentDescription = "Next",
                                tint = Color.White,
                                modifier = Modifier.size(34.dp)
                            )
                        }

                        // Repeat
                        IconButton(
                            onClick = onRepeatToggle,
                            modifier = Modifier.testTag("btn_repeat")
                        ) {
                            Icon(
                                imageVector = when (repeatMode) {
                                    RepeatMode.ONE -> Icons.Default.RepeatOne
                                    RepeatMode.ALL -> Icons.Default.Repeat
                                    RepeatMode.OFF -> Icons.Default.Repeat
                                },
                                contentDescription = "Repeat",
                                tint = if (repeatMode != RepeatMode.OFF) colors.primaryAccent else Color.White.copy(alpha = 0.55f),
                                modifier = Modifier.size(24.dp)
                            )
                        }
                    }

                    // Bottom Action Bar:
                    // [Device Connect icon] [DSP button] [Lyrics pill] [Share icon] [Queue icon]
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Device Switcher / Connect Icon
                        IconButton(
                            onClick = { showDevicePicker = true },
                            modifier = Modifier.testTag("btn_connect_device")
                        ) {
                            Icon(
                                imageVector = Icons.Default.Devices,
                                contentDescription = "Connect to a device",
                                tint = if (currentDeviceId != "phone") colors.primaryAccent else Color.White.copy(alpha = 0.70f),
                                modifier = Modifier.size(22.dp)
                            )
                        }

                        // Dedicated DSP Sound & Equalizer Button
                        ElevatedButton(
                            onClick = onOpenEqualizer,
                            colors = ButtonDefaults.elevatedButtonColors(
                                containerColor = Color(0xFF4A8B87).copy(alpha = 0.28f),
                                contentColor = Color(0xFF6ED0C9)
                            ),
                            border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF4A8B87).copy(alpha = 0.55f)),
                            shape = RoundedCornerShape(20.dp),
                            modifier = Modifier.testTag("btn_dsp")
                        ) {
                            Icon(
                                imageVector = Icons.Default.GraphicEq,
                                contentDescription = "DSP Equalizer",
                                tint = Color(0xFF6ED0C9),
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(5.dp))
                            Text(
                                text = "DSP",
                                style = MaterialTheme.typography.labelMedium.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFF6ED0C9)
                                )
                            )
                        }

                        // Lyrics Pill Button (Center)
                        ElevatedButton(
                            onClick = onOpenLyrics,
                            colors = ButtonDefaults.elevatedButtonColors(
                                containerColor = Color.White.copy(alpha = 0.16f),
                                contentColor = Color.White
                            ),
                            border = androidx.compose.foundation.BorderStroke(1.dp, Color.White.copy(alpha = 0.25f)),
                            shape = RoundedCornerShape(20.dp),
                            modifier = Modifier.testTag("btn_lyrics")
                        ) {
                            Icon(
                                imageVector = Icons.Default.Subtitles,
                                contentDescription = null,
                                tint = Color.White,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(5.dp))
                            Text(
                                text = "Lyrics",
                                style = MaterialTheme.typography.labelMedium.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White
                                )
                            )
                        }

                        // Share Button
                        IconButton(
                            onClick = {
                                com.example.core.share.ShareHelper.shareTrack(context, song)
                            }
                        ) {
                            Icon(
                                imageVector = Icons.Default.Share,
                                contentDescription = "Share",
                                tint = Color.White.copy(alpha = 0.70f),
                                modifier = Modifier.size(20.dp)
                            )
                        }

                        // Queue Button (Far Right)
                        IconButton(onClick = onOpenQueue) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.QueueMusic,
                                contentDescription = "Queue",
                                tint = Color.White.copy(alpha = 0.70f),
                                modifier = Modifier.size(22.dp)
                            )
                        }
                        }
                    }

                    // AI Recommendations Section (Gemini Sonic & Mood Match)
                    if (aiRecommendations.isNotEmpty() || isLoadingRecommendations) {
                        Spacer(modifier = Modifier.height(2.dp))
                        Surface(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(16.dp)),
                            color = artworkPalette.surfaceDark.copy(alpha = 0.5f)
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(10.dp),
                                verticalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.AutoAwesome,
                                        contentDescription = null,
                                        tint = colors.primaryAccent,
                                        modifier = Modifier.size(15.dp)
                                    )
                                    Text(
                                        text = "AI Recommendations (Sonic & Mood Match)",
                                        style = MaterialTheme.typography.labelMedium.copy(
                                            fontWeight = FontWeight.Bold,
                                            color = colors.primaryAccent,
                                            fontSize = 11.sp,
                                            letterSpacing = 0.4.sp
                                        )
                                    )
                                }

                                if (isLoadingRecommendations) {
                                    Box(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .height(40.dp),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        androidx.compose.material3.CircularProgressIndicator(
                                            modifier = Modifier.size(20.dp),
                                            color = colors.primaryAccent,
                                            strokeWidth = 2.dp
                                        )
                                    }
                                } else {
                                    aiRecommendations.forEach { recSong ->
                                        Row(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .clip(RoundedCornerShape(8.dp))
                                                .clickable { onSongSelected?.invoke(recSong) }
                                                .padding(4.dp),
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Box(
                                                modifier = Modifier
                                                    .size(32.dp)
                                                    .clip(RoundedCornerShape(6.dp))
                                                    .background(colors.elevatedSurface),
                                                contentAlignment = Alignment.Center
                                            ) {
                                                if (recSong.artworkUri.isNotEmpty()) {
                                                    AsyncImage(
                                                        model = recSong.artworkUri,
                                                        contentDescription = recSong.title,
                                                        contentScale = ContentScale.Crop,
                                                        modifier = Modifier.fillMaxSize()
                                                    )
                                                } else {
                                                    Icon(
                                                        imageVector = Icons.Default.MusicNote,
                                                        contentDescription = null,
                                                        tint = colors.primaryAccent,
                                                        modifier = Modifier.size(16.dp)
                                                    )
                                                }
                                            }
                                            Spacer(modifier = Modifier.width(8.dp))
                                            Column(modifier = Modifier.weight(1f)) {
                                                Text(
                                                    text = recSong.title,
                                                    style = MaterialTheme.typography.bodySmall.copy(
                                                        fontWeight = FontWeight.SemiBold,
                                                        color = colors.primaryText
                                                    ),
                                                    maxLines = 1,
                                                    overflow = TextOverflow.Ellipsis
                                                )
                                                Text(
                                                    text = recSong.artist,
                                                    style = MaterialTheme.typography.labelSmall.copy(
                                                        color = colors.secondaryText,
                                                        fontSize = 10.sp
                                                    ),
                                                    maxLines = 1,
                                                    overflow = TextOverflow.Ellipsis
                                                )
                                            }
                                            Icon(
                                                imageVector = Icons.Default.PlayArrow,
                                                contentDescription = "Play",
                                                tint = colors.primaryAccent,
                                                modifier = Modifier.size(18.dp)
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        // Modals
        if (showDevicePicker) {
            AudioDevicePickerDialog(
                currentDeviceId = currentDeviceId,
                onSelectDevice = { device ->
                    currentDeviceId = device.id
                    showDevicePicker = false
                },
                onDismiss = { showDevicePicker = false }
            )
        }

        if (showSleepTimer) {
            SleepTimerDialog(
                currentMinutes = currentSleepMinutes,
                secondsRemaining = sleepTimerSecondsRemaining,
                isFadingOut = isSleepTimerFading,
                onSelectMinutes = { mins ->
                    currentSleepMinutes = mins
                },
                onSetSleepTimer = { mins, fadeSecs, isEnd ->
                    currentSleepMinutes = mins
                    onSetSleepTimer?.invoke(mins, fadeSecs, isEnd)
                },
                onCancelTimer = {
                    onCancelSleepTimer?.invoke()
                },
                onAddMinutes = { mins ->
                    onAddSleepTimerMinutes?.invoke(mins)
                },
                onDismiss = { showSleepTimer = false }
            )
        }

        if (showQualitySpecs) {
            AudioQualitySpecsDialog(
                onDismiss = { showQualitySpecs = false }
            )
        }

        if (showListenTogetherDialog) {
            com.example.ui.components.ListenTogetherDialog(
                currentTrackTitle = song.title,
                currentTrackArtist = song.artist,
                activeSessionId = activeListenSessionId,
                onSessionStarted = { sessionId ->
                    activeListenSessionId = sessionId
                },
                onSessionJoined = { sessionId ->
                    activeListenSessionId = sessionId
                },
                onSessionLeft = {
                    activeListenSessionId = null
                },
                onDismiss = { showListenTogetherDialog = false }
            )
        }
    }
}
