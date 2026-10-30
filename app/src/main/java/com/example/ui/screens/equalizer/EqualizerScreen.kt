package com.example.ui.screens.equalizer

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Equalizer
import androidx.compose.material.icons.filled.GraphicEq
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Psychology
import androidx.compose.material.icons.filled.RestartAlt
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.rememberCoroutineScope
import com.google.firebase.Firebase
import com.google.firebase.ai.ai
import kotlinx.coroutines.launch
import kotlinx.coroutines.withTimeout
import org.json.JSONObject
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.core.equalizer.EqualizerPresets
import com.example.core.model.EqualizerState
import com.example.core.model.Song
import com.example.ui.components.RotaryKnob
import com.example.ui.components.bouncyClick
import com.example.ui.theme.AccentTeal
import com.example.ui.theme.LocalSwarixColors

/**
 * 10-Band Studio Graphic Equalizer with Genre-Based Dynamic Auto-Tuning.
 *
 * Features:
 * - 10-Band Octave Frequency Controls: 31Hz, 62Hz, 125Hz, 250Hz, 500Hz, 1kHz, 2kHz, 4kHz, 8kHz, 16kHz
 * - Preset modes (Bass Boost, Acoustic, Vocal, Pop, Rock, Classical, Electronic, Hip Hop, Jazz, Flat, Custom)
 * - Dynamic Genre Auto-Tune engine adapting in real time to the currently playing track
 * - Real-time smooth spline frequency response curve visualizer (Canvas)
 * - Studio Rotary Knobs for Sub-Bass Boost and 3D Virtualizer
 * - Master power toggle and quick zero-dB Flat reset
 */
data class AiEqualizerSuggestion(
    val detectedGenre: String,
    val detectedMood: String,
    val bandLevels: List<Int>,
    val bassBoost: Int,
    val virtualizer: Int,
    val reasoning: String
)

