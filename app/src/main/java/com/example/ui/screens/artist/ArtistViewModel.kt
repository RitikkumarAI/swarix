package com.example.ui.screens.artist

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.Firebase
import com.google.firebase.ai.*
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.TimeoutCancellationException
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withTimeout

sealed interface BiographyUiState {
    data object Idle : BiographyUiState
    data object Loading : BiographyUiState
    data class Success(val biography: String) : BiographyUiState
    data class Error(val message: String) : BiographyUiState
}

class ArtistViewModel : ViewModel() {
    private val model by lazy {
        Firebase.ai.generativeModel("gemini-3.8-flash")
    }

    private val _biographyState = MutableStateFlow<BiographyUiState>(BiographyUiState.Idle)
    val biographyState: StateFlow<BiographyUiState> = _biographyState.asStateFlow()

    fun fetchBiography(artistName: String) {
        viewModelScope.launch {
            _biographyState.value = BiographyUiState.Loading
            try {
                val prompt = "Provide a concise and engaging biography for the artist: $artistName. Focus on their musical style, impact, and notable achievements. Keep it under 200 words."
                
                val response = withTimeout(60_000L) {
                    model.generateContent(prompt)
                }
                
                val text = response.text
                if (!text.isNullOrBlank()) {
                    _biographyState.value = BiographyUiState.Success(text)
                } else {
                    _biographyState.value = BiographyUiState.Error("Could not fetch biography.")
                }
            } catch (e: TimeoutCancellationException) {
                Log.e("ArtistViewModel", "Biography fetch timed out", e)
                _biographyState.value = BiographyUiState.Error("Timed out. Please try again.")
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                Log.e("ArtistViewModel", "Failed to fetch biography", e)
                _biographyState.value = BiographyUiState.Error(e.localizedMessage ?: "Failed to fetch.")
            }
        }
    }
}
