package com.example.data.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "user_settings")
data class UserSettingsEntity(
    @PrimaryKey val id: Int = 1,
    val themeMode: String = "SYSTEM", // LIGHT, DARK, SYSTEM
    val fontSizeScale: Float = 1.0f,
    val defaultSpeechRate: Float = 1.0f,
    val defaultPitch: Float = 1.0f,
    val defaultSnoozeMinutes: Int = 5,
    val defaultSoundMode: String = "VERSE_THEN_ALARM",
    val defaultTranslation: String = "KJV",
    val ttsLanguage: String = "en-US",
    val alarmVolume: Int = 80
)
