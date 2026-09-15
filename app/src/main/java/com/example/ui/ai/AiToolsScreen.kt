package com.example.ui.ai

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.widget.Toast
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Forum
import androidx.compose.material.icons.filled.NotificationImportant
import androidx.compose.material.icons.filled.RecordVoiceOver
import androidx.compose.material.icons.filled.Summarize
import androidx.compose.material.icons.filled.Translate
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.TabRowDefaults.SecondaryIndicator
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.material3.Text
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.AiCallSummary
import com.example.data.DemoRepository
import com.example.ui.theme.AccentEmerald
import com.example.ui.theme.BrandCyan
import com.example.ui.theme.BrandCyanLight
import com.example.ui.theme.BrandIndigo
import com.example.ui.theme.BrandViolet
import com.example.ui.theme.DarkBackground
import com.example.ui.theme.DarkBorder
import com.example.ui.theme.DarkSurface
import com.example.ui.theme.DarkSurfaceElevated
import com.example.ui.theme.DarkSurfaceVariant

@Composable
fun AiToolsScreen(
    repository: DemoRepository,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val contacts by repository.contacts.collectAsState()
    val allMessages by repository.messages.collectAsState()
    val callSummaries by repository.aiSummaries.collectAsState()

    var selectedTab by remember { mutableIntStateOf(0) }
    val tabTitles = listOf("Summaries", "Rewrite & Polish", "Translate", "Call Intelligence")

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(DarkBackground)
    ) {
        // AI Hero Banner
        Surface(
            color = DarkSurface,
            shadowElevation = 4.dp,
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(38.dp)
                            .clip(CircleShape)
                            .background(
                                Brush.linearGradient(listOf(BrandCyan, BrandIndigo))
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.AutoAwesome,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(
                            text = "TalkText AI Engine",
                            color = Color.White,
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "Real-time speech analysis & conversation intelligence",
                            color = Color(0xFF94A3B8),
                            fontSize = 12.sp
                        )
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                TabRow(
                    selectedTabIndex = selectedTab,
                    containerColor = DarkSurfaceElevated,
                    contentColor = BrandCyanLight,
                    indicator = { tabPositions ->
                        SecondaryIndicator(
                            Modifier.tabIndicatorOffset(tabPositions[selectedTab]),
                            color = BrandCyan
                        )
                    },
                    modifier = Modifier.clip(RoundedCornerShape(12.dp))
                ) {
                    tabTitles.forEachIndexed { index, title ->
                        Tab(
                            selected = selectedTab == index,
                            onClick = { selectedTab = index },
                            text = {
                                Text(
                                    text = title,
                                    fontSize = 12.sp,
                                    fontWeight = if (selectedTab == index) FontWeight.Bold else FontWeight.Normal,
                                    color = if (selectedTab == index) BrandCyanLight else Color(0xFF94A3B8)
                                )
                            }
                        )
                    }
                }
            }
        }

        // Tab Content
        Box(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            when (selectedTab) {
                0 -> ChatSummarizerTab(repository, contacts, allMessages)
                1 -> MessageRewriterTab(repository)
                2 -> MessageTranslatorTab(repository)
                3 -> CallIntelligenceTab(callSummaries)
            }
        }
    }
}

