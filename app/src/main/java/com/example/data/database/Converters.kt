package com.example.data.database

import androidx.room.TypeConverter
import com.example.data.model.SoundMode
import com.example.data.model.VerseSelectionType

class Converters {
    @TypeConverter
    fun fromSoundMode(value: SoundMode?): String {
        return value?.name ?: SoundMode.VERSE_THEN_ALARM.name
    }

    @TypeConverter
    fun toSoundMode(value: String?): SoundMode {
        if (value.isNullOrBlank()) return SoundMode.VERSE_THEN_ALARM
        return try {
            SoundMode.valueOf(value)
        } catch (e: Exception) {
            SoundMode.VERSE_THEN_ALARM
        }
    }

    @TypeConverter
    fun fromVerseSelectionType(value: VerseSelectionType?): String {
        return value?.name ?: VerseSelectionType.DAILY_VERSE.name
    }

    @TypeConverter
    fun toVerseSelectionType(value: String?): VerseSelectionType {
        if (value.isNullOrBlank()) return VerseSelectionType.DAILY_VERSE
        return try {
            VerseSelectionType.valueOf(value)
        } catch (e: Exception) {
            VerseSelectionType.DAILY_VERSE
        }
    }
}