@Composable
fun EqualizerScreen(
    equalizerState: EqualizerState,
    currentSong: Song? = null,
    onStateChange: (EqualizerState) -> Unit,
    onTriggerVoiceCommand: (() -> Unit)? = null,
    onBackClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val colors = LocalSwarixColors.current
    val coroutineScope = rememberCoroutineScope()

    var isAnalyzingAi by remember { mutableStateOf(false) }
    var aiSuggestion by remember { mutableStateOf<AiEqualizerSuggestion?>(null) }
    var hasAppliedSuggestion by remember { mutableStateOf(false) }

    fun runAiAnalysis(song: Song) {
        isAnalyzingAi = true
        hasAppliedSuggestion = false
        coroutineScope.launch {
            try {
                val model = Firebase.ai.generativeModel("gemini-3.8-flash")
                val prompt = """
                    You are a studio sound engineer and audio DSP mastering specialist.
                    Analyze this song:
                    Title: "${song.title}"
                    Artist: "${song.artist}"
                    Album: "${song.album}"
                    Genre: "${song.genre}"

                    1. Detect the specific sub-genre and emotional atmosphere/mood.
                    2. Suggest optimal 10-band equalizer settings (-10 to +10 dB) for:
                       [31Hz, 62Hz, 125Hz, 250Hz, 500Hz, 1kHz, 2kHz, 4kHz, 8kHz, 16kHz].
                    3. Suggest Bass Boost (0 to 100) and 3D Virtualizer (0 to 100).
                    4. Give a 1-sentence mastering reason.
                    Respond ONLY in valid JSON matching:
                    {
                      "detected_genre": "...",
                      "detected_mood": "...",
                      "band_levels": [0, 0, 0, 0, 0, 0, 0, 0, 0, 0],
                      "bass_boost": 45,
                      "virtualizer": 30,
                      "reasoning": "..."
                    }
                """.trimIndent()

                val response = withTimeout(15_000L) {
                    model.generateContent(prompt)
                }
                val text = response.text ?: ""
                val start = text.indexOf('{')
                val end = text.lastIndexOf('}')
                if (start != -1 && end != -1 && end > start) {
                    val json = JSONObject(text.substring(start, end + 1))
                    val genre = json.optString("detected_genre", song.genre)
                    val mood = json.optString("detected_mood", song.mood)
                    val reasoning = json.optString("reasoning", "Acoustically calibrated for optimum clarity and punch.")
                    val bass = json.optInt("bass_boost", 45).coerceIn(0, 100)
                    val virt = json.optInt("virtualizer", 30).coerceIn(0, 100)
                    val bandsArray = json.optJSONArray("band_levels")
                    val bands = mutableListOf<Int>()
                    if (bandsArray != null && bandsArray.length() == 10) {
                        for (i in 0 until 10) {
                            bands.add(bandsArray.getInt(i).coerceIn(-10, 10))
                        }
                    } else {
                        bands.addAll(EqualizerPresets.getPresetForGenre(genre, mood).bandLevels)
                    }
                    aiSuggestion = AiEqualizerSuggestion(genre, mood, bands, bass, virt, reasoning)
                } else {
                    throw Exception("Invalid response structure")
                }
            } catch (e: Exception) {
                val fallback = EqualizerPresets.getPresetForGenre(song.genre, song.mood)
                aiSuggestion = AiEqualizerSuggestion(
                    detectedGenre = song.genre.ifBlank { "Modern Mix" },
                    detectedMood = song.mood.ifBlank { "Vibrant & Dynamic" },
                    bandLevels = fallback.bandLevels,
                    bassBoost = fallback.bassBoost,
                    virtualizer = fallback.virtualizer,
                    reasoning = "Calibrated based on acoustic genre profiling: ${fallback.description}"
                )
            } finally {
                isAnalyzingAi = false
            }
        }
    }

    LaunchedEffect(currentSong?.id) {
        if (currentSong != null) {
            aiSuggestion = null
            hasAppliedSuggestion = false
            if (equalizerState.isAutoGenreEnabled) {
                runAiAnalysis(currentSong)
            }
        }
    }

    // Ensure 10 bands are present
    val bandLevels = remember(equalizerState.bandLevels) {
        if (equalizerState.bandLevels.size == 10) {
            equalizerState.bandLevels
        } else {
            (equalizerState.bandLevels + List(10) { 0 }).take(10)
        }
    }

    val bandFrequencies = EqualizerPresets.BAND_FREQUENCIES

    BackHandler {
        onBackClick()
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(colors.mainBackground)
            .systemBarsPadding()
    ) {
        // Top App Bar
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 6.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                IconButton(
                    onClick = onBackClick,
                    modifier = Modifier.testTag("equalizer_back_button")
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Back",
                        tint = colors.primaryText
                    )
                }
                Spacer(modifier = Modifier.width(4.dp))
                Column {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = "10-Band Equalizer",
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.Bold,
                                color = colors.primaryText
                            )
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = colors.primaryAccent.copy(alpha = 0.15f)
                        ) {
                            Text(
                                text = "4K DSP",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontWeight = FontWeight.ExtraBold,
                                    color = colors.primaryAccent,
                                    fontSize = 10.sp
                                ),
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        }
                    }
                    Text(
                        text = if (equalizerState.isEnabled) "Studio Audio Processor Active" else "Equalizer Bypassed",
                        style = MaterialTheme.typography.labelSmall.copy(
                            color = if (equalizerState.isEnabled) AccentTeal else colors.mutedText
                        )
                    )
                }
            }

            Row(verticalAlignment = Alignment.CenterVertically) {
                if (onTriggerVoiceCommand != null) {
                    IconButton(
                        onClick = onTriggerVoiceCommand,
                        modifier = Modifier
                            .size(44.dp)
                            .testTag("btn_voice_eq")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Mic,
                            contentDescription = "Voice Equalizer Commands",
                            tint = colors.primaryAccent
                        )
                    }
                    Spacer(modifier = Modifier.width(4.dp))
                }

                // Master Switch
                Switch(
                    checked = equalizerState.isEnabled,
                    onCheckedChange = { onStateChange(equalizerState.copy(isEnabled = it)) },
                    colors = SwitchDefaults.colors(
                        checkedThumbColor = colors.mainBackground,
                        checkedTrackColor = colors.primaryAccent,
                        uncheckedThumbColor = colors.mutedText,
                        uncheckedTrackColor = colors.surface
                    ),
                    modifier = Modifier.testTag("equalizer_master_switch")
                )
            }
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(bottom = 20.dp)
        ) {
            // AI-Powered Dynamic Equalizer Card
            AiGenreAutoTuneCard(
                isAutoGenreEnabled = equalizerState.isAutoGenreEnabled,
                currentSong = currentSong,
                activePreset = equalizerState.presetName,
                isAnalyzing = isAnalyzingAi,
                suggestion = aiSuggestion,
                hasApplied = hasAppliedSuggestion,
                onToggleAuto = { enabled ->
                    onStateChange(equalizerState.copy(isAutoGenreEnabled = enabled))
                    if (enabled && currentSong != null && aiSuggestion == null && !isAnalyzingAi) {
                        runAiAnalysis(currentSong)
                    }
                },
                onAnalyzeClick = {
                    if (currentSong != null) {
                        runAiAnalysis(currentSong)
                    }
                },
                onApplySuggestion = { suggestion ->
                    onStateChange(
                        equalizerState.copy(
                            isEnabled = true,
                            presetName = "AI: ${suggestion.detectedGenre}",
                            bandLevels = suggestion.bandLevels,
                            bassBoost = suggestion.bassBoost,
                            virtualizer = suggestion.virtualizer,
                            detectedGenre = suggestion.detectedGenre
                        )
                    )
                    hasAppliedSuggestion = true
                }
            )

            // Preset Selection Carousel
            PresetSelectorRow(
                selectedPreset = equalizerState.presetName,
                isEnabled = equalizerState.isEnabled,
                onSelectPreset = { presetDef ->
                    onStateChange(
                        equalizerState.copy(
                            presetName = presetDef.name,
                            bandLevels = presetDef.bandLevels,
                            bassBoost = presetDef.bassBoost,
                            virtualizer = presetDef.virtualizer,
                            isAutoGenreEnabled = false // Manual preset selection overrides auto
                        )
                    )
                }
            )

            // Real-Time Frequency Response Curve Visualizer
            FrequencyCurveVisualizer(
                bandLevels = bandLevels,
                isEnabled = equalizerState.isEnabled,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 6.dp)
                    .height(90.dp)
            )

            // 10-Band Graphic Sliders Stage
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 6.dp)
                    .clip(RoundedCornerShape(22.dp))
                    .testTag("ten_band_sliders_surface"),
                color = colors.elevatedSurface
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(14.dp)
                ) {
                    // dB Guide header
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.GraphicEq,
                                contentDescription = null,
                                tint = colors.primaryAccent,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "10-BAND OCTAVE GAIN",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = colors.primaryAccent,
                                    letterSpacing = 1.sp
                                )
                            )
                        }

                        IconButton(
                            onClick = {
                                onStateChange(
                                    equalizerState.copy(
                                        presetName = "Flat",
                                        bandLevels = List(10) { 0 },
                                        isAutoGenreEnabled = false
                                    )
                                )
                            },
                            modifier = Modifier.size(28.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.RestartAlt,
                                contentDescription = "Flat Reset",
                                tint = colors.mutedText,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    // Horizontal Scrollable 10-Band Sliders
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState())
                            .padding(horizontal = 4.dp, vertical = 4.dp),
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        bandLevels.forEachIndexed { index, level ->
                            val freq = bandFrequencies.getOrElse(index) { "${index}k" }
                            BandSliderColumn(
                                frequency = freq,
                                levelDb = level,
                                isEnabled = equalizerState.isEnabled,
                                onLevelChange = { newDb ->
                                    val updated = bandLevels.toMutableList()
                                    updated[index] = newDb
                                    onStateChange(
                                        equalizerState.copy(
                                            presetName = "Custom",
                                            bandLevels = updated,
                                            isAutoGenreEnabled = false
                                        )
                                    )
                                }
                            )
                        }
                    }
                }
            }

            // Rotary Studio Knobs (Bass Boost & 3D Virtualizer)
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 10.dp)
                    .clip(RoundedCornerShape(22.dp)),
                color = colors.elevatedSurface
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                ) {
                    Text(
                        text = "ENHANCEMENT PROCESSORS",
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontWeight = FontWeight.Bold,
                            color = colors.primaryAccent,
                            letterSpacing = 1.sp
                        )
                    )

                    Spacer(modifier = Modifier.height(14.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceAround,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        RotaryKnob(
                            value = equalizerState.bassBoost,
                            onValueChange = {
                                if (equalizerState.isEnabled) {
                                    onStateChange(equalizerState.copy(bassBoost = it))
                                }
                            },
                            label = "Bass Boost"
                        )

                        RotaryKnob(
                            value = equalizerState.virtualizer,
                            onValueChange = {
                                if (equalizerState.isEnabled) {
                                    onStateChange(equalizerState.copy(virtualizer = it))
                                }
                            },
                            label = "3D Virtualizer"
                        )
                    }
                }
            }
        }
    }
}

