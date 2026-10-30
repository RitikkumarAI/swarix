package com.example.core.equalizer

import com.example.core.model.EqualizerState
import com.example.core.model.Song

/**
 * 10-Band Graphic Equalizer Preset Definitions & Genre-Based Dynamic Auto-Tuner.
 * Frequencies: 31Hz, 62Hz, 125Hz, 250Hz, 500Hz, 1kHz, 2kHz, 4kHz, 8kHz, 16kHz
 */
object EqualizerPresets {

    val BAND_FREQUENCIES = listOf(
        "31Hz", "62Hz", "125Hz", "250Hz", "500Hz",
        "1kHz", "2kHz", "4kHz", "8kHz", "16kHz"
    )

    data class PresetDefinition(
        val name: String,
        val bandLevels: List<Int>, // 10 bands from -10 to +10 dB
        val bassBoost: Int,        // 0 to 100
        val virtualizer: Int,      // 0 to 100
        val recommendedGenres: List<String>,
        val description: String,
        val iconEmoji: String
    )

    val PRESETS = linkedMapOf(
        "8K Ultra Studio Master" to PresetDefinition(
            name = "8K Ultra Studio Master",
            bandLevels = listOf(6, 5, 3, 2, 1, 2, 4, 6, 8, 9),
            bassBoost = 80,
            virtualizer = 60,
            recommendedGenres = listOf("All Genres", "Master", "Hi-Res", "Audiophile"),
            description = "Pristine 8K Ultra studio acoustic mastering with crystal highs, punchy sub-bass, and wide 3D binaural soundstage.",
            iconEmoji = "💎"
        ),
        "Bass Boost" to PresetDefinition(
            name = "Bass Boost",
            bandLevels = listOf(8, 7, 5, 2, 0, -1, 0, 1, 2, 3),
            bassBoost = 85,
            virtualizer = 35,
            recommendedGenres = listOf("Workout", "Party", "EDM", "Dance", "Electronic", "Club"),
            description = "Sub-bass excitation and heavy low-end punch for electronic drops and workout beats.",
            iconEmoji = "🔊"
        ),
        "Acoustic" to PresetDefinition(
            name = "Acoustic",
            bandLevels = listOf(2, 3, 2, 1, 2, 3, 4, 5, 4, 3),
            bassBoost = 25,
            virtualizer = 40,
            recommendedGenres = listOf("Acoustic", "Indie", "Folk", "Unplugged", "Lo-Fi", "Chill"),
            description = "Warm resonance and crisp string articulation for organic acoustic instruments.",
            iconEmoji = "🎸"
        ),
        "Vocal" to PresetDefinition(
            name = "Vocal",
            bandLevels = listOf(-3, -2, 0, 3, 6, 7, 5, 3, 1, -1),
            bassBoost = 15,
            virtualizer = 25,
            recommendedGenres = listOf("Romance", "Bollywood Romance", "Podcast", "Soul", "Ghazal", "Sad"),
            description = "Pushes 1kHz-4kHz speech and singing formant frequencies front and center.",
            iconEmoji = "🎤"
        ),
        "Pop" to PresetDefinition(
            name = "Pop",
            bandLevels = listOf(-1, 1, 3, 4, 5, 4, 2, 1, 0, 1),
            bassBoost = 40,
            virtualizer = 30,
            recommendedGenres = listOf("Pop", "Bollywood", "Commercial", "Dance Pop"),
            description = "Vibrant, forward modern mastering response for sparkling radio and streaming hooks.",
            iconEmoji = "✨"
        ),
        "Rock" to PresetDefinition(
            name = "Rock",
            bandLevels = listOf(5, 4, 2, -1, -2, 1, 3, 5, 6, 6),
            bassBoost = 55,
            virtualizer = 35,
            recommendedGenres = listOf("Rock", "Alternative", "Metal", "Hard Rock", "Punk"),
            description = "Aggressive upper-mid guitars, driving kick drum thump, and shimmering cymbals.",
            iconEmoji = "⚡"
        ),
        "Classical" to PresetDefinition(
            name = "Classical",
            bandLevels = listOf(4, 3, 2, 2, -1, -1, 1, 3, 4, 5),
            bassBoost = 20,
            virtualizer = 55,
            recommendedGenres = listOf("Classical", "Devotional", "Symphony", "Orchestral", "Spiritual"),
            description = "Concert hall acoustic space with lush stereo width and delicate dynamic range.",
            iconEmoji = "🎻"
        ),
        "Electronic" to PresetDefinition(
            name = "Electronic",
            bandLevels = listOf(7, 6, 4, 1, -1, 2, 4, 6, 7, 6),
            bassBoost = 75,
            virtualizer = 45,
            recommendedGenres = listOf("House", "Techno", "Synthwave", "Trance"),
            description = "Elevated sub-bass and ultra-high air for synthesizers and crisp hi-hat rhythms.",
            iconEmoji = "🎛️"
        ),
        "Hip Hop" to PresetDefinition(
            name = "Hip Hop",
            bandLevels = listOf(8, 7, 4, 1, 2, 1, 2, 3, 4, 4),
            bassBoost = 75,
            virtualizer = 30,
            recommendedGenres = listOf("Hip Hop", "Rap", "Trap", "R&B"),
            description = "Thumping 808 sub-bass accompanied by sharp snare cracks and transparent vocals.",
            iconEmoji = "🎧"
        ),
        "Jazz" to PresetDefinition(
            name = "Jazz",
            bandLevels = listOf(3, 2, 1, 2, -1, -1, 0, 2, 3, 3),
            bassBoost = 35,
            virtualizer = 35,
            recommendedGenres = listOf("Jazz", "Blues", "Soul", "Lounge"),
            description = "Warm upright bass tone, velvety brass textures, and mellow brushed snare sounds.",
            iconEmoji = "🎷"
        ),
        "Flat" to PresetDefinition(
            name = "Flat",
            bandLevels = listOf(0, 0, 0, 0, 0, 0, 0, 0, 0, 0),
            bassBoost = 0,
            virtualizer = 0,
            recommendedGenres = emptyList(),
            description = "Pure, uncolored studio audio reproduction as the mixing engineer intended.",
            iconEmoji = "⚖️"
        ),
        "Custom" to PresetDefinition(
            name = "Custom",
            bandLevels = listOf(3, 2, 1, 0, 1, 2, 4, 3, 2, 2),
            bassBoost = 45,
            virtualizer = 30,
            recommendedGenres = emptyList(),
            description = "User fine-tuned custom EQ curve.",
            iconEmoji = "🛠️"
        )
    )

