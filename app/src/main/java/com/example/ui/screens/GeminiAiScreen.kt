package com.example.ui.screens

import android.Manifest
import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Surface
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.automirrored.filled.VolumeUp
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.MicOff
import androidx.compose.material.icons.filled.OpenInNew
import androidx.compose.material.icons.filled.Psychology
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.PrimaryTabRow
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import com.example.ui.viewmodel.GeminiViewModel
import com.example.ui.viewmodel.UiState

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GeminiAiScreen(
    viewModel: GeminiViewModel
) {
    val context = LocalContext.current
    val uriHandler = LocalUriHandler.current

    var selectedTabIndex by remember { mutableIntStateOf(0) }
    val tabs = listOf("Voice Companion", "Prayer AI", "Search Grounding", "Exegesis Pro")

    val selectedLanguage by viewModel.selectedLanguage.collectAsState()
    val languages = listOf("Cebuano (Bisaya)", "Filipino (Tagalog)", "English")

    Scaffold(
        containerColor = Color.Transparent,
        topBar = {
            TopAppBar(
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.AutoAwesome,
                            contentDescription = "Gemini",
                            tint = Color(0xFFFFD700),
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Gemini Spiritual AI", fontWeight = FontWeight.Bold)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Transparent)
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            // Language Selection Chips
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp, vertical = 6.dp),
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("Lenggwahe:", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    items(languages) { lang ->
                        val isSelected = selectedLanguage == lang
                        Button(
                            onClick = { viewModel.setLanguage(lang) },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant
                            ),
                            shape = RoundedCornerShape(16.dp),
                            modifier = Modifier
                                .height(32.dp)
                                .testTag("lang_chip_$lang")
                        ) {
                            Text(
                                lang,
                                fontSize = 11.sp,
                                color = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }

            // Navigation Tabs
            PrimaryTabRow(
                selectedTabIndex = selectedTabIndex,
                containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.85f),
                contentColor = MaterialTheme.colorScheme.primary
            ) {
                tabs.forEachIndexed { index, title ->
                    Tab(
                        selected = selectedTabIndex == index,
                        onClick = { selectedTabIndex = index },
                        text = {
                            Text(
                                title,
                                fontSize = 12.sp,
                                fontWeight = if (selectedTabIndex == index) FontWeight.Bold else FontWeight.Normal
                            )
                        },
                        modifier = Modifier.testTag("gemini_tab_$index")
                    )
                }
            }

            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp)
            ) {
                when (selectedTabIndex) {
                    0 -> VoiceCompanionTab(viewModel)
                    1 -> PrayerAssistantTab(viewModel)
                    2 -> SearchGroundedTab(viewModel)
                    3 -> ExegesisProTab(viewModel)
                }
            }
        }
    }
}