@Composable
private fun ChatSummarizerTab(
    repository: DemoRepository,
    contacts: List<com.example.data.Contact>,
    allMessages: Map<String, List<com.example.data.Message>>
) {
    var selectedContactId by remember { mutableStateOf(contacts.firstOrNull()?.id ?: "") }
    var summaryResult by remember { mutableStateOf("") }
    val context = LocalContext.current

    val currentContact = contacts.find { it.id == selectedContactId } ?: contacts.firstOrNull()
    val messages = allMessages[selectedContactId] ?: emptyList()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        Text(
            text = "Summarize Conversation",
            color = Color.White,
            fontSize = 16.sp,
            fontWeight = FontWeight.Bold
        )

        Text(
            text = "Select a contact to generate key points, decisions, and action items:",
            color = Color(0xFF94A3B8),
            fontSize = 13.sp
        )

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            contacts.take(3).forEach { c ->
                FilterChip(
                    selected = c.id == selectedContactId,
                    onClick = {
                        selectedContactId = c.id
                        summaryResult = ""
                    },
                    label = { Text("${c.avatarEmoji} ${c.name.split(" ").first()}") },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = BrandCyan.copy(alpha = 0.25f),
                        selectedLabelColor = BrandCyanLight,
                        containerColor = DarkSurfaceElevated,
                        labelColor = Color.White
                    )
                )
            }
        }

        Button(
            onClick = {
                currentContact?.let {
                    summaryResult = repository.summarizeChat(it.name, messages)
                }
            },
            colors = ButtonDefaults.buttonColors(containerColor = BrandCyan),
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier
                .fillMaxWidth()
                .testTag("ai_summarize_button")
        ) {
            Icon(Icons.Default.Summarize, contentDescription = null, tint = Color(0xFF00273F))
            Spacer(modifier = Modifier.width(8.dp))
            Text("Generate Summary (${messages.size} Messages)", color = Color(0xFF00273F), fontWeight = FontWeight.Bold)
        }

        if (summaryResult.isNotBlank()) {
            Card(
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = DarkSurfaceElevated),
                border = androidx.compose.foundation.BorderStroke(1.dp, BrandCyan.copy(alpha = 0.4f)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Generated AI Summary",
                            color = BrandCyanLight,
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp
                        )
                        IconButton(onClick = {
                            val cb = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                            cb.setPrimaryClip(ClipData.newPlainText("AI Summary", summaryResult))
                            Toast.makeText(context, "Copied summary!", Toast.LENGTH_SHORT).show()
                        }) {
                            Icon(Icons.Default.ContentCopy, contentDescription = "Copy", tint = Color.White)
                        }
                    }
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = summaryResult,
                        color = Color.White,
                        fontSize = 13.sp,
                        lineHeight = 19.sp
                    )
                }
            }
        }
    }
}

@Composable
private fun MessageRewriterTab(repository: DemoRepository) {
    var inputMessage by remember { mutableStateOf("Hey can we talk about the project demo soon?") }
    var selectedTone by remember { mutableStateOf("Executive") }
    var rewrittenResult by remember { mutableStateOf("") }
    val tones = listOf("Executive", "Polite", "Casual", "Concise")
    val context = LocalContext.current

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        Text(
            text = "AI Message Rewriter & Tone Polisher",
            color = Color.White,
            fontSize = 16.sp,
            fontWeight = FontWeight.Bold
        )

        OutlinedTextField(
            value = inputMessage,
            onValueChange = { inputMessage = it },
            label = { Text("Draft Message") },
            colors = OutlinedTextFieldDefaults.colors(
                focusedTextColor = Color.White,
                unfocusedTextColor = Color.White,
                focusedBorderColor = BrandCyan,
                unfocusedBorderColor = DarkBorder,
                focusedContainerColor = DarkSurfaceElevated,
                unfocusedContainerColor = DarkSurfaceElevated
            ),
            modifier = Modifier.fillMaxWidth()
        )

        Text(text = "Select Tone:", color = Color(0xFF94A3B8), fontSize = 13.sp)

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            tones.forEach { tone ->
                FilterChip(
                    selected = selectedTone == tone,
                    onClick = { selectedTone = tone },
                    label = { Text(tone) },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = BrandViolet.copy(alpha = 0.3f),
                        selectedLabelColor = Color.White,
                        containerColor = DarkSurfaceElevated,
                        labelColor = Color(0xFF94A3B8)
                    )
                )
            }
        }

        Button(
            onClick = {
                rewrittenResult = repository.rewriteMessage(inputMessage, selectedTone)
            },
            colors = ButtonDefaults.buttonColors(containerColor = BrandViolet),
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Icon(Icons.Default.AutoAwesome, contentDescription = null, tint = Color.White)
            Spacer(modifier = Modifier.width(8.dp))
            Text("Rewrite in $selectedTone Tone", color = Color.White, fontWeight = FontWeight.Bold)
        }

        if (rewrittenResult.isNotBlank()) {
            Card(
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = DarkSurfaceElevated),
                border = androidx.compose.foundation.BorderStroke(1.dp, BrandViolet.copy(alpha = 0.4f)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Text(
                        text = "Polished Draft:",
                        color = Color(0xFFA78BFA),
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = rewrittenResult,
                        color = Color.White,
                        fontSize = 14.sp
                    )
                }
            }
        }
    }
}

