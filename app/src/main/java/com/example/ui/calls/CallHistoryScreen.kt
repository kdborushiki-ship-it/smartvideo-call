package com.example.ui.calls

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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.CallMade
import androidx.compose.material.icons.automirrored.filled.CallMissed
import androidx.compose.material.icons.automirrored.filled.CallReceived
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.PhoneCallback
import androidx.compose.material.icons.filled.Videocam
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.CallDirection
import com.example.data.CallRecord
import com.example.data.CallType
import com.example.data.Contact
import com.example.data.DemoRepository
import com.example.ui.theme.AccentEmerald
import com.example.ui.theme.AccentRose
import com.example.ui.theme.BrandCyan
import com.example.ui.theme.BrandCyanLight
import com.example.ui.theme.DarkBackground
import com.example.ui.theme.DarkBorder
import com.example.ui.theme.DarkSurface
import com.example.ui.theme.DarkSurfaceElevated

@Composable
fun CallHistoryScreen(
    repository: DemoRepository,
    onStartCallWithContact: (Contact, CallType) -> Unit,
    modifier: Modifier = Modifier
) {
    val callRecords by repository.callHistory.collectAsState()
    val contacts by repository.contacts.collectAsState()
    var filterType by remember { mutableStateOf("All") }

    val filteredRecords = remember(callRecords, filterType) {
        when (filterType) {
            "Missed" -> callRecords.filter { it.direction == CallDirection.MISSED }
            "Video" -> callRecords.filter { it.type == CallType.VIDEO }
            else -> callRecords
        }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(DarkBackground)
    ) {
        // Header
        Surface(
            color = DarkSurface,
            shadowElevation = 4.dp,
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp)) {
                Text(
                    text = "Calls & Video Logs",
                    color = Color.White,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(10.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    listOf("All", "Missed", "Video").forEach { f ->
                        FilterChip(
                            selected = filterType == f,
                            onClick = { filterType = f },
                            label = { Text(f) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = BrandCyan.copy(alpha = 0.25f),
                                selectedLabelColor = BrandCyanLight,
                                containerColor = DarkSurfaceElevated,
                                labelColor = Color(0xFF94A3B8)
                            )
                        )
                    }
                }
            }
        }

        if (filteredRecords.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(32.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("📞", fontSize = 48.sp)
                    Spacer(modifier = Modifier.height(10.dp))
                    Text(
                        text = "No call logs in this filter.",
                        color = Color(0xFF94A3B8),
                        fontSize = 14.sp
                    )
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 16.dp, vertical = 10.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                items(filteredRecords, key = { it.id }) { record ->
                    val contact = contacts.find { it.id == record.contactId } ?: Contact(
                        id = record.contactId,
                        name = record.contactName,
                        username = "",
                        avatarEmoji = record.contactAvatarEmoji,
                        statusBio = "",
                        isOnline = true,
                        lastSeen = ""
                    )

                    CallRecordCard(
                        record = record,
                        onCallBack = { onStartCallWithContact(contact, record.type) }
                    )
                }
            }
        }
    }
}

@Composable
private fun CallRecordCard(
    record: CallRecord,
    onCallBack: () -> Unit
) {
    Card(
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = DarkSurfaceElevated),
        border = androidx.compose.foundation.BorderStroke(0.8.dp, DarkBorder),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Avatar
            Box(
                modifier = Modifier
                    .size(46.dp)
                    .clip(CircleShape)
                    .background(Color(0xFF0F172A)),
                contentAlignment = Alignment.Center
            ) {
                Text(text = record.contactAvatarEmoji, fontSize = 24.sp)
            }

            Spacer(modifier = Modifier.width(12.dp))

            // Details
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = record.contactName,
                    color = Color.White,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.SemiBold
                )

                Spacer(modifier = Modifier.height(3.dp))

                Row(verticalAlignment = Alignment.CenterVertically) {
                    val (dirIcon, dirColor) = when (record.direction) {
                        CallDirection.INCOMING -> Pair(Icons.AutoMirrored.Filled.CallReceived, AccentEmerald)
                        CallDirection.OUTGOING -> Pair(Icons.AutoMirrored.Filled.CallMade, BrandCyanLight)
                        CallDirection.MISSED -> Pair(Icons.AutoMirrored.Filled.CallMissed, AccentRose)
                    }

                    Icon(
                        imageVector = dirIcon,
                        contentDescription = null,
                        tint = dirColor,
                        modifier = Modifier.size(14.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "${record.timestamp} • ${record.formattedDuration}",
                        color = Color(0xFF94A3B8),
                        fontSize = 12.sp
                    )
                }
            }

            // Call Back button (Video or Voice)
            IconButton(
                onClick = onCallBack,
                modifier = Modifier
                    .clip(CircleShape)
                    .background(BrandCyan.copy(alpha = 0.15f))
                    .testTag("callback_button")
            ) {
                Icon(
                    imageVector = if (record.type == CallType.VIDEO) Icons.Default.Videocam else Icons.Default.Call,
                    contentDescription = "Call Back",
                    tint = BrandCyanLight,
                    modifier = Modifier.size(20.dp)
                )
            }
        }
    }
}
