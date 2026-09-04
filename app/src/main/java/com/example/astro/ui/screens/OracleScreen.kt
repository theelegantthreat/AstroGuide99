package com.example.astro.ui.screens

import android.graphics.Bitmap
import androidx.compose.foundation.Image
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
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.CloudDone
import androidx.compose.material.icons.filled.CloudSync
import androidx.compose.material.icons.filled.Explore
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Map
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Send
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.astro.ai.ChatMessage
import com.example.astro.ai.GeminiAstrologyService
import com.example.astro.cloud.FirebaseAuthHelper
import com.example.astro.cloud.FirebaseFirestoreSync
import com.example.astro.model.ChartData
import com.example.astro.model.UserBirthProfile
import kotlinx.coroutines.launch

@Composable
fun OracleScreen(
    profile: UserBirthProfile,
    natalChart: ChartData?
) {
    val coroutineScope = rememberCoroutineScope()
    val geminiService = remember { GeminiAstrologyService() }
    val authHelper = remember { FirebaseAuthHelper() }
    val firestoreSync = remember { FirebaseFirestoreSync() }

    var selectedTab by remember { mutableIntStateOf(0) }
    val tabs = listOf("AI Consultation", "Search Grounding", "Maps Grounding", "Sacred Art", "Cloud Sync")

    // Multi-turn chat state
    val chatMessages = remember {
        mutableStateListOf(
            ChatMessage("model", "Greetings, Seeker ${profile.fullName}. I am the AstroGuide99 Celestial Oracle. Ask me any question regarding your natal planetary placements, transits, or esoteric alignments.")
        )
    }
    var chatInput by remember { mutableStateOf("") }
    var isChatLoading by remember { mutableStateOf(false) }

    // Search Grounding state
    var searchQuery by remember { mutableStateOf("2026 Solar and Lunar Eclipses astrology Swiss Ephemeris") }
    var searchResultText by remember { mutableStateOf<String?>(null) }
    var isSearchLoading by remember { mutableStateOf(false) }

    // Maps Grounding state
    var mapsQuery by remember { mutableStateOf("Ancient sacred astronomical observatories and megaliths") }
    var mapsResultText by remember { mutableStateOf<String?>(null) }
    var isMapsLoading by remember { mutableStateOf(false) }

    // Image generation state
    var imagePrompt by remember { mutableStateOf("Talisman mandala of the Sun in Gold with celestial violet lotus") }
    var generatedImage by remember { mutableStateOf<Bitmap?>(null) }
    var isImageLoading by remember { mutableStateOf(false) }

    // Cloud sync state
    var cloudStatusMessage by remember { mutableStateOf("Ready to synchronize") }
    var isSyncing by remember { mutableStateOf(false) }

    Column(modifier = Modifier.fillMaxSize()) {
        TabRow(
            selectedTabIndex = selectedTab,
            containerColor = MaterialTheme.colorScheme.background,
            contentColor = MaterialTheme.colorScheme.primary
        ) {
            tabs.forEachIndexed { index, title ->
                Tab(
                    selected = selectedTab == index,
                    onClick = { selectedTab = index },
                    text = {
                        Text(
                            text = title,
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = if (selectedTab == index) FontWeight.Bold else FontWeight.Normal,
                            color = if (selectedTab == index) MaterialTheme.colorScheme.tertiary else MaterialTheme.colorScheme.outline
                        )
                    }
                )
            }
        }

        when (selectedTab) {
            // TAB 0: Multi-Turn Chatbot
            0 -> {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp),
                    verticalArrangement = Arrangement.SpaceBetween
                ) {
                    LazyColumn(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        items(chatMessages) { msg ->
                            val isUser = msg.role == "user"
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = if (isUser) Arrangement.End else Arrangement.Start
                            ) {
                                Card(
                                    shape = RoundedCornerShape(14.dp),
                                    colors = CardDefaults.cardColors(
                                        containerColor = if (isUser) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceVariant
                                    ),
                                    modifier = Modifier.widthIn(max = 300.dp)
                                ) {
                                    Text(
                                        text = msg.content,
                                        modifier = Modifier.padding(12.dp),
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = if (isUser) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                        }
                        if (isChatLoading) {
                            item {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    CircularProgressIndicator(modifier = Modifier.size(20.dp))
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text("Oracle is consulting the celestial spheres...", style = MaterialTheme.typography.bodySmall)
                                }
                            }
                        }
                    }

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        OutlinedTextField(
                            value = chatInput,
                            onValueChange = { chatInput = it },
                            placeholder = { Text("Ask the Oracle...") },
                            modifier = Modifier
                                .weight(1f)
                                .testTag("input_oracle_chat")
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        IconButton(
                            onClick = {
                                if (chatInput.isNotBlank() && !isChatLoading) {
                                    val text = chatInput
                                    chatInput = ""
                                    chatMessages.add(ChatMessage("user", text))
                                    isChatLoading = true
                                    val natalSummary = natalChart?.summaryReading ?: "Sun in Taurus, Moon in Leo, Ascendant in Scorpio"
                                    coroutineScope.launch {
                                        val res = geminiService.chatWithOracle(chatMessages, text, natalSummary)
                                        isChatLoading = false
                                        res.onSuccess { r -> chatMessages.add(ChatMessage("model", r.text)) }
                                            .onFailure { e -> chatMessages.add(ChatMessage("model", "Error: ${e.localizedMessage}")) }
                                    }
                                }
                            },
                            modifier = Modifier.testTag("btn_send_chat")
                        ) {
                            Icon(Icons.Default.Send, contentDescription = "Send", tint = MaterialTheme.colorScheme.primary)
                        }
                    }
                }
            }

            // TAB 1: Search Grounding
            1 -> {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    item {
                        Text("Search-Grounded Ephemeris Intelligence", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                        Text("Utilizes the Google Search tool grounding to anchor interpretations in verified astronomical research and real-time celestial events.", style = MaterialTheme.typography.bodySmall)
                    }
                    item {
                        OutlinedTextField(
                            value = searchQuery,
                            onValueChange = { searchQuery = it },
                            label = { Text("Search Query") },
                            modifier = Modifier.fillMaxWidth().testTag("input_search_grounding")
                        )
                    }
                    item {
                        Button(
                            onClick = {
                                isSearchLoading = true
                                coroutineScope.launch {
                                    val res = geminiService.queryWithSearchGrounding(searchQuery)
                                    isSearchLoading = false
                                    res.onSuccess { searchResultText = it.text }
                                        .onFailure { searchResultText = "Error: ${it.localizedMessage}" }
                                }
                            },
                            modifier = Modifier.fillMaxWidth().testTag("btn_run_search_grounding"),
                            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                        ) {
                            Icon(Icons.Default.Search, contentDescription = null)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Run Search Grounding Query")
                        }
                    }
                    if (isSearchLoading) {
                        item {
                            Box(modifier = Modifier.fillMaxWidth().padding(24.dp), contentAlignment = Alignment.Center) {
                                CircularProgressIndicator()
                            }
                        }
                    }
                    if (searchResultText != null) {
                        item {
                            ElevatedCard(modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(14.dp)) {
                                Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                    Text("Grounded Findings", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                                    Text(searchResultText ?: "", style = MaterialTheme.typography.bodyMedium)
                                }
                            }
                        }
                    }
                }
            }

            // TAB 2: Maps Grounding
            2 -> {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    item {
                        Text("Maps Grounding: Sacred Astronomical Sites", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                        Text("Locate ancient megalithic stone circles, historic observatories, and celestial power spots near any geographic coordinate.", style = MaterialTheme.typography.bodySmall)
                    }
                    item {
                        OutlinedTextField(
                            value = mapsQuery,
                            onValueChange = { mapsQuery = it },
                            label = { Text("Search Location / Region") },
                            modifier = Modifier.fillMaxWidth().testTag("input_maps_grounding")
                        )
                    }
                    item {
                        Button(
                            onClick = {
                                isMapsLoading = true
                                coroutineScope.launch {
                                    val res = geminiService.queryWithMapsGrounding(mapsQuery)
                                    isMapsLoading = false
                                    res.onSuccess { mapsResultText = it.text }
                                        .onFailure { mapsResultText = "Error: ${it.localizedMessage}" }
                                }
                            },
                            modifier = Modifier.fillMaxWidth().testTag("btn_run_maps_grounding"),
                            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondary)
                        ) {
                            Icon(Icons.Default.Map, contentDescription = null)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Find Sacred Sites with Google Maps")
                        }
                    }
                    if (isMapsLoading) {
                        item {
                            Box(modifier = Modifier.fillMaxWidth().padding(24.dp), contentAlignment = Alignment.Center) {
                                CircularProgressIndicator()
                            }
                        }
                    }
                    if (mapsResultText != null) {
                        item {
                            ElevatedCard(modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(14.dp)) {
                                Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                    Text("Sacred Coordinates", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                                    Text(mapsResultText ?: "", style = MaterialTheme.typography.bodyMedium)
                                }
                            }
                        }
                    }
                }
            }

            // TAB 3: Sacred Art / Talisman Generation
            3 -> {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    item {
                        Text("Astrological Sacred Art Synthesis", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                        Text("Generate personalized celestial mandalas, natal planetary talismans, and esoteric sacred geometry via Gemini generative imaging.", style = MaterialTheme.typography.bodySmall)
                    }
                    item {
                        OutlinedTextField(
                            value = imagePrompt,
                            onValueChange = { imagePrompt = it },
                            label = { Text("Art Prompt Description") },
                            modifier = Modifier.fillMaxWidth().testTag("input_art_prompt")
                        )
                    }
                    item {
                        Button(
                            onClick = {
                                isImageLoading = true
                                coroutineScope.launch {
                                    val res = geminiService.generateCelestialArt(imagePrompt)
                                    isImageLoading = false
                                    res.onSuccess { generatedImage = it }
                                        .onFailure { cloudStatusMessage = "Image generation failed: ${it.localizedMessage}" }
                                }
                            },
                            modifier = Modifier.fillMaxWidth().testTag("btn_generate_art"),
                            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.tertiary)
                        ) {
                            Icon(Icons.Default.Image, contentDescription = null)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Generate Sacred Celestial Art")
                        }
                    }
                    if (isImageLoading) {
                        item {
                            Box(modifier = Modifier.fillMaxWidth().padding(24.dp), contentAlignment = Alignment.Center) {
                                CircularProgressIndicator()
                            }
                        }
                    }
                    if (generatedImage != null) {
                        item {
                            Card(modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(16.dp)) {
                                Image(
                                    bitmap = generatedImage!!.asImageBitmap(),
                                    contentDescription = "Generated Astrological Art",
                                    modifier = Modifier.fillMaxWidth().height(300.dp)
                                )
                            }
                        }
                    }
                }
            }

            // TAB 4: Cloud Sync & Firebase
            4 -> {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    item {
                        Text("Firebase Cloud Synchronization", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                        Text("Securely backup and synchronize your natal profiles and saved Swiss Ephemeris charts to Firebase Firestore.", style = MaterialTheme.typography.bodySmall)
                    }
                    item {
                        ElevatedCard(modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(14.dp)) {
                            Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                                val user = authHelper.currentUser
                                Text("Auth Status: ${if (user != null) "Signed in as ${user.uid.take(8)}..." else "Offline / Local Mode"}", fontWeight = FontWeight.SemiBold)
                                Text("Cloud Engine: Google Cloud Firestore", style = MaterialTheme.typography.bodySmall)
                                Text("Status: $cloudStatusMessage", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.secondary)

                                Spacer(modifier = Modifier.height(6.dp))
                                Button(
                                    onClick = {
                                        isSyncing = true
                                        cloudStatusMessage = "Connecting to Firebase..."
                                        coroutineScope.launch {
                                            if (authHelper.currentUser == null) {
                                                authHelper.signInAnonymously()
                                            }
                                            val profileSynced = firestoreSync.syncProfileToCloud(profile)
                                            val chartSynced = if (natalChart != null) firestoreSync.saveChartToCloud(natalChart) else true
                                            isSyncing = false
                                            cloudStatusMessage = if (profileSynced && chartSynced) "Successfully synced to Firestore cloud!" else "Sync completed with offline cache preservation."
                                        }
                                    },
                                    modifier = Modifier.fillMaxWidth().testTag("btn_sync_firestore"),
                                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                                ) {
                                    Icon(Icons.Default.CloudSync, contentDescription = null)
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text("Sync Profile & Charts to Cloud")
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