// 1. Voice Conversation Companion (gemini-3.1-flash-live-preview)
@Composable
fun VoiceCompanionTab(viewModel: GeminiViewModel) {
    val context = LocalContext.current
    val voiceMessages by viewModel.voiceMessages.collectAsState()
    val isListening by viewModel.isListening.collectAsState()
    val isSpeaking by viewModel.isSpeaking.collectAsState()
    val statusText by viewModel.voiceStatusText.collectAsState()

    var manualText by remember { mutableStateOf("") }

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            viewModel.startListening()
        }
    }

    Column(modifier = Modifier.fillMaxSize()) {
        Card(
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.85f)),
            shape = RoundedCornerShape(16.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier.padding(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.AutoAwesome,
                    contentDescription = null,
                    tint = Color(0xFF9C27B0),
                    modifier = Modifier.size(32.dp)
                )
                Spacer(modifier = Modifier.width(12.dp))
                Column {
                    Text("Voice Conversation Companion", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)
                    Text("Powered by gemini-3.5-flash", fontSize = 11.sp, color = Color.Gray)
                }
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Conversation history
        LazyColumn(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            if (voiceMessages.isEmpty()) {
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(24.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            "Tap the mic button below to talk with Gemini, or type your question for voice guidance.",
                            textAlign = TextAlign.Center,
                            color = Color.Gray,
                            fontSize = 14.sp
                        )
                    }
                }
            }

            items(voiceMessages) { msg ->
                val isUser = msg.sender == "User"
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = if (isUser) Arrangement.End else Arrangement.Start
                ) {
                    Surface(
                        color = if (isUser) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.secondaryContainer,
                        shape = RoundedCornerShape(16.dp),
                        modifier = Modifier.fillMaxWidth(0.85f)
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    msg.sender,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 12.sp,
                                    color = if (isUser) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSecondaryContainer
                                )
                                if (!isUser) {
                                    Spacer(modifier = Modifier.weight(1f))
                                    IconButton(
                                        onClick = { viewModel.speakText(msg.text) },
                                        modifier = Modifier.size(24.dp)
                                    ) {
                                        Icon(
                                            imageVector = Icons.AutoMirrored.Filled.VolumeUp,
                                            contentDescription = "Read aloud",
                                            modifier = Modifier.size(16.dp)
                                        )
                                    }
                                }
                            }
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(msg.text, fontSize = 14.sp)
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Voice Controls & Mic Button
        Card(
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.9f)),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier.padding(12.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(statusText, fontSize = 12.sp, color = MaterialTheme.colorScheme.primary, textAlign = TextAlign.Center)

                Spacer(modifier = Modifier.height(8.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    OutlinedTextField(
                        value = manualText,
                        onValueChange = { manualText = it },
                        placeholder = { Text("Ask Gemini anything...") },
                        modifier = Modifier
                            .weight(1f)
                            .testTag("voice_text_input"),
                        shape = RoundedCornerShape(24.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            unfocusedBorderColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.5f)
                        )
                    )

                    Spacer(modifier = Modifier.width(8.dp))

                    if (manualText.isNotBlank()) {
                        IconButton(
                            onClick = {
                                viewModel.sendVoicePrompt(manualText)
                                manualText = ""
                            },
                            modifier = Modifier
                                .background(MaterialTheme.colorScheme.primary, CircleShape)
                                .testTag("send_voice_text_button")
                        ) {
                            Icon(imageVector = Icons.AutoMirrored.Filled.Send, contentDescription = "Send", tint = Color.White)
                        }
                    } else {
                        // Mic Button
                        IconButton(
                            onClick = {
                                if (isListening) {
                                    viewModel.stopListening()
                                } else {
                                    val hasPermission = ContextCompat.checkSelfPermission(
                                        context,
                                        Manifest.permission.RECORD_AUDIO
                                    ) == PackageManager.PERMISSION_GRANTED

                                    if (hasPermission) {
                                        viewModel.startListening()
                                    } else {
                                        permissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
                                    }
                                }
                            },
                            modifier = Modifier
                                .size(48.dp)
                                .background(
                                    if (isListening) Color.Red else MaterialTheme.colorScheme.primary,
                                    CircleShape
                                )
                                .testTag("mic_toggle_button")
                        ) {
                            Icon(
                                imageVector = if (isListening) Icons.Default.MicOff else Icons.Default.Mic,
                                contentDescription = "Mic",
                                tint = Color.White
                            )
                        }
                    }

                    if (isSpeaking) {
                        Spacer(modifier = Modifier.width(8.dp))
                        IconButton(
                            onClick = { viewModel.stopSpeaking() },
                            modifier = Modifier
                                .background(Color.Red, CircleShape)
                        ) {
                            Icon(imageVector = Icons.Default.Stop, contentDescription = "Stop TTS", tint = Color.White)
                        }
                    }
                }
            }
        }
    }
}

