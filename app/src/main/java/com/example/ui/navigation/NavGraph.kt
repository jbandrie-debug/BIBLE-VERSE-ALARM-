package com.example.ui.navigation

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.R
import com.example.data.entity.AlarmEntity
import com.example.ui.screens.AddEditAlarmScreen
import com.example.ui.screens.AlarmsScreen
import com.example.ui.screens.FavoritesScreen
import com.example.ui.screens.GeminiAiScreen
import com.example.ui.screens.SettingsScreen
import com.example.ui.screens.VersesScreen
import com.example.ui.viewmodel.AlarmViewModel
import com.example.ui.viewmodel.GeminiViewModel
import com.example.ui.viewmodel.SettingsViewModel
import com.example.ui.viewmodel.VerseViewModel

@Composable
fun MainNavGraph(
    alarmViewModel: AlarmViewModel,
    verseViewModel: VerseViewModel,
    settingsViewModel: SettingsViewModel,
    geminiViewModel: GeminiViewModel
) {
    val navController = rememberNavController()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    var editingAlarm by remember { mutableStateOf<AlarmEntity?>(null) }

    val bottomBarScreens = listOf(
        Screen.Alarms,
        Screen.Verses,
        Screen.GeminiAi,
        Screen.Favorites,
        Screen.Settings
    )

    Scaffold(
        containerColor = Color.Transparent,
        bottomBar = {
            if (currentRoute in bottomBarScreens.map { it.route }) {
                NavigationBar(
                    modifier = Modifier.windowInsetsPadding(WindowInsets.navigationBars),
                    containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.92f)
                ) {
                    bottomBarScreens.forEach { screen ->
                        NavigationBarItem(
                            selected = (currentRoute == screen.route),
                            onClick = {
                                navController.navigate(screen.route) {
                                    popUpTo(navController.graph.findStartDestination().id) {
                                        saveState = true
                                    }
                                    launchSingleTop = true
                                    restoreState = true
                                }
                            },
                            icon = { Icon(imageVector = screen.icon, contentDescription = screen.title) },
                            label = { Text(screen.title) },
                            modifier = Modifier.testTag("nav_tab_${screen.route}")
                        )
                    }
                }
            }
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
                .padding(innerPadding)
        ) {
            // Background image layer (30% visible with safe fallback)
            val bgPainter = com.example.util.rememberSafePainterResource(id = R.drawable.prayer_bg)

            if (bgPainter != null) {
                Image(
                    painter = bgPainter,
                    contentDescription = null,
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop,
                    alpha = 0.30f
                )
            }

            NavHost(
                navController = navController,
                startDestination = Screen.Alarms.route,
                modifier = Modifier.fillMaxSize()
            ) {
                composable(Screen.Alarms.route) {
                    AlarmsScreen(
                        viewModel = alarmViewModel,
                        onAddAlarmClick = {
                            editingAlarm = null
                            navController.navigate(Screen.AddEditAlarm.route)
                        },
                        onEditAlarmClick = { alarm ->
                            editingAlarm = alarm
                            navController.navigate(Screen.AddEditAlarm.route)
                        }
                    )
                }

                composable(Screen.AddEditAlarm.route) {
                    AddEditAlarmScreen(
                        initialAlarm = editingAlarm,
                        verseViewModel = verseViewModel,
                        onSave = { alarm ->
                            alarmViewModel.saveAlarm(alarm)
                            navController.popBackStack()
                        },
                        onBack = { navController.popBackStack() }
                    )
                }

                composable(Screen.Verses.route) {
                    VersesScreen(viewModel = verseViewModel)
                }

                composable(Screen.GeminiAi.route) {
                    GeminiAiScreen(viewModel = geminiViewModel)
                }

                composable(Screen.Favorites.route) {
                    FavoritesScreen(verseViewModel = verseViewModel)
                }

                composable(Screen.Settings.route) {
                    SettingsScreen(
                        settingsViewModel = settingsViewModel,
                        verseViewModel = verseViewModel
                    )
                }
            }
        }
    }
}

