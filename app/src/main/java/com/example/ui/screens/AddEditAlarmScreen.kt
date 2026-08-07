package com.example.ui.screens

import android.app.TimePickerDialog
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.ArrowDropUp
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Slider
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.entity.AlarmEntity
import com.example.data.model.SoundMode
import com.example.data.model.VerseSelectionType
import com.example.ui.viewmodel.VerseViewModel
import com.example.util.DateTimeUtils

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddEditAlarmScreen(
    initialAlarm: AlarmEntity?,
    verseViewModel: VerseViewModel,
    onSave: (AlarmEntity) -> Unit,
    onBack: () -> Unit
) {
    val context = LocalContext.current
    val categories by verseViewModel.categories.collectAsState()
    val books by verseViewModel.books.collectAsState()

    var hour by remember { mutableIntStateOf(initialAlarm?.timeHour ?: 7) }
    var minute by remember { mutableIntStateOf(initialAlarm?.timeMinute ?: 0) }
    var label by remember { mutableStateOf(initialAlarm?.label ?: "Bible Verse Alarm") }
    var isEnabled by remember { mutableStateOf(initialAlarm?.isEnabled ?: true) }
    var repeatDaysBitmask by remember { mutableIntStateOf(initialAlarm?.repeatDaysBitmask ?: 62) }
    var soundMode by remember { mutableStateOf(initialAlarm?.soundMode ?: SoundMode.VERSE_THEN_ALARM) }
    
    var verseSelectionType by remember { mutableStateOf(initialAlarm?.verseSelectionType ?: VerseSelectionType.DAILY_VERSE) }
    var selectedCategory by remember { mutableStateOf(initialAlarm?.selectedCategory ?: "Morning") }
    var selectedBook by remember { mutableStateOf(initialAlarm?.selectedBook ?: "Psalms") }
    var selectedVerseId by remember { mutableStateOf<Long?>(initialAlarm?.selectedVerseId) }
    
    var snoozeMinutes by remember { mutableIntStateOf(initialAlarm?.snoozeDurationMinutes ?: 5) }
    var speechRate by remember { mutableFloatStateOf(initialAlarm?.ttsSpeechRate ?: 1.0f) }
    var pitch by remember { mutableFloatStateOf(initialAlarm?.ttsPitch ?: 1.0f) }

    var isCategoryDropdownExpanded by remember { mutableStateOf(false) }
    var isBookDropdownExpanded by remember { mutableStateOf(false) }
    var isAdvancedSettingsExpanded by remember { mutableStateOf(false) }

    val formattedTime = remember(hour, minute) {
        val h = if (hour == 0) 12 else if (hour > 12) hour - 12 else hour
        val amPm = if (hour >= 12) "PM" else "AM"
        val m = minute.toString().padStart(2, '0')
        "$h:$m $amPm"
    }

    Scaffold(
        containerColor = Color.Transparent,
        topBar = {
            TopAppBar(
                title = { Text(if (initialAlarm == null) "New Alarm" else "Edit Alarm") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(imageVector = Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Transparent)
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 24.dp, vertical = 16.dp),
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            // Time and Toggle
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = formattedTime,
                    fontSize = 56.sp,
                    fontWeight = FontWeight.Light,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.clickable {
                        TimePickerDialog(
                            context,
                            { _, selectedHour, selectedMinute ->
                                hour = selectedHour
                                minute = selectedMinute
                            },
                            hour,
                            minute,
                            false
                        ).show()
                    }
                )
                Switch(
                    checked = isEnabled,
                    onCheckedChange = { isEnabled = it },
                    modifier = Modifier.testTag("alarm_on_off_toggle")
                )
            }

            // Days of Week
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                DateTimeUtils.DayOfWeek.values().forEach { day ->
                    val isSelected = DateTimeUtils.isDaySelected(repeatDaysBitmask, day)
                    Box(
                        modifier = Modifier
                            .size(40.dp)
                            .clip(CircleShape)
                            .background(
                                if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant
                            )
                            .clickable {
                                repeatDaysBitmask = DateTimeUtils.toggleDay(repeatDaysBitmask, day)
                            },
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = day.shortName.take(1),
                            color = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurfaceVariant,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }

            // Verse Selection Strategy
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
            ) {
                Column(modifier = Modifier.padding(20.dp)) {
                    Text(
                        text = "Verse Source",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    VerseSelectionType.values().filter { it != VerseSelectionType.SPECIFIC_VERSE }.forEach { type ->
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { verseSelectionType = type }
                                .padding(vertical = 8.dp)
                        ) {
                            RadioButton(
                                selected = (verseSelectionType == type),
                                onClick = { verseSelectionType = type }
                            )
                            Spacer(modifier = Modifier.width(12.dp))
                            Text(text = type.displayName, style = MaterialTheme.typography.bodyLarge)
                        }
                    }

                    // Conditional Dropdowns based on selection
                    AnimatedVisibility(visible = verseSelectionType == VerseSelectionType.RANDOM_BY_CATEGORY) {
                        Column(modifier = Modifier.padding(top = 16.dp)) {
                            Text("Select Theme", style = MaterialTheme.typography.labelLarge)
                            Box {
                                OutlinedTextField(
                                    value = selectedCategory,
                                    onValueChange = {},
                                    readOnly = true,
                                    modifier = Modifier.fillMaxWidth(),
                                    trailingIcon = {
                                        IconButton(onClick = { isCategoryDropdownExpanded = true }) {
                                            Icon(Icons.Default.ArrowDropDown, null)
                                        }
                                    },
                                    shape = RoundedCornerShape(12.dp)
                                )
                                DropdownMenu(
                                    expanded = isCategoryDropdownExpanded,
                                    onDismissRequest = { isCategoryDropdownExpanded = false }
                                ) {
                                    categories.forEach { cat ->
                                        DropdownMenuItem(
                                            text = { Text(cat) },
                                            onClick = {
                                                selectedCategory = cat
                                                isCategoryDropdownExpanded = false
                                            }
                                        )
                                    }
                                }
                            }
                        }
                    }

                    AnimatedVisibility(visible = verseSelectionType == VerseSelectionType.RANDOM_BY_BOOK) {
                        Column(modifier = Modifier.padding(top = 16.dp)) {
                            Text("Select Book", style = MaterialTheme.typography.labelLarge)
                            Box {
                                OutlinedTextField(
                                    value = selectedBook,
                                    onValueChange = {},
                                    readOnly = true,
                                    modifier = Modifier.fillMaxWidth(),
                                    trailingIcon = {
                                        IconButton(onClick = { isBookDropdownExpanded = true }) {
                                            Icon(Icons.Default.ArrowDropDown, null)
                                        }
                                    },
                                    shape = RoundedCornerShape(12.dp)
                                )
                                DropdownMenu(
                                    expanded = isBookDropdownExpanded,
                                    onDismissRequest = { isBookDropdownExpanded = false }
                                ) {
                                    books.forEach { book ->
                                        DropdownMenuItem(
                                            text = { Text(book) },
                                            onClick = {
                                                selectedBook = book
                                                isBookDropdownExpanded = false
                                            }
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
            
            // Toggle Advanced Settings
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { isAdvancedSettingsExpanded = !isAdvancedSettingsExpanded }
                    .padding(vertical = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("Advanced Settings", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
                Icon(
                    imageVector = if (isAdvancedSettingsExpanded) Icons.Default.ArrowDropUp else Icons.Default.ArrowDropDown,
                    contentDescription = "Toggle",
                    modifier = Modifier.size(32.dp)
                )
            }
            
            AnimatedVisibility(visible = isAdvancedSettingsExpanded) {
                Column(verticalArrangement = Arrangement.spacedBy(20.dp)) {
                    // Label Input
                    OutlinedTextField(
                        value = label,
                        onValueChange = { label = it },
                        label = { Text("Alarm Label") },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        singleLine = true
                    )
                    
                    // Sound Mode
                    Text("Sound Mode", style = MaterialTheme.typography.labelLarge)
                    Column {
                        SoundMode.values().forEach { mode ->
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable { soundMode = mode }
                                    .padding(vertical = 4.dp)
                            ) {
                                RadioButton(selected = (soundMode == mode), onClick = { soundMode = mode })
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(mode.displayName)
                            }
                        }
                    }
                    
                    // Voice Settings
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f))
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text("Voice Settings", style = MaterialTheme.typography.labelLarge)
                            Spacer(modifier = Modifier.height(12.dp))
                            Text("Speech Rate: ${String.format("%.1f", speechRate)}x")
                            Slider(value = speechRate, onValueChange = { speechRate = it }, valueRange = 0.5f..2.0f)
                            Spacer(modifier = Modifier.height(8.dp))
                            Text("Voice Pitch: ${String.format("%.1f", pitch)}")
                            Slider(value = pitch, onValueChange = { pitch = it }, valueRange = 0.5f..2.0f)
                        }
                    }
                    
                    // Snooze Duration
                    Text("Snooze Duration", style = MaterialTheme.typography.labelLarge)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        listOf(5, 10, 15, 20).forEach { mins ->
                            FilterChip(
                                selected = (snoozeMinutes == mins),
                                onClick = { snoozeMinutes = mins },
                                label = { Text("$mins m") }
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Action Buttons
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                OutlinedButton(
                    onClick = onBack,
                    modifier = Modifier.weight(1f).height(56.dp),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Text("Cancel")
                }

                Button(
                    onClick = {
                        val newAlarm = AlarmEntity(
                            id = initialAlarm?.id ?: 0L,
                            timeHour = hour,
                            timeMinute = minute,
                            label = label.ifBlank { "Bible Verse Alarm" },
                            isEnabled = isEnabled,
                            repeatDaysBitmask = repeatDaysBitmask,
                            soundMode = soundMode,
                            verseSelectionType = verseSelectionType,
                            selectedVerseId = selectedVerseId,
                            selectedCategory = selectedCategory,
                            selectedBook = selectedBook,
                            snoozeDurationMinutes = snoozeMinutes,
                            ttsSpeechRate = speechRate,
                            ttsPitch = pitch
                        )
                        onSave(newAlarm)
                    },
                    modifier = Modifier
                        .weight(1f)
                        .height(56.dp)
                        .testTag("save_alarm_button"),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Text("Save Alarm", fontWeight = FontWeight.Bold)
                }
            }
            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}