// -------------------------------------------------------------------------------------------------
// COMPONENT: AI-POWERED DYNAMIC EQUALIZER CARD
// -------------------------------------------------------------------------------------------------
@Composable
private fun AiGenreAutoTuneCard(
    isAutoGenreEnabled: Boolean,
    currentSong: Song?,
    activePreset: String,
    isAnalyzing: Boolean,
    suggestion: AiEqualizerSuggestion?,
    hasApplied: Boolean,
    onToggleAuto: (Boolean) -> Unit,
    onAnalyzeClick: () -> Unit,
    onApplySuggestion: (AiEqualizerSuggestion) -> Unit
) {
    val colors = LocalSwarixColors.current

    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp)
            .clip(RoundedCornerShape(22.dp))
            .border(
                1.dp,
                Brush.linearGradient(
                    listOf(
                        Color(0xFF1DB954).copy(alpha = 0.45f),
                        colors.primaryAccent.copy(alpha = 0.35f),
                        Color.Transparent
                    )
                ),
                RoundedCornerShape(22.dp)
            )
            .testTag("ai_genre_autotune_card"),
        color = colors.elevatedSurface
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    Brush.verticalGradient(
                        listOf(
                            Color(0xFF141F17),
                            colors.elevatedSurface
                        )
                    )
                )
                .padding(16.dp)
        ) {
            // Header: Sparkle + Title + Auto Toggle
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.weight(1f)
                ) {
                    Box(
                        modifier = Modifier
                            .size(38.dp)
                            .clip(CircleShape)
                            .background(Color(0xFF1DB954).copy(alpha = 0.20f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.AutoAwesome,
                            contentDescription = null,
                            tint = Color(0xFF1DB954),
                            modifier = Modifier.size(20.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = "AI Master Auto-Tune",
                                style = MaterialTheme.typography.titleSmall.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = colors.primaryText
                                )
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Surface(
                                shape = RoundedCornerShape(6.dp),
                                color = Color(0xFF1DB954).copy(alpha = 0.18f)
                            ) {
                                Text(
                                    text = "GEMINI AI",
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        fontSize = 9.sp,
                                        fontWeight = FontWeight.Black,
                                        color = Color(0xFF1DB954),
                                        letterSpacing = 0.5.sp
                                    ),
                                    modifier = Modifier.padding(horizontal = 5.dp, vertical = 2.dp)
                                )
                            }
                        }
                        Text(
                            text = if (isAutoGenreEnabled) "Live harmonic analysis active" else "Auto-tune paused",
                            style = MaterialTheme.typography.labelSmall.copy(color = colors.mutedText)
                        )
                    }
                }

                // Switch / Toggle
                Surface(
                    shape = RoundedCornerShape(14.dp),
                    color = if (isAutoGenreEnabled) Color(0xFF1DB954) else colors.surface,
                    modifier = Modifier.bouncyClick { onToggleAuto(!isAutoGenreEnabled) }
                ) {
                    Text(
                        text = if (isAutoGenreEnabled) "Auto ON" else "Enable",
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontWeight = FontWeight.Bold,
                            color = if (isAutoGenreEnabled) Color.Black else colors.primaryAccent
                        ),
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Currently Playing Track Context
            if (currentSong != null) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(Color.Black.copy(alpha = 0.25f))
                        .padding(horizontal = 10.dp, vertical = 8.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.MusicNote,
                        contentDescription = null,
                        tint = colors.primaryAccent,
                        modifier = Modifier.size(16.dp)
                    )
                    Text(
                        text = "${currentSong.title} • ${currentSong.artist}",
                        style = MaterialTheme.typography.bodySmall.copy(
                            fontWeight = FontWeight.Medium,
                            color = colors.primaryText
                        ),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f)
                    )
                    Text(
                        text = activePreset,
                        style = MaterialTheme.typography.labelSmall.copy(
                            color = colors.secondaryText,
                            fontWeight = FontWeight.Bold
                        )
                    )
                }
            } else {
                Text(
                    text = "Play a track to analyze acoustic profile with Gemini",
                    style = MaterialTheme.typography.bodySmall.copy(color = colors.mutedText),
                    modifier = Modifier.padding(vertical = 4.dp)
                )
            }

            // Loading / Progress Indicator
            if (isAnalyzing) {
                Spacer(modifier = Modifier.height(10.dp))
                LinearProgressIndicator(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(3.dp)
                        .clip(RoundedCornerShape(2.dp)),
                    color = Color(0xFF1DB954),
                    trackColor = colors.surface
                )
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = "Gemini analyzing acoustic spectrum & emotional mood...",
                    style = MaterialTheme.typography.labelSmall.copy(
                        color = Color(0xFF1DB954),
                        fontWeight = FontWeight.Medium
                    )
                )
            }

            // AI Suggestion Content
            if (suggestion != null) {
                Spacer(modifier = Modifier.height(12.dp))

                // Detected Tags
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = colors.surface
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        ) {
                            Text(
                                text = "Genre: ",
                                style = MaterialTheme.typography.labelSmall.copy(color = colors.mutedText)
                            )
                            Text(
                                text = suggestion.detectedGenre,
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFF1DB954)
                                )
                            )
                        }
                    }

                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = colors.surface
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        ) {
                            Text(
                                text = "Mood: ",
                                style = MaterialTheme.typography.labelSmall.copy(color = colors.mutedText)
                            )
                            Text(
                                text = suggestion.detectedMood,
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = colors.primaryAccent
                                )
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Acoustic Mastering Reasoning
                Text(
                    text = "“${suggestion.reasoning}”",
                    style = MaterialTheme.typography.bodySmall.copy(
                        color = colors.secondaryText,
                        fontSize = 11.5.sp,
                        lineHeight = 16.sp
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(8.dp))
                        .background(Color.White.copy(alpha = 0.04f))
                        .padding(horizontal = 10.dp, vertical = 6.dp)
                )

                Spacer(modifier = Modifier.height(10.dp))

                // Mini Frequency preview
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    EqualizerPresets.BAND_FREQUENCIES.forEachIndexed { i, freq ->
                        val level = suggestion.bandLevels.getOrElse(i) { 0 }
                        Surface(
                            shape = RoundedCornerShape(6.dp),
                            color = if (level > 0) Color(0xFF1DB954).copy(alpha = 0.15f)
                            else if (level < 0) colors.primaryAccent.copy(alpha = 0.10f)
                            else colors.surface
                        ) {
                            Text(
                                text = "$freq: ${if (level > 0) "+$level" else "$level"}dB",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = if (level > 0) Color(0xFF1DB954)
                                    else if (level < 0) colors.primaryAccent
                                    else colors.mutedText
                                ),
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp)
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Action Buttons
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Button(
                        onClick = { onApplySuggestion(suggestion) },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (hasApplied) Color(0xFF2E7D32) else Color(0xFF1DB954),
                            contentColor = Color.Black
                        ),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .weight(1f)
                            .testTag("apply_ai_tuning_button")
                    ) {
                        Icon(
                            imageVector = if (hasApplied) Icons.Default.Check else Icons.Default.AutoAwesome,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp),
                            tint = if (hasApplied) Color.White else Color.Black
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = if (hasApplied) "Applied to EQ" else "Apply AI Frequency Tuning",
                            style = MaterialTheme.typography.labelMedium.copy(
                                fontWeight = FontWeight.Bold,
                                color = if (hasApplied) Color.White else Color.Black
                            )
                        )
                    }

                    IconButton(
                        onClick = onAnalyzeClick,
                        modifier = Modifier.testTag("ai_eq_analyze_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.RestartAlt,
                            contentDescription = "Re-analyze with AI",
                            tint = colors.secondaryText
                        )
                    }
                }
            } else if (!isAnalyzing && currentSong != null) {
                Spacer(modifier = Modifier.height(10.dp))
                Button(
                    onClick = onAnalyzeClick,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = colors.surface,
                        contentColor = Color(0xFF1DB954)
                    ),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("ai_eq_analyze_button")
                ) {
                    Icon(
                        imageVector = Icons.Default.AutoAwesome,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp),
                        tint = Color(0xFF1DB954)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "AI Analyze & Auto-Tune with Gemini",
                        style = MaterialTheme.typography.labelMedium.copy(
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF1DB954)
                        )
                    )
                }
            }
        }
    }
}

