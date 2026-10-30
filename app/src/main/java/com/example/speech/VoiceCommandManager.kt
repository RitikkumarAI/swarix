package com.example.speech

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.speech.RecognitionListener
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
import android.util.Log
import com.example.core.equalizer.EqualizerPresets
import com.example.core.model.EqualizerState
import com.google.firebase.Firebase
import com.google.firebase.ai.ai
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withTimeout
import org.json.JSONObject
import java.util.Locale

sealed class VoiceCommandResult {
    data class MoodRadio(val mood: String, val message: String) : VoiceCommandResult()
    data class Equalizer(val updatedState: EqualizerState, val message: String) : VoiceCommandResult()
    data class PlaybackAction(val action: String, val message: String) : VoiceCommandResult()
    data class Unrecognized(val spokenText: String, val suggestion: String) : VoiceCommandResult()
}

/**
 * Natural language voice command manager using Android Speech-to-Text.
 * Interprets spoken commands to:
 * 1. Trigger AI-driven Mood Radio playlists (e.g. "play focus music", "start chill mood radio")
 * 2. Dynamically adjust 10-band equalizer settings (e.g. "boost bass", "set acoustic preset", "increase treble")
 * 3. Fallback to Gemini AI natural language intent parsing when online.
 */
class VoiceCommandManager(private val context: Context) {

    companion object {
        private const val TAG = "VoiceCommandManager"
    }

    private val mainHandler = Handler(Looper.getMainLooper())
    private val scope = CoroutineScope(Dispatchers.IO)

    private var speechRecognizer: SpeechRecognizer? = null

    private val _isListening = MutableStateFlow(false)
    val isListening = _isListening.asStateFlow()

    private val _recognizedText = MutableStateFlow("")
    val recognizedText = _recognizedText.asStateFlow()

    private val _partialText = MutableStateFlow("")
    val partialText = _partialText.asStateFlow()

    private val _audioRms = MutableStateFlow(0f)
    val audioRms = _audioRms.asStateFlow()

    private val _statusMessage = MutableStateFlow("Tap microphone to speak")
    val statusMessage = _statusMessage.asStateFlow()

    private val _commandResult = MutableStateFlow<VoiceCommandResult?>(null)
    val commandResult = _commandResult.asStateFlow()

    private val _isProcessing = MutableStateFlow(false)
    val isProcessing = _isProcessing.asStateFlow()

    fun isSpeechRecognitionAvailable(): Boolean {
        return SpeechRecognizer.isRecognitionAvailable(context)
    }

