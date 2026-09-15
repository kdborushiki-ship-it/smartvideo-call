package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import com.example.data.Contact
import com.example.data.DemoRepository
import com.example.ui.auth.AuthScreen
import com.example.ui.call.VideoCallScreen
import com.example.ui.chat.ChatScreen
import com.example.ui.dashboard.DashboardScreen
import com.example.ui.theme.TalkTextAITheme

sealed class AppDestination {
    data object Dashboard : AppDestination()
    data class Chat(val contact: Contact) : AppDestination()
    data class VideoCall(val contact: Contact) : AppDestination()
}

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        val repository = DemoRepository.getInstance(applicationContext)

        setContent {
            val isDarkMode by repository.isDarkMode.collectAsState()
            val currentUser by repository.currentUser.collectAsState()

            TalkTextAITheme(darkTheme = isDarkMode) {
                Box(modifier = Modifier.fillMaxSize()) {
                    if (currentUser == null) {
                        AuthScreen(
                            repository = repository,
                            onLoginSuccess = {
                                // Handled automatically by currentUser StateFlow
                            }
                        )
                    } else {
                        MainAppContent(repository = repository)
                    }
                }
            }
        }
    }
}

@Composable
fun MainAppContent(repository: DemoRepository) {
    var destination by remember { mutableStateOf<AppDestination>(AppDestination.Dashboard) }

    when (val dest = destination) {
        is AppDestination.Dashboard -> {
            DashboardScreen(
                repository = repository,
                onSelectContact = { contact ->
                    destination = AppDestination.Chat(contact)
                },
                onStartVideoCall = { contact ->
                    destination = AppDestination.VideoCall(contact)
                },
                onStartVoiceCall = { contact ->
                    // Voice call can also launch with video disabled / audio mode
                    destination = AppDestination.VideoCall(contact)
                },
                onLogout = {
                    // Handled automatically by repository
                }
            )
        }
        is AppDestination.Chat -> {
            BackHandler {
                destination = AppDestination.Dashboard
            }
            ChatScreen(
                contact = dest.contact,
                repository = repository,
                onBack = {
                    destination = AppDestination.Dashboard
                },
                onStartVideoCall = {
                    destination = AppDestination.VideoCall(dest.contact)
                },
                onStartVoiceCall = {
                    destination = AppDestination.VideoCall(dest.contact)
                }
            )
        }
        is AppDestination.VideoCall -> {
            BackHandler {
                destination = AppDestination.Dashboard
            }
            VideoCallScreen(
                contact = dest.contact,
                repository = repository,
                onEndCall = {
                    destination = AppDestination.Dashboard
                }
            )
        }
    }
}

