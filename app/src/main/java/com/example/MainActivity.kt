package com.example

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.core.content.ContextCompat
import com.example.alarm.AlarmScheduler
import com.example.data.database.AppDatabase
import com.example.data.repository.AlarmRepository
import com.example.data.repository.SettingsRepository
import com.example.data.repository.VerseRepository
import com.example.ui.navigation.MainNavGraph
import com.example.ui.theme.BibleVerseAlarmTheme
import com.example.ui.viewmodel.AlarmViewModel
import com.example.ui.viewmodel.SettingsViewModel
import com.example.ui.viewmodel.VerseViewModel

class MainActivity : ComponentActivity() {

    @android.annotation.SuppressLint("InvalidFragmentVersionForActivityResult")
    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted: Boolean ->
        // Notification permission result handled
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        requestNotificationPermissionIfNeeded()

        val db = AppDatabase.getInstance(applicationContext)
        val scheduler = AlarmScheduler(applicationContext)

        val alarmRepo = AlarmRepository(db.alarmDao())
        val verseRepo = VerseRepository(db.verseDao(), db.favoriteDao())
        val settingsRepo = SettingsRepository(db.userSettingsDao())

        val alarmViewModel = AlarmViewModel(alarmRepo, scheduler)
        val verseViewModel = VerseViewModel(verseRepo)
        val settingsViewModel = SettingsViewModel(settingsRepo)

        setContent {
            val userSettings by settingsViewModel.settings.collectAsState()

            val isDarkTheme = when (userSettings?.themeMode) {
                "LIGHT" -> false
                "DARK" -> true
                else -> isSystemInDarkTheme()
            }

            BibleVerseAlarmTheme(darkTheme = isDarkTheme) {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    MainNavGraph(
                        alarmViewModel = alarmViewModel,
                        verseViewModel = verseViewModel,
                        settingsViewModel = settingsViewModel
                    )
                }
            }
        }
    }

    private fun requestNotificationPermissionIfNeeded() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            try {
                if (ContextCompat.checkSelfPermission(
                        this,
                        Manifest.permission.POST_NOTIFICATIONS
                    ) != PackageManager.PERMISSION_GRANTED
                ) {
                    requestPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                }
            } catch (e: Exception) {
                android.util.Log.e("MainActivity", "Error requesting notification permission", e)
            }
        }
    }
}