    fun startListening(currentEqualizerState: EqualizerState, onCommandExecuted: (VoiceCommandResult) -> Unit) {
        mainHandler.post {
            try {
                if (!SpeechRecognizer.isRecognitionAvailable(context)) {
                    _statusMessage.value = "Speech recognition service unavailable on device."
                    return@post
                }

                stopListeningInternal()

                speechRecognizer = SpeechRecognizer.createSpeechRecognizer(context).apply {
                    setRecognitionListener(object : RecognitionListener {
                        override fun onReadyForSpeech(params: Bundle?) {
                            _isListening.value = true
                            _recognizedText.value = ""
                            _partialText.value = ""
                            _statusMessage.value = "Listening for command..."
                            Log.d(TAG, "SpeechRecognizer: Ready for speech")
                        }

                        override fun onBeginningOfSpeech() {
                            _statusMessage.value = "Listening..."
                        }

                        override fun onRmsChanged(rmsdB: Float) {
                            _audioRms.value = rmsdB.coerceIn(0f, 10f)
                        }

                        override fun onBufferReceived(buffer: ByteArray?) {}

                        override fun onEndOfSpeech() {
                            _isListening.value = false
                            _statusMessage.value = "Processing voice command..."
                        }

                        override fun onError(error: Int) {
                            _isListening.value = false
                            val errorMsg = when (error) {
                                SpeechRecognizer.ERROR_AUDIO -> "Audio recording error"
                                SpeechRecognizer.ERROR_CLIENT -> "Client speech error"
                                SpeechRecognizer.ERROR_INSUFFICIENT_PERMISSIONS -> "Microphone permission required"
                                SpeechRecognizer.ERROR_NETWORK -> "Network error during recognition"
                                SpeechRecognizer.ERROR_NETWORK_TIMEOUT -> "Network timed out"
                                SpeechRecognizer.ERROR_NO_MATCH -> "No speech recognized. Try again!"
                                SpeechRecognizer.ERROR_RECOGNIZER_BUSY -> "Recognition service busy"
                                SpeechRecognizer.ERROR_SERVER -> "Recognition server error"
                                SpeechRecognizer.ERROR_SPEECH_TIMEOUT -> "No speech detected"
                                else -> "Speech recognition error ($error)"
                            }
                            _statusMessage.value = errorMsg
                            Log.w(TAG, "SpeechRecognizer error: $errorMsg ($error)")
                        }

                        override fun onResults(results: Bundle?) {
                            _isListening.value = false
                            val matches = results?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
                            val spokenText = matches?.firstOrNull() ?: ""
                            _recognizedText.value = spokenText
                            _partialText.value = spokenText

                            if (spokenText.isNotBlank()) {
                                processSpokenCommand(spokenText, currentEqualizerState, onCommandExecuted)
                            } else {
                                _statusMessage.value = "No voice detected. Please try again."
                            }
                        }

                        override fun onPartialResults(partialResults: Bundle?) {
                            val partial = partialResults?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)?.firstOrNull()
                            if (!partial.isNullOrBlank()) {
                                _partialText.value = partial
                            }
                        }

                        override fun onEvent(eventType: Int, params: Bundle?) {}
                    })
                }

                val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
                    putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
                    putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.getDefault())
                    putExtra(RecognizerIntent.EXTRA_MAX_RESULTS, 3)
                    putExtra(RecognizerIntent.EXTRA_PARTIAL_RESULTS, true)
                }

