package com.example.playback

import com.example.core.model.Song
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * Manages synchronized playback sessions across multiple users.
 */
object SyncSessionManager {

    private val _isSyncSessionActive = MutableStateFlow(false)
    val isSyncSessionActive: StateFlow<Boolean> = _isSyncSessionActive.asStateFlow()

    private val _sharedQueue = MutableStateFlow<List<Song>>(emptyList())
    val sharedQueue: StateFlow<List<Song>> = _sharedQueue.asStateFlow()

    private val _currentSyncSong = MutableStateFlow<Song?>(null)
    val currentSyncSong: StateFlow<Song?> = _currentSyncSong.asStateFlow()

    fun joinSession(songs: List<Song>) {
        _isSyncSessionActive.value = true
        _sharedQueue.value = songs
        _currentSyncSong.value = songs.firstOrNull()
    }

    fun leaveSession() {
        _isSyncSessionActive.value = false
        _sharedQueue.value = emptyList()
        _currentSyncSong.value = null
    }

    fun updateSyncSong(song: Song) {
        _currentSyncSong.value = song
    }
}
