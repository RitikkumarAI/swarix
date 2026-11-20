package com.example.core.search

import com.example.core.model.Song

data class LyricMatchResult(
    val song: Song,
    val matchedSnippet: String,
    val matchScore: Double
)

/**
 * Advanced Multilingual Lyrics Search Engine for SOREX.
 * Supports exact phrase matching, partial token matching, fuzzy search,
 * and Devanagari <-> Hinglish transliteration equivalence.
 */
object LyricsSearchEngine {

    // Devanagari to Roman transliteration mapping table
    private val DEVANAGARI_MAP = mapOf(
        'अ' to "a", 'आ' to "aa", 'इ' to "i", 'ई' to "ee", 'उ' to "u", 'ऊ' to "oo",
        'ए' to "e", 'ऐ' to "ai", 'ओ' to "o", 'औ' to "au", 'क' to "k", 'ख' to "kh",
        'ग' to "g", 'घ' to "gh", 'च' to "ch", 'छ' to "chh", 'ज' to "j", 'झ' to "jh",
        'ट' to "t", 'ठ' to "th", 'ड' to "d", 'ढ' to "dh", 'ण' to "n", 'त' to "t",
        'थ' to "th", 'द' to "d", 'ध' to "dh", 'न' to "n", 'प' to "p", 'फ' to "ph",
        'ब' to "b", 'भ' to "bh", 'म' to "m", 'य' to "y", 'र' to "r", 'ल' to "l",
        'व' to "v", 'श' to "sh", 'ष' to "sh", 'स' to "s", 'ह' to "h",
        'ा' to "a", 'ि' to "i", 'ी' to "ee", 'ु' to "u", 'ू' to "oo",
        'े' to "e", 'ै' to "ai", 'ो' to "o", 'ौ' to "au", '्' to "",
        'ं' to "n", 'ँ' to "n", 'ः' to "h"
    )

    /**
     * Normalizes text by converting Devanagari characters to Roman phonetics,
     * stripping punctuation, and unifying common Hinglish spelling variants
     * (e.g., 'ee' <-> 'i', 'oo' <-> 'u', 'aa' <-> 'a').
     */
    fun normalizePhonetic(input: String): String {
        val transliterated = StringBuilder()
        for (ch in input) {
            val replacement = DEVANAGARI_MAP[ch]
            if (replacement != null) {
                transliterated.append(replacement)
            } else {
                transliterated.append(ch)
            }
        }

        var normalized = transliterated.toString().lowercase()
        // Strip non-alphanumeric except spaces
        normalized = normalized.replace(Regex("[^a-z0-9\\s]"), " ")

        // Unify phonetic variants
        normalized = normalized
            .replace("ee", "i")
            .replace("oo", "u")
            .replace("aa", "a")
            .replace("sh", "s")
            .replace("kh", "k")
            .replace("bh", "b")
            .replace("dh", "d")
            .replace("th", "t")
            .replace("ph", "f")
            .replace(Regex("\\s+"), " ")
            .trim()

        return normalized
    }

    /**
     * Searches a song's lyrics for query matches.
     * Returns a LyricMatchResult with score and highlighted context snippet if found.
     */
    fun searchSongLyrics(song: Song, query: String): LyricMatchResult? {
        val lyrics = song.lyrics.trim()
        if (lyrics.isBlank() || query.isBlank()) return null

        val normQuery = normalizePhonetic(query)
        if (normQuery.length < 3) return null

        val lines = lyrics.lines().map { it.trim() }.filter { it.isNotBlank() }
        val queryTokens = normQuery.split(" ").filter { it.length >= 2 }
        if (queryTokens.isEmpty()) return null

        var bestScore = 0.0
        var bestSnippet = ""

        // 1. Check for exact full phrase match across lines
        val fullLyricsNorm = normalizePhonetic(lyrics)
        if (fullLyricsNorm.contains(normQuery)) {
            // Find the original line that contains this
            for (line in lines) {
                if (normalizePhonetic(line).contains(normQuery) || line.contains(query, ignoreCase = true)) {
                    bestSnippet = line
                    bestScore = 100.0
                    break
                }
            }
            if (bestSnippet.isBlank()) {
                bestSnippet = lines.firstOrNull { normalizePhonetic(it).contains(queryTokens.first()) } ?: lines.first()
                bestScore = 90.0
            }
            return LyricMatchResult(song, bestSnippet, bestScore)
        }

        // 2. Multi-token match across individual lines
        for (line in lines) {
            val normLine = normalizePhonetic(line)
            var tokenHits = 0
            for (token in queryTokens) {
                if (normLine.contains(token)) {
                    tokenHits++
                }
            }

            if (tokenHits > 0) {
                val lineScore = (tokenHits.toDouble() / queryTokens.size.toDouble()) * 80.0
                if (lineScore > bestScore) {
                    bestScore = lineScore
                    bestSnippet = line
                }
            }
        }

        return if (bestScore >= 50.0) {
            LyricMatchResult(song, bestSnippet, bestScore)
        } else {
            null
        }
    }

    /**
     * Searches a collection of songs for lyrics matching the query.
     */
    fun searchCatalogByLyrics(catalog: List<Song>, query: String, limit: Int = 10): List<LyricMatchResult> {
        return catalog.mapNotNull { searchSongLyrics(it, query) }
            .sortedByDescending { it.matchScore }
            .take(limit)
    }
}
