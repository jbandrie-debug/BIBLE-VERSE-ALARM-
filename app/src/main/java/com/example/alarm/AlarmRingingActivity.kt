package com.example.alarm

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.WindowManager
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AlarmOff
import androidx.compose.material.icons.filled.Book
import androidx.compose.material.icons.filled.Snooze
import androidx.compose.material.icons.automirrored.filled.VolumeUp
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.database.AppDatabase
import com.example.data.entity.AlarmEntity
import com.example.data.entity.VerseEntity
import com.example.data.model.VerseSelectionType
import com.example.ui.theme.BibleVerseAlarmTheme
import com.example.util.DateTimeUtils
import kotlinx.coroutines.delay
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

import androidx.core.content.ContextCompat

class AlarmRingingActivity : ComponentActivity() {

    private val currentVerseState = mutableStateOf<VerseEntity?>(null)
    private val isSpeakingState = mutableStateOf(false)

    private val verseStartedReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            if (intent?.action == AlarmReceiver.ACTION_VERSE_SPEAKING_STARTED) {
                Log.d("AlarmRingingActivity", "Received ACTION_VERSE_SPEAKING_STARTED broadcast")
                extractVerseFromIntent(intent)
                isSpeakingState.value = true
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        ContextCompat.registerReceiver(
            this,
            verseStartedReceiver,
            IntentFilter(AlarmReceiver.ACTION_VERSE_SPEAKING_STARTED),
            ContextCompat.RECEIVER_NOT_EXPORTED
        )

        // Turn screen on and show above lockscreen
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O_MR1) {
            setShowWhenLocked(true)
            setTurnScreenOn(true)
        } else {
            @Suppress("DEPRECATION")
            window.addFlags(
                WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED or
                        WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD or
                        WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON or
                        WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON
            )
        }

        extractVerseFromIntent(intent)

        val alarmId = intent.getLongExtra(AlarmReceiver.EXTRA_ALARM_ID, -1L)