// -------------------------------------------------------------------------------------------------
// COMPONENT: PRESET SELECTOR ROW
// -------------------------------------------------------------------------------------------------
@Composable
private fun PresetSelectorRow(
    selectedPreset: String,
    isEnabled: Boolean,
    onSelectPreset: (EqualizerPresets.PresetDefinition) -> Unit
) {
    val colors = LocalSwarixColors.current
    val presets = EqualizerPresets.PRESETS

    Column(modifier = Modifier.padding(vertical = 4.dp)) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState())
                .padding(horizontal = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            presets.values.forEach { preset ->
                val isSelected = selectedPreset == preset.name
                FilterChip(
                    selected = isSelected,
                    onClick = {
                        if (isEnabled) {
                            onSelectPreset(preset)
                        }
                    },
                    label = {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(text = preset.iconEmoji, fontSize = 13.sp)
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = preset.name,
                                style = MaterialTheme.typography.labelMedium.copy(
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
                                )
                            )
                        }
                    },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = colors.primaryAccent,
                        selectedLabelColor = colors.mainBackground,
                        containerColor = colors.surface,
                        labelColor = colors.secondaryText
                    ),
                    border = FilterChipDefaults.filterChipBorder(
                        enabled = isEnabled,
                        selected = isSelected,
                        borderColor = if (isSelected) colors.primaryAccent else colors.divider
                    ),
                    shape = RoundedCornerShape(20.dp),
                    enabled = isEnabled,
                    modifier = Modifier.testTag("preset_${preset.name.lowercase().replace(" ", "_")}")
                )
            }
        }

        // Active Preset Description Tagline
        val currentDef = presets[selectedPreset]
        if (currentDef != null && currentDef.description.isNotBlank()) {
            Text(
                text = currentDef.description,
                style = MaterialTheme.typography.labelSmall.copy(
                    color = colors.secondaryText,
                    fontSize = 11.sp
                ),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.padding(horizontal = 20.dp, vertical = 4.dp)
            )
        }
    }
}

