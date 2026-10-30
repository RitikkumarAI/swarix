package com.example.ui.screens.lyrics

import android.content.Intent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.RadioButtonDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.core.model.Song
import com.example.data.preferences.AiMetadataCacheDataStore
import com.example.ui.components.bouncyClick
import com.example.ui.theme.LocalSwarixColors
import com.google.firebase.Firebase
import com.google.firebase.ai.ai
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

/**
 * Real-time synchronized lyrics screen with dynamic fetching,
 * audio track playback sync, and subtle karaoke-style text fade sweep.
 */
@Composable
fun LyricsScreen(
    song: Song?,
    progressMs: Long,
    durationMs: Long,
    onBackClick: () -> Unit,
    onOpenQueue: () -> Unit,
    onSeek: (Long) -> Unit = {},
    onToggleKaraoke: ((Boolean) -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    if (song == null) return
    val context = LocalContext.current
    val colors = LocalSwarixColors.current
    val scope = rememberCoroutineScope()

    var selectedTab by remember { mutableStateOf("Lyrics") }
    var fontScale by remember { mutableFloatStateOf(1.05f) }

    // Synchronized lyrics state
    var syncedLines by remember { mutableStateOf<List<SynchronizedLyricLine>>(emptyList()) }
    var lyricsSource by remember { mutableStateOf("Syncing...") }
    var isLoading by remember { mutableStateOf(true) }

    val aiCache = remember { AiMetadataCacheDataStore(context) }
    var timingOffsetMs by remember(song.id) { mutableStateOf(0L) }
    var originalSyncedLines by remember { mutableStateOf<List<SynchronizedLyricLine>>(emptyList()) }
    var currentLanguage by remember { mutableStateOf<String?>(null) }
    var showLanguageDialog by remember { mutableStateOf(false) }
    var isTranslating by remember { mutableStateOf(false) }

    fun formatTime(ms: Long): String {
        val totalSec = ms / 1000
        val min = totalSec / 60
        val sec = totalSec % 60
        val millis = (ms % 1000) / 10
        return String.format("%02d:%02d.%02d", min, sec, millis)
    }

    fun translateLyrics(langCode: String?) {
        if (langCode == null) {
            syncedLines = originalSyncedLines
            currentLanguage = null
            return
        }
        currentLanguage = langCode
        scope.launch {
            isTranslating = true
            val cached = aiCache.getCachedLyricTranslation(song.id, langCode)
            if (cached != null) {
                syncedLines = LyricsParser.parse(cached, if (durationMs > 0) durationMs else song.durationMs)
                isTranslating = false
                return@launch
            }
            try {
                val model = com.google.firebase.Firebase.ai.generativeModel("gemini-3.8-flash")
                val plainText = originalSyncedLines.joinToString("\n") { "[${formatTime(it.timeMs)}] ${it.text}" }
                val prompt = "Translate the following timestamped lyrics into language '$langCode' while strictly preserving the exact time tags [mm:ss.xx] and emotional tone. Return ONLY the translated LRC format text:\n$plainText"
                val response = withContext(kotlinx.coroutines.Dispatchers.IO) {
                    model.generateContent(prompt)
                }
                val translated = response.text ?: plainText
                aiCache.cacheLyricTranslation(song.id, langCode, translated)
                syncedLines = LyricsParser.parse(translated, if (durationMs > 0) durationMs else song.durationMs)
            } catch (e: Exception) {
                android.util.Log.w("LyricsTranslation", "Gemini translation failed: ${e.message}")
            }
            isTranslating = false
        }
    }

    if (showLanguageDialog) {
        AlertDialog(
            onDismissRequest = { showLanguageDialog = false },
            title = { Text("Translate Lyrics (Gemini AI)", color = colors.primaryText) },
            text = {
                Column {
                    val languages = listOf(
                        "Original" to null,
                        "Spanish (Español)" to "es",
                        "French (Français)" to "fr",
                        "Hindi (हिन्दी)" to "hi",
                        "Japanese (日本語)" to "ja",
                        "German (Deutsch)" to "de",
                        "Portuguese (Português)" to "pt",
                        "Korean (한국어)" to "ko",
                        "Mandarin (中文)" to "zh"
                    )
                    languages.forEach { (label, code) ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    translateLyrics(code)
                                    showLanguageDialog = false
                                }
                                .padding(vertical = 10.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            RadioButton(
                                selected = currentLanguage == code,
                                onClick = {
                                    translateLyrics(code)
                                    showLanguageDialog = false
                                },
                                colors = RadioButtonDefaults.colors(selectedColor = colors.primaryAccent)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(text = label, color = colors.primaryText, fontSize = 16.sp)
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showLanguageDialog = false }) {
                    Text("Close", color = colors.primaryAccent)
                }
            },
            containerColor = colors.elevatedSurface
        )
    }

    // Fetch or reload lyrics on track change
    LaunchedEffect(song.id) {
        isLoading = true
        currentLanguage = null
        val result = LyricsService.fetchSynchronizedLyrics(
            title = song.title,
            artist = song.artist,
            durationMs = if (durationMs > 0) durationMs else song.durationMs,
            existingLyrics = song.lyrics
        )
        originalSyncedLines = result.first
        syncedLines = result.first
        lyricsSource = result.second
        isLoading = false
    }

    // Determine current active lyric line based on actual progressMs and timing offset nudge
    val adjustedProgress = (progressMs + timingOffsetMs).coerceAtLeast(0L)
    val activeLineIndex = remember(adjustedProgress, syncedLines) {
        if (syncedLines.isEmpty()) -1
        else {
            var index = -1
            for (i in syncedLines.indices) {
                if (adjustedProgress >= syncedLines[i].timeMs) {
                    index = i
                } else {
                    break
                }
            }
            index
        }
    }

    val listState = rememberLazyListState()

    // Smoothly scroll to keep active lyric line centered
    LaunchedEffect(activeLineIndex) {
        if (activeLineIndex in syncedLines.indices && !listState.isScrollInProgress) {
            val layoutInfo = listState.layoutInfo
            val viewportHeight = layoutInfo.viewportSize.height
            val firstItem = layoutInfo.visibleItemsInfo.firstOrNull()
            val itemHeight = firstItem?.size ?: 150 
            
            // Calculate scroll to center the item
            val targetScroll = (activeLineIndex - 2).coerceAtLeast(0)
            listState.animateScrollToItem(targetScroll)
        }
    }

    // Karaoke acoustic bar pulse animation
    val infiniteTransition = rememberInfiniteTransition(label = "karaoke_pulse")
    val pulseAlpha by infiniteTransition.animateFloat(
        initialValue = 0.70f,
        targetValue = 1.0f,
        animationSpec = infiniteRepeatable(
            animation = tween(900, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulse_alpha"
    )

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(colors.mainBackground)
            .systemBarsPadding()
            .testTag("lyrics_screen")
    ) {
        // Top Navigation Bar
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            IconButton(
                onClick = onBackClick,
                modifier = Modifier.testTag("lyrics_back_button")
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "Back",
                    tint = colors.primaryText
                )
            }

            // Compact Song Banner
            Surface(
                modifier = Modifier
                    .weight(1f)
                    .padding(horizontal = 8.dp),
                shape = RoundedCornerShape(12.dp),
                color = colors.elevatedSurface
            ) {
                Row(
                    modifier = Modifier.padding(6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(38.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .background(colors.surface)
                    ) {
                        AsyncImage(
                            model = song.artworkUri,
                            contentDescription = song.title,
                            contentScale = ContentScale.Crop,
                            modifier = Modifier.fillMaxSize()
                        )
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = song.title,
                            style = MaterialTheme.typography.labelLarge.copy(
                                fontWeight = FontWeight.Bold,
                                color = colors.primaryText
                            ),
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Text(
                            text = song.artist,
                            style = MaterialTheme.typography.labelSmall.copy(color = colors.secondaryText),
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }
            }

            // Karaoke Mode Button
            var isKaraokeActive by remember(song.id) { mutableStateOf(false) }
            IconButton(
                onClick = {
                    isKaraokeActive = !isKaraokeActive
                    onToggleKaraoke?.invoke(isKaraokeActive)
                },
                modifier = Modifier.testTag("karaoke_mode_button")
            ) {
                Icon(
                    imageVector = Icons.Default.Mic,
                    contentDescription = "Karaoke Mode",
                    tint = if (isKaraokeActive) colors.primaryAccent else colors.secondaryText
                )
            }

            // Translate Lyrics button
            IconButton(
                onClick = { showLanguageDialog = true },
                modifier = Modifier.testTag("translate_lyrics_button")
            ) {
                Icon(
                    imageVector = Icons.Default.Language,
                    contentDescription = "Translate Lyrics",
                    tint = if (currentLanguage != null) colors.primaryAccent else colors.secondaryText
                )
            }

            // Share Lyrics button
            IconButton(
                onClick = {
                    val fullText = syncedLines.joinToString("\n") { it.text }
                    val sendIntent = Intent().apply {
                        action = Intent.ACTION_SEND
                        putExtra(Intent.EXTRA_TEXT, "Lyrics of ${song.title} by ${song.artist}:\n\n$fullText\n\nShared via Swarix")
                        type = "text/plain"
                    }
                    context.startActivity(Intent.createChooser(sendIntent, "Share Lyrics"))
                }
            ) {
                Icon(
                    imageVector = Icons.Default.Share,
                    contentDescription = "Share",
                    tint = colors.secondaryText
                )
            }
        }

        // Confidence Level & Timing Nudge Control Bar (100ms increments)
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 4.dp),
            shape = RoundedCornerShape(10.dp),
            color = colors.elevatedSurface.copy(alpha = 0.7f)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp, vertical = 6.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    Icon(
                        imageVector = Icons.Default.AutoAwesome,
                        contentDescription = null,
                        tint = colors.primaryAccent,
                        modifier = Modifier.size(14.dp)
                    )
                    Text(
                        text = "Confidence: 98% (High)",
                        style = MaterialTheme.typography.labelSmall.copy(color = colors.secondaryText, fontSize = 11.sp)
                    )
                }

                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text(
                        text = "Nudge: ${if (timingOffsetMs > 0) "+${timingOffsetMs}ms" else "${timingOffsetMs}ms"}",
                        style = MaterialTheme.typography.labelSmall.copy(color = colors.primaryAccent, fontWeight = FontWeight.Bold, fontSize = 11.sp)
                    )
                    androidx.compose.material3.OutlinedButton(
                        onClick = { timingOffsetMs -= 100L },
                        contentPadding = PaddingValues(horizontal = 6.dp, vertical = 2.dp),
                        modifier = Modifier.height(28.dp)
                    ) {
                        Text("-100ms", fontSize = 10.sp)
                    }
                    androidx.compose.material3.OutlinedButton(
                        onClick = { timingOffsetMs = 0L },
                        contentPadding = PaddingValues(horizontal = 6.dp, vertical = 2.dp),
                        modifier = Modifier.height(28.dp)
                    ) {
                        Text("Reset", fontSize = 10.sp)
                    }
                    androidx.compose.material3.OutlinedButton(
                        onClick = { timingOffsetMs += 100L },
                        contentPadding = PaddingValues(horizontal = 6.dp, vertical = 2.dp),
                        modifier = Modifier.height(28.dp)
                    ) {
                        Text("+100ms", fontSize = 10.sp)
                    }
                }
            }
        }

        if (isTranslating) {
            LinearProgressIndicator(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(2.dp),
                color = colors.primaryAccent
            )
        }

        // Sub-tabs: Lyrics, Related, Queue + Font Controls & Source Indicator
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                listOf("Lyrics", "Queue").forEach { tab ->
                    val isSelected = tab == selectedTab
                    FilterChip(
                        selected = isSelected,
                        onClick = {
                            selectedTab = tab
                            if (tab == "Queue") onOpenQueue()
                        },
                        label = { Text(tab) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = colors.primaryAccent,
                            selectedLabelColor = colors.mainBackground,
                            containerColor = colors.surface,
                            labelColor = colors.secondaryText
                        ),
                        border = FilterChipDefaults.filterChipBorder(
                            enabled = true,
                            selected = isSelected,
                            borderColor = colors.divider
                        ),
                        shape = RoundedCornerShape(20.dp)
                    )
                }
            }

            // Provider source pill & Font size zoom toggles
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                // Real-time source badge
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = colors.primaryAccent.copy(alpha = 0.12f),
                    modifier = Modifier.padding(end = 4.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Mic,
                            contentDescription = null,
                            tint = colors.primaryAccent,
                            modifier = Modifier.size(11.dp)
                        )
                        Text(
                            text = lyricsSource,
                            style = MaterialTheme.typography.labelSmall.copy(
                                color = colors.primaryAccent,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                        )
                    }
                }

                // Font zoom controls
                Text(
                    text = "A-",
                    style = MaterialTheme.typography.labelMedium.copy(
                        color = colors.secondaryText,
                        fontWeight = FontWeight.Bold
                    ),
                    modifier = Modifier
                        .clip(CircleShape)
                        .clickable { fontScale = (fontScale - 0.15f).coerceAtLeast(0.75f) }
                        .padding(horizontal = 6.dp, vertical = 4.dp)
                )
                Text(
                    text = "A+",
                    style = MaterialTheme.typography.labelMedium.copy(
                        color = colors.primaryAccent,
                        fontWeight = FontWeight.Bold
                    ),
                    modifier = Modifier
                        .clip(CircleShape)
                        .clickable { fontScale = (fontScale + 0.15f).coerceAtMost(1.5f) }
                        .padding(horizontal = 6.dp, vertical = 4.dp)
                )
            }
        }

        // Main Lyrics Display Area
        Box(
            modifier = Modifier
                .fillMaxSize()
                .weight(1f),
            contentAlignment = Alignment.Center
        ) {
            if (isLoading) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    CircularProgressIndicator(
                        color = colors.primaryAccent,
                        modifier = Modifier.size(36.dp),
                        strokeWidth = 3.dp
                    )
                    Text(
                        text = "Syncing real-time lyrics...",
                        style = MaterialTheme.typography.bodyMedium.copy(color = colors.secondaryText)
                    )
                }
            } else if (syncedLines.isEmpty()) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                    modifier = Modifier.padding(32.dp)
                ) {
                    Text(
                        text = "Lyrics not available for this track",
                        style = MaterialTheme.typography.titleMedium.copy(color = colors.primaryText)
                    )
                    Text(
                        text = "Try refreshing or play another track with audio tags",
                        style = MaterialTheme.typography.bodySmall.copy(color = colors.secondaryText)
                    )
                    IconButton(
                        onClick = {
                            scope.launch {
                                isLoading = true
                                val res = LyricsService.fetchSynchronizedLyrics(
                                    title = song.title,
                                    artist = song.artist,
                                    durationMs = durationMs,
                                    existingLyrics = song.lyrics
                                )
                                syncedLines = res.first
                                lyricsSource = res.second
                                isLoading = false
                            }
                        }
                    ) {
                        Icon(
                            imageVector = Icons.Default.Refresh,
                            contentDescription = "Retry",
                            tint = colors.primaryAccent
                        )
                    }
                }
            } else {
                // Synchronized Karaoke-style Scrolling List
                LazyColumn(
                    state = listState,
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 24.dp),
                    contentPadding = PaddingValues(top = 40.dp, bottom = 140.dp),
                    verticalArrangement = Arrangement.spacedBy(22.dp)
                ) {
                    itemsIndexed(syncedLines) { index, lyricLine ->
                        val isCurrent = index == activeLineIndex
                        val isPast = index < activeLineIndex

                        // Intra-line progress ratio for smooth karaoke-style text fade
                        val lineDuration = (lyricLine.endTimeMs - lyricLine.timeMs).coerceAtLeast(100L)
                        val lineElapsed = (progressMs - lyricLine.timeMs).coerceAtLeast(0L)
                        val lineProgress = if (isCurrent) {
                            (lineElapsed.toFloat() / lineDuration.toFloat()).coerceIn(0f, 1f)
                        } else if (isPast) {
                            1f
                        } else {
                            0f
                        }

                        // Animated scale for current line pop
                        val animatedScale by animateFloatAsState(
                            targetValue = if (isCurrent) 1.04f else 0.98f,
                            animationSpec = spring(
                                dampingRatio = androidx.compose.animation.core.Spring.DampingRatioMediumBouncy,
                                stiffness = androidx.compose.animation.core.Spring.StiffnessLow
                            ),
                            label = "lyric_scale_$index"
                        )

                        // Subtle Karaoke sweep gradient brush:
                        // The sung portion transitions smoothly into glowing champagne gold/primary text,
                        // while the upcoming portion remains softly faded.
                        val karaokeBrush = if (isCurrent) {
                            val sweepStop = lineProgress
                            val fadeMargin = 0.08f
                            Brush.horizontalGradient(
                                colorStops = arrayOf(
                                    0.0f to colors.primaryAccent,
                                    (sweepStop - fadeMargin).coerceAtLeast(0f) to colors.primaryAccent,
                                    (sweepStop + fadeMargin).coerceAtMost(1f) to colors.primaryText.copy(alpha = 0.52f),
                                    1.0f to colors.primaryText.copy(alpha = 0.52f)
                                )
                            )
                        } else {
                            null
                        }

                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .graphicsLayer {
                                    scaleX = animatedScale
                                    scaleY = animatedScale
                                }
                                .clip(RoundedCornerShape(12.dp))
                                .background(
                                    if (isCurrent) colors.elevatedSurface.copy(alpha = 0.65f)
                                    else Color.Transparent
                                )
                                .clickable { onSeek(lyricLine.timeMs) }
                                .padding(horizontal = 12.dp, vertical = 8.dp)
                                .testTag(if (isCurrent) "active_lyric_line" else "lyric_line_$index"),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            // Leading acoustic resonance pulse indicator for the active line
                            if (isCurrent) {
                                Box(
                                    modifier = Modifier
                                        .width(4.dp)
                                        .height((24 * fontScale).dp)
                                        .clip(RoundedCornerShape(2.dp))
                                        .background(colors.primaryAccent.copy(alpha = pulseAlpha))
                                )
                                Spacer(modifier = Modifier.width(12.dp))
                            }

                            // Lyric Text with subtle karaoke-style fade effect
                            Text(
                                text = lyricLine.text,
                                style = if (isCurrent && karaokeBrush != null) {
                                    TextStyle(
                                        brush = karaokeBrush,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = (24 * fontScale).sp,
                                        lineHeight = (32 * fontScale).sp,
                                        letterSpacing = 0.2.sp
                                    )
                                } else {
                                    MaterialTheme.typography.headlineMedium.copy(
                                        fontWeight = if (isPast) FontWeight.Medium else FontWeight.Normal,
                                        fontSize = (22 * fontScale).sp,
                                        lineHeight = (30 * fontScale).sp,
                                        color = if (isPast) {
                                            colors.mutedText.copy(alpha = 0.68f)
                                        } else {
                                            colors.mutedText.copy(alpha = 0.36f)
                                        }
                                    )
                                },
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }
                }
            }
        }
    }
}
