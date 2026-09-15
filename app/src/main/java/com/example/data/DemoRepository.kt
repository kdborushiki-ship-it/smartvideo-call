package com.example.data

import android.content.Context
import android.content.SharedPreferences
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.UUID

class DemoRepository private constructor(context: Context) {

    private val prefs: SharedPreferences =
        context.getSharedPreferences("talktext_prefs", Context.MODE_PRIVATE)

    // Current User
    private val _currentUser = MutableStateFlow<User?>(loadSavedUser())
    val currentUser: StateFlow<User?> = _currentUser.asStateFlow()

    // Contacts
    private val _contacts = MutableStateFlow<List<Contact>>(initialContacts())
    val contacts: StateFlow<List<Contact>> = _contacts.asStateFlow()

    // Messages per ContactId
    private val _messages = MutableStateFlow<Map<String, List<Message>>>(initialMessages())
    val messages: StateFlow<Map<String, List<Message>>> = _messages.asStateFlow()

    // Call History
    private val _callHistory = MutableStateFlow<List<CallRecord>>(initialCallHistory())
    val callHistory: StateFlow<List<CallRecord>> = _callHistory.asStateFlow()

    // Notifications
    private val _notifications = MutableStateFlow<List<NotificationItem>>(initialNotifications())
    val notifications: StateFlow<List<NotificationItem>> = _notifications.asStateFlow()

    // Dark Mode preference
    private val _isDarkMode = MutableStateFlow(prefs.getBoolean("is_dark_mode", true))
    val isDarkMode: StateFlow<Boolean> = _isDarkMode.asStateFlow()

    // Caption Settings
    private val _captionFontSize = MutableStateFlow(prefs.getInt("caption_font_size", 18))
    val captionFontSize: StateFlow<Int> = _captionFontSize.asStateFlow()

    private val _highReadabilityCaptions = MutableStateFlow(prefs.getBoolean("high_readability", true))
    val highReadabilityCaptions: StateFlow<Boolean> = _highReadabilityCaptions.asStateFlow()

    private val _captionLanguage = MutableStateFlow(
        if (prefs.getString("caption_lang", "en") == "ta") CaptionLanguage.TAMIL else CaptionLanguage.ENGLISH
    )
    val captionLanguage: StateFlow<CaptionLanguage> = _captionLanguage.asStateFlow()

    // Pre-saved AI call summaries
    private val _aiSummaries = MutableStateFlow<List<AiCallSummary>>(initialAiSummaries())
    val aiSummaries: StateFlow<List<AiCallSummary>> = _aiSummaries.asStateFlow()

    private fun loadSavedUser(): User? {
        val isLoggedIn = prefs.getBoolean("is_logged_in", false)
        if (!isLoggedIn) return null
        val email = prefs.getString("user_email", "") ?: ""
        if (email.isBlank()) return null
        val name = prefs.getString("user_name", "User") ?: "User"
        val username = prefs.getString("user_handle", "user") ?: "user"
        val bio = prefs.getString("user_bio", "SmartVideocall • Real-time Speech Transcriber 🚀") ?: "SmartVideocall • Real-time Speech Transcriber 🚀"
        val emoji = prefs.getString("user_emoji", "👤") ?: "👤"
        return User(
            id = prefs.getString("user_id", "user_me") ?: "user_me",
            name = name,
            username = username,
            email = email,
            avatarEmoji = emoji,
            statusBio = bio,
            isOnline = true
        )
    }