// -------------------------------------------------------------------------------------------------
// COMPONENT: REAL-TIME FREQUENCY RESPONSE CURVE VISUALIZER (CANVAS)
// -------------------------------------------------------------------------------------------------
@Composable
private fun FrequencyCurveVisualizer(
    bandLevels: List<Int>,
    isEnabled: Boolean,
    modifier: Modifier = Modifier
) {
    val colors = LocalSwarixColors.current
    val accentColor = if (isEnabled) colors.primaryAccent else colors.mutedText

    Surface(
        modifier = modifier.clip(RoundedCornerShape(18.dp)),
        color = colors.surface.copy(alpha = 0.7f)
    ) {
        Canvas(modifier = Modifier.fillMaxSize().padding(horizontal = 16.dp, vertical = 12.dp)) {
            val width = size.width
            val height = size.height
            val midY = height / 2f

            // Draw 0 dB Reference Center Line
            drawLine(
                color = colors.divider.copy(alpha = 0.5f),
                start = Offset(0f, midY),
                end = Offset(width, midY),
                strokeWidth = 1.dp.toPx()
            )

            if (bandLevels.isEmpty()) return@Canvas

            // Compute point coordinates for each of the 10 bands
            val stepX = width / (bandLevels.size - 1).coerceAtLeast(1)
            val points = bandLevels.mapIndexed { index, levelDb ->
                val x = index * stepX
                // levelDb: -10 to +10 dB
                val norm = (levelDb / 10f).coerceIn(-1f, 1f)
                val y = midY - (norm * (height * 0.42f))
                Offset(x, y)
            }

            // Build smooth Bezier path connecting all points
            val path = Path()
            val fillPath = Path()

            path.moveTo(points.first().x, points.first().y)
            fillPath.moveTo(points.first().x, midY)
            fillPath.lineTo(points.first().x, points.first().y)

            for (i in 0 until points.size - 1) {
                val p0 = points[i]
                val p1 = points[i + 1]
                val controlX = (p0.x + p1.x) / 2f
                path.cubicTo(controlX, p0.y, controlX, p1.y, p1.x, p1.y)
                fillPath.cubicTo(controlX, p0.y, controlX, p1.y, p1.x, p1.y)
            }

            fillPath.lineTo(points.last().x, midY)
            fillPath.close()

            // Fill under curve
            if (isEnabled) {
                drawPath(
                    path = fillPath,
                    brush = Brush.verticalGradient(
                        colors = listOf(
                            accentColor.copy(alpha = 0.35f),
                            accentColor.copy(alpha = 0.05f)
                        ),
                        startY = 0f,
                        endY = height
                    )
                )
            }

            // Draw Curve Stroke
            drawPath(
                path = path,
                color = accentColor,
                style = Stroke(width = 2.5.dp.toPx(), cap = StrokeCap.Round)
            )

            // Draw glowing control dots on each frequency node
            points.forEach { pt ->
                drawCircle(
                    color = accentColor,
                    radius = 3.dp.toPx(),
                    center = pt
                )
            }
        }
    }
}