        setContent {
            BibleVerseAlarmTheme(darkTheme = true) {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = Color(0xFF100028)
                ) {
                    AlarmRingingScreenContent(
                        alarmId = alarmId,
                        onSnooze = {
                            val snoozeIntent = Intent(this, AlarmService::class.java).apply {
                                action = AlarmService.ACTION_SNOOZE
                                putExtra(AlarmReceiver.EXTRA_ALARM_ID, alarmId)
                            }
                            startService(snoozeIntent)
                            finish()
                        },
                        onStop = {
                            val stopIntent = Intent(this, AlarmService::class.java).apply {
                                action = AlarmService.ACTION_STOP
                            }
                            startService(stopIntent)
                            finish()
                        }
                    )
                }
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        try {
            unregisterReceiver(verseStartedReceiver)
        } catch (e: Exception) {
            Log.e("AlarmRingingActivity", "Error unregistering receiver", e)
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        extractVerseFromIntent(intent)
    }

    private fun extractVerseFromIntent(intent: Intent?) {
        intent ?: return
        val text = intent.getStringExtra(AlarmReceiver.EXTRA_VERSE_TEXT)
        if (!text.isNullOrBlank()) {
            val book = intent.getStringExtra(AlarmReceiver.EXTRA_VERSE_BOOK) ?: "Psalms"
            val chapter = intent.getIntExtra(AlarmReceiver.EXTRA_VERSE_CHAPTER, 1)
            val verseNum = intent.getIntExtra(AlarmReceiver.EXTRA_VERSE_NUMBER, 1)
            val translation = intent.getStringExtra(AlarmReceiver.EXTRA_VERSE_TRANSLATION) ?: "KJV"
            val id = intent.getLongExtra(AlarmReceiver.EXTRA_VERSE_ID, 0L)
            currentVerseState.value = VerseEntity(
                id = id,
                book = book,
                chapter = chapter,
                verseNumber = verseNum,
                text = text,
                translation = translation
            )
        }
    }

    @Composable
    private fun AlarmRingingScreenContent(
        alarmId: Long,
        onSnooze: () -> Unit,
        onStop: () -> Unit
    ) {
        var currentTime by remember { mutableStateOf("") }
        var currentDate by remember { mutableStateOf("") }
        var alarmEntity by remember { mutableStateOf<AlarmEntity?>(null) }
        val verseEntity by currentVerseState

        LaunchedEffect(alarmId) {
            if (alarmId != -1L) {
                val db = AppDatabase.getInstance(applicationContext)
                val alarm = db.alarmDao().getAlarmById(alarmId)
                alarmEntity = alarm

                if (verseEntity == null && alarm != null) {
                    val resolved = when (alarm.verseSelectionType) {
                        VerseSelectionType.SPECIFIC_VERSE -> alarm.selectedVerseId?.let { db.verseDao().getVerseById(it) }
                        VerseSelectionType.RANDOM_VERSE -> db.verseDao().getRandomVerseByTranslation(alarm.selectedTranslation)
                        VerseSelectionType.DAILY_VERSE -> com.example.data.repository.VerseRepository(db.verseDao(), db.favoriteDao()).getDailyVerse(alarm.selectedTranslation)
                        VerseSelectionType.RANDOM_BY_CATEGORY -> alarm.selectedCategory?.let { db.verseDao().getRandomVerseByCategory(it) }
                        VerseSelectionType.RANDOM_BY_BOOK -> alarm.selectedBook?.let { db.verseDao().getRandomVerseByBook(it) }
                    }
                    currentVerseState.value = resolved ?: db.verseDao().getRandomVerse() ?: VerseEntity(
                        book = "Psalms", chapter = 118, verseNumber = 24,
                        text = "This is the day which the LORD hath made; we will rejoice and be glad in it.",
                        translation = "KJV", category = "Morning"
                    )
                }
            }
            while (true) {
                val now = Date()
                currentTime = SimpleDateFormat("hh:mm:ss a", Locale.getDefault()).format(now)
                currentDate = SimpleDateFormat("EEEE, MMMM d, yyyy", Locale.getDefault()).format(now)
                delay(1000)
            }
        }

        val infiniteTransition = rememberInfiniteTransition(label = "pulse")
        val pulseScale by infiniteTransition.animateFloat(
            initialValue = 0.95f,
            targetValue = 1.08f,
            animationSpec = infiniteRepeatable(
                animation = tween(1200, easing = FastOutSlowInEasing),
                repeatMode = RepeatMode.Reverse
            ),
            label = "scale"
        )

        Box(modifier = Modifier.fillMaxSize()) {
            androidx.compose.foundation.Image(
                painter = androidx.compose.ui.res.painterResource(id = com.example.R.drawable.prayer_bg),
                contentDescription = null,
                modifier = Modifier.fillMaxSize(),
                contentScale = androidx.compose.ui.layout.ContentScale.Crop,
                alpha = 0.30f
            )

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        Brush.verticalGradient(
                            colors = listOf(
                                Color(0xFF1B1B3A).copy(alpha = 0.85f),
                                Color(0xFF0D0E23).copy(alpha = 0.90f),
                                Color(0xFF050512).copy(alpha = 0.95f)
                            )
                        )
                    )
                    .padding(24.dp)
                    .verticalScroll(rememberScrollState()),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.SpaceBetween
            ) {
            // Header & Clock
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.padding(top = 24.dp)
            ) {
                Box(
                    modifier = Modifier
                        .scale(pulseScale)
                        .size(72.dp)
                        .clip(CircleShape)
                        .background(Color(0xFF8B5CF6).copy(alpha = 0.2f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.VolumeUp,
                        contentDescription = "Reading Verse",
                        tint = Color(0xFFC084FC),
                        modifier = Modifier.size(36.dp)
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = alarmEntity?.label ?: "Bible Verse Alarm",
                    style = MaterialTheme.typography.titleMedium,
                    color = Color(0xFFD8B4FE)
                )

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = currentTime,
                    style = MaterialTheme.typography.displayMedium.copy(
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace
                    ),
                    color = Color.White
                )

                Text(
                    text = currentDate,
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color.LightGray
                )
            }

            // Verse Card
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 24.dp),
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(
                    containerColor = Color(0xFF26274D).copy(alpha = 0.9f)
                ),
                elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
            ) {
                Column(
                    modifier = Modifier.padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Book,
                            contentDescription = "Bible Verse",
                            tint = Color(0xFFFBBF24),
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "${verseEntity?.book ?: "Psalms"} ${verseEntity?.chapter ?: 118}:${verseEntity?.verseNumber ?: 24} (${verseEntity?.translation ?: "KJV"})",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                            color = Color(0xFFFBBF24)
                        )
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    Text(
                        text = "\"${verseEntity?.text ?: "This is the day which the LORD hath made; we will rejoice and be glad in it."}\"",
                        style = MaterialTheme.typography.bodyLarge.copy(
                            fontSize = 18.sp,
                            lineHeight = 26.sp
                        ),
                        color = Color.White,
                        textAlign = TextAlign.Center
                    )
                }
            }

            // Snooze and Stop Buttons
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Button(
                    onClick = onStop,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp)
                        .testTag("stop_alarm_button"),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFFEF4444)
                    ),
                    shape = RoundedCornerShape(28.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.AlarmOff,
                        contentDescription = "Stop Alarm",
                        tint = Color.White
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "STOP ALARM",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        color = Color.White
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                OutlinedButton(
                    onClick = onSnooze,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp)
                        .testTag("snooze_alarm_button"),
                    shape = RoundedCornerShape(26.dp),
                    colors = ButtonDefaults.outlinedButtonColors(
                        contentColor = Color(0xFFD8B4FE)
                    )
                ) {
                    Icon(
                        imageVector = Icons.Default.Snooze,
                        contentDescription = "Snooze Alarm",
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Snooze (${alarmEntity?.snoozeDurationMinutes ?: 5} Min)",
                        style = MaterialTheme.typography.titleMedium
                    )
                }
            }
        }
    }
}
}
