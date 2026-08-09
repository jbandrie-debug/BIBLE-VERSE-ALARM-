package com.example.ui.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Alarm
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Book
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Settings
import androidx.compose.ui.graphics.vector.ImageVector

sealed class Screen(val route: String, val title: String, val icon: ImageVector) {
    object Alarms : Screen("alarms", "Alarms", Icons.Default.Alarm)
    object Verses : Screen("verses", "Verses", Icons.Default.Book)
    object GeminiAi : Screen("gemini_ai", "AI Companion", Icons.Default.AutoAwesome)
    object Favorites : Screen("favorites", "Favorites", Icons.Default.Favorite)
    object Settings : Screen("settings", "Settings", Icons.Default.Settings)
    object AddEditAlarm : Screen("add_edit_alarm", "Add Alarm", Icons.Default.Alarm)
}