// 2. Prayer & Situation Assistant (gemini-3.5-flash)
@Composable
fun PrayerAssistantTab(viewModel: GeminiViewModel) {
    var situation by remember { mutableStateOf("") }
    val prayerState by viewModel.prayerState.collectAsState()

    val selectedLanguage by viewModel.selectedLanguage.collectAsState()

    val quickSituations = if (selectedLanguage.contains("Cebuano")) {
        listOf("Kalinaw sa Kalag", "Kusog sa Pagsulay", "Pangamuyo sa Pamilya", "Pasalamat sa Buntag", "Giya sa Pagtuon")
    } else if (selectedLanguage.contains("Filipino")) {
        listOf("Kapayapaan sa Puso", "Lakas sa Pagsubok", "Panalangin sa Pamilya", "Pasasalamat sa Umaga", "Paggabay sa Pag-aaral")
    } else {
        listOf("Peace in Anxiety", "Strength for Exams", "Comfort in Grief", "Morning Thanksgiving", "Family Healing")
    }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.85f)),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("AI Prayer & Scripture Finder", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)
                    Text("Powered by gemini-3.5-flash", fontSize = 11.sp, color = Color.Gray)
                    Spacer(modifier = Modifier.height(8.dp))
                    Text("Enter what you're feeling or facing today, and Gemini will generate a custom prayer with relevant scripture.", fontSize = 13.sp)
                }
            }
        }

        item {
            Text("Quick Suggestions:", fontWeight = FontWeight.Bold, fontSize = 13.sp)
            Spacer(modifier = Modifier.height(4.dp))
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                items(quickSituations) { item ->
                    Button(
                        onClick = {
                            situation = item
                            viewModel.generatePrayerAndScripture(item)
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondaryContainer),
                        shape = RoundedCornerShape(20.dp)
                    ) {
                        Text(item, color = MaterialTheme.colorScheme.onSecondaryContainer, fontSize = 12.sp)
                    }
                }
            }
        }

        item {
            OutlinedTextField(
                value = situation,
                onValueChange = { situation = it },
                label = { Text("What is on your heart today?") },
                placeholder = { Text("e.g. Asking for direction in my career...") },
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("prayer_situation_input"),
                shape = RoundedCornerShape(12.dp)
            )

            Spacer(modifier = Modifier.height(8.dp))

            Button(
                onClick = { viewModel.generatePrayerAndScripture(situation) },
                enabled = situation.isNotBlank() && prayerState !is UiState.Loading,
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("generate_prayer_button"),
                shape = RoundedCornerShape(12.dp)
            ) {
                Icon(imageVector = Icons.Default.AutoAwesome, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Generate Prayer & Scriptures")
            }
        }

        item {
            when (val state = prayerState) {
                is UiState.Loading -> {
                    Box(modifier = Modifier.fillMaxWidth().padding(24.dp), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator()
                    }
                }
                is UiState.Error -> {
                    Text("Error: ${state.message}", color = Color.Red, fontSize = 13.sp)
                }
                is UiState.Success -> {
                    Card(
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.9f)),
                        shape = RoundedCornerShape(16.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text("Your Custom Prayer:", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onPrimaryContainer)
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(state.data.first, fontSize = 14.sp)

                            Spacer(modifier = Modifier.height(12.dp))
                            Text("Recommended Scripture:", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onPrimaryContainer)
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(state.data.second, fontSize = 14.sp)
                        }
                    }
                }
                UiState.Idle -> {}
            }
        }
    }
}

// 3. Search Grounding Tab (gemini-3.5-flash with Google Search)
@Composable
fun SearchGroundedTab(viewModel: GeminiViewModel) {
    val uriHandler = LocalUriHandler.current
    var topic by remember { mutableStateOf("Hope and faith in modern world") }
    val groundedState by viewModel.groundedState.collectAsState()

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.85f)),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(imageVector = Icons.Default.Search, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Google Search Grounded Devotionals", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)
                    }
                    Text("Powered by gemini-3.5-flash (with googleSearch tool)", fontSize = 11.sp, color = Color.Gray)
                    Spacer(modifier = Modifier.height(8.dp))
                    Text("Retrieves real-time current news and connects today's events with scripture.", fontSize = 13.sp)
                }
            }
        }

        item {
            OutlinedTextField(
                value = topic,
                onValueChange = { topic = it },
                label = { Text("Devotional Topic / Current Event") },
                modifier = Modifier.fillMaxWidth().testTag("grounding_topic_input"),
                shape = RoundedCornerShape(12.dp)
            )

            Spacer(modifier = Modifier.height(8.dp))

            Button(
                onClick = { viewModel.generateGroundedDevotional(topic) },
                enabled = topic.isNotBlank() && groundedState !is UiState.Loading,
                modifier = Modifier.fillMaxWidth().testTag("generate_grounded_button"),
                shape = RoundedCornerShape(12.dp)
            ) {
                Icon(imageVector = Icons.Default.Search, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Search & Connect Scripture")
            }
        }

        item {
            when (val state = groundedState) {
                is UiState.Loading -> {
                    Box(modifier = Modifier.fillMaxWidth().padding(24.dp), contentAlignment = Alignment.Center) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            CircularProgressIndicator()
                            Spacer(modifier = Modifier.height(8.dp))
                            Text("Searching Google for news & connecting scripture...", fontSize = 12.sp)
                        }
                    }
                }
                is UiState.Error -> {
                    Text("Error: ${state.message}", color = Color.Red, fontSize = 13.sp)
                }
                is UiState.Success -> {
                    Card(
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.9f)),
                        shape = RoundedCornerShape(16.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text("Search-Grounded Devotional", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(state.data.devotionalText, fontSize = 14.sp)

                            if (state.data.searchSources.isNotEmpty()) {
                                Spacer(modifier = Modifier.height(16.dp))
                                Text("Web Sources & News Articles:", fontWeight = FontWeight.Bold, fontSize = 12.sp, color = MaterialTheme.colorScheme.primary)
                                Spacer(modifier = Modifier.height(4.dp))
                                state.data.searchSources.forEach { source ->
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .clickable { uriHandler.openUri(source.url) }
                                            .padding(vertical = 4.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Icon(imageVector = Icons.Default.OpenInNew, contentDescription = null, modifier = Modifier.size(14.dp), tint = MaterialTheme.colorScheme.primary)
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text(source.title, fontSize = 12.sp, color = MaterialTheme.colorScheme.primary)
                                    }
                                }
                            }
                        }
                    }
                }
                UiState.Idle -> {}
            }
        }
    }
}

