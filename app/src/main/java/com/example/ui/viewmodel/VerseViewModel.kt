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
    val verses: StateFlow<List<VerseEntity>> = _searchQuery.flatMapLatest { query ->
        if (query.isNotBlank()) {
            repository.searchVerses(query)
        } else {
            val category = _selectedCategory.value
            val translation = _selectedTranslation.value
            when {
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
        loadDailyVerse()
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

    fun previewVerseSpeech(context: Context, verse: VerseEntity, rate: Float = 1.0f, pitch: Float = 1.0f) {
        if (previewTts == null) {
            previewTts = TtsManager(context.applicationContext)
        }
        previewTts?.configure(speechRate = rate, pitch = pitch)
        val textToSpeak = "Verse from ${verse.book}, chapter ${verse.chapter}, verse ${verse.verseNumber}. ${verse.text}"
        previewTts?.speak(textToSpeak)
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