    // All available users in SmartVideocall system for evaluation and login
    val allSystemUsers: List<User> = listOf(
        User(
            id = "u_borushiki",
            name = "Borushiki",
            username = "borushiki",
            email = "kdborushiki@gmail.com",
            avatarEmoji = "👑",
            statusBio = "Lead Developer & Primary Admin • SmartVideocall 🚀",
            isOnline = true
        ),
        User(
            id = "u_alex",
            name = "Alex Rivera",
            username = "alex_ai",
            email = "alex.rivera@smartvideocall.ai",
            avatarEmoji = "👨‍💻",
            statusBio = "Neural Speech & AI Video Pipeline Architect ⚡",
            isOnline = true
        ),
        User(
            id = "u_priya",
            name = "Priya Sharma",
            username = "priya_tech",
            email = "priya.sharma@smartvideocall.ai",
            avatarEmoji = "👩‍🔬",
            statusBio = "Bilingual Live Captions Expert (தமிழ் / English) 🎙️",
            isOnline = true
        ),
        User(
            id = "u_david",
            name = "David Chen",
            username = "david_ux",
            email = "david.chen@smartvideocall.ai",
            avatarEmoji = "🎨",
            statusBio = "Glassmorphic UI & Accessibility Specialist",
            isOnline = false
        ),
        User(
            id = "u_vikram",
            name = "Vikram Raman",
            username = "vikram_nlp",
            email = "vikram.raman@smartvideocall.ai",
            avatarEmoji = "🔬",
            statusBio = "Real-time speech-to-text accuracy researcher",
            isOnline = true
        ),
        User(
            id = "u_sarah",
            name = "Sarah Connor",
            username = "sarah_c",
            email = "sarah.connor@smartvideocall.ai",
            avatarEmoji = "🚀",
            statusBio = "60fps WebRTC video & low-latency streaming",
            isOnline = false
        ),
        User(
            id = "u_ananya",
            name = "Ananya Patel",
            username = "ananya_p",
            email = "ananya.patel@smartvideocall.ai",
            avatarEmoji = "📱",
            statusBio = "SmartVideocall Mobile Core • In-call text messaging",
            isOnline = true
        ),
        User(
            id = "u_marcus",
            name = "Marcus Vance",
            username = "marcus_audio",
            email = "marcus.vance@smartvideocall.ai",
            avatarEmoji = "🎙️",
            statusBio = "Noise cancellation & audio RMS equalizer engineer",
            isOnline = true
        )
    )

    fun login(username: String, email: String, name: String) {
        loginWithEmail(email = email, name = name, username = username)
    }

    fun loginAsUser(user: User) {
        _currentUser.value = user
        prefs.edit()
            .putBoolean("is_logged_in", true)
            .putString("user_id", user.id)
            .putString("user_name", user.name)
            .putString("user_handle", user.username)
            .putString("user_email", user.email)
            .putString("user_emoji", user.avatarEmoji)
            .putString("user_bio", user.statusBio)
            .apply()
        refreshContactsForCurrentUser(user)
    }

    fun loginWithEmail(email: String, name: String? = null, username: String? = null, avatarEmoji: String = "👤") {
        val cleanEmail = email.trim()
        val match = allSystemUsers.firstOrNull { it.email.equals(cleanEmail, ignoreCase = true) }
        if (match != null) {
            loginAsUser(match)
            return
        }

        val derivedUsername = if (!username.isNullOrBlank()) username else cleanEmail.substringBefore("@").lowercase().ifBlank { "user" }
        val derivedName = if (!name.isNullOrBlank()) name else derivedUsername.replaceFirstChar { it.uppercase() }
        val user = User(
            id = "u_" + UUID.randomUUID().toString().take(6),
            name = derivedName,
            username = derivedUsername,
            email = cleanEmail,
            avatarEmoji = avatarEmoji,
            statusBio = "Using SmartVideocall with live text captions 🎙️"
        )
        loginAsUser(user)
    }

    private fun refreshContactsForCurrentUser(user: User) {
        val list = allSystemUsers
            .filter { it.email.lowercase() != user.email.lowercase() }
            .map { u ->
                Contact(
                    id = u.id,
                    name = u.name,
                    username = u.username,
                    avatarEmoji = u.avatarEmoji,
                    statusBio = u.statusBio,
                    isOnline = u.isOnline,
                    lastSeen = if (u.isOnline) "Online" else "Recently",
                    unreadCount = if (u.id == "u_alex" || u.id == "u_vikram") 1 else 0
                )
            }
        _contacts.value = list
    }

    fun logout() {
        _currentUser.value = null
        prefs.edit().putBoolean("is_logged_in", false).apply()
    }

    fun updateProfile(name: String, bio: String, avatar: String) {
        val cur = _currentUser.value ?: return
        val updated = cur.copy(name = name, statusBio = bio, avatarEmoji = avatar)
        _currentUser.value = updated
        prefs.edit()
            .putString("user_name", name)
            .putString("user_bio", bio)
            .apply()
    }

    fun setDarkMode(dark: Boolean) {
        _isDarkMode.value = dark
        prefs.edit().putBoolean("is_dark_mode", dark).apply()
    }

    fun setCaptionFontSize(sizeSp: Int) {
        _captionFontSize.value = sizeSp
        prefs.edit().putInt("caption_font_size", sizeSp).apply()
    }

    fun setHighReadability(enabled: Boolean) {
        _highReadabilityCaptions.value = enabled
        prefs.edit().putBoolean("high_readability", enabled).apply()
    }

    fun setCaptionLanguage(lang: CaptionLanguage) {
        _captionLanguage.value = lang
        prefs.edit().putString("caption_lang", if (lang == CaptionLanguage.TAMIL) "ta" else "en").apply()
    }

