package com.example.ui.viewmodel

import android.app.Application
import android.content.Intent
import android.os.Bundle
import android.speech.RecognitionListener
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
import android.speech.tts.TextToSpeech
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.repository.GeminiRepository
import com.example.data.repository.GroundedDevotionalResult
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.util.Locale

sealed interface UiState<out T> {
    object Idle : UiState<Nothing>
    object Loading : UiState<Nothing>
    data class Success<T>(val data: T) : UiState<T>
    data class Error(val message: String) : UiState<Nothing>
}

data class VoiceMessage(
    val sender: String, // "User" or "Gemini"
    val text: String
)

class GeminiViewModel(application: Application) : AndroidViewModel(application), TextToSpeech.OnInitListener {

    private val repository = GeminiRepository()

    // Language Selection State
    private val _selectedLanguage = MutableStateFlow("Cebuano (Bisaya)")
    val selectedLanguage: StateFlow<String> = _selectedLanguage.asStateFlow()

    fun setLanguage(lang: String) {
        _selectedLanguage.value = lang
    }

    // Deep Reflection State (gemini-3.1-pro-preview)
    private val _deepReflectionState = MutableStateFlow<UiState<String>>(UiState.Idle)
    val deepReflectionState: StateFlow<UiState<String>> = _deepReflectionState.asStateFlow()

    // Prayer & Situation Helper State (gemini-3.5-flash)
    private val _prayerState = MutableStateFlow<UiState<Pair<String, String>>>(UiState.Idle)
    val prayerState: StateFlow<UiState<Pair<String, String>>> = _prayerState.asStateFlow()

    // Search Grounded Devotional State (gemini-3.5-flash + Google Search)
    private val _groundedState = MutableStateFlow<UiState<GroundedDevotionalResult>>(UiState.Idle)
    val groundedState: StateFlow<UiState<GroundedDevotionalResult>> = _groundedState.asStateFlow()

    // Live Voice Companion State (gemini-3.5-flash)
    private val _voiceMessages = MutableStateFlow<List<VoiceMessage>>(emptyList())
    val voiceMessages: StateFlow<List<VoiceMessage>> = _voiceMessages.asStateFlow()

    private val _isListening = MutableStateFlow(false)
    val isListening: StateFlow<Boolean> = _isListening.asStateFlow()

    private val _isSpeaking = MutableStateFlow(false)
    val isSpeaking: StateFlow<Boolean> = _isSpeaking.asStateFlow()

    private val _voiceStatusText = MutableStateFlow("Pindota ang mic para makig-storya kang Gemini sa Cebuano")
    val voiceStatusText: StateFlow<String> = _voiceStatusText.asStateFlow()

    private var speechRecognizer: SpeechRecognizer? = null
    private var tts: TextToSpeech? = null
    private var isTtsReady = false

    init {
        tts = TextToSpeech(application, this)
        initSpeechRecognizer()
    }

    override fun onInit(status: Int) {
        if (status == TextToSpeech.SUCCESS) {
            val result = tts?.setLanguage(Locale.US)
            if (result != TextToSpeech.LANG_MISSING_DATA && result != TextToSpeech.LANG_NOT_SUPPORTED) {
                isTtsReady = true
            }
        }
    }

    private fun initSpeechRecognizer() {
        if (SpeechRecognizer.isRecognitionAvailable(getApplication())) {
            speechRecognizer = SpeechRecognizer.createSpeechRecognizer(getApplication()).apply {
                setRecognitionListener(object : RecognitionListener {
                    override fun onReadyForSpeech(params: Bundle?) {
                        _isListening.value = true
                        _voiceStatusText.value = "Listening to your voice..."
                    }

                    override fun onBeginningOfSpeech() {}
                    override fun onRmsChanged(rmsdB: Float) {}
                    override fun onBufferReceived(buffer: ByteArray?) {}
                    override fun onEndOfSpeech() {
                        _isListening.value = false
                        _voiceStatusText.value = "Processing with Gemini..."
                    }

                    override fun onError(error: Int) {
                        _isListening.value = false
                        _voiceStatusText.value = "Speech recognition error ($error). Tap mic to retry."
                    }

                    override fun onResults(results: Bundle?) {
                        val matches = results?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
                        val text = matches?.firstOrNull() ?: ""
                        if (text.isNotEmpty()) {
                            sendVoicePrompt(text)
                        } else {
                            _voiceStatusText.value = "Could not hear clearly. Try again."
                        }
                    }

                    override fun onPartialResults(partialResults: Bundle?) {}
                    override fun onEvent(eventType: Int, params: Bundle?) {}
                })
            }
        }
    }

