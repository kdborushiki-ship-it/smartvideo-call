package com.example.ui.call

import android.Manifest
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.pm.PackageManager
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.CallEnd
import androidx.compose.material.icons.filled.Cameraswitch
import androidx.compose.material.icons.filled.Chat
import androidx.compose.material.icons.filled.ClosedCaption
import androidx.compose.material.icons.filled.ClosedCaptionOff
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.filled.Fullscreen
import androidx.compose.material.icons.filled.FullscreenExit
import androidx.compose.material.icons.filled.GraphicEq
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.MicOff
import androidx.compose.material.icons.filled.RecordVoiceOver
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Subtitles
import androidx.compose.material.icons.filled.Translate
import androidx.compose.material.icons.filled.Videocam
import androidx.compose.material.icons.filled.VideocamOff
import androidx.compose.material.icons.filled.VolumeDown
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.FloatingActionButtonDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import com.example.data.AiCallSummary
import com.example.data.CallType
import com.example.data.CaptionLanguage
import com.example.data.Contact
import com.example.data.DemoRepository
import com.example.service.AndroidSpeechManager
import com.example.ui.theme.AccentEmerald
import com.example.ui.theme.AccentRose
import com.example.ui.theme.BrandCyan
import com.example.ui.theme.BrandCyanLight
import com.example.ui.theme.BrandIndigo
import com.example.ui.theme.BrandViolet
import com.example.ui.theme.DarkBackground
import com.example.ui.theme.DarkBorder
import com.example.ui.theme.DarkSurface
import com.example.ui.theme.DarkSurfaceElevated
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.util.UUID

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun VideoCallScreen(
    contact: Contact,
    repository: DemoRepository,
    onEndCall: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val speechManager = remember { AndroidSpeechManager(context) }

    // Call duration timer
    var callSeconds by remember { mutableIntStateOf(0) }
    LaunchedEffect(Unit) {
        while (true) {
            delay(1000)
            callSeconds++
        }
    }

    // Call controls
    val coroutineScope = rememberCoroutineScope()
    val currentUser by repository.currentUser.collectAsState()
    var isCameraOn by remember { mutableStateOf(true) }
    var isMicOn by remember { mutableStateOf(true) }
    var isSpeakerOn by remember { mutableStateOf(true) }
    var isFrontCamera by remember { mutableStateOf(true) }
    var isFullScreen by remember { mutableStateOf(false) }
    var areCaptionsEnabled by remember { mutableStateOf(true) }
    var showTranscriptSheet by remember { mutableStateOf(false) }
    var activeCallTab by remember { mutableIntStateOf(0) } // 0: Live Captions, 1: In-Call Text Chat
    var inCallTextInput by remember { mutableStateOf("") }

    // Settings from repository
    val captionFontSize by repository.captionFontSize.collectAsState()
    val highReadability by repository.highReadabilityCaptions.collectAsState()
    val selectedLanguage by speechManager.selectedLanguage.collectAsState()
    val currentSentence by speechManager.currentSentence.collectAsState()
    val isRemoteSpeaking by speechManager.isRemoteSpeaking.collectAsState()
    val activeSpeakerName by speechManager.activeSpeakerName.collectAsState()
    val soundLevel by speechManager.soundLevelRms.collectAsState()
    val transcriptList by speechManager.captionsTranscript.collectAsState()

    // Permission launcher for Camera & Mic
    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        val micGranted = permissions[Manifest.permission.RECORD_AUDIO] ?: false
        val camGranted = permissions[Manifest.permission.CAMERA] ?: false
        if (micGranted && areCaptionsEnabled) {
            speechManager.startListening(contact.name)
        }
    }

    LaunchedEffect(Unit) {
        val hasMic = ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.RECORD_AUDIO
        ) == PackageManager.PERMISSION_GRANTED
        val hasCam = ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.CAMERA
        ) == PackageManager.PERMISSION_GRANTED

        if (!hasMic || !hasCam) {
            permissionLauncher.launch(
                arrayOf(Manifest.permission.RECORD_AUDIO, Manifest.permission.CAMERA)
            )
        }
        speechManager.startListening(contact.name)
    }

    DisposableEffect(Unit) {
        onDispose {
            speechManager.stopListening()
            // Record call in repository
            repository.addCallRecord(contact, CallType.VIDEO, callSeconds)

            // If transcript contains items, auto-generate AI call summary
            if (transcriptList.isNotEmpty()) {
                val summary = AiCallSummary(
                    id = UUID.randomUUID().toString(),
                    callTitle = "Video Call with ${contact.name}",
                    timestamp = "Just now",
                    duration = "${callSeconds / 60}m ${callSeconds % 60}s",
                    participant = contact.name,
                    overview = "Completed real-time speech-to-text captioned video call with ${contact.name}. Captions were active throughout the session.",
                    keyPoints = listOf(
                        "Live speech captions demonstrated high accuracy with low latency.",
                        "Discussed bilingual accessibility features in English and Tamil.",
                        "Total conversation logged ${transcriptList.size} live transcript items."
                    ),
                    actionItems = listOf(
                        "Export transcript if needed for college expo presentation.",
                        "Review follow-up notes in the AI Tools dashboard."
                    )
                )
                repository.addAiSummary(summary)
            }
        }
    }

    // Pulsing animation for active speaker audio
    val infiniteTransition = rememberInfiniteTransition(label = "audio_pulse")
    val pulseScale by infiniteTransition.animateFloat(
        initialValue = 1.0f,
        targetValue = if (isRemoteSpeaking) 1.15f else 1.02f,
        animationSpec = infiniteRepeatable(
            animation = tween(600, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulse"
    )

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(DarkBackground)
    ) {
        // --- 1. REMOTE CALLER VIDEO AREA ---
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            Color(0xFF0F172A),
                            Color(0xFF0B1120),
                            Color(0xFF060913)
                        )
                    )
                ),
            contentAlignment = Alignment.Center
        ) {
            // Stylized Remote Video Feed: Digital Caller Canvas with ambient glowing aura
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center,
                modifier = Modifier.padding(bottom = 120.dp)
            ) {
                // Animated Glowing Ring around Caller Avatar when speaking
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier
                        .size(150.dp)
                        .scale(if (isRemoteSpeaking) pulseScale else 1f)
                ) {
                    if (isRemoteSpeaking) {
                        Box(
                            modifier = Modifier
                                .size(148.dp)
                                .clip(CircleShape)
                                .background(BrandCyan.copy(alpha = 0.25f))
                                .blur(12.dp)
                        )
                    }
                    Box(
                        modifier = Modifier
                            .size(120.dp)
                            .clip(CircleShape)
                            .border(
                                width = if (isRemoteSpeaking) 3.dp else 2.dp,
                                color = if (isRemoteSpeaking) BrandCyan else DarkBorder,
                                shape = CircleShape
                            )
                            .background(DarkSurfaceElevated),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = contact.avatarEmoji,
                            fontSize = 60.sp
                        )
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                Text(
                    text = contact.name,
                    color = Color.White,
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Bold
                )

                Spacer(modifier = Modifier.height(6.dp))

                // Speaking status indicator
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    if (isRemoteSpeaking) {
                        Icon(
                            imageVector = Icons.Default.GraphicEq,
                            contentDescription = "Speaking",
                            tint = BrandCyan,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "Speaking now...",
                            color = BrandCyan,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                    } else {
                        Box(
                            modifier = Modifier
                                .size(8.dp)
                                .clip(CircleShape)
                                .background(AccentEmerald)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "Connected • HD 1080p • 60fps",
                            color = Color(0xFF94A3B8),
                            fontSize = 12.sp
                        )
                    }
                }
            }
        }

        // --- 2. TOP HEADER BAR ---
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .statusBarsPadding()
                .padding(horizontal = 16.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .clip(RoundedCornerShape(20.dp))
                    .background(Color(0x880F172A))
                    .padding(horizontal = 10.dp, vertical = 6.dp)
            ) {
                IconButton(
                    onClick = onEndCall,
                    modifier = Modifier.size(32.dp)
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Back",
                        tint = Color.White,
                        modifier = Modifier.size(20.dp)
                    )
                }
                Spacer(modifier = Modifier.width(6.dp))
                Column {
                    Text(
                        text = contact.name,
                        color = Color.White,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        text = String.format("%02d:%02d", callSeconds / 60, callSeconds % 60),
                        color = BrandCyanLight,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium
                    )
                }
            }

            // Top action pill: Transcript & Fullscreen
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // Transcript button with count badge
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(18.dp))
                        .background(Color(0x880F172A))
                        .clickable { showTranscriptSheet = true }
                        .padding(horizontal = 12.dp, vertical = 8.dp)
                        .testTag("transcript_button")
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Subtitles,
                            contentDescription = "Transcript",
                            tint = BrandCyan,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "Transcript (${transcriptList.size})",
                            color = Color.White,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }

                // Language quick toggle chip
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(18.dp))
                        .background(if (selectedLanguage == CaptionLanguage.TAMIL) BrandViolet else BrandIndigo)
                        .clickable {
                            val next = if (selectedLanguage == CaptionLanguage.ENGLISH) {
                                CaptionLanguage.TAMIL
                            } else {
                                CaptionLanguage.ENGLISH
                            }
                            speechManager.setLanguage(next)
                            Toast.makeText(context, "Captions: ${next.displayName}", Toast.LENGTH_SHORT).show()
                        }
                        .padding(horizontal = 10.dp, vertical = 8.dp)
                        .testTag("language_toggle")
                ) {
                    Text(
                        text = if (selectedLanguage == CaptionLanguage.TAMIL) "தமிழ்" else "EN",
                        color = Color.White,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }

        // --- 3. PIP LOCAL CAMERA PREVIEW (DRAGGABLE/FLOATING TOP-RIGHT) ---
        if (!isFullScreen) {
            Box(
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .statusBarsPadding()
                    .padding(top = 60.dp, end = 16.dp)
                    .size(width = 110.dp, height = 155.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .border(2.dp, BrandCyan.copy(alpha = 0.6f), RoundedCornerShape(16.dp))
                    .shadow(12.dp, RoundedCornerShape(16.dp))
            ) {
                CameraPreview(
                    isCameraOn = isCameraOn,
                    isFrontCamera = isFrontCamera,
                    modifier = Modifier.fillMaxSize()
                )

                // Lens switch button on PiP
                IconButton(
                    onClick = { isFrontCamera = !isFrontCamera },
                    modifier = Modifier
                        .align(Alignment.BottomEnd)
                        .padding(4.dp)
                        .size(30.dp)
                        .clip(CircleShape)
                        .background(Color(0xAA000000))
                ) {
                    Icon(
                        imageVector = Icons.Default.Cameraswitch,
                        contentDescription = "Switch Camera",
                        tint = Color.White,
                        modifier = Modifier.size(16.dp)
                    )
                }
            }
        }

        // --- 4. ⭐ LIVE SPEECH-TO-TEXT CAPTIONS (MAIN HIGHLIGHT) ---
        AnimatedVisibility(
            visible = areCaptionsEnabled,
            enter = slideInVertically(initialOffsetY = { it / 2 }) + fadeIn(),
            exit = slideOutVertically(targetOffsetY = { it / 2 }) + fadeOut(),
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 96.dp) // Sits strictly above call controls bar
                .padding(horizontal = 14.dp)
                .fillMaxWidth()
        ) {
            Surface(
                shape = RoundedCornerShape(20.dp),
                color = if (highReadability) Color(0xF50B1120) else Color(0xD00F172A),
                border = androidx.compose.foundation.BorderStroke(
                    width = 1.5.dp,
                    brush = Brush.horizontalGradient(
                        listOf(BrandCyan.copy(alpha = 0.8f), BrandIndigo.copy(alpha = 0.8f))
                    )
                ),
                shadowElevation = 16.dp,
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("live_captions_card")
            ) {
                Column(
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp)
                ) {
                    // Caption Header: Badge / Tab Selector & Language Indicator
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            // Captions Mode Tab
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(if (activeCallTab == 0) BrandCyan else Color(0xFF1E293B))
                                    .clickable { activeCallTab = 0 }
                                    .padding(horizontal = 8.dp, vertical = 4.dp)
                            ) {
                                Text(
                                    text = "🎙️ CAPTIONS",
                                    color = if (activeCallTab == 0) Color(0xFF002238) else Color(0xFFCBD5E1),
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.ExtraBold,
                                    letterSpacing = 0.5.sp
                                )
                            }

                            // In-Call Text Mode Tab
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(if (activeCallTab == 1) BrandCyan else Color(0xFF1E293B))
                                    .clickable { activeCallTab = 1 }
                                    .padding(horizontal = 8.dp, vertical = 4.dp)
                            ) {
                                Text(
                                    text = "💬 IN-CALL TEXT",
                                    color = if (activeCallTab == 1) Color(0xFF002238) else Color(0xFFCBD5E1),
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.ExtraBold,
                                    letterSpacing = 0.5.sp
                                )
                            }
                        }

                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = selectedLanguage.displayName,
                                color = Color(0xFF94A3B8),
                                fontSize = 11.sp
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Icon(
                                imageVector = Icons.Default.GraphicEq,
                                contentDescription = "Active speech",
                                tint = if (isRemoteSpeaking) BrandCyan else Color(0xFF64748B),
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    if (activeCallTab == 0) {
                        // --- TAB 0: LIVE SPEECH CAPTIONS ---
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = "Speaker: $activeSpeakerName",
                                color = BrandCyanLight,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                        }

                        // Prominently Displayed Recognized Speech Text
                        Text(
                            text = "“$currentSentence”",
                            color = Color.White,
                            fontSize = captionFontSize.sp,
                            lineHeight = (captionFontSize + 6).sp,
                            fontWeight = FontWeight.Medium,
                            textAlign = TextAlign.Start,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp)
                        )

                        // Quick Demo Voice Chips (For Expo judge testing without mic or in noisy room)
                        Spacer(modifier = Modifier.height(6.dp))
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .horizontalScroll(rememberScrollState()),
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            QuickSpeechChip(
                                label = "“Hello, I can hear you!”",
                                onClick = { speechManager.recordUserSpeech("Hello, I can hear you clearly through SmartVideocall!") }
                            )
                            QuickSpeechChip(
                                label = "“Show live captions”",
                                onClick = { speechManager.recordUserSpeech("The live captions are rendering in real time with high accuracy!") }
                            )
                            QuickSpeechChip(
                                label = "“வணக்கம் (Tamil)”",
                                onClick = { speechManager.recordUserSpeech("வணக்கம்! இந்த பயன்பாடு தமிழில் மிகச் சிறப்பாக செயல்படுகிறது.") }
                            )
                        }
                    } else {
                        // --- TAB 1: IN-CALL LIVE TEXT CHAT ---
                        val recentInCallMessages = transcriptList.takeLast(3)
                        if (recentInCallMessages.isNotEmpty()) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 4.dp),
                                verticalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                recentInCallMessages.forEach { msg ->
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .clip(RoundedCornerShape(8.dp))
                                            .background(if (msg.isRemote) Color(0xFF1E293B) else BrandCyan.copy(alpha = 0.18f))
                                            .padding(horizontal = 8.dp, vertical = 4.dp),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(
                                            text = "${msg.speakerName}: ${msg.text.removePrefix("💬 ")}",
                                            color = if (msg.isRemote) Color.White else BrandCyanLight,
                                            fontSize = 12.sp,
                                            maxLines = 2,
                                            overflow = TextOverflow.Ellipsis,
                                            modifier = Modifier.weight(1f)
                                        )
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text(
                                            text = msg.timestamp,
                                            color = Color(0xFF64748B),
                                            fontSize = 10.sp
                                        )
                                    }
                                }
                            }
                        }

                        // In-Call Text Composer
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(top = 4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            OutlinedTextField(
                                value = inCallTextInput,
                                onValueChange = { inCallTextInput = it },
                                placeholder = { Text("Type text during video call...", color = Color(0xFF94A3B8), fontSize = 12.sp) },
                                singleLine = true,
                                modifier = Modifier
                                    .weight(1f)
                                    .testTag("incall_text_input"),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedTextColor = Color.White,
                                    unfocusedTextColor = Color.White,
                                    focusedBorderColor = BrandCyan,
                                    unfocusedBorderColor = DarkBorder,
                                    focusedContainerColor = DarkSurfaceElevated,
                                    unfocusedContainerColor = DarkSurfaceElevated
                                ),
                                shape = RoundedCornerShape(12.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            IconButton(
                                onClick = {
                                    if (inCallTextInput.isNotBlank()) {
                                        val msgText = inCallTextInput.trim()
                                        val sender = currentUser?.name ?: "You"
                                        speechManager.recordInCallTextMessage(sender, msgText, isRemote = false)
                                        repository.sendMessage(contact.id, msgText)
                                        inCallTextInput = ""
                                        coroutineScope.launch {
                                            delay(1200)
                                            val reply = when {
                                                msgText.contains("வணக்கம்", ignoreCase = true) -> "வணக்கம்! உங்கள் செய்தி கிடைத்தது."
                                                msgText.contains("repeat", ignoreCase = true) -> "Sure! I was explaining the live speech-to-text pipeline."
                                                else -> "Received: \"$msgText\"! Video calling with in-call text works great."
                                            }
                                            speechManager.recordInCallTextMessage(contact.name, reply, isRemote = true)
                                            repository.sendMessage(contact.id, reply)
                                        }
                                    }
                                },
                                modifier = Modifier
                                    .size(42.dp)
                                    .clip(CircleShape)
                                    .background(BrandCyan)
                                    .testTag("send_incall_text_button")
                            ) {
                                Icon(
                                    imageVector = Icons.AutoMirrored.Filled.Send,
                                    contentDescription = "Send Text",
                                    tint = Color(0xFF00273F),
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        }

                        // Quick In-Call Text Chips
                        Spacer(modifier = Modifier.height(4.dp))
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .horizontalScroll(rememberScrollState()),
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            val sender = currentUser?.name ?: "You"
                            QuickSpeechChip(
                                label = "“I can hear you! 👍”",
                                onClick = {
                                    val quick = "I can hear you clearly! 👍"
                                    speechManager.recordInCallTextMessage(sender, quick, isRemote = false)
                                    repository.sendMessage(contact.id, quick)
                                }
                            )
                            QuickSpeechChip(
                                label = "“Could you speak slower?”",
                                onClick = {
                                    val quick = "Could you please speak slightly slower?"
                                    speechManager.recordInCallTextMessage(sender, quick, isRemote = false)
                                    repository.sendMessage(contact.id, quick)
                                }
                            )
                            QuickSpeechChip(
                                label = "“வணக்கம் நலமா? (Tamil)”",
                                onClick = {
                                    val quick = "வணக்கம், நீங்கள் நலமா?"
                                    speechManager.recordInCallTextMessage(sender, quick, isRemote = false)
                                    repository.sendMessage(contact.id, quick)
                                }
                            )
                        }
                    }
                }
            }
        }

        // --- 5. BOTTOM CALL CONTROLS DOCK ---
        Box(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .navigationBarsPadding()
                .padding(bottom = 12.dp, start = 16.dp, end = 16.dp)
        ) {
            Surface(
                shape = RoundedCornerShape(32.dp),
                color = Color(0xE6111827),
                border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF334155)),
                shadowElevation = 24.dp,
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 12.dp, vertical = 10.dp),
                    horizontalArrangement = Arrangement.SpaceEvenly,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Mic toggle
                    CallControlButton(
                        icon = if (isMicOn) Icons.Default.Mic else Icons.Default.MicOff,
                        isActive = isMicOn,
                        contentDescription = "Microphone",
                        onClick = { isMicOn = !isMicOn }
                    )

                    // Camera toggle
                    CallControlButton(
                        icon = if (isCameraOn) Icons.Default.Videocam else Icons.Default.VideocamOff,
                        isActive = isCameraOn,
                        contentDescription = "Camera",
                        onClick = { isCameraOn = !isCameraOn }
                    )

                    // Speaker toggle
                    CallControlButton(
                        icon = if (isSpeakerOn) Icons.Default.VolumeUp else Icons.Default.VolumeDown,
                        isActive = isSpeakerOn,
                        contentDescription = "Speaker",
                        onClick = { isSpeakerOn = !isSpeakerOn }
                    )

                    // Captions toggle
                    CallControlButton(
                        icon = if (areCaptionsEnabled) Icons.Default.ClosedCaption else Icons.Default.ClosedCaptionOff,
                        isActive = areCaptionsEnabled,
                        activeColor = BrandCyan,
                        contentDescription = "Captions",
                        onClick = { areCaptionsEnabled = !areCaptionsEnabled }
                    )

                    // In-Call Text toggle
                    CallControlButton(
                        icon = Icons.Default.Chat,
                        isActive = areCaptionsEnabled && activeCallTab == 1,
                        activeColor = BrandCyan,
                        contentDescription = "In-Call Text",
                        onClick = {
                            if (!areCaptionsEnabled) {
                                areCaptionsEnabled = true
                                activeCallTab = 1
                            } else {
                                activeCallTab = if (activeCallTab == 1) 0 else 1
                            }
                        }
                    )

                    // Fullscreen toggle
                    CallControlButton(
                        icon = if (isFullScreen) Icons.Default.FullscreenExit else Icons.Default.Fullscreen,
                        isActive = isFullScreen,
                        contentDescription = "Full Screen",
                        onClick = { isFullScreen = !isFullScreen }
                    )

                    // End Call (Prominent Red Button)
                    FloatingActionButton(
                        onClick = onEndCall,
                        containerColor = AccentRose,
                        contentColor = Color.White,
                        shape = CircleShape,
                        elevation = FloatingActionButtonDefaults.elevation(6.dp),
                        modifier = Modifier
                            .size(52.dp)
                            .testTag("end_call_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.CallEnd,
                            contentDescription = "End Call",
                            modifier = Modifier.size(24.dp)
                        )
                    }
                }
            }
        }
    }

    // --- 6. FULL TRANSCRIPT MODAL BOTTOM SHEET ---
    if (showTranscriptSheet) {
        ModalBottomSheet(
            onDismissRequest = { showTranscriptSheet = false },
            sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
            containerColor = DarkSurface,
            contentColor = Color.White
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp)
                    .padding(bottom = 32.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "Live Call Transcript",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                        Text(
                            text = "${transcriptList.size} spoken statements logged",
                            fontSize = 12.sp,
                            color = Color(0xFF94A3B8)
                        )
                    }

                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        // Copy All
                        IconButton(
                            onClick = {
                                val fullText = transcriptList.joinToString("\n\n") {
                                    "[${it.timestamp}] ${it.speakerName}: ${it.text}"
                                }
                                val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                                clipboard.setPrimaryClip(ClipData.newPlainText("TalkText AI Transcript", fullText))
                                Toast.makeText(context, "Transcript copied to clipboard!", Toast.LENGTH_SHORT).show()
                            }
                        ) {
                            Icon(
                                imageVector = Icons.Default.ContentCopy,
                                contentDescription = "Copy Transcript",
                                tint = BrandCyanLight
                            )
                        }

                        // Clear Transcript
                        IconButton(
                            onClick = {
                                speechManager.clearTranscript()
                                Toast.makeText(context, "Transcript cleared", Toast.LENGTH_SHORT).show()
                            }
                        ) {
                            Icon(
                                imageVector = Icons.Default.DeleteOutline,
                                contentDescription = "Clear Transcript",
                                tint = AccentRose
                            )
                        }
                    }
                }

                HorizontalDivider(
                    modifier = Modifier.padding(vertical = 12.dp),
                    color = DarkBorder
                )

                if (transcriptList.isEmpty()) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(160.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "No captions recorded yet. Speak into the microphone or listen to the caller!",
                            color = Color(0xFF94A3B8),
                            fontSize = 14.sp,
                            textAlign = TextAlign.Center
                        )
                    }
                } else {
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(350.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        items(transcriptList) { caption ->
                            TranscriptItemCard(caption = caption)
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun CallControlButton(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    isActive: Boolean,
    contentDescription: String,
    activeColor: Color = Color.White,
    onClick: () -> Unit
) {
    val bgColor by animateColorAsState(
        targetValue = if (isActive) Color(0xFF1E293B) else Color(0x33EF4444),
        label = "btn_bg"
    )
    val iconTint by animateColorAsState(
        targetValue = if (isActive) activeColor else AccentRose,
        label = "btn_tint"
    )

    IconButton(
        onClick = onClick,
        colors = IconButtonDefaults.iconButtonColors(containerColor = bgColor),
        modifier = Modifier
            .size(44.dp)
            .clip(CircleShape)
            .testTag(contentDescription.lowercase().replace(" ", "_") + "_button")
    ) {
        Icon(
            imageVector = icon,
            contentDescription = contentDescription,
            tint = iconTint,
            modifier = Modifier.size(20.dp)
        )
    }
}

@Composable
private fun QuickSpeechChip(
    label: String,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(12.dp))
            .background(Color(0x3338BDF8))
            .border(1.dp, BrandCyan.copy(alpha = 0.4f), RoundedCornerShape(12.dp))
            .clickable(onClick = onClick)
            .padding(horizontal = 8.dp, vertical = 4.dp)
    ) {
        Text(
            text = label,
            color = BrandCyanLight,
            fontSize = 11.sp,
            fontWeight = FontWeight.Medium
        )
    }
}

@Composable
private fun TranscriptItemCard(caption: com.example.data.LiveCaption) {
    Surface(
        shape = RoundedCornerShape(12.dp),
        color = Color(0xFF1E293B),
        border = androidx.compose.foundation.BorderStroke(0.8.dp, Color(0xFF334155))
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(10.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = caption.speakerName,
                    color = if (caption.isRemote) BrandCyanLight else Color(0xFFA5B4FC),
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = caption.timestamp,
                    color = Color(0xFF64748B),
                    fontSize = 10.sp
                )
            }
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = caption.text,
                color = Color(0xFFF1F5F9),
                fontSize = 14.sp,
                lineHeight = 18.sp
            )
        }
    }
}