    fun sendMessage(
        contactId: String,
        text: String,
        replyToText: String? = null,
        attachmentType: AttachmentType = AttachmentType.NONE,
        attachmentName: String? = null,
        voiceDurationSeconds: Int = 0
    ) {
        val timeNow = SimpleDateFormat("h:mm a", Locale.getDefault()).format(Date())
        val isImportant = text.contains("deadline", ignoreCase = true) ||
                text.contains("meeting", ignoreCase = true) ||
                text.contains("urgent", ignoreCase = true) ||
                text.contains("demo", ignoreCase = true) ||
                text.contains("review", ignoreCase = true)

        val newMsg = Message(
            id = UUID.randomUUID().toString(),
            chatId = contactId,
            senderId = "user_me",
            text = text,
            timestamp = timeNow,
            isSentByMe = true,
            status = MessageStatus.DELIVERED,
            replyToText = replyToText,
            attachmentType = attachmentType,
            attachmentName = attachmentName,
            voiceDurationSeconds = voiceDurationSeconds,
            isImportant = isImportant
        )

        val currentMap = _messages.value.toMutableMap()
        val list = (currentMap[contactId] ?: emptyList()).toMutableList()
        list.add(newMsg)
        currentMap[contactId] = list
        _messages.value = currentMap
    }

    fun deleteMessage(contactId: String, messageId: String) {
        val currentMap = _messages.value.toMutableMap()
        val list = currentMap[contactId]?.filter { it.id != messageId } ?: emptyList()
        currentMap[contactId] = list
        _messages.value = currentMap
    }

    fun clearChat(contactId: String) {
        val currentMap = _messages.value.toMutableMap()
        currentMap[contactId] = emptyList()
        _messages.value = currentMap
    }

    fun addCallRecord(contact: Contact, type: CallType, durationSeconds: Int) {
        val timeNow = SimpleDateFormat("h:mm a, MMM d", Locale.getDefault()).format(Date())
        val record = CallRecord(
            id = UUID.randomUUID().toString(),
            contactId = contact.id,
            contactName = contact.name,
            contactAvatarEmoji = contact.avatarEmoji,
            type = type,
            direction = CallDirection.OUTGOING,
            timestamp = timeNow,
            durationSeconds = durationSeconds
        )
        val list = _callHistory.value.toMutableList()
        list.add(0, record)
        _callHistory.value = list
    }

    fun addAiSummary(summary: AiCallSummary) {
        val list = _aiSummaries.value.toMutableList()
        list.add(0, summary)
        _aiSummaries.value = list

        // Add a notification
        val notif = NotificationItem(
            id = UUID.randomUUID().toString(),
            title = "AI Call Summary Ready",
            description = "Summary generated for call with ${summary.participant}",
            timestamp = "Just now",
            type = "AI_SUMMARY",
            targetContactId = null
        )
        val nList = _notifications.value.toMutableList()
        nList.add(0, notif)
        _notifications.value = nList
    }

    fun markNotificationsRead() {
        _notifications.value = _notifications.value.map { it.copy(isRead = true) }
    }

    fun clearNotifications() {
        _notifications.value = emptyList()
    }

    // AI Helper methods
    fun summarizeChat(contactName: String, messages: List<Message>): String {
        if (messages.isEmpty()) return "No conversation data available to summarize."
        val recent = messages.takeLast(10)
        val topics = recent.joinToString("\n") { (if (it.isSentByMe) "You" else contactName) + ": " + it.text }
        return """
            ### 🤖 AI Conversation Summary: $contactName
            
            **Key Discussion Points:**
            • Reviewed project architecture and real-time live caption integration.
            • Verified latency benchmarks (<50ms) for Speech-to-Text streaming.
            • Coordinated demo preparations for the upcoming college project expo presentation.
            
            **Action Items:**
            ✅ Present live SpeechRecognition capability during the video call demo.
            ✅ Highlight Tamil & English bilingual caption capabilities.
            ✅ Review UI responsive layout on Android screen.
        """.trimIndent()
    }

    fun rewriteMessage(input: String, tone: String): String {
        return when (tone.lowercase()) {
            "polite" -> "Could you kindly look into this when you have a free moment? $input. Appreciate your help!"
            "executive" -> "High priority: $input. Please review and confirm deliverables by EOD."
            "casual" -> "Hey! Just wanted to share: $input ✨ Let's catch up soon!"
            "concise" -> input.split(".").firstOrNull() ?: input
            else -> "✨ $input (Polished with TalkText AI)"
        }
    }

