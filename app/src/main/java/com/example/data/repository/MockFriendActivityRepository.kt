package com.example.data.repository

import com.example.core.model.FriendActivity
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlin.random.Random

/**
 * Mock Firestore repository to provide real-time updates for friend activity.
 */
class MockFriendActivityRepository {

    private val mockFriends = listOf(
        FriendActivity("1", "Sarah", "https://images.unsplash.com/photo-1544005313-94ddf0286df2?w=100", "Heeriye", "Jasleen Royal", "", "Listening now", true, "swarix_2"),
        FriendActivity("2", "Rahul", "https://images.unsplash.com/photo-1507003211169-0a1dd7228f2d?w=100", "Apna Bana Le", "Arijit Singh", "", "Listening now", true, "swarix_1"),
        FriendActivity("3", "Priya", "https://images.unsplash.com/photo-1506794778202-cad84cf45f1d?w=100", "Dil Ibaadat", "Pritam", "", "Offline", false, "swarix_3")
    )

    fun getFriendActivity(): Flow<List<FriendActivity>> = flow {
        while (true) {
            // Mock real-time updates
            val updatedFriends = mockFriends.map { friend ->
                if (friend.isListeningNow && Random.nextFloat() > 0.8) {
                    friend.copy(isListeningNow = false, statusText = "Offline")
                } else if (!friend.isListeningNow && Random.nextFloat() > 0.8) {
                    friend.copy(isListeningNow = true, statusText = "Listening now")
                } else {
                    friend
                }
            }
            emit(updatedFriends)
            delay(5000) // Emit every 5 seconds
        }
    }
}
