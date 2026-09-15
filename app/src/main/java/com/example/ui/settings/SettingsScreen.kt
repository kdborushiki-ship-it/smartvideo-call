package com.example.ui.settings

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
import androidx.compose.material.icons.filled.Accessibility
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.LightMode
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Logout
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Subtitles
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
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
import com.example.data.CaptionLanguage
import com.example.data.DemoRepository
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

@Composable
fun SettingsScreen(
    repository: DemoRepository,
    onLogout: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val currentUser by repository.currentUser.collectAsState()
    val isDarkMode by repository.isDarkMode.collectAsState()
    val captionFontSize by repository.captionFontSize.collectAsState()
    val highReadability by repository.highReadabilityCaptions.collectAsState()
    val captionLanguage by repository.captionLanguage.collectAsState()

    var showEditProfileDialog by remember { mutableStateOf(false) }
    var showAboutDialog by remember { mutableStateOf(false) }

    var notificationsEnabled by remember { mutableStateOf(true) }
    var readReceiptsEnabled by remember { mutableStateOf(true) }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(DarkBackground)
            .verticalScroll(rememberScrollState())
    ) {
        // Top Header
        Surface(
            color = DarkSurface,
            shadowElevation = 4.dp,
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = "Settings & Accessibility",
                    color = Color.White,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "Manage account, live caption preferences, and display",
                    color = Color(0xFF94A3B8),
                    fontSize = 12.sp
                )
            }
        }

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // --- 1. USER PROFILE CARD ---
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = DarkSurfaceElevated),
                border = androidx.compose.foundation.BorderStroke(1.dp, DarkBorder),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(54.dp)
                                .clip(CircleShape)
                                .background(
                                    Brush.linearGradient(listOf(BrandCyan, BrandIndigo))
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(text = currentUser?.avatarEmoji ?: "✨", fontSize = 28.sp)
                        }

                        Spacer(modifier = Modifier.width(14.dp))

                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = currentUser?.name ?: "User",
                                color = Color.White,
                                fontSize = 17.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "@${currentUser?.username ?: "user"}",
                                color = BrandCyanLight,
                                fontSize = 12.sp
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = currentUser?.statusBio ?: "",
                                color = Color(0xFF94A3B8),
                                fontSize = 12.sp,
                                maxLines = 1
                            )
                        }

                        IconButton(onClick = { showEditProfileDialog = true }) {
                            Icon(
                                imageVector = Icons.Default.Edit,
                                contentDescription = "Edit Profile",
                                tint = BrandCyanLight
                            )
                        }
                    }

                    HorizontalDivider(
                        color = DarkBorder,
                        modifier = Modifier.padding(vertical = 10.dp)
                    )

                    // Logged in Email Details
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Lock,
                                contentDescription = null,
                                tint = AccentEmerald,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = currentUser?.email ?: "No email registered",
                                color = Color.White,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                        }

                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(6.dp))
                                .background(AccentEmerald.copy(alpha = 0.2f))
                                .padding(horizontal = 6.dp, vertical = 2.dp)
                        ) {
                            Text(
                                text = "Verified",
                                color = AccentEmerald,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }

            // --- 2. ⭐ LIVE CAPTIONS & ACCESSIBILITY SECTION ---
            Text(
                text = "⭐ Live Speech Captions & Accessibility",
                color = BrandCyanLight,
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold
            )

            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = DarkSurfaceElevated),
                border = androidx.compose.foundation.BorderStroke(1.dp, DarkBorder),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    // Caption Font Size Slider
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Accessibility, contentDescription = null, tint = BrandCyan, modifier = Modifier.size(20.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Caption Font Size", color = Color.White, fontSize = 14.sp, fontWeight = FontWeight.SemiBold)
                        }
                        Text("${captionFontSize}sp", color = BrandCyanLight, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                    }

                    Slider(
                        value = captionFontSize.toFloat(),
                        onValueChange = { repository.setCaptionFontSize(it.toInt()) },
                        valueRange = 14f..28f,
                        steps = 6,
                        colors = SliderDefaults.colors(
                            thumbColor = BrandCyan,
                            activeTrackColor = BrandCyanLight
                        ),
                        modifier = Modifier.padding(vertical = 4.dp)
                    )

                    // Caption Sample Preview Box
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(10.dp))
                            .background(if (highReadability) Color(0xF00B1120) else Color(0x990F172A))
                            .border(1.dp, BrandCyan.copy(alpha = 0.5f), RoundedCornerShape(10.dp))
                            .padding(10.dp)
                    ) {
                        Text(
                            text = "“Preview: TalkText AI live captions render here with high contrast.”",
                            color = Color.White,
                            fontSize = captionFontSize.sp,
                            lineHeight = (captionFontSize + 6).sp,
                            fontWeight = FontWeight.Medium
                        )
                    }

                    HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp), color = DarkBorder)

                    // High Readability Mode Toggle
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text("High Readability Captions", color = Color.White, fontSize = 14.sp, fontWeight = FontWeight.SemiBold)
                            Text("Solid opaque background for best contrast", color = Color(0xFF94A3B8), fontSize = 12.sp)
                        }
                        Switch(
                            checked = highReadability,
                            onCheckedChange = { repository.setHighReadability(it) },
                            colors = SwitchDefaults.colors(checkedThumbColor = BrandCyan)
                        )
                    }

                    HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp), color = DarkBorder)

                    // Default Speech Language
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text("Default Caption Language", color = Color.White, fontSize = 14.sp, fontWeight = FontWeight.SemiBold)
                            Text("Primary speech recognition engine", color = Color(0xFF94A3B8), fontSize = 12.sp)
                        }
                        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            FilterChip(
                                selected = captionLanguage == CaptionLanguage.ENGLISH,
                                onClick = { repository.setCaptionLanguage(CaptionLanguage.ENGLISH) },
                                label = { Text("EN") },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = BrandCyan.copy(alpha = 0.25f),
                                    selectedLabelColor = BrandCyanLight
                                )
                            )
                            FilterChip(
                                selected = captionLanguage == CaptionLanguage.TAMIL,
                                onClick = { repository.setCaptionLanguage(CaptionLanguage.TAMIL) },
                                label = { Text("தமிழ்") },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = BrandViolet.copy(alpha = 0.3f),
                                    selectedLabelColor = Color.White
                                )
                            )
                        }
                    }
                }
            }

            // --- 3. APP PREFERENCES & THEME ---
            Text(
                text = "Preferences & Privacy",
                color = Color(0xFF94A3B8),
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold
            )

            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = DarkSurfaceElevated),
                border = androidx.compose.foundation.BorderStroke(1.dp, DarkBorder),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    // Dark Mode Toggle
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = if (isDarkMode) Icons.Default.DarkMode else Icons.Default.LightMode,
                                contentDescription = null,
                                tint = BrandCyan,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(10.dp))
                            Text("Dark Theme", color = Color.White, fontSize = 14.sp)
                        }
                        Switch(
                            checked = isDarkMode,
                            onCheckedChange = { repository.setDarkMode(it) },
                            colors = SwitchDefaults.colors(checkedThumbColor = BrandCyan)
                        )
                    }

                    HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp), color = DarkBorder)

                    // Notifications Toggle
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Notifications, contentDescription = null, tint = BrandCyan, modifier = Modifier.size(20.dp))
                            Spacer(modifier = Modifier.width(10.dp))
                            Text("Message & Call Alerts", color = Color.White, fontSize = 14.sp)
                        }
                        Switch(
                            checked = notificationsEnabled,
                            onCheckedChange = { notificationsEnabled = it },
                            colors = SwitchDefaults.colors(checkedThumbColor = BrandCyan)
                        )
                    }

                    HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp), color = DarkBorder)

                    // Read Receipts
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Lock, contentDescription = null, tint = BrandCyan, modifier = Modifier.size(20.dp))
                            Spacer(modifier = Modifier.width(10.dp))
                            Text("End-to-End Encrypted Calls", color = Color.White, fontSize = 14.sp)
                        }
                        Text("Active 🔒", color = AccentEmerald, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }

            // --- 4. ALL USERS DIRECTORY (QUICK SWITCH) ---
            Text(
                text = "👥 All System Users (Quick Switch)",
                color = BrandCyanLight,
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold
            )

            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = DarkSurfaceElevated),
                border = androidx.compose.foundation.BorderStroke(1.dp, DarkBorder),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        text = "Tap any user below to immediately switch active account:",
                        color = Color(0xFF94A3B8),
                        fontSize = 12.sp
                    )

                    repository.allSystemUsers.forEach { sysUser ->
                        val isCurrent = sysUser.id == currentUser?.id
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(10.dp))
                                .background(if (isCurrent) BrandCyan.copy(alpha = 0.15f) else Color(0xFF1E293B))
                                .border(
                                    1.dp,
                                    if (isCurrent) BrandCyan else Color.Transparent,
                                    RoundedCornerShape(10.dp)
                                )
                                .clickable {
                                    if (!isCurrent) {
                                        repository.loginAsUser(sysUser)
                                        Toast.makeText(context, "Switched to ${sysUser.name}", Toast.LENGTH_SHORT).show()
                                    }
                                }
                                .padding(horizontal = 12.dp, vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                                Text(sysUser.avatarEmoji, fontSize = 22.sp)
                                Spacer(modifier = Modifier.width(10.dp))
                                Column {
                                    Text(
                                        text = sysUser.name,
                                        color = Color.White,
                                        fontSize = 13.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                    Text(
                                        text = sysUser.email,
                                        color = if (isCurrent) BrandCyanLight else Color(0xFF94A3B8),
                                        fontSize = 11.sp
                                    )
                                }
                            }
                            if (isCurrent) {
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(6.dp))
                                        .background(AccentEmerald.copy(alpha = 0.25f))
                                        .padding(horizontal = 6.dp, vertical = 2.dp)
                                ) {
                                    Text("Active", color = AccentEmerald, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                                }
                            } else {
                                Text("Switch", color = BrandCyanLight, fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
                            }
                        }
                    }
                }
            }

            // --- 5. PROJECT EXPO & ABOUT SECTION ---
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = DarkSurfaceElevated),
                border = androidx.compose.foundation.BorderStroke(1.dp, BrandCyan.copy(alpha = 0.4f)),
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { showAboutDialog = true }
                    .testTag("about_smartvideocall_card")
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(44.dp)
                            .clip(CircleShape)
                            .background(BrandCyan.copy(alpha = 0.2f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(Icons.Default.Info, contentDescription = null, tint = BrandCyanLight)
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "About SmartVideocall",
                            color = Color.White,
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "Real-time video calling with live text & captions",
                            color = Color(0xFF94A3B8),
                            fontSize = 12.sp
                        )
                    }
                }
            }

            // --- 6. SWITCH EMAIL / LOGOUT BUTTONS ---
            OutlinedButton(
                onClick = {
                    repository.logout()
                    onLogout()
                },
                border = androidx.compose.foundation.BorderStroke(1.dp, BrandCyan.copy(alpha = 0.5f)),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("settings_switch_email_button")
            ) {
                Icon(Icons.Default.Lock, contentDescription = null, tint = BrandCyanLight, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text("Switch Email Account", color = BrandCyanLight, fontWeight = FontWeight.SemiBold)
            }

            Button(
                onClick = {
                    repository.logout()
                    onLogout()
                },
                colors = ButtonDefaults.buttonColors(containerColor = Color(0x33EF4444)),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("settings_logout_button")
            ) {
                Icon(Icons.Default.Logout, contentDescription = null, tint = AccentRose)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Log Out", color = AccentRose, fontWeight = FontWeight.Bold)
            }

            Spacer(modifier = Modifier.height(24.dp))
        }
    }

    // --- ABOUT SMARTVIDEOCALL DIALOG ---
    if (showAboutDialog) {
        AlertDialog(
            onDismissRequest = { showAboutDialog = false },
            title = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text("About SmartVideocall", fontWeight = FontWeight.Bold)
                }
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text(
                        text = "“SmartVideocall is a smart multi-user communication platform that combines video calling with real-time speech-to-text captions and in-call live text messaging. It helps all users communicate effortlessly by displaying spoken words as live text during video calls.”",
                        color = Color.White,
                        fontSize = 14.sp,
                        lineHeight = 20.sp,
                        fontWeight = FontWeight.Medium
                    )

                    HorizontalDivider(color = DarkBorder)

                    Text(
                        text = "Key SmartVideocall Features:",
                        color = BrandCyanLight,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text("• All-user login directory with 1-tap fast switching", color = Color(0xFFCBD5E1), fontSize = 12.sp)
                    Text("• Crystal-clear video calls with real-time speech captions", color = Color(0xFFCBD5E1), fontSize = 12.sp)
                    Text("• In-call interactive text messaging & simulated responses", color = Color(0xFFCBD5E1), fontSize = 12.sp)
                    Text("• English & Tamil bilingual speech-to-text recognition", color = Color(0xFFCBD5E1), fontSize = 12.sp)
                    Text("• CameraX hardware local camera preview & lens toggle", color = Color(0xFFCBD5E1), fontSize = 12.sp)
                    Text("• Automatic post-call AI summaries and action item extraction", color = Color(0xFFCBD5E1), fontSize = 12.sp)
                }
            },
            confirmButton = {
                TextButton(onClick = { showAboutDialog = false }) {
                    Text("Got it")
                }
            }
        )
    }

    // --- EDIT PROFILE DIALOG ---
    if (showEditProfileDialog) {
        var editName by remember { mutableStateOf(currentUser?.name ?: "") }
        var editBio by remember { mutableStateOf(currentUser?.statusBio ?: "") }
        var editAvatar by remember { mutableStateOf(currentUser?.avatarEmoji ?: "✨") }
        val avatarOptions = listOf("✨", "🚀", "👨‍💻", "👩‍🔬", "🎨", "🎙️", "⭐", "⚡")

        AlertDialog(
            onDismissRequest = { showEditProfileDialog = false },
            title = { Text("Edit Profile") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text("Choose Avatar:", color = Color(0xFF94A3B8), fontSize = 12.sp)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceAround
                    ) {
                        avatarOptions.forEach { emoji ->
                            Text(
                                text = emoji,
                                fontSize = 24.sp,
                                modifier = Modifier
                                    .clip(CircleShape)
                                    .background(if (editAvatar == emoji) BrandCyan.copy(alpha = 0.3f) else Color.Transparent)
                                    .clickable { editAvatar = emoji }
                                    .padding(4.dp)
                            )
                        }
                    }

                    OutlinedTextField(
                        value = editName,
                        onValueChange = { editName = it },
                        label = { Text("Full Name") },
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White
                        ),
                        modifier = Modifier.fillMaxWidth()
                    )

                    OutlinedTextField(
                        value = editBio,
                        onValueChange = { editBio = it },
                        label = { Text("Status Bio") },
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White
                        ),
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        repository.updateProfile(editName, editBio, editAvatar)
                        showEditProfileDialog = false
                        Toast.makeText(context, "Profile updated!", Toast.LENGTH_SHORT).show()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = BrandCyan)
                ) {
                    Text("Save", color = Color(0xFF00273F), fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showEditProfileDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }
}
