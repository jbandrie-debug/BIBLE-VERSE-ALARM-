package com.example.ui.viewmodel

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.entity.VerseEntity
import com.example.data.repository.VerseRepository
import com.example.service.TtsManager
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class VerseViewModel(
    private val repository: VerseRepository
) : ViewModel() {

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    private val _selectedCategory = MutableStateFlow<String?>(null)
    val selectedCategory: StateFlow<String?> = _selectedCategory.asStateFlow()

    private val _selectedTranslation = MutableStateFlow<String?>(null)
    val selectedTranslation: StateFlow<String?> = _selectedTranslation.asStateFlow()

    val categories: StateFlow<List<String>> = repository.categories
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val books: StateFlow<List<String>> = repository.books
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val translations: StateFlow<List<String>> = repository.translations
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val favoriteVerses: StateFlow<List<VerseEntity>> = repository.favoriteVerses
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    @OptIn(ExperimentalCoroutinesApi::class)
    val verses: StateFlow<List<VerseEntity>> = combine(
        _searchQuery,
        _selectedCategory,
        _selectedTranslation
    ) { query, category, translation ->
        Triple(query, category, translation)
    }.flatMapLatest { (query, category, translation) ->
        if (query.isNotBlank()) {
            repository.searchVerses(query)
        } else {
            when {
                category != null && translation != null -> repository.getVersesByCategoryAndTranslation(category, translation)
                category != null -> repository.getVersesByCategory(category)
                translation != null -> repository.getVersesByTranslation(translation)
                else -> repository.allVerses
            }
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private val _dailyVerse = MutableStateFlow<VerseEntity?>(null)
    val dailyVerse: StateFlow<VerseEntity?> = _dailyVerse.asStateFlow()

    private var previewTts: TtsManager? = null

    init {
        viewModelScope.launch {
            repository.ensureVersesLoaded()
            loadDailyVerse()
        }
    }

    fun loadDailyVerse() {
        viewModelScope.launch {
            _dailyVerse.value = repository.getDailyVerse()
        }
    }

    fun setSearchQuery(query: String) {
        _searchQuery.value = query
    }

    fun selectCategory(category: String?) {
        _selectedCategory.value = if (_selectedCategory.value == category) null else category
    }

    fun selectTranslation(translation: String?) {
        _selectedTranslation.value = if (_selectedTranslation.value == translation) null else translation
    }

    fun toggleFavorite(verseId: Long) {
        viewModelScope.launch {
            repository.toggleFavorite(verseId)
        }
    }

    fun isFavorite(verseId: Long) = repository.isFavorite(verseId)

    fun previewVerseSpeech(
        context: Context,
        verse: VerseEntity,
        rate: Float? = null,
        pitch: Float? = null,
        voiceName: String? = null
    ) {
        viewModelScope.launch {
            val db = com.example.data.database.AppDatabase.getInstance(context.applicationContext)
            val settings = db.userSettingsDao().getUserSettingsDirect()

            val finalRate = rate ?: settings?.defaultSpeechRate ?: 1.0f
            val finalPitch = pitch ?: settings?.defaultPitch ?: 1.0f
            val finalVoiceName = voiceName ?: settings?.ttsVoiceName ?: ""

            if (previewTts == null) {
                previewTts = TtsManager(context.applicationContext)
            }
            previewTts?.configure(speechRate = finalRate, pitch = finalPitch, voiceName = finalVoiceName)
            val prayerPart = if (verse.prayer.isNotBlank()) ". Prayer: ${verse.prayer}" else ""
            val textToSpeak = "${verse.book}, chapter ${verse.chapter}, verse ${verse.verseNumber}. ${verse.text}$prayerPart"
            previewTts?.speak(textToSpeak, playBell = false)
        }
    }

    fun getAvailableVoiceProfiles(context: Context): List<com.example.service.VoiceProfile> {
        if (previewTts == null) {
            previewTts = TtsManager(context.applicationContext)
        }
        return previewTts?.getAvailableVoiceProfiles() ?: emptyList()
    }

    fun stopSpeechPreview() {
        previewTts?.stop()
    }

    fun addNewVerse(verse: VerseEntity) {
        viewModelScope.launch {
            repository.addVerse(verse)
        }
    }

    override fun onCleared() {
        previewTts?.shutdown()
        super.onCleared()
    }
}
