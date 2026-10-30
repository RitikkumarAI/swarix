package com.example.data.repository

import android.content.ContentUris
import android.content.Context
import android.database.Cursor
import android.net.Uri
import android.os.Build
import android.provider.MediaStore
import android.util.Log
import com.example.core.model.Album
import com.example.core.model.Artist
import com.example.core.model.FriendActivity
import com.example.core.model.GenreStats
import com.example.core.model.ListeningStats
import com.example.core.model.Playlist
import com.example.core.model.Song
import com.example.data.local.EqualizerPresetEntity
import com.example.data.local.PlaybackHistoryEntity
import com.example.data.local.PlaylistEntity
import com.example.data.local.PlaylistSongCrossRef
import com.example.data.local.SearchQueryEntity
import com.example.data.local.SongEntity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import com.example.data.local.MusicDao
import com.example.data.local.LocalMediaMetadataHelper

class MusicRepository(
    private val context: Context,
    private val musicDao: MusicDao
) {

    private val metadataHelper = LocalMediaMetadataHelper(context)

    val allSongs: Flow<List<Song>> = musicDao.getAllSongs().map { entities ->
        entities.map { it.toDomain() }
    }.flowOn(Dispatchers.IO)

    val favoriteSongs: Flow<List<Song>> = musicDao.getFavoriteSongs().map { entities ->
        entities.map { it.toDomain() }
    }.flowOn(Dispatchers.IO)

    val recentlyPlayed: Flow<List<Song>> = musicDao.getRecentlyPlayed().map { entities ->
        entities.map { it.toDomain() }
    }.flowOn(Dispatchers.IO)

    val mostPlayed: Flow<List<Song>> = musicDao.getMostPlayed().map { entities ->
        entities.map { it.toDomain() }
    }.flowOn(Dispatchers.IO)

    val allPlaylists: Flow<List<Playlist>> = musicDao.getAllPlaylists().map { entities ->
        entities.map { it.toDomain() }
    }.flowOn(Dispatchers.IO)

    suspend fun getSongById(id: String): Song? {
        return withContext(Dispatchers.IO) {
            musicDao.getSongById(id)?.toDomain()
        }
    }

    fun getSongsForPlaylist(playlistId: Long): Flow<List<Song>> {
        return musicDao.getSongsForPlaylist(playlistId).map { entities ->
            entities.map { it.toDomain() }
        }.flowOn(Dispatchers.IO)
    }

    fun search(query: String): Flow<List<Song>> {
        return musicDao.searchSongs(query).map { entities ->
            entities.map { it.toDomain() }
        }.flowOn(Dispatchers.IO)
    }

    suspend fun toggleFavorite(songId: String, isCurrentlyFavorite: Boolean) {
        withContext(Dispatchers.IO) {
            musicDao.updateFavorite(songId, !isCurrentlyFavorite)
        }
    }

    suspend fun recordPlayback(songId: String) {
        withContext(Dispatchers.IO) {
            musicDao.incrementPlayCount(songId)
            musicDao.insertHistory(PlaybackHistoryEntity(songId = songId))
        }
    }

    suspend fun createPlaylist(name: String, description: String, coverUri: String = ""): Long {
        return withContext(Dispatchers.IO) {
            musicDao.insertPlaylist(
                PlaylistEntity(
                    name = name,
                    description = description,
                    coverUri = coverUri
                )
            )
        }
    }

    suspend fun addSongToPlaylist(playlistId: Long, songId: String) {
        withContext(Dispatchers.IO) {
            musicDao.addSongToPlaylist(PlaylistSongCrossRef(playlistId = playlistId, songId = songId))
        }
    }

    suspend fun removeSongFromPlaylist(playlistId: Long, songId: String) {
        withContext(Dispatchers.IO) {
            musicDao.removeSongFromPlaylist(playlistId, songId)
        }
    }

    suspend fun clearHistory() {
        withContext(Dispatchers.IO) {
            musicDao.clearHistory()
        }
    }

    fun getRecentSearches(): Flow<List<SearchQueryEntity>> = musicDao.getRecentSearches().flowOn(Dispatchers.IO)

    suspend fun insertSearchQuery(query: SearchQueryEntity) {
        withContext(Dispatchers.IO) {
            musicDao.insertSearchQuery(query)
        }
    }

    suspend fun clearSearchQueries() {
        withContext(Dispatchers.IO) {
            musicDao.clearSearchQueries()
        }
    }

    suspend fun initializeCatalogIfEmpty() {
        withContext(Dispatchers.IO) {
            val existing = musicDao.getAllSongs().first()
            if (existing.isEmpty()) {
                val seedSongs = getSeedSongs()
                musicDao.insertSongs(seedSongs)

                // Seed Playlists
                val p1 = musicDao.insertPlaylist(
                    PlaylistEntity(
                        name = "Chill Vibes",
                        description = "A calm collection to relax, focus and feel good. Made by Swarix",
                        coverUri = "https://images.unsplash.com/photo-1518495973542-4542c06a5843?w=600&auto=format&fit=crop&q=80"
                    )
                )
                val p2 = musicDao.insertPlaylist(
                    PlaylistEntity(
                        name = "Workout Mix",
                        description = "High energy tracks to power your training sessions",
                        coverUri = "https://images.unsplash.com/photo-1517838277536-f5f99be501cd?w=600&auto=format&fit=crop&q=80"
                    )
                )
                val p3 = musicDao.insertPlaylist(
                    PlaylistEntity(
                        name = "Romance Hits",
                        description = "Timeless love melodies and romantic ballads",
                        coverUri = "https://images.unsplash.com/photo-1516589178581-6cd7833ae3b2?w=600&auto=format&fit=crop&q=80"
                    )
                )
                val p4 = musicDao.insertPlaylist(
                    PlaylistEntity(
                        name = "Old is Gold",
                        description = "Classic melodies from legendary eras",
                        coverUri = "https://images.unsplash.com/photo-1539185441755-769473a23570?w=600&auto=format&fit=crop&q=80"
                    )
                )
                val p5 = musicDao.insertPlaylist(
                    PlaylistEntity(
                        name = "Study Focus",
                        description = "Minimalist and ambient sounds for peak productivity",
                        coverUri = "https://images.unsplash.com/photo-1434030216411-0b793f4b4173?w=600&auto=format&fit=crop&q=80"
                    )
                )

                // Add songs to playlists
                seedSongs.take(4).forEachIndexed { idx, s ->
                    musicDao.addSongToPlaylist(PlaylistSongCrossRef(p1, s.id, idx))
                }
                seedSongs.filter { it.mood == "Workout" }.forEachIndexed { idx, s ->
                    musicDao.addSongToPlaylist(PlaylistSongCrossRef(p2, s.id, idx))
                }
                seedSongs.filter { it.mood == "Romance" }.forEachIndexed { idx, s ->
                    musicDao.addSongToPlaylist(PlaylistSongCrossRef(p3, s.id, idx))
                }
            }
        }
    }

    suspend fun scanLocalDeviceAudio(): Int {
        return withContext(Dispatchers.IO) {
            val localSongs = mutableListOf<SongEntity>()
            val uri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI
            val projection = arrayOf(
                MediaStore.Audio.Media._ID,
                MediaStore.Audio.Media.TITLE,
                MediaStore.Audio.Media.ARTIST,
                MediaStore.Audio.Media.ALBUM,
                MediaStore.Audio.Media.DURATION,
                MediaStore.Audio.Media.ALBUM_ID,
                MediaStore.Audio.Media.DATE_ADDED
            )
            val selection = "${MediaStore.Audio.Media.IS_MUSIC} != 0 AND ${MediaStore.Audio.Media.DURATION} > 10000"
            val sortOrder = "${MediaStore.Audio.Media.TITLE} ASC"

            try {
                val cursor: Cursor? = context.contentResolver.query(
                    uri,
                    projection,
                    selection,
                    null,
                    sortOrder
                )

                cursor?.use {
                    val idCol = it.getColumnIndexOrThrow(MediaStore.Audio.Media._ID)
                    val titleCol = it.getColumnIndexOrThrow(MediaStore.Audio.Media.TITLE)
                    val artistCol = it.getColumnIndexOrThrow(MediaStore.Audio.Media.ARTIST)
                    val albumCol = it.getColumnIndexOrThrow(MediaStore.Audio.Media.ALBUM)
                    val durationCol = it.getColumnIndexOrThrow(MediaStore.Audio.Media.DURATION)
                    val albumIdCol = it.getColumnIndexOrThrow(MediaStore.Audio.Media.ALBUM_ID)
                    val dateAddedCol = it.getColumnIndexOrThrow(MediaStore.Audio.Media.DATE_ADDED)

                    while (it.moveToNext()) {
                        val mediaId = it.getLong(idCol)
                        val rawTitle = it.getString(titleCol) ?: "Unknown Track"
                        val rawArtist = it.getString(artistCol) ?: "Unknown Artist"
                        val rawAlbum = it.getString(albumCol) ?: "Local Music"
                        val rawDuration = it.getLong(durationCol)
                        val albumId = it.getLong(albumIdCol)
                        val dateAdded = it.getLong(dateAddedCol) * 1000L

                        val trackUri = ContentUris.withAppendedId(
                            MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
                            mediaId
                        ).toString()

                        val fallbackArtUri = ContentUris.withAppendedId(
                            Uri.parse("content://media/external/audio/albumart"),
                            albumId
                        ).toString()

                        // Extract detailed ID3 tags and embedded album art via MediaMetadataRetriever
                        val extracted = metadataHelper.extractMetadata(Uri.parse(trackUri), mediaId.toString())

                        val finalTitle = if (extracted.title != "Unknown Track" && extracted.title != "Local Track") extracted.title else rawTitle
                        val finalArtist = if (extracted.artist != "Unknown Artist") extracted.artist else rawArtist
                        val finalAlbum = if (extracted.album != "Local Music") extracted.album else rawAlbum
                        val finalDuration = if (extracted.durationMs > 0) extracted.durationMs else rawDuration
                        val finalArtUri = if (extracted.artworkUri.isNotEmpty()) extracted.artworkUri else fallbackArtUri

                        localSongs.add(
                            SongEntity(
                                id = "local_$mediaId",
                                title = finalTitle,
                                artist = finalArtist,
                                album = finalAlbum,
                                durationMs = finalDuration,
                                contentUri = trackUri,
                                artworkUri = finalArtUri,
                                genre = extracted.genre,
                                mood = "Chill",
                                lyrics = "Local audio track: $finalTitle by $finalArtist",
                                isFavorite = false,
                                playCount = 0,
                                dateAdded = dateAdded,
                                isLocal = true
                            )
                        )
                    }
                }
            } catch (e: Exception) {
                Log.e("MusicRepository", "Error scanning local device audio", e)
            }

            if (localSongs.isNotEmpty()) {
                musicDao.insertSongs(localSongs)
            }
            localSongs.size
        }
    }

    fun getFriendActivity(): Flow<List<FriendActivity>> = flow {
        val friends = listOf(
            FriendActivity(
                id = "friend_1",
                friendName = "Aryan Sharma",
                avatarUrl = "https://images.unsplash.com/photo-1534528741775-53994a69daeb?w=200&auto=format&fit=crop&q=80",
                songTitle = "Apna Bana Le",
                artistName = "Arijit Singh",
                albumArtUrl = "https://images.unsplash.com/photo-1518495973542-4542c06a5843?w=500&auto=format&fit=crop&q=80",
                statusText = "Listening now",
                isListeningNow = true,
                songId = "swarix_1"
            ),
            FriendActivity(
                id = "friend_2",
                friendName = "Priya Mehta",
                avatarUrl = "https://images.unsplash.com/photo-1517841905240-472988babdf9?w=200&auto=format&fit=crop&q=80",
                songTitle = "Heeriye",
                artistName = "Jasleen Royal & Arijit",
                albumArtUrl = "https://images.unsplash.com/photo-1516589178581-6cd7833ae3b2?w=500&auto=format&fit=crop&q=80",
                statusText = "Listening now",
                isListeningNow = true,
                songId = "swarix_2"
            ),
            FriendActivity(
                id = "friend_3",
                friendName = "Kabir Verma",
                avatarUrl = "https://images.unsplash.com/photo-1506794778202-cad84cf45f1d?w=200&auto=format&fit=crop&q=80",
                songTitle = "Energy Pulse",
                artistName = "Workout Beat Crew",
                albumArtUrl = "https://images.unsplash.com/photo-1517838277536-f5f99be501cd?w=500&auto=format&fit=crop&q=80",
                statusText = "Played 15m ago",
                isListeningNow = false,
                songId = "swarix_11"
            ),
            FriendActivity(
                id = "friend_4",
                friendName = "Ananya Roy",
                avatarUrl = "https://images.unsplash.com/photo-1544005313-94ddf0286df2?w=200&auto=format&fit=crop&q=80",
                songTitle = "Kesariya",
                artistName = "Arijit Singh",
                albumArtUrl = "https://images.unsplash.com/photo-1507525428034-b723cf961d3e?w=500&auto=format&fit=crop&q=80",
                statusText = "Listening now",
                isListeningNow = true,
                songId = "swarix_3"
            )
        )
        emit(friends)
    }

    fun getListeningStats(): Flow<ListeningStats> {
        return musicDao.getAllSongs().map { songs ->
            val totalPlays = songs.sumOf { it.playCount }.coerceAtLeast(1)
            val totalMinutes = songs.sumOf { (it.playCount * (it.durationMs / 60000)).toInt() }.coerceAtLeast(42)

            val sortedSongs = songs.sortedByDescending { it.playCount }
            val topSongs = sortedSongs.take(10).map { it.toDomain() }

            val topArtists = songs
                .groupBy { it.artist }
                .map { (artist, list) -> artist to list.sumOf { it.playCount } }
                .sortedByDescending { it.second }
                .take(5)

            val genreGroups = songs.groupBy { it.genre }
            val topGenres = genreGroups.map { (genre, list) ->
                val count = list.size
                val pct = ((count.toFloat() / songs.size.toFloat().coerceAtLeast(1f)) * 100).toInt()
                GenreStats(genre = genre, percentage = pct, trackCount = count)
            }.sortedByDescending { it.trackCount }.take(4)

            val dominantGenre = topGenres.firstOrNull()?.genre ?: "Bollywood"
            val (personality, description) = when {
                dominantGenre.contains("Romance", ignoreCase = true) ->
                    "Hopeless Romantic" to "Your soul gravitates toward tender verses and sweeping melodies that evoke cinematic love."
                dominantGenre.contains("Chill", ignoreCase = true) || dominantGenre.contains("Indie", ignoreCase = true) ->
                    "Nocturnal Dreamer" to "You seek solace in atmospheric soundscapes, listening most when the rest of the world is asleep."
                dominantGenre.contains("Workout", ignoreCase = true) || dominantGenre.contains("Party", ignoreCase = true) ->
                    "Euphoric Flowmaster" to "High BPMs and relentless energy drive your days. You use music as your personal sonic battery."
                dominantGenre.contains("Devotional", ignoreCase = true) ->
                    "Spiritual Harmonizer" to "Music is sacred to you. You find peace and balance in acoustic purity and timeless chants."
                else ->
                    "Nocturnal Audiophile" to "You curate music with exquisite taste, seeking nuanced studio mixing and high-fidelity sound."
            }

            val topArtistCount = topArtists.firstOrNull()?.second ?: 1
            val artistLoyaltyPct = ((topArtistCount.toFloat() / totalPlays.toFloat().coerceAtLeast(1f)) * 100).toInt().coerceIn(15, 85)

            val baseHours = (totalMinutes.toFloat() / 60f) / 4f
            val weeklyActivity = listOf(
                "Mon" to (baseHours * 0.7f).coerceAtLeast(1.2f),
                "Tue" to (baseHours * 0.9f).coerceAtLeast(1.8f),
                "Wed" to (baseHours * 1.1f).coerceAtLeast(2.4f),
                "Thu" to (baseHours * 0.8f).coerceAtLeast(1.5f),
                "Fri" to (baseHours * 1.6f).coerceAtLeast(3.8f),
                "Sat" to (baseHours * 2.0f).coerceAtLeast(4.9f),
                "Sun" to (baseHours * 1.4f).coerceAtLeast(3.2f)
            )

            ListeningStats(
                totalMinutesListened = totalMinutes,
                totalTracksPlayed = totalPlays,
                topArtists = topArtists,
                topGenres = topGenres,
                streakDays = 7,
                topSongs = topSongs,
                soundPersonality = personality,
                personalityDescription = description,
                topMood = songs.firstOrNull()?.mood ?: "Chill",
                weeklyActivityHours = weeklyActivity,
                listeningGoalMinutes = 2500,
                peakListeningTime = "10:30 PM - 1:00 AM",
                artistLoyaltyPercentage = artistLoyaltyPct
            )
        }.flowOn(Dispatchers.IO)
    }

    private fun getSeedSongs(): List<SongEntity> {
        // High quality legal royalty-free audio streams for seamless immediate playback
        // Featuring the titles, artists and albums from the user's reference mockup!
        val audioStream1 = "https://commondatastorage.googleapis.com/codeskulptor-demos/DDR_assets/Kangaroo_MusiQue_-_The_Neverending_Story.mp3"
        val audioStream2 = "https://commondatastorage.googleapis.com/codeskulptor-demos/DDR_assets/Sevish_-__nbsp_.mp3"
        val audioStream3 = "https://commondatastorage.googleapis.com/codeskulptor-assets/Epoq-Lepidoptera.mp3"
        val audioStream4 = "https://commondatastorage.googleapis.com/codeskulptor-assets/sounddogs/soundtrack.mp3"

        return listOf(
            SongEntity(
                id = "swarix_1",
                title = "Apna Bana Le",
                artist = "Arijit Singh",
                album = "Bhediya",
                durationMs = 261000,
                contentUri = audioStream1,
                artworkUri = "https://images.unsplash.com/photo-1518495973542-4542c06a5843?w=600&auto=format&fit=crop&q=80",
                genre = "Bollywood Romance",
                mood = "Romance",
                lyrics = """Mujhe apna bana le piya
Apna bana le piya
Dil ke nagar mein
Shehar tu basa le piya
Piya o...
Mujhe apna bana le piya
Apna bana le...
Tujhko hi maana hai dil ne
Tujhko hi apna jaana hai
Haar ke bhi main jeet gaya hoon
Tujhko paake khud ko paaya""".trimIndent(),
                isFavorite = true,
                playCount = 14,
                dateAdded = System.currentTimeMillis() - 100000,
                isLocal = false
            ),
            SongEntity(
                id = "swarix_2",
                title = "Heeriye",
                artist = "Jasleen Royal & Arijit Singh",
                album = "Heeriye Single",
                durationMs = 205000,
                contentUri = audioStream2,
                artworkUri = "https://images.unsplash.com/photo-1516589178581-6cd7833ae3b2?w=600&auto=format&fit=crop&q=80",
                genre = "Acoustic Pop",
                mood = "Chill",
                lyrics = """Heeriye heeriye aa...
Heeriye heeriye aa...
Teri hoke marjavan
Kujh aisa kar javan
Ni heeriye aa...
Hathan te likh leya ve
Dil da main haal sunavan
Kinve tainu samjhavan""".trimIndent(),
                isFavorite = true,
                playCount = 10,
                dateAdded = System.currentTimeMillis() - 200000,
                isLocal = false
            ),
            SongEntity(
                id = "swarix_3",
                title = "Kesariya",
                artist = "Arijit Singh",
                album = "Brahmastra",
                durationMs = 268000,
                contentUri = audioStream3,
                artworkUri = "https://images.unsplash.com/photo-1507525428034-b723cf961d3e?w=600&auto=format&fit=crop&q=80",
                genre = "Romantic",
                mood = "Romance",
                lyrics = """Kesariya tera ishq hai piya
Rang jaaun jo main haath lagaun
Din beete saara teri fikr mein
Rain saari teri khair manaun
Patjhad ke mausam mein bhi
Rangin bahaarein aayi hain""".trimIndent(),
                isFavorite = false,
                playCount = 8,
                dateAdded = System.currentTimeMillis() - 300000,
                isLocal = false
            ),
            SongEntity(
                id = "swarix_4",
                title = "Tum Kya Mile",
                artist = "Arijit Singh & Shreya Ghoshal",
                album = "Rocky Aur Rani",
                durationMs = 217000,
                contentUri = audioStream4,
                artworkUri = "https://images.unsplash.com/photo-1511671782779-c97d3d27a1d4?w=600&auto=format&fit=crop&q=80",
                genre = "Duet Romance",
                mood = "Romance",
                lyrics = """Tum kya mile hum na rahe hum
Bikhre the jo milke bane hum
Jeene ko ab to tayyar hain hum
Duniya se ladke keh denge sab se""".trimIndent(),
                isFavorite = true,
                playCount = 6,
                dateAdded = System.currentTimeMillis() - 400000,
                isLocal = false
            ),
            SongEntity(
                id = "swarix_5",
                title = "Pehle Bhi Main",
                artist = "Vishal Mishra",
                album = "Animal",
                durationMs = 250000,
                contentUri = audioStream1,
                artworkUri = "https://images.unsplash.com/photo-1492684223066-81342ee5ff30?w=600&auto=format&fit=crop&q=80",
                genre = "Soulful",
                mood = "Sad",
                lyrics = """Pehle bhi main tumse mila hoon
Pehli dafa hi milke laga
Tune chhuwa zakhmon ko mere
Marham sa kyun lagne laga""".trimIndent(),
                isFavorite = false,
                playCount = 5,
                dateAdded = System.currentTimeMillis() - 500000,
                isLocal = false
            ),
            SongEntity(
                id = "swarix_6",
                title = "Tera Fitoor",
                artist = "Arijit Singh",
                album = "Genius",
                durationMs = 311000,
                contentUri = audioStream2,
                artworkUri = "https://images.unsplash.com/photo-1514525253161-7a46d19cd819?w=600&auto=format&fit=crop&q=80",
                genre = "Romantic",
                mood = "Romance",
                lyrics = """Tera fitoor jab se chadh gaya re
Hawa mein baatein karne laga hai dil mera
Ishq ki galiyon mein bhatke
Aisa hua nasha tera""".trimIndent(),
                isFavorite = false,
                playCount = 4,
                dateAdded = System.currentTimeMillis() - 600000,
                isLocal = false
            ),
            SongEntity(
                id = "swarix_7",
                title = "Raataan Lambiyan",
                artist = "Jubin Nautiyal & Asees Kaur",
                album = "Shershaah",
                durationMs = 230000,
                contentUri = audioStream3,
                artworkUri = "https://images.unsplash.com/photo-1506744038136-46273834b3fb?w=600&auto=format&fit=crop&q=80",
                genre = "Romantic",
                mood = "Chill",
                lyrics = """Kaate kate na re raina
Tere bina beswaad lagdi hai
Raataan lambiyan lambiyan re
Katte tere sang sang saare sapne""".trimIndent(),
                isFavorite = true,
                playCount = 12,
                dateAdded = System.currentTimeMillis() - 700000,
                isLocal = false
            ),
            SongEntity(
                id = "swarix_8",
                title = "Hasi",
                artist = "Ami Mishra",
                album = "Hamari Adhuri Kahani",
                durationMs = 212000,
                contentUri = audioStream4,
                artworkUri = "https://images.unsplash.com/photo-1470225620780-dba8ba36b745?w=600&auto=format&fit=crop&q=80",
                genre = "Melody",
                mood = "Sad",
                lyrics = """Haan hasi ban gaye
Haan nami ban gaye
Tum mere aasmaan
Meri zameen ban gaye""".trimIndent(),
                isFavorite = false,
                playCount = 3,
                dateAdded = System.currentTimeMillis() - 800000,
                isLocal = false
            ),
            SongEntity(
                id = "swarix_9",
                title = "Iktara",
                artist = "Amit Trivedi & Kavita Seth",
                album = "Wake Up Sid",
                durationMs = 254000,
                contentUri = audioStream1,
                artworkUri = "https://images.unsplash.com/photo-1511379938547-c1f69419868d?w=600&auto=format&fit=crop&q=80",
                genre = "Sufi Pop",
                mood = "Chill",
                lyrics = """O re manwa tu to bawra hai
Tu hi jaane tu kya sochta hai
Baaware se is jahan mein
Iktara goonje har gali""".trimIndent(),
                isFavorite = true,
                playCount = 7,
                dateAdded = System.currentTimeMillis() - 900000,
                isLocal = false
            ),
            SongEntity(
                id = "swarix_10",
                title = "Safar",
                artist = "Jubin Nautiyal",
                album = "Single",
                durationMs = 278000,
                contentUri = audioStream2,
                artworkUri = "https://images.unsplash.com/photo-1459749411175-04bf5292ceea?w=600&auto=format&fit=crop&q=80",
                genre = "Acoustic",
                mood = "Focus",
                lyrics = """Safar khoobsurat hai manzil se bhi
Chalte chalo raste naye
Subah ki dhoop aur shaam ki chhaon
Har mod pe naye fasane""".trimIndent(),
                isFavorite = false,
                playCount = 2,
                dateAdded = System.currentTimeMillis() - 1000000,
                isLocal = false
            ),
            SongEntity(
                id = "swarix_11",
                title = "Energy Pulse",
                artist = "Workout Beat Crew",
                album = "Cardio Peak",
                durationMs = 195000,
                contentUri = audioStream3,
                artworkUri = "https://images.unsplash.com/photo-1517838277536-f5f99be501cd?w=600&auto=format&fit=crop&q=80",
                genre = "EDM",
                mood = "Workout",
                lyrics = "Feel the rhythm, push the boundary, never stop the pump!",
                isFavorite = false,
                playCount = 9,
                dateAdded = System.currentTimeMillis() - 1100000,
                isLocal = false
            ),
            SongEntity(
                id = "swarix_12",
                title = "Devotion & Peace",
                artist = "Spiritual Sanctuary",
                album = "Divine Waves",
                durationMs = 320000,
                contentUri = audioStream4,
                artworkUri = "https://images.unsplash.com/photo-1506744038136-46273834b3fb?w=600&auto=format&fit=crop&q=80",
                genre = "Devotional",
                mood = "Devotional",
                lyrics = "Om Shanti Shanti Shanti. Eternal peace and light fills every soul.",
                isFavorite = true,
                playCount = 11,
                dateAdded = System.currentTimeMillis() - 1200000,
                isLocal = false
            )
        )
    }

    private fun SongEntity.toDomain(): Song {
        return Song(
            id = id,
            title = title,
            artist = artist,
            album = album,
            durationMs = durationMs,
            contentUri = contentUri,
            artworkUri = artworkUri,
            genre = genre,
            mood = mood,
            lyrics = lyrics,
            isFavorite = isFavorite,
            playCount = playCount,
            dateAdded = dateAdded,
            isLocal = isLocal
        )
    }

    private fun PlaylistEntity.toDomain(): Playlist {
        return Playlist(
            id = id,
            name = name,
            description = description,
            coverUri = coverUri,
            isSmart = isSmart
        )
    }
}
