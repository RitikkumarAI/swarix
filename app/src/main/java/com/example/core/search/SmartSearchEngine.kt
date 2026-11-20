package com.example.core.search

import com.example.core.model.Song

data class ScoredSearchResult(
    val song: Song,
    val score: Double,
    val matchedLyricSnippet: String? = null,
    val matchType: String = "TITLE"
)

/**
 * Advanced Multi-Signal Search Engine with Typo Tolerance,
 * Phonetic Transliteration, and Multilingual Ranking.
 */
object SmartSearchEngine {

    /**
     * Calculates the Levenshtein edit distance between two strings.
     */
    fun levenshteinDistance(s1: String, s2: String): Int {
        val dp = Array(s1.length + 1) { IntArray(s2.length + 1) }
        for (i in 0..s1.length) dp[i][0] = i
        for (j in 0..s2.length) dp[0][j] = j

        for (i in 1..s1.length) {
            for (j in 1..s2.length) {
                val cost = if (s1[i - 1].lowercaseChar() == s2[j - 1].lowercaseChar()) 0 else 1
                dp[i][j] = minOf(
                    dp[i - 1][j] + 1,      // deletion
                    dp[i][j - 1] + 1,      // insertion
                    dp[i - 1][j - 1] + cost // substitution
                )
            }
        }
        return dp[s1.length][s2.length]
    }

    /**
     * Checks if two words match fuzzily with allowed typo tolerance.
     */
    fun isFuzzyMatch(word1: String, word2: String, maxDistance: Int = 2): Boolean {
        if (word1.equals(word2, ignoreCase = true)) return true
        if (word1.contains(word2, ignoreCase = true) || word2.contains(word1, ignoreCase = true)) return true
        val lenDiff = kotlin.math.abs(word1.length - word2.length)
        if (lenDiff > maxDistance) return false
        return levenshteinDistance(word1.lowercase(), word2.lowercase()) <= maxDistance
    }

    /**
     * Ranks catalog songs for a search query using multi-field relevance scoring.
     */
    fun rankSongsForQuery(
        query: String,
        catalog: List<Song>,
        limit: Int = 30
    ): List<ScoredSearchResult> {
        val trimmed = query.trim()
        if (trimmed.isBlank()) return emptyList()

        val lowerQuery = trimmed.lowercase()
        val phoneticQuery = LyricsSearchEngine.normalizePhonetic(trimmed)
        val queryTokens = lowerQuery.split(Regex("\\s+")).filter { it.isNotBlank() }

        val scoredResults = mutableListOf<ScoredSearchResult>()

        for (song in catalog) {
            var score = 0.0
            var lyricSnippet: String? = null
            var matchType = "TITLE"

            val lowerTitle = song.title.lowercase()
            val lowerArtist = song.artist.lowercase()
            val lowerAlbum = song.album.lowercase()
            val lowerGenre = song.genre.lowercase()
            val lowerMood = song.mood.lowercase()
            val phoneticTitle = LyricsSearchEngine.normalizePhonetic(song.title)

            // 1. Exact Title Match
            if (lowerTitle == lowerQuery || phoneticTitle == phoneticQuery) {
                score += 120.0
                matchType = "EXACT_TITLE"
            }
            // 2. Title Starts With Query
            else if (lowerTitle.startsWith(lowerQuery) || phoneticTitle.startsWith(phoneticQuery)) {
                score += 90.0
                matchType = "TITLE_PREFIX"
            }
            // 3. Substring in Title
            else if (lowerTitle.contains(lowerQuery) || phoneticTitle.contains(phoneticQuery)) {
                score += 75.0
                matchType = "TITLE_SUBSTRING"
            }

            // 4. Artist Match
            if (lowerArtist.equals(lowerQuery, ignoreCase = true)) {
                score += 85.0
                matchType = "ARTIST_EXACT"
            } else if (lowerArtist.contains(lowerQuery)) {
                score += 60.0
                if (matchType == "TITLE") matchType = "ARTIST"
            }

            // 5. Token Level Word Matches
            var tokenHits = 0
            for (token in queryTokens) {
                if (lowerTitle.contains(token) || lowerArtist.contains(token)) {
                    tokenHits++
                } else if (isFuzzyMatch(token, lowerTitle) || isFuzzyMatch(token, lowerArtist)) {
                    tokenHits++
                    score += 15.0
                }
            }
            if (tokenHits > 0) {
                score += (tokenHits.toDouble() / queryTokens.size.toDouble()) * 40.0
            }

            // 6. Typo Tolerance for full query against title
            if (score == 0.0 && trimmed.length >= 4) {
                val distance = levenshteinDistance(lowerTitle.take(trimmed.length), lowerQuery)
                if (distance <= 2) {
                    score += (35.0 - (distance * 10))
                    matchType = "TYPO_FUZZY"
                }
            }

            // 7. Album, Genre, Mood matches
            if (lowerAlbum.contains(lowerQuery)) score += 25.0
            if (lowerGenre.contains(lowerQuery)) score += 20.0
            if (lowerMood.contains(lowerQuery)) score += 20.0

            // 8. Lyrics Search Match
            val lyricMatch = LyricsSearchEngine.searchSongLyrics(song, trimmed)
            if (lyricMatch != null) {
                score += (lyricMatch.matchScore * 0.7) // up to 70 points
                lyricSnippet = lyricMatch.matchedSnippet
                if (score < 60.0) matchType = "LYRICS"
            }

            // Bonus for popularity / favorites
            if (song.isFavorite) score += 5.0
            score += (song.playCount.coerceAtMost(20) * 0.5)

            if (score > 15.0) {
                scoredResults.add(ScoredSearchResult(song, score, lyricSnippet, matchType))
            }
        }

        return scoredResults
            .sortedByDescending { it.score }
            .distinctBy { it.song.id }
            .take(limit)
    }
}