    fun translateMessage(text: String, targetLanguage: String): String {
        // High quality demonstration translations for common demo sentences
        if (targetLanguage.contains("Tamil", ignoreCase = true)) {
            return when {
                text.contains("hello", ignoreCase = true) || text.contains("hi", ignoreCase = true) ->
                    "வணக்கம், நீங்கள் எப்படி இருக்கிறீர்கள்? (Vanakkam, epdi irukeenga?)"
                text.contains("ready", ignoreCase = true) ->
                    "ஆமாம், டெமோ மற்றும் நேரடி தலைப்புகள் முற்றிலும் தயாராக உள்ளன!"
                text.contains("video", ignoreCase = true) || text.contains("call", ignoreCase = true) ->
                    "நேரடி உரைபெயர்ப்புடன் கூடிய வீடியோ அழைப்பைத் தொடங்குங்கள்."
                else -> "வணக்கம்! \"$text\" — (Tamil Translation via TalkText AI)"
            }
        }
        if (targetLanguage.contains("Spanish", ignoreCase = true)) {
            return "¡Hola! \"$text\" — (Traducción en tiempo real)"
        }
        if (targetLanguage.contains("Hindi", ignoreCase = true)) {
            return "नमस्ते! \"$text\" — (टॉकटेक्स्ट एआई अनुवाद)"
        }
        return "[$targetLanguage] $text"
    }

    init {
        _currentUser.value?.let { refreshContactsForCurrentUser(it) }
    }

    // Returns all contacts available for video call in text
    fun getAllCallableUsers(): List<Contact> {
        val myEmail = _currentUser.value?.email?.lowercase() ?: ""
        return allSystemUsers
            .filter { it.email.lowercase() != myEmail }
            .map { u ->
                Contact(
                    id = u.id,
                    name = u.name,
                    username = u.username,
                    avatarEmoji = u.avatarEmoji,
                    statusBio = u.statusBio,
                    isOnline = u.isOnline,
                    lastSeen = if (u.isOnline) "Online" else "Recently",
                    unreadCount = 0
                )
            }
    }

    // Initial Mock Data
    private fun initialContacts(): List<Contact> = listOf(
        Contact(
            id = "u_alex",
            name = "Alex Rivera",
            username = "alex_ai",
            avatarEmoji = "👨‍💻",
            statusBio = "Neural Speech & AI Video Pipeline Architect ⚡",
            isOnline = true,
            lastSeen = "Online",
            unreadCount = 2
        ),
        Contact(
            id = "u_priya",
            name = "Priya Sharma",
            username = "priya_tech",
            avatarEmoji = "👩‍🔬",
            statusBio = "SmartVideocall project co-presenter • Tamil/English",
            isOnline = true,
            lastSeen = "Active 5m ago",
            unreadCount = 0
        ),
        Contact(
            id = "u_david",
            name = "David Chen",
            username = "david_ux",
            avatarEmoji = "🎨",
            statusBio = "Glassmorphic UI & accessibility enthusiast",
            isOnline = false,
            lastSeen = "Yesterday",
            unreadCount = 0
        ),
        Contact(
            id = "u_vikram",
            name = "Vikram Raman",
            username = "vikram_nlp",
            avatarEmoji = "🔬",
            statusBio = "Speech recognition & multilingual models (தமிழ்)",
            isOnline = true,
            lastSeen = "Online",
            unreadCount = 1
        ),
        Contact(
            id = "u_sarah",
            name = "Sarah Connor",
            username = "sarah_c",
            avatarEmoji = "🚀",
            statusBio = "Testing edge-to-edge video pipeline",
            isOnline = false,
            lastSeen = "3 hours ago",
            unreadCount = 0
        ),
        Contact(
            id = "u_ananya",
            name = "Ananya Patel",
            username = "ananya_p",
            avatarEmoji = "📱",
            statusBio = "SmartVideocall Mobile Core • In-call text messaging",
            isOnline = true,
            lastSeen = "Online",
            unreadCount = 0
        ),
        Contact(
            id = "u_marcus",
            name = "Marcus Vance",
            username = "marcus_audio",
            avatarEmoji = "🎙️",
            statusBio = "Audio DSP & Equalizer Lead",
            isOnline = true,
            lastSeen = "Active now",
            unreadCount = 0
        )
    )

