package com.example.data.model

enum class SoundMode(val displayName: String, val description: String) {
    VERSE_THEN_ALARM("Verse Then Alarm", "Reads verse aloud first, then plays ringtone"),
    ALARM_THEN_VERSE("Alarm Then Verse", "Plays ringtone first, then reads verse aloud"),
    VERSE_ONLY("Verse Only", "Only reads verse aloud with gentle background tone"),
    ALARM_ONLY("Alarm Sound Only", "Plays classic alarm ringtone only")
}