@Composable
private fun MessageTranslatorTab(repository: DemoRepository) {
    var textToTranslate by remember {
        mutableStateOf("Hello, are you ready for our video call with live captions?")
    }
    var targetLanguage by remember { mutableStateOf("Tamil") }
    var translationOutput by remember { mutableStateOf("") }
    val languages = listOf("Tamil", "Spanish", "Hindi")

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        Text(
            text = "Bilingual Live Message Translator",
            color = Color.White,
            fontSize = 16.sp,
            fontWeight = FontWeight.Bold
        )

        OutlinedTextField(
            value = textToTranslate,
            onValueChange = { textToTranslate = it },
            label = { Text("Original Text (English)") },
            colors = OutlinedTextFieldDefaults.colors(
                focusedTextColor = Color.White,
                unfocusedTextColor = Color.White,
                focusedBorderColor = BrandCyan,
                unfocusedBorderColor = DarkBorder,
                focusedContainerColor = DarkSurfaceElevated,
                unfocusedContainerColor = DarkSurfaceElevated
            ),
            modifier = Modifier.fillMaxWidth()
        )

        Text(text = "Target Language:", color = Color(0xFF94A3B8), fontSize = 13.sp)

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            languages.forEach { lang ->
                FilterChip(
                    selected = targetLanguage == lang,
                    onClick = { targetLanguage = lang },
                    label = { Text(if (lang == "Tamil") "Tamil (தமிழ்)" else lang) },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = BrandCyan.copy(alpha = 0.25f),
                        selectedLabelColor = BrandCyanLight,
                        containerColor = DarkSurfaceElevated,
                        labelColor = Color(0xFF94A3B8)
                    )
                )
            }
        }

        Button(
            onClick = {
                translationOutput = repository.translateMessage(textToTranslate, targetLanguage)
            },
            colors = ButtonDefaults.buttonColors(containerColor = BrandCyan),
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Icon(Icons.Default.Translate, contentDescription = null, tint = Color(0xFF00273F))
            Spacer(modifier = Modifier.width(8.dp))
            Text("Translate into $targetLanguage", color = Color(0xFF00273F), fontWeight = FontWeight.Bold)
        }

        if (translationOutput.isNotBlank()) {
            Card(
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = DarkSurfaceElevated),
                border = androidx.compose.foundation.BorderStroke(1.dp, BrandCyan.copy(alpha = 0.4f)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Text(
                        text = "Translation Output ($targetLanguage):",
                        color = BrandCyanLight,
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = translationOutput,
                        color = Color.White,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Medium
                    )
                }
            }
        }
    }
}

@Composable
private fun CallIntelligenceTab(callSummaries: List<AiCallSummary>) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        Text(
            text = "Video Call Intelligence & Transcripts",
            color = Color.White,
            fontSize = 16.sp,
            fontWeight = FontWeight.Bold
        )

        Text(
            text = "Summaries generated automatically from live speech captions during your video calls:",
            color = Color(0xFF94A3B8),
            fontSize = 13.sp
        )

        if (callSummaries.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(140.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "No call summaries yet.\nFinish a video call to view automatic meeting intelligence!",
                    color = Color(0xFF94A3B8),
                    fontSize = 13.sp,
                    textAlign = androidx.compose.ui.text.style.TextAlign.Center
                )
            }
        } else {
            callSummaries.forEach { summary ->
                Card(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = DarkSurfaceElevated),
                    border = androidx.compose.foundation.BorderStroke(1.dp, DarkBorder),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = summary.callTitle,
                                color = Color.White,
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = summary.duration,
                                color = BrandCyanLight,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                        }

                        Text(
                            text = "With ${summary.participant} • ${summary.timestamp}",
                            color = Color(0xFF94A3B8),
                            fontSize = 11.sp
                        )

                        HorizontalDivider(
                            modifier = Modifier.padding(vertical = 10.dp),
                            color = DarkBorder
                        )

                        Text(
                            text = summary.overview,
                            color = Color(0xFFE2E8F0),
                            fontSize = 13.sp,
                            lineHeight = 18.sp
                        )

                        Spacer(modifier = Modifier.height(10.dp))
                        Text(
                            text = "Key Takeaways:",
                            color = BrandCyanLight,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold
                        )
                        summary.keyPoints.forEach { pt ->
                            Row(modifier = Modifier.padding(top = 4.dp)) {
                                Text(text = "• ", color = BrandCyanLight)
                                Text(text = pt, color = Color(0xFFCBD5E1), fontSize = 12.sp)
                            }
                        }

                        Spacer(modifier = Modifier.height(10.dp))
                        Text(
                            text = "Action Items:",
                            color = AccentEmerald,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold
                        )
                        summary.actionItems.forEach { item ->
                            Row(modifier = Modifier.padding(top = 4.dp)) {
                                Text(text = "✅ ", fontSize = 12.sp)
                                Text(text = item, color = Color(0xFFCBD5E1), fontSize = 12.sp)
                            }
                        }
                    }
                }
            }
        }
    }
}
