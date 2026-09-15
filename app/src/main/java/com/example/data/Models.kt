package com.example.data

enum class MessageStatus {
    SENT,
    DELIVERED,
    READ
}

enum class AttachmentType {
    NONE,
    IMAGE,
    FILE,
    VOICE
}

enum class CallType {
    VIDEO,
    VOICE
}

enum class CallDirection {
    INCOMING,
    OUTGOING,
    MISSED
}

enum class CaptionLanguage(val code: String, val displayName: String) {
    ENGLISH("en-US", "English"),
    TAMIL("ta-IN", "Tamil (தமிழ்)")
}

data class User(
    val id: String,
    val name: String,
    val username: String,
    val email: String,
    val avatarEmoji: String = "👤",
    val statusBio: String = "Using TalkText AI with live captions 🎙️",
    val isOnline: Boolean = true
)

data class Contact(
    val id: String,
    val name: String,
    val username: String,
    val avatarEmoji: String,
    val statusBio: String,
    val isOnline: Boolean,
    val lastSeen: String,
    val unreadCount: Int = 0
)

data class Message(
    val id: String,
    val chatId: String,
    val senderId: String,
    val text: String,
    val timestamp: String,
    val isSentByMe: Boolean,
    val status: MessageStatus = MessageStatus.READ,
    val replyToText: String? = null,
    val attachmentType: AttachmentType = AttachmentType.NONE,
    val attachmentName: String? = null,
    val attachmentSize: String? = null,
    val voiceDurationSeconds: Int = 0,
    val isImportant: Boolean = false
)

data class CallRecord(
    val id: String,
    val contactId: String,
    val contactName: String,
    val contactAvatarEmoji: String,
    val type: CallType,
    val direction: CallDirection,
    val timestamp: String,
    val durationSeconds: Int = 0
) {
    val formattedDuration: String
        get() = if (direction == CallDirection.MISSED) "Missed"
        else {
            val mins = durationSeconds / 60
            val secs = durationSeconds % 60
            "${mins}m ${secs}s"
        }
}

data class LiveCaption(
    val id: String,
    val speakerName: String,
    val isRemote: Boolean,
    val text: String,
    val timestamp: String,
    val language: CaptionLanguage = CaptionLanguage.ENGLISH
)

data class NotificationItem(
    val id: String,
    val title: String,
    val description: String,
    val timestamp: String,
    val type: String, // "MESSAGE", "CALL", "AI_SUMMARY"
    val isRead: Boolean = false,
    val targetContactId: String? = null
)

data class AiCallSummary(
    val id: String,
    val callTitle: String,
    val timestamp: String,
    val duration: String,
    val participant: String,
    val overview: String,
    val keyPoints: List<String>,
    val actionItems: List<String>
)
