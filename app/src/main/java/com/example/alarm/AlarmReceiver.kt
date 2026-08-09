package com.example.alarm

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.PowerManager
import android.util.Log
import com.example.data.database.AppDatabase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class AlarmReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        val action = intent.action
        Log.d("AlarmReceiver", "Received action: $action")

        when (action) {
            ACTION_ALARM_TRIGGER -> {
                val alarmId = intent.getLongExtra(EXTRA_ALARM_ID, -1L)
                if (alarmId != -1L) {
                    val powerManager = context.getSystemService(Context.POWER_SERVICE) as PowerManager
                    val wakeLock = powerManager.newWakeLock(
                        PowerManager.PARTIAL_WAKE_LOCK,
                        "BibleVerseAlarm:WakeLock"
                    )
                    wakeLock.acquire(3000)

                    // Start AlarmService
                    val serviceIntent = Intent(context, AlarmService::class.java).apply {
                        putExtra(EXTRA_ALARM_ID, alarmId)
                    }
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                        context.startForegroundService(serviceIntent)
                    } else {
                        context.startService(serviceIntent)
                    }

                    // Launch Full Screen Activity
                    val ringingIntent = Intent(context, AlarmRingingActivity::class.java).apply {
                        putExtra(EXTRA_ALARM_ID, alarmId)
                        flags = Intent.FLAG_ACTIVITY_NEW_TASK or
                                Intent.FLAG_ACTIVITY_CLEAR_TOP or
                                Intent.FLAG_ACTIVITY_SINGLE_TOP
                    }
                    context.startActivity(ringingIntent)
                }
            }

            Intent.ACTION_BOOT_COMPLETED -> {
                Log.d("AlarmReceiver", "Re-scheduling alarms after reboot...")
                val pendingResult = goAsync()
                CoroutineScope(Dispatchers.IO).launch {
                    try {
                        val db = AppDatabase.getInstance(context)
                        val scheduler = AlarmScheduler(context)
                        val enabledAlarms = db.alarmDao().getEnabledAlarmsList()
                        enabledAlarms.forEach { alarm ->
                            scheduler.scheduleAlarm(alarm)
                        }
                    } catch (e: Exception) {
                        Log.e("AlarmReceiver", "Error re-scheduling alarms after reboot", e)
                    } finally {
                        pendingResult.finish()
                    }
                }
            }

            ACTION_STOP_ALARM -> {
                val stopServiceIntent = Intent(context, AlarmService::class.java).apply {
                    this.action = AlarmService.ACTION_STOP
                }
                androidx.core.content.ContextCompat.startForegroundService(context, stopServiceIntent)
            }

            ACTION_SNOOZE_ALARM -> {
                val alarmId = intent.getLongExtra(EXTRA_ALARM_ID, -1L)
                val snoozeIntent = Intent(context, AlarmService::class.java).apply {
                    this.action = AlarmService.ACTION_SNOOZE
                    putExtra(EXTRA_ALARM_ID, alarmId)
                }
                androidx.core.content.ContextCompat.startForegroundService(context, snoozeIntent)
            }
        }
    }

    companion object {
        const val ACTION_ALARM_TRIGGER = "com.example.bibleversealarm.ACTION_TRIGGER"
        const val ACTION_STOP_ALARM = "com.example.bibleversealarm.ACTION_STOP"
        const val ACTION_SNOOZE_ALARM = "com.example.bibleversealarm.ACTION_SNOOZE"
        const val ACTION_VERSE_SPEAKING_STARTED = "com.example.bibleversealarm.ACTION_VERSE_SPEAKING_STARTED"
        const val EXTRA_ALARM_ID = "extra_alarm_id"
        const val EXTRA_VERSE_ID = "extra_verse_id"
        const val EXTRA_VERSE_BOOK = "extra_verse_book"
        const val EXTRA_VERSE_CHAPTER = "extra_verse_chapter"
        const val EXTRA_VERSE_NUMBER = "extra_verse_number"
        const val EXTRA_VERSE_TEXT = "extra_verse_text"
        const val EXTRA_VERSE_TRANSLATION = "extra_verse_translation"
    }
}