    fun startListening() {
        if (_isSpeaking.value) {
            stopSpeaking()
        }
        val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
            putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
            putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.getDefault())
        }
        try {
            speechRecognizer?.startListening(intent)
        } catch (e: Exception) {
            _voiceStatusText.value = "Recognizer unavailable: ${e.message}"
        }
    }

    fun stopListening() {
        speechRecognizer?.stopListening()
        _isListening.value = false
    }

    fun sendVoicePrompt(userText: String) {
        val currentList = _voiceMessages.value.toMutableList()
        currentList.add(VoiceMessage("User", userText))
        _voiceMessages.value = currentList
        _voiceStatusText.value = "Gemini thinking..."

        viewModelScope.launch {
            val history = currentList.dropLast(1).map { it.sender to it.text }
            val lang = if (_selectedLanguage.value.contains("Cebuano")) "Cebuano" else if (_selectedLanguage.value.contains("Filipino")) "Filipino" else "English"
            val result = repository.sendLiveVoiceMessage(userText, history, lang)
            result.onSuccess { reply ->
                val updated = _voiceMessages.value.toMutableList()
                updated.add(VoiceMessage("Gemini", reply))
                _voiceMessages.value = updated
                _voiceStatusText.value = "Gemini responding..."
                speakText(reply)
            }.onFailure { err ->
                _voiceStatusText.value = "Error: ${err.message}"
            }
        }
    }

    fun speakText(text: String) {
        if (isTtsReady) {
            _isSpeaking.value = true
            tts?.speak(text, TextToSpeech.QUEUE_FLUSH, null, "GEMINI_VOICE_ID")
        }
    }

    fun stopSpeaking() {
        tts?.stop()
        _isSpeaking.value = false
    }

    // 1. Deep Reflection (gemini-3.1-pro-preview)
    fun generateDeepReflection(verseText: String, reference: String) {
        _deepReflectionState.value = UiState.Loading
        viewModelScope.launch {
            val lang = if (_selectedLanguage.value.contains("Cebuano")) "Cebuano" else if (_selectedLanguage.value.contains("Filipino")) "Filipino" else "English"
            val result = repository.getDeepReflection(verseText, reference, lang)
            result.onSuccess {
                _deepReflectionState.value = UiState.Success(it)
            }.onFailure {
                _deepReflectionState.value = UiState.Error(it.message ?: "Failed to generate reflection")
            }
        }
    }

    fun clearDeepReflection() {
        _deepReflectionState.value = UiState.Idle
    }

    // 2. Prayer Generator (gemini-3.5-flash)
    fun generatePrayerAndScripture(situation: String) {
        _prayerState.value = UiState.Loading
        viewModelScope.launch {
            val lang = if (_selectedLanguage.value.contains("Cebuano")) "Cebuano" else if (_selectedLanguage.value.contains("Filipino")) "Filipino" else "English"
            val result = repository.getPrayerAndScripture(situation, lang)
            result.onSuccess {
                _prayerState.value = UiState.Success(it)
            }.onFailure {
                _prayerState.value = UiState.Error(it.message ?: "Failed to generate prayer")
            }
        }
    }

    // 3. Search Grounded Devotional (gemini-3.5-flash + Google Search)
    fun generateGroundedDevotional(topic: String) {
        _groundedState.value = UiState.Loading
        viewModelScope.launch {
            val lang = if (_selectedLanguage.value.contains("Cebuano")) "Cebuano" else if (_selectedLanguage.value.contains("Filipino")) "Filipino" else "English"
            val result = repository.getSearchGroundedDevotional(topic, lang)
            result.onSuccess {
                _groundedState.value = UiState.Success(it)
            }.onFailure {
                _groundedState.value = UiState.Error(it.message ?: "Search grounding failed")
            }
        }
    }

    override fun onCleared() {
        super.onCleared()
        speechRecognizer?.destroy()
        tts?.stop()
        tts?.shutdown()
    }
}
