package com.example.core.listentogether

import android.util.Log
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import java.util.UUID

data class ListenSession(
    val sessionId: String = "",
    val hostName: String = "",
    val trackTitle: String = "",
    val trackArtist: String = "",
    val isPlaying: Boolean = false,
    val positionMs: Long = 0L,
    val participantsCount: Int = 1
)

object ListenTogetherManager {
    private const val TAG = "ListenTogetherManager"
    private val dbRef = FirebaseDatabase.getInstance().getReference("listen_sessions")

    fun createSession(hostName: String, initialTrackTitle: String, initialTrackArtist: String): String {
        val sessionId = UUID.randomUUID().toString().take(6).uppercase()
        val session = ListenSession(
            sessionId = sessionId,
            hostName = hostName,
            trackTitle = initialTrackTitle,
            trackArtist = initialTrackArtist,
            isPlaying = true,
            positionMs = 0L,
            participantsCount = 1
        )
        dbRef.child(sessionId).setValue(session)
        return sessionId
    }

    fun updateSessionState(sessionId: String, trackTitle: String, trackArtist: String, isPlaying: Boolean, positionMs: Long) {
        if (sessionId.isBlank()) return
        val updates = mapOf(
            "trackTitle" to trackTitle,
            "trackArtist" to trackArtist,
            "isPlaying" to isPlaying,
            "positionMs" to positionMs
        )
        dbRef.child(sessionId).updateChildren(updates)
    }

    fun observeSession(sessionId: String): Flow<ListenSession?> = callbackFlow {
        val listener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val session = snapshot.getValue(ListenSession::class.java)
                trySend(session)
            }

            override fun onCancelled(error: DatabaseError) {
                Log.e(TAG, "Failed to observe session: ${error.message}")
                trySend(null)
            }
        }
        val query = dbRef.child(sessionId)
        query.addValueEventListener(listener)
        awaitClose { query.removeEventListener(listener) }
    }

    fun joinSession(sessionId: String, onJoined: (Boolean) -> Unit) {
        dbRef.child(sessionId).get().addOnSuccessListener { snapshot ->
            if (snapshot.exists()) {
                val currentCount = snapshot.child("participantsCount").getValue(Int::class.java) ?: 1
                dbRef.child(sessionId).child("participantsCount").setValue(currentCount + 1)
                onJoined(true)
            } else {
                onJoined(false)
            }
        }.addOnFailureListener {
            onJoined(false)
        }
    }

    fun leaveSession(sessionId: String) {
        if (sessionId.isBlank()) return
        dbRef.child(sessionId).child("participantsCount").get().addOnSuccessListener { snapshot ->
            val currentCount = snapshot.getValue(Int::class.java) ?: 1
            if (currentCount <= 1) {
                dbRef.child(sessionId).removeValue()
            } else {
                dbRef.child(sessionId).child("participantsCount").setValue(currentCount - 1)
            }
        }
    }
}
