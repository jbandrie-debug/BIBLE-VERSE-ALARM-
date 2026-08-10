package com.example.alarm

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.media.AudioAttributes
import android.media.Ringtone
import android.media.RingtoneManager
import android.net.Uri
import android.os.Build
import android.os.IBinder
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import android.util.Log
import androidx.core.app.NotificationCompat
import com.example.data.database.AppDatabase
import com.example.data.entity.AlarmEntity
import com.example.data.entity.VerseEntity
import com.example.data.model.SoundMode
import com.example.data.model.VerseSelectionType
import com.example.service.TtsManager
import com.example.util.DateTimeUtils
import java.util.Calendar
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class AlarmService : Service() {

    private val serviceScope = CoroutineScope(Dispatchers.IO + Job())
    private var ringtone: Ringtone? = null
    private var vibrator: Vibrator? = null
    private var ttsManager: TtsManager? = null
    private var currentAlarm: AlarmEntity? = null
    private var currentVerse: VerseEntity? = null

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onCreate() {
        super.onCreate()
        createNotificationChannel()
        ttsManager = TtsManager(applicationContext)
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        val action = intent?.action
        val alarmId = intent?.getLongExtra(AlarmReceiver.EXTRA_ALARM_ID, -1L) ?: -1L

        startInitialForegroundNotification()

        when (action) {
            ACTION_STOP -> {
                stopAlarmAndSelf()
                return START_NOT_STICKY
            }
            ACTION_SNOOZE -> {
                snoozeAlarm(alarmId)
                return START_NOT_STICKY
            }
        }

        if (alarmId != -1L) {
            serviceScope.launch {
                val db = AppDatabase.getInstance(applicationContext)
                val alarm = db.alarmDao().getAlarmById(alarmId)
                if (alarm != null) {
                    currentAlarm = alarm
                    val verse = resolveVerseForAlarm(db, alarm)
                    currentVerse = verse

                    withContext(Dispatchers.Main) {
                        startForegroundWithNotification(alarm, verse)
                        executeAlarmAudioFlow(alarm, verse)
                    }
                }
            }
        }

        return START_STICKY
    }

    private fun startInitialForegroundNotification() {
        val notification = NotificationCompat.Builder(this, CHANNEL_ID)
            .setSmallIcon(android.R.drawable.ic_lock_idle_alarm)
            .setContentTitle("Bible Verse Alarm")
            .setContentText("Alarm is ringing...")
            .setPriority(NotificationCompat.PRIORITY_MAX)
            .setCategory(NotificationCompat.CATEGORY_ALARM)
            .setOngoing(true)
            .build()
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            startForeground(NOTIFICATION_ID, notification, android.content.pm.ServiceInfo.FOREGROUND_SERVICE_TYPE_MEDIA_PLAYBACK)
        } else {
            startForeground(NOTIFICATION_ID, notification)
        }
    }

    private suspend fun resolveVerseForAlarm(db: AppDatabase, alarm: AlarmEntity): VerseEntity {
        return when (alarm.verseSelectionType) {
            VerseSelectionType.SPECIFIC_VERSE -> {
                alarm.selectedVerseId?.let { db.verseDao().getVerseById(it) }
                    ?: db.verseDao().getRandomVerse()
                    ?: defaultFallbackVerse()
            }
            VerseSelectionType.RANDOM_VERSE -> {
                db.verseDao().getRandomVerseByTranslation(alarm.selectedTranslation)
                    ?: db.verseDao().getRandomVerse()
                    ?: defaultFallbackVerse()
            }
            VerseSelectionType.DAILY_VERSE -> {
                val repo = com.example.data.repository.VerseRepository(db.verseDao(), db.favoriteDao())
                repo.getDailyVerse(alarm.selectedTranslation) ?: defaultFallbackVerse()
            }
            VerseSelectionType.RANDOM_BY_CATEGORY -> {
                val cat = alarm.selectedCategory ?: "Morning"
                db.verseDao().getRandomVerseByCategory(cat) ?: defaultFallbackVerse()
            }
            VerseSelectionType.RANDOM_BY_BOOK -> {
                val book = alarm.selectedBook ?: "Psalms"
                db.verseDao().getRandomVerseByBook(book) ?: defaultFallbackVerse()
            }
        }
    }

    private fun defaultFallbackVerse() = VerseEntity(
        book = "Psalms", chapter = 118, verseNumber = 24,
        text = "This is the day which the LORD hath made; we will rejoice and be glad in it.",
        translation = "KJV", category = "Morning"
    )

    private fun startForegroundWithNotification(alarm: AlarmEntity, verse: VerseEntity) {
        val ringingIntent = Intent(this, AlarmRingingActivity::class.java).apply {
            putExtra(AlarmReceiver.EXTRA_ALARM_ID, alarm.id)
            putExtra(AlarmReceiver.EXTRA_VERSE_ID, verse.id)
            putExtra(AlarmReceiver.EXTRA_VERSE_BOOK, verse.book)
            putExtra(AlarmReceiver.EXTRA_VERSE_CHAPTER, verse.chapter)
            putExtra(AlarmReceiver.EXTRA_VERSE_NUMBER, verse.verseNumber)
            putExtra(AlarmReceiver.EXTRA_VERSE_TEXT, verse.text)
            putExtra(AlarmReceiver.EXTRA_VERSE_TRANSLATION, verse.translation)
            putExtra(AlarmReceiver.EXTRA_VERSE_PRAYER, verse.prayer)
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
        }

        try {
            startActivity(ringingIntent)
        } catch (e: Exception) {
            Log.e("AlarmService", "Failed to start AlarmRingingActivity", e)
        }

        val fullScreenPendingIntent = PendingIntent.getActivity(
            this,
            alarm.id.toInt(),
            ringingIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val stopIntent = Intent(this, AlarmService::class.java).apply { action = ACTION_STOP }
        val stopPendingIntent = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            PendingIntent.getForegroundService(
                this,
                (alarm.id * 10 + 1).toInt(),
                stopIntent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )
        } else {
            PendingIntent.getService(
                this,
                (alarm.id * 10 + 1).toInt(),
                stopIntent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )
        }

        val snoozeIntent = Intent(this, AlarmService::class.java).apply {
            action = ACTION_SNOOZE
            putExtra(AlarmReceiver.EXTRA_ALARM_ID, alarm.id)
        }
        val snoozePendingIntent = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            PendingIntent.getForegroundService(
                this,
                (alarm.id * 10 + 2).toInt(),
                snoozeIntent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )
        } else {
            PendingIntent.getService(
                this,
                (alarm.id * 10 + 2).toInt(),
                snoozeIntent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )
        }

        val notification = NotificationCompat.Builder(this, CHANNEL_ID)
            .setSmallIcon(android.R.drawable.ic_lock_idle_alarm)
            .setContentTitle(alarm.label)
            .setContentText("${verse.book} ${verse.chapter}:${verse.verseNumber} - \"${verse.text}\"")
            .setStyle(
                NotificationCompat.BigTextStyle()
                    .bigText("${verse.book} ${verse.chapter}:${verse.verseNumber}\n\"${verse.text}\"")
            )
            .setPriority(NotificationCompat.PRIORITY_MAX)
            .setCategory(NotificationCompat.CATEGORY_ALARM)
            .setFullScreenIntent(fullScreenPendingIntent, true)
            .addAction(android.R.drawable.ic_menu_close_clear_cancel, "Stop", stopPendingIntent)
            .addAction(android.R.drawable.ic_popup_sync, "Snooze", snoozePendingIntent)
            .setOngoing(true)
            .build()

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            startForeground(NOTIFICATION_ID, notification, android.content.pm.ServiceInfo.FOREGROUND_SERVICE_TYPE_MEDIA_PLAYBACK)
        } else {
            startForeground(NOTIFICATION_ID, notification)
        }
    }

    private fun notifyVerseSpeaking(verse: VerseEntity) {
        val intent = Intent(AlarmReceiver.ACTION_VERSE_SPEAKING_STARTED).apply {
            putExtra(AlarmReceiver.EXTRA_VERSE_ID, verse.id)
            putExtra(AlarmReceiver.EXTRA_VERSE_BOOK, verse.book)
            putExtra(AlarmReceiver.EXTRA_VERSE_CHAPTER, verse.chapter)
            putExtra(AlarmReceiver.EXTRA_VERSE_NUMBER, verse.verseNumber)
            putExtra(AlarmReceiver.EXTRA_VERSE_TEXT, verse.text)
            putExtra(AlarmReceiver.EXTRA_VERSE_TRANSLATION, verse.translation)
            putExtra(AlarmReceiver.EXTRA_VERSE_PRAYER, verse.prayer)
            setPackage(packageName)
        }
        sendBroadcast(intent)
    }

    private fun executeAlarmAudioFlow(alarm: AlarmEntity, verse: VerseEntity) {
        serviceScope.launch(Dispatchers.IO) {
            val db = AppDatabase.getInstance(applicationContext)
            val settings = db.userSettingsDao().getUserSettingsDirect()
            val voiceName = settings?.ttsVoiceName ?: ""
            val rate = if (alarm.ttsSpeechRate > 0) alarm.ttsSpeechRate else (settings?.defaultSpeechRate ?: 1.0f)
            val pitch = if (alarm.ttsPitch > 0) alarm.ttsPitch else (settings?.defaultPitch ?: 1.0f)

            withContext(Dispatchers.Main) {
                ttsManager?.configure(
                    speechRate = rate,
                    pitch = pitch,
                    voiceName = voiceName
                )

                if (alarm.isVibrate) {
                    startVibration()
                }

                val isTagalogSpeech = voiceName.contains("filipino") || voiceName.contains("tagalog") || voiceName.contains("pedro") || voiceName == "preset-old-male" ||
                        verse.translation.equals("FSV", ignoreCase = true) || verse.translation.equals("TAG", ignoreCase = true) ||
                        verse.translation.equals("ADB", ignoreCase = true) || verse.translation.equals("SND", ignoreCase = true)

                val greeting = if (isTagalogSpeech) {
                    when {
                        alarm.timeHour in 4..11 -> "Magandang umaga! Narito ang salita ng Diyos para sa iyo ngayong araw."
                        alarm.timeHour in 12..17 -> "Magandang hapon! Narito ang salita ng Diyos para sa iyo ngayong araw."
                        else -> "Magandang gabi! Narito ang salita ng Diyos para sa iyo."
                    }
                } else {
                    "${DateTimeUtils.getTimeBasedGreeting(alarm.timeHour)}! Here is your Bible verse for today."
                }

                val prayerLabel = if (isTagalogSpeech) ". Panalangin: " else ". Prayer: "
                val prayerPart = if (verse.prayer.isNotBlank()) "$prayerLabel${verse.prayer}" else ""
                val refPart = if (isTagalogSpeech) {
                    "${verse.book}, kapitulo ${verse.chapter}, bersikulo ${verse.verseNumber}"
                } else {
                    "${verse.book}, chapter ${verse.chapter}, verse ${verse.verseNumber}"
                }

                val verseTextToSpeak = "$greeting $refPart. ${verse.text}$prayerPart"

                when (alarm.soundMode) {
                    SoundMode.VERSE_THEN_ALARM -> {
                        ttsManager?.speak(
                            text = verseTextToSpeak,
                            onStart = { notifyVerseSpeaking(verse) },
                            onDone = {
                                serviceScope.launch(Dispatchers.Main) {
                                    startRingtone(alarm)
                                }
                            }
                        )
                    }
                    SoundMode.ALARM_THEN_VERSE -> {
                        startRingtone(alarm)
                        serviceScope.launch {
                            delay(10000) // Ring for 10 seconds first
                            withContext(Dispatchers.Main) {
                                stopRingtone()
                                ttsManager?.speak(
                                    text = verseTextToSpeak,
                                    onStart = { notifyVerseSpeaking(verse) }
                                )
                            }
                        }
                    }
                    SoundMode.VERSE_ONLY -> {
                        ttsManager?.speak(
                            text = verseTextToSpeak,
                            onStart = { notifyVerseSpeaking(verse) }
                        )
                    }
                    SoundMode.ALARM_ONLY -> {
                        startRingtone(alarm)
                        notifyVerseSpeaking(verse)
                    }
                }
            }
        }
    }

    private fun startRingtone(alarm: AlarmEntity) {
        try {
            val ringtoneUri = if (!alarm.ringtoneUri.isNullOrEmpty()) {
                Uri.parse(alarm.ringtoneUri)
            } else {
                RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM)
                    ?: RingtoneManager.getDefaultUri(RingtoneManager.TYPE_RINGTONE)
            }

            ringtone = RingtoneManager.getRingtone(applicationContext, ringtoneUri).apply {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                    audioAttributes = AudioAttributes.Builder()
                        .setUsage(AudioAttributes.USAGE_ALARM)
                        .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                        .build()
                }
                play()
            }
        } catch (e: Exception) {
            Log.e("AlarmService", "Failed to play ringtone", e)
        }
    }

    private fun stopRingtone() {
        ringtone?.stop()
        ringtone = null
    }

    private fun startVibration() {
        try {
            vibrator = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                val vm = getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as VibratorManager
                vm.defaultVibrator
            } else {
                @Suppress("DEPRECATION")
                getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
            }

            val pattern = longArrayOf(0, 500, 500)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                vibrator?.vibrate(VibrationEffect.createWaveform(pattern, 0))
            } else {
                @Suppress("DEPRECATION")
                vibrator?.vibrate(pattern, 0)
            }
        } catch (e: Exception) {
            Log.e("AlarmService", "Failed to start vibration", e)
        }
    }

    private fun stopVibration() {
        vibrator?.cancel()
        vibrator = null
    }

    private fun stopAlarmAndSelf() {
        ttsManager?.stop()
        stopRingtone()
        stopVibration()

        currentAlarm?.let { alarm ->
            serviceScope.launch {
                val db = AppDatabase.getInstance(applicationContext)
                val scheduler = AlarmScheduler(applicationContext)

                if (alarm.repeatDaysBitmask == 0) {
                    // One-time alarm -> disable
                    db.alarmDao().setAlarmEnabled(alarm.id, false)
                } else {
                    // Re-schedule for next recurring day
                    scheduler.scheduleAlarm(alarm)
                }
            }
        }

        stopForeground(Service.STOP_FOREGROUND_REMOVE)
        stopSelf()
    }

    private fun snoozeAlarm(alarmId: Long) {
        ttsManager?.stop()
        stopRingtone()
        stopVibration()

        serviceScope.launch {
            val db = AppDatabase.getInstance(applicationContext)
            val alarm = db.alarmDao().getAlarmById(alarmId) ?: currentAlarm
            if (alarm != null) {
                val snoozeMinutes = alarm.snoozeDurationMinutes.coerceAtLeast(1)
                val snoozeTriggerMillis = System.currentTimeMillis() + (snoozeMinutes * 60 * 1000)

                val snoozeAlarm = alarm.copy(
                    timeHour = (Calendar.getInstance().apply { timeInMillis = snoozeTriggerMillis }.get(Calendar.HOUR_OF_DAY)),
                    timeMinute = (Calendar.getInstance().apply { timeInMillis = snoozeTriggerMillis }.get(Calendar.MINUTE))
                )

                val scheduler = AlarmScheduler(applicationContext)
                scheduler.scheduleAlarm(snoozeAlarm)
            }
        }

        stopForeground(Service.STOP_FOREGROUND_REMOVE)
        stopSelf()
    }

    override fun onDestroy() {

        ttsManager?.shutdown()
        stopRingtone()
        stopVibration()
        super.onDestroy()
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                "Bible Verse Alarm Channel",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Channel for active Bible Verse Alarms"
                setBypassDnd(true)
                lockscreenVisibility = NotificationCompat.VISIBILITY_PUBLIC
            }
            val manager = getSystemService(NotificationManager::class.java)
            manager.createNotificationChannel(channel)
        }
    }

    companion object {
        const val CHANNEL_ID = "bible_verse_alarm_channel"
        const val NOTIFICATION_ID = 1001
        const val ACTION_STOP = "com.example.bibleversealarm.SERVICE_STOP"
        const val ACTION_SNOOZE = "com.example.bibleversealarm.SERVICE_SNOOZE"
    }
}