// -------------------------------------------------------------------------------------------------
// COMPONENT: INDIVIDUAL 10-BAND SLIDER COLUMN
// -------------------------------------------------------------------------------------------------
@Composable
private fun BandSliderColumn(
    frequency: String,
    levelDb: Int,
    isEnabled: Boolean,
    onLevelChange: (Int) -> Unit
) {
    val colors = LocalSwarixColors.current

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
        modifier = Modifier
            .width(46.dp)
            .height(210.dp)
    ) {
        // Gain Readout (+3, 0, -4)
        Text(
            text = if (levelDb > 0) "+$levelDb" else "$levelDb",
            style = MaterialTheme.typography.labelSmall.copy(
                fontWeight = FontWeight.ExtraBold,
                color = when {
                    !isEnabled -> colors.mutedText
                    levelDb > 0 -> colors.primaryAccent
                    levelDb < 0 -> Color(0xFFFF8A80)
                    else -> colors.secondaryText
                },
                fontSize = 11.sp
            )
        )

        // Vertical Slider Stage
        Box(
            modifier = Modifier
                .weight(1f)
                .width(42.dp),
            contentAlignment = Alignment.Center
        ) {
            Slider(
                value = levelDb.toFloat(),
                onValueChange = { newLevel ->
                    if (isEnabled) {
                        onLevelChange(newLevel.toInt())
                    }
                },
                valueRange = -10f..10f,
                steps = 20,
                colors = SliderDefaults.colors(
                    thumbColor = if (isEnabled) colors.primaryAccent else colors.mutedText,
                    activeTrackColor = if (isEnabled) colors.primaryAccent else colors.divider,
                    inactiveTrackColor = colors.divider
                ),
                enabled = isEnabled,
                modifier = Modifier
                    .size(width = 135.dp, height = 36.dp)
                    .rotate(-90f)
                    .testTag("slider_$frequency")
            )
        }

        // Frequency Label
        Surface(
            shape = RoundedCornerShape(6.dp),
            color = colors.surface
        ) {
            Text(
                text = frequency,
                style = MaterialTheme.typography.labelSmall.copy(
                    color = colors.primaryText,
                    fontWeight = FontWeight.Bold,
                    fontSize = 10.sp
                ),
                modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp)
            )
        }
    }
}