    /**
     * Determines the optimal EQ preset dynamically based on the song's genre and mood tags.
     */
    fun getPresetForGenre(genre: String, mood: String = ""): PresetDefinition {
        val query = "$genre $mood".lowercase()
        return when {
            query.contains("workout") || query.contains("party") || query.contains("edm") ||
            query.contains("club") -> PRESETS["Bass Boost"]!!

            query.contains("electronic") || query.contains("dance") || query.contains("synth") ||
            query.contains("house") || query.contains("techno") -> PRESETS["Electronic"]!!

            query.contains("acoustic") || query.contains("indie") || query.contains("folk") ||
            query.contains("unplugged") || query.contains("chill") || query.contains("focus") -> PRESETS["Acoustic"]!!

            query.contains("romance") || query.contains("vocal") || query.contains("love") ||
            query.contains("ghazal") || query.contains("podcast") || query.contains("sad") -> PRESETS["Vocal"]!!

            query.contains("rock") || query.contains("metal") || query.contains("alternative") ||
            query.contains("grunge") || query.contains("punk") -> PRESETS["Rock"]!!

            query.contains("classical") || query.contains("devotional") || query.contains("sufi") ||
            query.contains("spiritual") || query.contains("ambient") -> PRESETS["Classical"]!!

            query.contains("hip hop") || query.contains("rap") || query.contains("trap") ||
            query.contains("r&b") -> PRESETS["Hip Hop"]!!

            query.contains("jazz") || query.contains("blues") || query.contains("soul") -> PRESETS["Jazz"]!!

            else -> PRESETS["Pop"]!!
        }
    }

    /**
     * Generates a new EqualizerState adapted to the provided song.
     */
    fun autoTuneForSong(song: Song, currentState: EqualizerState): EqualizerState {
        val preset = getPresetForGenre(song.genre, song.mood)
        return currentState.copy(
            presetName = preset.name,
            bandLevels = preset.bandLevels,
            bassBoost = preset.bassBoost,
            virtualizer = preset.virtualizer,
            detectedGenre = if (song.genre.isNotBlank()) song.genre else "Auto"
        )
    }
}
