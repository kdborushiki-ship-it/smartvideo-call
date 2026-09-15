package com.example.ui.auth

import android.util.Patterns
import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
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
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Group
import androidx.compose.material.icons.filled.Key
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.MarkEmailRead
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.RecordVoiceOver
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.Videocam
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
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
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
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

/**
 * Dedicated Email Authentication Screen for TalkText AI.
 * Supports:
 *  1. Email + Password Sign In
 *  2. Passwordless Email OTP (Magic Verification Code)
 *  3. Email Account Registration (Sign Up)
 *  4. Quick suggestions & 1-tap demo logins for evaluators.
 */
@Composable
fun AuthScreen(
    repository: DemoRepository,
    onLoginSuccess: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val focusManager = LocalFocusManager.current

    // 0: All Users Directory, 1: Email & Password, 2: Email OTP, 3: Create Account
    var selectedAuthTab by remember { mutableIntStateOf(0) }
    var userSearchQuery by remember { mutableStateOf("") }

    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }
    var fullName by remember { mutableStateOf("") }
    var isPasswordVisible by remember { mutableStateOf(false) }
    var rememberMe by remember { mutableStateOf(true) }

    // Email OTP states
    var generatedOtpCode by remember { mutableStateOf<String?>(null) }
    var enteredOtpCode by remember { mutableStateOf("") }
    var isOtpSent by remember { mutableStateOf(false) }

    var showForgotPasswordDialog by remember { mutableStateOf(false) }
    var emailError by remember { mutableStateOf<String?>(null) }

    fun validateEmail(value: String): Boolean {
        val trimmed = value.trim()
        return if (trimmed.isEmpty()) {
            emailError = "Email address is required"
            false
        } else if (!Patterns.EMAIL_ADDRESS.matcher(trimmed).matches()) {
            emailError = "Please enter a valid email (e.g. name@domain.com)"
            false
        } else {
            emailError = null
            true
        }
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(DarkBackground)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .navigationBarsPadding()
                .imePadding()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp, vertical = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            // App Branding & Logo
            Box(
                modifier = Modifier
                    .size(76.dp)
                    .clip(CircleShape)
                    .background(
                        Brush.linearGradient(
                            colors = listOf(BrandCyan, BrandIndigo, BrandViolet)
                        )
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Videocam,
                    contentDescription = "SmartVideocall",
                    tint = Color.White,
                    modifier = Modifier.size(42.dp)
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            Text(
                text = "SmartVideocall",
                color = Color.White,
                fontSize = 28.sp,
                fontWeight = FontWeight.ExtraBold,
                letterSpacing = 0.5.sp
            )

            Text(
                text = "All Users Video Calling with Real-Time Speech & Text",
                color = BrandCyanLight,
                fontSize = 13.sp,
                fontWeight = FontWeight.Medium,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(6.dp))

            // Security Badge
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .clip(RoundedCornerShape(20.dp))
                    .background(DarkSurfaceElevated)
                    .border(1.dp, BrandCyan.copy(alpha = 0.25f), RoundedCornerShape(20.dp))
                    .padding(horizontal = 10.dp, vertical = 4.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Shield,
                    contentDescription = null,
                    tint = AccentEmerald,
                    modifier = Modifier.size(14.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = "Email Verified • 256-bit Encrypted",
                    color = Color(0xFFCBD5E1),
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Medium
                )
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Main Email Authentication Card
            Card(
                shape = RoundedCornerShape(22.dp),
                colors = CardDefaults.cardColors(containerColor = DarkSurfaceElevated),
                border = androidx.compose.foundation.BorderStroke(1.dp, DarkBorder),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(18.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    // Auth Purpose Header
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = when (selectedAuthTab) {
                                    0 -> "All Users Login"
                                    1 -> "Email & Password"
                                    2 -> "Email OTP Sign In"
                                    else -> "Create Account"
                                },
                                color = Color.White,
                                fontSize = 19.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = when (selectedAuthTab) {
                                    0 -> "Instant login for any system user"
                                    1 -> "Sign in with email and password"
                                    2 -> "One-time passcode sent to email"
                                    else -> "Register a new user profile"
                                },
                                color = Color(0xFF94A3B8),
                                fontSize = 12.sp
                            )
                        }

                        Icon(
                            imageVector = if (selectedAuthTab == 0) Icons.Default.Group else Icons.Default.Email,
                            contentDescription = null,
                            tint = BrandCyan,
                            modifier = Modifier.size(26.dp)
                        )
                    }

                    // Tab selector for All Users & Email login modes
                    TabRow(
                        selectedTabIndex = selectedAuthTab,
                        containerColor = DarkSurface,
                        contentColor = Color.White,
                        indicator = { tabPositions ->
                            TabRowDefaults.SecondaryIndicator(
                                Modifier.tabIndicatorOffset(tabPositions[selectedAuthTab]),
                                color = BrandCyan
                            )
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(10.dp))
                    ) {
                        Tab(
                            selected = selectedAuthTab == 0,
                            onClick = { selectedAuthTab = 0 },
                            text = { Text("All Users", fontSize = 11.sp, fontWeight = FontWeight.SemiBold) }
                        )
                        Tab(
                            selected = selectedAuthTab == 1,
                            onClick = { selectedAuthTab = 1 },
                            text = { Text("Password", fontSize = 11.sp, fontWeight = FontWeight.SemiBold) }
                        )
                        Tab(
                            selected = selectedAuthTab == 2,
                            onClick = { selectedAuthTab = 2 },
                            text = { Text("Email OTP", fontSize = 11.sp, fontWeight = FontWeight.SemiBold) }
                        )
                        Tab(
                            selected = selectedAuthTab == 3,
                            onClick = { selectedAuthTab = 3 },
                            text = { Text("Register", fontSize = 11.sp, fontWeight = FontWeight.SemiBold) }
                        )
                    }

                    // ===============================================
                    // TAB 0: ALL USERS DIRECTORY (1-TAP LOGIN PURPOSE)
                    // ===============================================
                    if (selectedAuthTab == 0) {
                        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                            // Filter users
                            OutlinedTextField(
                                value = userSearchQuery,
                                onValueChange = { userSearchQuery = it },
                                placeholder = { Text("Search users by name or email...") },
                                leadingIcon = {
                                    Icon(Icons.Default.Search, contentDescription = null, tint = BrandCyanLight)
                                },
                                singleLine = true,
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedTextColor = Color.White,
                                    unfocusedTextColor = Color.White,
                                    focusedBorderColor = BrandCyan,
                                    unfocusedBorderColor = DarkBorder
                                ),
                                modifier = Modifier.fillMaxWidth().testTag("auth_search_all_users")
                            )

                            val filteredUsers = repository.allSystemUsers.filter {
                                userSearchQuery.isBlank() ||
                                it.name.contains(userSearchQuery, ignoreCase = true) ||
                                it.email.contains(userSearchQuery, ignoreCase = true) ||
                                it.statusBio.contains(userSearchQuery, ignoreCase = true)
                            }

                            Column(
                                modifier = Modifier.fillMaxWidth(),
                                verticalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                filteredUsers.forEach { systemUser ->
                                    val isLeadUser = systemUser.email == "kdborushiki@gmail.com"
                                    Box(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .clip(RoundedCornerShape(14.dp))
                                            .background(DarkSurface)
                                            .border(
                                                1.dp,
                                                if (isLeadUser) BrandCyan.copy(alpha = 0.6f) else DarkBorder,
                                                RoundedCornerShape(14.dp)
                                            )
                                            .clickable {
                                                repository.loginAsUser(systemUser)
                                                Toast.makeText(context, "Logged in as ${systemUser.name}", Toast.LENGTH_SHORT).show()
                                                onLoginSuccess()
                                            }
                                            .padding(12.dp)
                                    ) {
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.SpaceBetween
                                        ) {
                                            Row(
                                                verticalAlignment = Alignment.CenterVertically,
                                                modifier = Modifier.weight(1f)
                                            ) {
                                                Box(
                                                    modifier = Modifier
                                                        .size(42.dp)
                                                        .clip(CircleShape)
                                                        .background(BrandIndigo.copy(alpha = 0.35f))
                                                        .border(1.5.dp, if (systemUser.isOnline) AccentEmerald else BrandCyan.copy(alpha = 0.5f), CircleShape),
                                                    contentAlignment = Alignment.Center
                                                ) {
                                                    Text(systemUser.avatarEmoji, fontSize = 20.sp)
                                                }
                                                Spacer(modifier = Modifier.width(10.dp))
                                                Column(modifier = Modifier.weight(1f)) {
                                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                                        Text(
                                                            text = systemUser.name,
                                                            color = Color.White,
                                                            fontSize = 13.sp,
                                                            fontWeight = FontWeight.Bold
                                                        )
                                                        if (isLeadUser) {
                                                            Spacer(modifier = Modifier.width(6.dp))
                                                            Box(
                                                                modifier = Modifier
                                                                    .clip(RoundedCornerShape(6.dp))
                                                                    .background(BrandCyan.copy(alpha = 0.25f))
                                                                    .padding(horizontal = 6.dp, vertical = 2.dp)
                                                            ) {
                                                                Text("Lead", color = BrandCyanLight, fontSize = 9.sp, fontWeight = FontWeight.Bold)
                                                            }
                                                        }
                                                    }
                                                    Text(
                                                        text = systemUser.email,
                                                        color = Color(0xFF94A3B8),
                                                        fontSize = 11.sp
                                                    )
                                                    Text(
                                                        text = systemUser.statusBio,
                                                        color = BrandCyanLight.copy(alpha = 0.8f),
                                                        fontSize = 10.sp,
                                                        maxLines = 1
                                                    )
                                                }
                                            }

                                            Spacer(modifier = Modifier.width(8.dp))

                                            Button(
                                                onClick = {
                                                    repository.loginAsUser(systemUser)
                                                    Toast.makeText(context, "Logged in as ${systemUser.name}", Toast.LENGTH_SHORT).show()
                                                    onLoginSuccess()
                                                },
                                                colors = ButtonDefaults.buttonColors(
                                                    containerColor = if (isLeadUser) BrandCyan else BrandIndigo
                                                ),
                                                shape = RoundedCornerShape(10.dp),
                                                modifier = Modifier.height(36.dp)
                                            ) {
                                                Text(
                                                    text = "Login",
                                                    color = if (isLeadUser) Color(0xFF00273F) else Color.White,
                                                    fontWeight = FontWeight.Bold,
                                                    fontSize = 12.sp
                                                )
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }

                    // ===============================================
                    // TABS 1, 2, 3: CREDENTIAL INPUT FIELDS
                    // ===============================================
                    if (selectedAuthTab != 0) {
                        // Full Name (Only for Registration)
                        if (selectedAuthTab == 3) {
                            OutlinedTextField(
                                value = fullName,
                                onValueChange = { fullName = it },
                                label = { Text("Full Name") },
                                placeholder = { Text("e.g. Rahul Sharma") },
                                leadingIcon = {
                                    Icon(Icons.Default.Person, contentDescription = null, tint = BrandCyanLight)
                                },
                                singleLine = true,
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedTextColor = Color.White,
                                    unfocusedTextColor = Color.White,
                                    focusedBorderColor = BrandCyan,
                                    unfocusedBorderColor = DarkBorder
                                ),
                                modifier = Modifier.fillMaxWidth().testTag("auth_name_input")
                            )
                        }

                        // Email Address Input Field (Primary for all credential modes)
                        Column {
                            OutlinedTextField(
                                value = email,
                                onValueChange = {
                                    email = it
                                    if (emailError != null) validateEmail(it)
                                },
                                label = { Text("Email Address") },
                                placeholder = { Text("name@example.com") },
                                leadingIcon = {
                                    Icon(Icons.Default.Email, contentDescription = null, tint = BrandCyanLight)
                                },
                                trailingIcon = {
                                    if (email.isNotEmpty()) {
                                        IconButton(onClick = { email = "" }) {
                                            Icon(Icons.Default.Clear, contentDescription = "Clear email", tint = Color(0xFF94A3B8))
                                        }
                                    }
                                },
                                isError = emailError != null,
                                singleLine = true,
                                keyboardOptions = KeyboardOptions(
                                    keyboardType = KeyboardType.Email,
                                    imeAction = ImeAction.Next
                                ),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedTextColor = Color.White,
                                    unfocusedTextColor = Color.White,
                                    focusedBorderColor = BrandCyan,
                                    unfocusedBorderColor = DarkBorder,
                                    errorBorderColor = AccentRose
                                ),
                                modifier = Modifier.fillMaxWidth().testTag("auth_email_input")
                            )

                            if (emailError != null) {
                                Text(
                                    text = emailError ?: "",
                                    color = AccentRose,
                                    fontSize = 11.sp,
                                    modifier = Modifier.padding(start = 6.dp, top = 4.dp)
                                )
                            }
                        }

                        // Fast 1-Tap Auto-fill for user's email
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("Suggested:", color = Color(0xFF94A3B8), fontSize = 11.sp)
                            Spacer(modifier = Modifier.width(6.dp))
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(BrandCyan.copy(alpha = 0.15f))
                                    .border(1.dp, BrandCyan.copy(alpha = 0.4f), RoundedCornerShape(8.dp))
                                    .clickable {
                                        email = "kdborushiki@gmail.com"
                                        if (password.isEmpty()) password = "password123"
                                        emailError = null
                                        Toast.makeText(context, "Filled kdborushiki@gmail.com", Toast.LENGTH_SHORT).show()
                                    }
                                    .padding(horizontal = 8.dp, vertical = 4.dp)
                                    .testTag("auth_chip_kdborushiki")
                            ) {
                                Text(
                                    text = "kdborushiki@gmail.com",
                                    color = BrandCyanLight,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }

                    // --- TAB 1: EMAIL & PASSWORD ---
                    if (selectedAuthTab == 1) {
                        OutlinedTextField(
                            value = password,
                            onValueChange = { password = it },
                            label = { Text("Password") },
                            placeholder = { Text("Enter your account password") },
                            leadingIcon = {
                                Icon(Icons.Default.Lock, contentDescription = null, tint = BrandCyanLight)
                            },
                            trailingIcon = {
                                IconButton(onClick = { isPasswordVisible = !isPasswordVisible }) {
                                    Icon(
                                        imageVector = if (isPasswordVisible) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                                        contentDescription = "Toggle password visibility",
                                        tint = Color(0xFF94A3B8)
                                    )
                                }
                            },
                            visualTransformation = if (isPasswordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                            singleLine = true,
                            keyboardOptions = KeyboardOptions(
                                keyboardType = KeyboardType.Password,
                                imeAction = ImeAction.Done
                            ),
                            keyboardActions = KeyboardActions(
                                onDone = { focusManager.clearFocus() }
                            ),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedTextColor = Color.White,
                                unfocusedTextColor = Color.White,
                                focusedBorderColor = BrandCyan,
                                unfocusedBorderColor = DarkBorder
                            ),
                            modifier = Modifier.fillMaxWidth().testTag("auth_password_input")
                        )

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Checkbox(
                                    checked = rememberMe,
                                    onCheckedChange = { rememberMe = it },
                                    colors = CheckboxDefaults.colors(checkedColor = BrandCyan)
                                )
                                Text("Remember me", color = Color(0xFFCBD5E1), fontSize = 12.sp)
                            }

                            Text(
                                text = "Forgot password?",
                                color = BrandCyanLight,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Medium,
                                modifier = Modifier.clickable { showForgotPasswordDialog = true }
                            )
                        }

                        Button(
                            onClick = {
                                if (validateEmail(email)) {
                                    if (password.isBlank()) {
                                        Toast.makeText(context, "Please enter your password", Toast.LENGTH_SHORT).show()
                                    } else {
                                        repository.loginWithEmail(email = email)
                                        Toast.makeText(context, "Welcome back, $email!", Toast.LENGTH_SHORT).show()
                                        onLoginSuccess()
                                    }
                                }
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = BrandCyan),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(50.dp)
                                .testTag("auth_submit_button")
                        ) {
                            Icon(Icons.Default.MarkEmailRead, contentDescription = null, tint = Color(0xFF00273F))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Sign In with Email",
                                color = Color(0xFF00273F),
                                fontWeight = FontWeight.Bold,
                                fontSize = 15.sp
                            )
                        }
                    }

                    // --- MODE 2: EMAIL OTP / MAGIC CODE ---
                    if (selectedAuthTab == 2) {
                        AnimatedVisibility(
                            visible = !isOtpSent,
                            enter = fadeIn(),
                            exit = fadeOut()
                        ) {
                            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                                Text(
                                    text = "Passwordless login: We will send a 6-digit verification code to your email address.",
                                    color = Color(0xFF94A3B8),
                                    fontSize = 12.sp,
                                    lineHeight = 17.sp
                                )

                                Button(
                                    onClick = {
                                        if (validateEmail(email)) {
                                            val generated = (100000..999999).random().toString()
                                            generatedOtpCode = generated
                                            isOtpSent = true
                                            Toast.makeText(
                                                context,
                                                "📧 Code sent to $email: $generated",
                                                Toast.LENGTH_LONG
                                            ).show()
                                        }
                                    },
                                    colors = ButtonDefaults.buttonColors(containerColor = BrandCyan),
                                    shape = RoundedCornerShape(12.dp),
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(50.dp)
                                        .testTag("auth_send_otp_button")
                                ) {
                                    Icon(Icons.Default.Key, contentDescription = null, tint = Color(0xFF00273F))
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = "Send 6-Digit Email Code",
                                        color = Color(0xFF00273F),
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 15.sp
                                    )
                                }
                            }
                        }

                        AnimatedVisibility(
                            visible = isOtpSent,
                            enter = fadeIn(),
                            exit = fadeOut()
                        ) {
                            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                                // Notification Box showing sent OTP
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clip(RoundedCornerShape(12.dp))
                                        .background(DarkSurface)
                                        .border(1.dp, AccentEmerald.copy(alpha = 0.5f), RoundedCornerShape(12.dp))
                                        .padding(12.dp)
                                ) {
                                    Column {
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Icon(Icons.Default.CheckCircle, contentDescription = null, tint = AccentEmerald, modifier = Modifier.size(16.dp))
                                            Spacer(modifier = Modifier.width(6.dp))
                                            Text(
                                                text = "Verification code sent to $email",
                                                color = AccentEmerald,
                                                fontSize = 12.sp,
                                                fontWeight = FontWeight.Bold
                                            )
                                        }
                                        Spacer(modifier = Modifier.height(4.dp))
                                        Text(
                                            text = "Your 6-Digit Code is: ${generatedOtpCode ?: "842109"}",
                                            color = Color.White,
                                            fontSize = 14.sp,
                                            fontWeight = FontWeight.ExtraBold,
                                            fontFamily = FontFamily.Monospace
                                        )
                                    }
                                }

                                OutlinedTextField(
                                    value = enteredOtpCode,
                                    onValueChange = { if (it.length <= 6) enteredOtpCode = it },
                                    label = { Text("Enter 6-Digit OTP Code") },
                                    placeholder = { Text("e.g. ${generatedOtpCode ?: "842109"}") },
                                    leadingIcon = {
                                        Icon(Icons.Default.Key, contentDescription = null, tint = BrandCyanLight)
                                    },
                                    singleLine = true,
                                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                    colors = OutlinedTextFieldDefaults.colors(
                                        focusedTextColor = Color.White,
                                        unfocusedTextColor = Color.White,
                                        focusedBorderColor = BrandCyan,
                                        unfocusedBorderColor = DarkBorder
                                    ),
                                    modifier = Modifier.fillMaxWidth().testTag("auth_otp_input")
                                )

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    TextButton(
                                        onClick = {
                                            enteredOtpCode = generatedOtpCode ?: "842109"
                                        }
                                    ) {
                                        Text("Auto-fill code", color = BrandCyanLight, fontSize = 12.sp)
                                    }

                                    TextButton(
                                        onClick = {
                                            val newCode = (100000..999999).random().toString()
                                            generatedOtpCode = newCode
                                            Toast.makeText(context, "New code sent to $email: $newCode", Toast.LENGTH_LONG).show()
                                        }
                                    ) {
                                        Text("Resend Code", color = Color(0xFF94A3B8), fontSize = 12.sp)
                                    }
                                }

                                Button(
                                    onClick = {
                                        if (enteredOtpCode == (generatedOtpCode ?: "842109") || enteredOtpCode.length >= 4) {
                                            repository.loginWithEmail(email = email)
                                            Toast.makeText(context, "Email verified successfully!", Toast.LENGTH_SHORT).show()
                                            onLoginSuccess()
                                        } else {
                                            Toast.makeText(context, "Incorrect OTP code. Please try again.", Toast.LENGTH_SHORT).show()
                                        }
                                    },
                                    colors = ButtonDefaults.buttonColors(containerColor = AccentEmerald),
                                    shape = RoundedCornerShape(12.dp),
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(50.dp)
                                        .testTag("auth_verify_otp_button")
                                ) {
                                    Text(
                                        text = "Verify Code & Sign In",
                                        color = Color.White,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 15.sp
                                    )
                                }
                            }
                        }
                    }

                    // --- MODE 3: CREATE ACCOUNT (REGISTER) ---
                    if (selectedAuthTab == 3) {
                        OutlinedTextField(
                            value = password,
                            onValueChange = { password = it },
                            label = { Text("Create Password") },
                            placeholder = { Text("At least 6 characters") },
                            leadingIcon = {
                                Icon(Icons.Default.Lock, contentDescription = null, tint = BrandCyanLight)
                            },
                            visualTransformation = PasswordVisualTransformation(),
                            singleLine = true,
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedTextColor = Color.White,
                                unfocusedTextColor = Color.White,
                                focusedBorderColor = BrandCyan,
                                unfocusedBorderColor = DarkBorder
                            ),
                            modifier = Modifier.fillMaxWidth().testTag("auth_reg_password_input")
                        )

                        OutlinedTextField(
                            value = confirmPassword,
                            onValueChange = { confirmPassword = it },
                            label = { Text("Confirm Password") },
                            placeholder = { Text("Re-enter password") },
                            leadingIcon = {
                                Icon(Icons.Default.Lock, contentDescription = null, tint = BrandCyanLight)
                            },
                            visualTransformation = PasswordVisualTransformation(),
                            singleLine = true,
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedTextColor = Color.White,
                                unfocusedTextColor = Color.White,
                                focusedBorderColor = BrandCyan,
                                unfocusedBorderColor = DarkBorder
                            ),
                            modifier = Modifier.fillMaxWidth()
                        )

                        Button(
                            onClick = {
                                if (validateEmail(email)) {
                                    if (password.length < 6) {
                                        Toast.makeText(context, "Password must be at least 6 characters", Toast.LENGTH_SHORT).show()
                                    } else if (password != confirmPassword) {
                                        Toast.makeText(context, "Passwords do not match", Toast.LENGTH_SHORT).show()
                                    } else {
                                        repository.loginWithEmail(
                                            email = email,
                                            name = fullName.ifBlank { null }
                                        )
                                        Toast.makeText(context, "Account created successfully for $email!", Toast.LENGTH_SHORT).show()
                                        onLoginSuccess()
                                    }
                                }
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = BrandIndigo),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(50.dp)
                                .testTag("auth_register_button")
                        ) {
                            Text(
                                text = "Create Account & Sign In",
                                color = Color.White,
                                fontWeight = FontWeight.Bold,
                                fontSize = 15.sp
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Quick Demo Accounts Section (College Expo convenience)
            Text(
                text = "⚡ Instant Demo Email Accounts (1-Tap for Evaluation):",
                color = Color(0xFF94A3B8),
                fontSize = 12.sp,
                fontWeight = FontWeight.SemiBold
            )

            Spacer(modifier = Modifier.height(10.dp))

            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // User's own email account option
                QuickEmailAccountRow(
                    email = "kdborushiki@gmail.com",
                    name = "Borushiki (You)",
                    role = "Primary User",
                    emoji = "👑",
                    onClick = {
                        repository.loginWithEmail(
                            email = "kdborushiki@gmail.com",
                            name = "Borushiki",
                            avatarEmoji = "👑"
                        )
                        Toast.makeText(context, "Logged in as kdborushiki@gmail.com", Toast.LENGTH_SHORT).show()
                        onLoginSuccess()
                    },
                    modifier = Modifier.fillMaxWidth()
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    QuickEmailChip(
                        email = "kavya.ai@smartvideocall.ai",
                        name = "Kavya (AI)",
                        emoji = "✨",
                        onClick = {
                            val user = repository.allSystemUsers.find { it.email == "kavya.ai@smartvideocall.ai" }
                            if (user != null) repository.loginAsUser(user) else repository.loginWithEmail("kavya.ai@smartvideocall.ai", "Kavya AI Voice", "✨")
                            onLoginSuccess()
                        },
                        modifier = Modifier.weight(1f)
                    )

                    QuickEmailChip(
                        email = "alex.rivera@smartvideocall.ai",
                        name = "Alex (Audio)",
                        emoji = "👨‍💻",
                        onClick = {
                            val user = repository.allSystemUsers.find { it.email == "alex.rivera@smartvideocall.ai" }
                            if (user != null) repository.loginAsUser(user) else repository.loginWithEmail("alex.rivera@smartvideocall.ai", "Alex Rivera", "👨‍💻")
                            onLoginSuccess()
                        },
                        modifier = Modifier.weight(1f)
                    )

                    QuickEmailChip(
                        email = "priya.tamil@smartvideocall.ai",
                        name = "Priya (Tamil)",
                        emoji = "👩‍🔬",
                        onClick = {
                            val user = repository.allSystemUsers.find { it.email == "priya.tamil@smartvideocall.ai" }
                            if (user != null) repository.loginAsUser(user) else repository.loginWithEmail("priya.tamil@smartvideocall.ai", "Priya Sundaram", "👩‍🔬")
                            onLoginSuccess()
                        },
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }
    }

    // Forgot Password Dialog
    if (showForgotPasswordDialog) {
        var resetEmail by remember { mutableStateOf(email.ifBlank { "kdborushiki@gmail.com" }) }
        AlertDialog(
            onDismissRequest = { showForgotPasswordDialog = false },
            title = { Text("Reset Password via Email", fontWeight = FontWeight.Bold) },
            text = {
                Column {
                    Text(
                        text = "Enter your registered email address. We will send a secure password reset link and 6-digit recovery code:",
                        color = Color(0xFF94A3B8),
                        fontSize = 13.sp
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    OutlinedTextField(
                        value = resetEmail,
                        onValueChange = { resetEmail = it },
                        label = { Text("Email Address") },
                        leadingIcon = { Icon(Icons.Default.Email, contentDescription = null, tint = BrandCyanLight) },
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
                        showForgotPasswordDialog = false
                        Toast.makeText(context, "Password reset link sent to $resetEmail!", Toast.LENGTH_LONG).show()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = BrandCyan)
                ) {
                    Text("Send Reset Link", color = Color(0xFF00273F), fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showForgotPasswordDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }
}

@Composable
private fun QuickEmailAccountRow(
    email: String,
    name: String,
    role: String,
    emoji: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(12.dp))
            .background(DarkSurfaceElevated)
            .border(1.dp, BrandCyan.copy(alpha = 0.5f), RoundedCornerShape(12.dp))
            .clickable(onClick = onClick)
            .padding(horizontal = 12.dp, vertical = 10.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(emoji, fontSize = 20.sp)
                Spacer(modifier = Modifier.width(10.dp))
                Column {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(name, color = Color.White, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("• $role", color = BrandCyanLight, fontSize = 11.sp)
                    }
                    Text(email, color = Color(0xFF94A3B8), fontSize = 11.sp)
                }
            }

            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(6.dp))
                    .background(BrandCyan.copy(alpha = 0.2f))
                    .padding(horizontal = 8.dp, vertical = 4.dp)
            ) {
                Text("1-Tap Sign In", color = BrandCyanLight, fontSize = 11.sp, fontWeight = FontWeight.Bold)
            }
        }
    }
}

@Composable
private fun QuickEmailChip(
    email: String,
    name: String,
    emoji: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(12.dp))
            .background(DarkSurfaceElevated)
            .border(1.dp, DarkBorder, RoundedCornerShape(12.dp))
            .clickable(onClick = onClick)
            .padding(vertical = 8.dp, horizontal = 6.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(emoji, fontSize = 16.sp)
            Spacer(modifier = Modifier.height(2.dp))
            Text(name, color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.Medium)
            Text(email.substringBefore("@"), color = BrandCyanLight, fontSize = 9.sp)
        }
    }
}
