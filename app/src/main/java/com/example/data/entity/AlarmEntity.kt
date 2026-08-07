package com.example.data.entity

import androidx.room.Entity
import androidx.room.Ignore
import androidx.room.PrimaryKey
import com.example.data.model.SoundMode
import com.example.data.model.VerseSelectionType

@Entity(tableName = "alarms")
data class AlarmEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val timeHour: Int,
    val timeMinute: Int,
    val label: String = "Bible Verse Alarm",
    val isEnabled: Boolean = true,
    val repeatDaysBitmask: Int = 0, // 0 = One time alarm; 1=Sun, 2=Mon, 4=Tue, 8=Wed, 16=Thu, 32=Fri, 64=Sat
    val soundMode: SoundMode = SoundMode.VERSE_THEN_ALARM,
    val verseSelectionType: VerseSelectionType = VerseSelectionType.DAILY_VERSE,
    val selectedVerseId: Long? = null,
    val selectedTranslation: String = "KJV",
    val selectedCategory: String? = null,
    val selectedBook: String? = null,
    val snoozeDurationMinutes: Int = 5,
    val ttsSpeechRate: Float = 1.0f,
    val ttsPitch: Float = 1.0f,
    val isVibrate: Boolean = true,
    val ringtoneUri: String? = null
) {
    @get:Ignore
    val time: Long
        get() {
            val calendar = java.util.Calendar.getInstance().apply {
                set(java.util.Calendar.HOUR_OF_DAY, timeHour)
                set(java.util.Calendar.MINUTE, timeMinute)
                set(java.util.Calendar.SECOND, 0)
                set(java.util.Calendar.MILLISECOND, 0)
            }
            return calendar.timeInMillis
        }

    @get:Ignore
    val enabled: Boolean
        get() = isEnabled

    @get:Ignore
    val repeatDays: Int
        get() = repeatDaysBitmask
}