// 4. In-Depth Exegesis Pro (gemini-3.1-pro-preview)
@Composable
fun ExegesisProTab(viewModel: GeminiViewModel) {
    var verseText by remember { mutableStateOf("Trust in the LORD with all thine heart; and lean not unto thine own understanding.") }
    var reference by remember { mutableStateOf("Proverbs 3:5") }

    val reflectionState by viewModel.deepReflectionState.collectAsState()

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.85f)),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(imageVector = Icons.Default.Psychology, contentDescription = null, tint = Color(0xFFFF9800))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Complex Theological Exegesis", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)
                    }
                    Text("Powered by gemini-3.1-pro-preview", fontSize = 11.sp, color = Color.Gray)
                    Spacer(modifier = Modifier.height(8.dp))
                    Text("Deep scholarly reasoning, historical background, and study questions.", fontSize = 13.sp)
                }
            }
        }

        item {
            OutlinedTextField(
                value = reference,
                onValueChange = { reference = it },
                label = { Text("Verse Reference") },
                modifier = Modifier.fillMaxWidth().testTag("exegesis_ref_input"),
                shape = RoundedCornerShape(12.dp)
            )

            Spacer(modifier = Modifier.height(8.dp))

            OutlinedTextField(
                value = verseText,
                onValueChange = { verseText = it },
                label = { Text("Verse Text") },
                modifier = Modifier.fillMaxWidth().testTag("exegesis_text_input"),
                shape = RoundedCornerShape(12.dp)
            )

            Spacer(modifier = Modifier.height(8.dp))

            Button(
                onClick = { viewModel.generateDeepReflection(verseText, reference) },
                enabled = verseText.isNotBlank() && reflectionState !is UiState.Loading,
                modifier = Modifier.fillMaxWidth().testTag("analyze_verse_pro_button"),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF673AB7))
            ) {
                Icon(imageVector = Icons.Default.AutoAwesome, contentDescription = null, tint = Color.White)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Analyze with Gemini Pro", color = Color.White)
            }
        }

        item {
            when (val state = reflectionState) {
                is UiState.Loading -> {
                    Box(modifier = Modifier.fillMaxWidth().padding(24.dp), contentAlignment = Alignment.Center) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            CircularProgressIndicator(color = Color(0xFF673AB7))
                            Spacer(modifier = Modifier.height(8.dp))
                            Text("Gemini 3.1 Pro analyzing verse context & exegesis...", fontSize = 12.sp)
                        }
                    }
                }
                is UiState.Error -> {
                    Text("Error: ${state.message}", color = Color.Red, fontSize = 13.sp)
                }
                is UiState.Success -> {
                    Card(
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.95f)),
                        shape = RoundedCornerShape(16.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text("Deep Exegesis & Reflection", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium, color = Color(0xFF673AB7))
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(state.data, fontSize = 14.sp)
                        }
                    }
                }
                UiState.Idle -> {}
            }
        }
    }
}