                speechRecognizer?.startListening(intent)
            } catch (e: Exception) {
                _isListening.value = false
                _statusMessage.value = "Could not start speech recognizer: ${e.message}"
                Log.e(TAG, "Error starting speech listener", e)
            }
        }
    }

    fun stopListening() {
        mainHandler.post {
            stopListeningInternal()
        }
    }

    private fun stopListeningInternal() {
        try {
            _isListening.value = false
            speechRecognizer?.stopListening()
            speechRecognizer?.destroy()
            speechRecognizer = null
        } catch (e: Exception) {
            Log.w(TAG, "Error releasing speechRecognizer: ${e.message}")
        }
    }

    /**
     * Interprets natural language voice command text using fast local intent recognition
     * and fallback to Gemini AI for complex queries.
     */
    fun processSpokenCommand(
        text: String,
        currentEqualizerState: EqualizerState,
        onResult: (VoiceCommandResult) -> Unit
    ) {
        _isProcessing.value = true
        _statusMessage.value = "Analyzing command: \"$text\""

        // 1. Fast, zero-latency local deterministic regex/keyword pattern matching
        val localMatch = parseLocally(text.trim().lowercase(), currentEqualizerState)
        if (localMatch != null) {
            _commandResult.value = localMatch
            _isProcessing.value = false
            _statusMessage.value = when (localMatch) {
                is VoiceCommandResult.MoodRadio -> "Starting ${localMatch.mood} Mood Radio..."
                is VoiceCommandResult.Equalizer -> localMatch.message
                is VoiceCommandResult.PlaybackAction -> localMatch.message
                is VoiceCommandResult.Unrecognized -> localMatch.suggestion
            }
            onResult(localMatch)
            return
        }

        // 2. Complex Natural Language Understanding via Gemini AI if online
        scope.launch {
            try {
                val model = Firebase.ai.generativeModel("gemini-3.8-flash")
                val prompt = """
                    You are a voice assistant for the music player app Swarix.
                    User voice input: "$text"
                    
                    Classify the intent into either:
                    1. "MOOD_RADIO": Trigger mood playlist. Mood options: Chill, Energy, Relax, Focus, Party, Romance, Workout.
                    2. "EQUALIZER": Adjust audio equalizer. Preset options: Bass Boost, Acoustic, Vocal, Pop, Rock, Electronic, Classical, Jazz, Flat.
                    3. "PLAYBACK": Play, Pause, Next, Previous, Smart Shuffle.
                    
                    Respond ONLY with JSON:
                    {"intent": "MOOD_RADIO" | "EQUALIZER" | "PLAYBACK" | "UNKNOWN", "param": "value", "description": "short user explanation"}
                    
                    Examples:
                    "I want heavy low end for the gym" -> {"intent": "EQUALIZER", "param": "Bass Boost", "description": "Applied Bass Boost preset with heavy low end"}
                    "Put on some calming tunes for study" -> {"intent": "MOOD_RADIO", "param": "Focus", "description": "Playing Focus Mood Radio"}
                """.trimIndent()

                val response = withTimeout(6000L) {
                    model.generateContent(prompt)
                }

                val aiText = response.text ?: ""
                val start = aiText.indexOf('{')
                val end = aiText.lastIndexOf('}')
                if (start != -1 && end != -1) {
                    val json = JSONObject(aiText.substring(start, end + 1))
                    val intent = json.optString("intent")
                    val param = json.optString("param")
                    val desc = json.optString("description", "")

                    val result = when (intent) {
                        "MOOD_RADIO" -> {
                            val mood = normalizeMood(param)
                            VoiceCommandResult.MoodRadio(mood, desc.ifBlank { "Starting $mood Mood Radio" })
                        }
                        "EQUALIZER" -> {
                            val presetDef = EqualizerPresets.PRESETS[param]
                            val updated = if (presetDef != null) {
                                currentEqualizerState.copy(
                                    isEnabled = true,
                                    presetName = presetDef.name,
                                    bandLevels = presetDef.bandLevels,
                                    bassBoost = presetDef.bassBoost,
                                    virtualizer = presetDef.virtualizer
                                )
                            } else {
                                currentEqualizerState.copy(isEnabled = true, bassBoost = 80)
                            }
                            VoiceCommandResult.Equalizer(updated, desc.ifBlank { "Equalizer adjusted: $param" })
                        }
                        "PLAYBACK" -> {
                            VoiceCommandResult.PlaybackAction(param.lowercase(), desc.ifBlank { "Command executed: $param" })
                        }
                        else -> {
                            VoiceCommandResult.Unrecognized(text, "Try 'Play Focus music' or 'Boost bass'")
                        }
                    }

                    mainHandler.post {
                        _commandResult.value = result
                        _isProcessing.value = false
                        _statusMessage.value = when (result) {
                            is VoiceCommandResult.MoodRadio -> result.message
                            is VoiceCommandResult.Equalizer -> result.message
                            is VoiceCommandResult.PlaybackAction -> result.message
                            is VoiceCommandResult.Unrecognized -> result.suggestion
                        }
                        onResult(result)
                    }
                    return@launch
                }
            } catch (e: Exception) {
                Log.w(TAG, "Gemini voice parsing skipped: ${e.message}")
            }

            // Fallback if AI was unavailable or timed out
            mainHandler.post {
                val fallback = VoiceCommandResult.Unrecognized(
                    text,
                    "Command not recognized. Say 'Play Chill music' or 'Boost bass'."
                )
                _commandResult.value = fallback
                _isProcessing.value = false
                _statusMessage.value = fallback.suggestion
                onResult(fallback)
            }
        }
    }

    private fun parseLocally(lower: String, currentEq: EqualizerState): VoiceCommandResult? {
        // Equalizer commands
        if (lower.contains("bass boost") || lower.contains("boost bass") || lower.contains("heavy bass") || lower.contains("more bass") || lower.contains("extra bass")) {
            val preset = EqualizerPresets.PRESETS["Bass Boost"]
            val state = currentEq.copy(
                isEnabled = true,
                presetName = "Bass Boost",
                bandLevels = preset?.bandLevels ?: listOf(8, 7, 5, 2, 0, -1, 0, 1, 2, 3),
                bassBoost = 85,
                virtualizer = 35
            )
            return VoiceCommandResult.Equalizer(state, "Bass Boost activated (+85% sub-bass excitation)")
        }

        if (lower.contains("acoustic") || lower.contains("unplugged") || lower.contains("guitar sound")) {
            val preset = EqualizerPresets.PRESETS["Acoustic"]
            val state = currentEq.copy(
                isEnabled = true,
                presetName = "Acoustic",
                bandLevels = preset?.bandLevels ?: listOf(2, 3, 2, 1, 2, 3, 4, 5, 4, 3),
                bassBoost = 25,
                virtualizer = 40
            )
            return VoiceCommandResult.Equalizer(state, "Acoustic preset activated (warm acoustic string clarity)")
        }

        if (lower.contains("vocal") || lower.contains("voice boost") || lower.contains("clear voice") || lower.contains("speech")) {
            val preset = EqualizerPresets.PRESETS["Vocal"]
            val state = currentEq.copy(
                isEnabled = true,
                presetName = "Vocal",
                bandLevels = preset?.bandLevels ?: listOf(-3, -2, 0, 3, 6, 7, 5, 3, 1, -1),
                bassBoost = 15,
                virtualizer = 25
            )
            return VoiceCommandResult.Equalizer(state, "Vocal Booster preset activated (prominent 1-4kHz clarity)")
        }

        if (lower.contains("pop") && (lower.contains("preset") || lower.contains("mode") || lower.contains("equalizer"))) {
            val preset = EqualizerPresets.PRESETS["Pop"]
            val state = currentEq.copy(
                isEnabled = true,
                presetName = "Pop",
                bandLevels = preset?.bandLevels ?: listOf(-1, 1, 3, 4, 5, 4, 2, 1, 0, 1),
                bassBoost = 40,
                virtualizer = 30
            )
            return VoiceCommandResult.Equalizer(state, "Pop preset activated")
        }

        if (lower.contains("rock") && (lower.contains("preset") || lower.contains("mode") || lower.contains("equalizer"))) {
            val preset = EqualizerPresets.PRESETS["Rock"]
            val state = currentEq.copy(
                isEnabled = true,
                presetName = "Rock",
                bandLevels = preset?.bandLevels ?: listOf(5, 4, 2, -1, -2, 0, 2, 4, 5, 6),
                bassBoost = 60,
                virtualizer = 30
            )
            return VoiceCommandResult.Equalizer(state, "Rock preset activated")
        }

        if (lower.contains("electronic") || lower.contains("edm") || lower.contains("dance mode")) {
            val preset = EqualizerPresets.PRESETS["Electronic"]
            val state = currentEq.copy(
                isEnabled = true,
                presetName = "Electronic",
                bandLevels = preset?.bandLevels ?: listOf(6, 5, 2, 0, -2, 2, 1, 3, 5, 6),
                bassBoost = 75,
                virtualizer = 45
            )
            return VoiceCommandResult.Equalizer(state, "Electronic preset activated")
        }

        if (lower.contains("classical") || lower.contains("orchestra")) {
            val preset = EqualizerPresets.PRESETS["Classical"]
            val state = currentEq.copy(
                isEnabled = true,
                presetName = "Classical",
                bandLevels = preset?.bandLevels ?: listOf(4, 3, 2, 1, -1, -1, 0, 2, 3, 4),
                bassBoost = 20,
                virtualizer = 50
            )
            return VoiceCommandResult.Equalizer(state, "Classical preset activated")
        }

        if (lower.contains("jazz")) {
            val preset = EqualizerPresets.PRESETS["Jazz"]
            val state = currentEq.copy(
                isEnabled = true,
                presetName = "Jazz",
                bandLevels = preset?.bandLevels ?: listOf(3, 2, 0, 2, -1, -1, 0, 1, 3, 4),
                bassBoost = 30,
                virtualizer = 35
            )
            return VoiceCommandResult.Equalizer(state, "Jazz preset activated")
        }

        if (lower.contains("reset equalizer") || lower.contains("flat equalizer") || lower.contains("default equalizer")) {
            val state = currentEq.copy(
                isEnabled = true,
                presetName = "Flat",
                bandLevels = List(10) { 0 },
                bassBoost = 0,
                virtualizer = 0
            )
            return VoiceCommandResult.Equalizer(state, "Equalizer reset to Flat studio curve")
        }

        if (lower.contains("turn on equalizer") || lower.contains("enable equalizer")) {
            return VoiceCommandResult.Equalizer(currentEq.copy(isEnabled = true), "Studio equalizer enabled")
        }

        if (lower.contains("turn off equalizer") || lower.contains("disable equalizer") || lower.contains("bypass equalizer")) {
            return VoiceCommandResult.Equalizer(currentEq.copy(isEnabled = false), "Studio equalizer bypassed")
        }

        if (lower.contains("treble") || lower.contains("highs") || lower.contains("crisp")) {
            val newBands = currentEq.bandLevels.toMutableList()
            if (newBands.size >= 10) {
                newBands[8] = (newBands[8] + 4).coerceAtMost(10)
                newBands[9] = (newBands[9] + 5).coerceAtMost(10)
            }
            return VoiceCommandResult.Equalizer(
                currentEq.copy(isEnabled = true, presetName = "Custom", bandLevels = newBands),
                "Treble frequencies boosted (+5dB at 8-16kHz)"
            )
        }

        // Mood Radio commands
        if (lower.contains("focus") || lower.contains("study") || lower.contains("concentrat") || lower.contains("work mode")) {
            return VoiceCommandResult.MoodRadio("Focus", "Generating Focus Mood Radio playlist...")
        }

        if (lower.contains("chill") || lower.contains("lo-fi") || lower.contains("lofi") || lower.contains("mellow")) {
            return VoiceCommandResult.MoodRadio("Chill", "Generating Chill Mood Radio playlist...")
        }

        if (lower.contains("energy") || lower.contains("workout") || lower.contains("pump") || lower.contains("gym") || lower.contains("running")) {
            return VoiceCommandResult.MoodRadio("Energy", "Generating Energy Mood Radio playlist...")
        }

        if (lower.contains("relax") || lower.contains("calm") || lower.contains("meditat") || lower.contains("peace")) {
            return VoiceCommandResult.MoodRadio("Relax", "Generating Relax Mood Radio playlist...")
        }

        if (lower.contains("party") || lower.contains("dance") || lower.contains("celebrat") || lower.contains("club")) {
            return VoiceCommandResult.MoodRadio("Party", "Generating Party Mood Radio playlist...")
        }

        if (lower.contains("romance") || lower.contains("romantic") || lower.contains("love")) {
            return VoiceCommandResult.MoodRadio("Romance", "Generating Romance Mood Radio playlist...")
        }

        // General playback commands
        if (lower == "play" || lower == "resume") {
            return VoiceCommandResult.PlaybackAction("resume", "Resuming playback")
        }
        if (lower == "pause" || lower == "stop") {
            return VoiceCommandResult.PlaybackAction("pause", "Paused playback")
        }
        if (lower.contains("next") || lower.contains("skip")) {
            return VoiceCommandResult.PlaybackAction("next", "Skipping to next track")
        }
        if (lower.contains("previous") || lower.contains("back")) {
            return VoiceCommandResult.PlaybackAction("previous", "Playing previous track")
        }
        if (lower.contains("smart shuffle") || lower.contains("shuffle")) {
            return VoiceCommandResult.PlaybackAction("smart_shuffle", "Triggering AI Smart Shuffle")
        }

        return null
    }

    private fun normalizeMood(param: String): String {
        val lower = param.lowercase()
        return when {
            lower.contains("focus") || lower.contains("study") -> "Focus"
            lower.contains("chill") || lower.contains("lofi") -> "Chill"
            lower.contains("energy") || lower.contains("workout") -> "Energy"
            lower.contains("relax") || lower.contains("calm") -> "Relax"
            lower.contains("party") || lower.contains("dance") -> "Party"
            lower.contains("romance") || lower.contains("love") -> "Romance"
            else -> "Chill"
        }
    }

    fun release() {
        stopListeningInternal()
    }
}