    private fun initialMessages(): Map<String, List<Message>> = mapOf(
        "c1" to listOf(
            Message(
                id = "m1_1",
                chatId = "c1",
                senderId = "c1",
                text = "Hey! Have you tested the live speech captions feature during the video call?",
                timestamp = "10:14 AM",
                isSentByMe = false,
                status = MessageStatus.READ
            ),
            Message(
                id = "m1_2",
                chatId = "c1",
                senderId = "user_me",
                text = "Yes! The live captions render with ultra-low latency directly under the video stream.",
                timestamp = "10:15 AM",
                isSentByMe = true,
                status = MessageStatus.READ
            ),
            Message(
                id = "m1_3",
                chatId = "c1",
                senderId = "c1",
                text = "That's fantastic. Let's start a test video call right now so you can see how it handles natural speech!",
                timestamp = "10:16 AM",
                isSentByMe = false,
                status = MessageStatus.DELIVERED,
                isImportant = true
            )
        ),
        "c2" to listOf(
            Message(
                id = "m2_1",
                chatId = "c2",
                senderId = "c2",
                text = "Vanakkam! I've loaded the Tamil speech recognition benchmark. Ready for the expo demo! 🎙️",
                timestamp = "9:30 AM",
                isSentByMe = false,
                status = MessageStatus.READ
            ),
            Message(
                id = "m2_2",
                chatId = "c2",
                senderId = "user_me",
                text = "Awesome Priya! The jury will love how TalkText AI seamlessly switches between English and Tamil captions.",
                timestamp = "9:32 AM",
                isSentByMe = true,
                status = MessageStatus.READ
            )
        ),
        "c4" to listOf(
            Message(
                id = "m4_1",
                chatId = "c4",
                senderId = "c4",
                text = "Important note: Please review the meeting deadline for final slide submission at 4 PM.",
                timestamp = "Yesterday",
                isSentByMe = false,
                status = MessageStatus.READ,
                isImportant = true
            )
        )
    )

    private fun initialCallHistory(): List<CallRecord> = listOf(
        CallRecord(
            id = "call_1",
            contactId = "c1",
            contactName = "Alex Rivera",
            contactAvatarEmoji = "👨‍💻",
            type = CallType.VIDEO,
            direction = CallDirection.INCOMING,
            timestamp = "Today, 10:12 AM",
            durationSeconds = 245
        ),
        CallRecord(
            id = "call_2",
            contactId = "c2",
            contactName = "Priya Sharma",
            contactAvatarEmoji = "👩‍🔬",
            type = CallType.VIDEO,
            direction = CallDirection.OUTGOING,
            timestamp = "Yesterday, 3:40 PM",
            durationSeconds = 480
        ),
        CallRecord(
            id = "call_3",
            contactId = "c4",
            contactName = "Vikram Raman",
            contactAvatarEmoji = "🔬",
            type = CallType.VOICE,
            direction = CallDirection.MISSED,
            timestamp = "Sep 12, 11:20 AM",
            durationSeconds = 0
        )
    )

    private fun initialNotifications(): List<NotificationItem> = listOf(
        NotificationItem(
            id = "n1",
            title = "New Message from Alex Rivera",
            description = "Let's start a test video call right now...",
            timestamp = "5m ago",
            type = "MESSAGE",
            targetContactId = "c1"
        ),
        NotificationItem(
            id = "n2",
            title = "Missed Call from Vikram Raman",
            description = "Voice call missed at 11:20 AM",
            timestamp = "Yesterday",
            type = "CALL",
            targetContactId = "c4"
        ),
        NotificationItem(
            id = "n3",
            title = "AI Meeting Summary Generated",
            description = "TalkText AI has compiled notes from your 8-minute call with Priya Sharma.",
            timestamp = "Yesterday",
            type = "AI_SUMMARY"
        )
    )

    private fun initialAiSummaries(): List<AiCallSummary> = listOf(
        AiCallSummary(
            id = "sum_1",
            callTitle = "TalkText AI Architecture Sync",
            timestamp = "Yesterday, 3:45 PM",
            duration = "8m 00s",
            participant = "Priya Sharma",
            overview = "Discussed the real-time Speech-to-Text captioning pipeline, front/back camera toggling, and multilingual transcript storage.",
            keyPoints = listOf(
                "Speech recognizer latency averages ~45ms on Android devices.",
                "High readability mode ensures captions remain legible over high-contrast camera feeds.",
                "Exportable call transcripts enable post-call AI summarization and action item tracking."
            ),
            actionItems = listOf(
                "Confirm speech permission flow gracefully handles fallback on emulator.",
                "Test Tamil speech recognition (ta-IN) in noisy environment.",
                "Finalize college project presentation deck."
            )
        )
    )

    companion object {
        @Volatile
        private var INSTANCE: DemoRepository? = null

        fun getInstance(context: Context): DemoRepository {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: DemoRepository(context.applicationContext).also { INSTANCE = it }
            }
        }
    }
}
