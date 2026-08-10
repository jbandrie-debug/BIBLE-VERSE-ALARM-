package com.example.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.entity.UserSettingsEntity
import com.example.data.repository.SettingsRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class SettingsViewModel(
    private val repository: SettingsRepository
) : ViewModel() {

    val settings: StateFlow<UserSettingsEntity?> = repository.userSettings
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = UserSettingsEntity()
        )

    fun updateThemeMode(mode: String) {
        viewModelScope.launch {
            val current = repository.getSettingsDirect()
            repository.updateSettings(current.copy(themeMode = mode))
        }
    }

    fun updateDefaultTts(rate: Float, pitch: Float, voiceName: String = "") {
        viewModelScope.launch {
            val current = repository.getSettingsDirect()
            repository.updateSettings(current.copy(defaultSpeechRate = rate, defaultPitch = pitch, ttsVoiceName = voiceName))
        }
    }

    fun updateDefaultSnooze(minutes: Int) {
        viewModelScope.launch {
            val current = repository.getSettingsDirect()
            repository.updateSettings(current.copy(defaultSnoozeMinutes = minutes))
        }
    }

    fun updateFontSize(scale: Float) {
        viewModelScope.launch {
            val current = repository.getSettingsDirect()
            repository.updateSettings(current.copy(fontSizeScale = scale))
        }
    }

    fun updateElevenLabsSettings(enabled: Boolean, apiKey: String, voiceId: String) {
        viewModelScope.launch {
            val current = repository.getSettingsDirect()
            repository.updateSettings(
                current.copy(
                    useElevenLabs = enabled,
                    elevenLabsApiKey = apiKey,
                    elevenLabsVoiceId = voiceId
                )
            )
        }
    }

    fun updateSettings(settings: UserSettingsEntity) {
        viewModelScope.launch {
            repository.updateSettings(settings)
        }
    }
}
