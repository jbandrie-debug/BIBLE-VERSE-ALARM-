package com.example.util

import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

object DateTimeUtils {

    enum class DayOfWeek(val calendarConst: Int, val bitFlag: Int, val shortName: String, val fullName: String) {
        SUNDAY(Calendar.SUNDAY, 1, "Sun", "Sunday"),
        MONDAY(Calendar.MONDAY, 2, "Mon", "Monday"),
        TUESDAY(Calendar.TUESDAY, 4, "Tue", "Tuesday"),
        WEDNESDAY(Calendar.WEDNESDAY, 8, "Wed", "Wednesday"),
        THURSDAY(Calendar.THURSDAY, 16, "Thu", "Thursday"),
        FRIDAY(Calendar.FRIDAY, 32, "Fri", "Friday"),
        SATURDAY(Calendar.SATURDAY, 64, "Sat", "Saturday")
    }

    fun formatTime(hour: Int, minute: Int, is24Hour: Boolean = false): String {
        val cal = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, hour)
            set(Calendar.MINUTE, minute)
        }
        val pattern = if (is24Hour) "HH:mm" else "hh:mm a"
        val sdf = SimpleDateFormat(pattern, Locale.getDefault())
        return sdf.format(cal.time)
    }

    fun isDaySelected(bitmask: Int, day: DayOfWeek): Boolean {
        return (bitmask and day.bitFlag) != 0
    }

    fun toggleDay(bitmask: Int, day: DayOfWeek): Int {
        return bitmask xor day.bitFlag
    }

    fun formatRepeatSummary(bitmask: Int): String {
        if (bitmask == 0) return "One-time alarm"
        if (bitmask == 127) return "Every day"
        if (bitmask == 62) return "Weekdays (Mon-Fri)"
        if (bitmask == 65) return "Weekends (Sat-Sun)"

        val selected = DayOfWeek.values().filter { isDaySelected(bitmask, it) }
        return selected.joinToString(", ") { it.shortName }
    }

    fun calculateNextTriggerMillis(hour: Int, minute: Int, repeatDaysBitmask: Int): Long {
        val now = Calendar.getInstance()
        val target = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, hour)
            set(Calendar.MINUTE, minute)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }

        if (repeatDaysBitmask == 0) {
            // One time alarm
            if (target.before(now)) {
                target.add(Calendar.DAY_OF_YEAR, 1)
            }
            return target.timeInMillis
        }

        // Repeating alarm: check up to 7 days ahead
        for (i in 0..7) {
            val candidate = (target.clone() as Calendar).apply {
                add(Calendar.DAY_OF_YEAR, i)
            }
            if (candidate.after(now)) {
                val dayConst = candidate.get(Calendar.DAY_OF_WEEK)
                val dayEnum = DayOfWeek.values().find { it.calendarConst == dayConst }
                if (dayEnum != null && isDaySelected(repeatDaysBitmask, dayEnum)) {
                    return candidate.timeInMillis
                }
            }
        }

        // Fallback: next occurrence of target time
        if (target.before(now)) {
            target.add(Calendar.DAY_OF_YEAR, 1)
        }
        return target.timeInMillis
    }

    fun getTimeUntilTriggerMessage(triggerMillis: Long): String {
        val diffMs = triggerMillis - System.currentTimeMillis()
        if (diffMs <= 0) return "Alarm ringing soon"

        val minutesTotal = diffMs / (1000 * 60)
        val hours = minutesTotal / 60
        val minutes = minutesTotal % 60

        return when {
            hours == 0L && minutes == 0L -> "Alarm set for less than a minute from now"
            hours == 0L -> "Alarm set for $minutes minutes from now"
            minutes == 0L -> "Alarm set for $hours hours from now"
            else -> "Alarm set for $hours hours and $minutes minutes from now"
        }
    }

    fun getTimeBasedGreeting(hour: Int = Calendar.getInstance().get(Calendar.HOUR_OF_DAY)): String {
        return when (hour) {
            in 4..11 -> "Good morning"
            in 12..16 -> "Good afternoon"
            in 17..21 -> "Good evening"
            else -> "Good night"
        }
    }
}
