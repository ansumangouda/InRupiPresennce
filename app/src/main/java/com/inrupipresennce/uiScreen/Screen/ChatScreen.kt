package com.inrupipresennce.uiScreen.Screen

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Send
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.compose.viewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

// --- Data Model ---
data class ChatMessage(
    val id: String,
    val senderId: String,
    val receiverId: String,
    val message: String,
    val timestamp: Long
)

// --- Repository ---
class ChatRepository {
    private val messages = mutableListOf(
        ChatMessage("1", "user1", "user2", "Hello!", System.currentTimeMillis() - 10000),
        ChatMessage("2", "user2", "user1", "Hi there!", System.currentTimeMillis() - 5000),
        ChatMessage("3", "user1", "user2", "How are you?", System.currentTimeMillis()),
    )

    suspend fun getMessages(senderId: String, receiverId: String): List<ChatMessage> {
        delay(1000) // Simulate network delay
        return messages.filter {
            (it.senderId == senderId && it.receiverId == receiverId) ||
            (it.senderId == receiverId && it.receiverId == senderId)
        }.sortedBy { it.timestamp }
    }

    suspend fun sendMessage(message: ChatMessage) {
        delay(500) // Simulate network delay
        messages.add(message)
    }
}

// --- ViewModel ---
class ChatViewModel(private val repository: ChatRepository) : ViewModel() {

    private val _messages = MutableStateFlow<List<ChatMessage>>(emptyList())
    val messages: StateFlow<List<ChatMessage>> = _messages

    fun getMessages(senderId: String, receiverId: String) {
        viewModelScope.launch {
            _messages.value = repository.getMessages(senderId, receiverId)
        }
    }

    fun sendMessage(senderId: String, receiverId: String, message: String) {
        viewModelScope.launch {
            val newMessage = ChatMessage(
                id = (messages.value.size + 1).toString(),
                senderId = senderId,
                receiverId = receiverId,
                message = message,
                timestamp = System.currentTimeMillis()
            )
            repository.sendMessage(newMessage)
            // Refresh messages after sending
            _messages.value = repository.getMessages(senderId, receiverId)
        }
    }
}

// --- ViewModelFactory ---
class ChatViewModelFactory(private val repository: ChatRepository) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(ChatViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return ChatViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}

// --- UI ---
@Composable
fun ChatScreen(
    chatViewModel: ChatViewModel = viewModel(factory = ChatViewModelFactory(ChatRepository())),
    senderId: String = "user1", // Should be dynamically set based on logged in user
    receiverId: String = "user2" // Should be selected from a user list
) {
    val messages by chatViewModel.messages.collectAsState()
    var newMessage by remember { mutableStateOf("") }

    LaunchedEffect(key1 = senderId, key2 = receiverId) {
        chatViewModel.getMessages(senderId, receiverId)
    }

    Column(modifier = Modifier.fillMaxSize()) {
        LazyColumn(
            modifier = Modifier
                .weight(1f)
                .padding(8.dp),
            reverseLayout = true
        ) {
            items(messages.reversed()) { message ->
                MessageItem(message, senderId)
            }
        }

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            OutlinedTextField(
                value = newMessage,
                onValueChange = { newMessage = it },
                modifier = Modifier.weight(1f),
                placeholder = { Text("Type a message...") }
            )
            Spacer(modifier = Modifier.width(8.dp))
            IconButton(onClick = {
                if (newMessage.isNotBlank()) {
                    chatViewModel.sendMessage(senderId, receiverId, newMessage)
                    newMessage = ""
                }
            }) {
                Icon(Icons.Default.Send, contentDescription = "Send Message")
            }
        }
    }
}

@Composable
fun MessageItem(message: ChatMessage, currentUserId: String) {
    val isSentByCurrentUser = message.senderId == currentUserId
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        horizontalAlignment = if (isSentByCurrentUser) Alignment.End else Alignment.Start
    ) {
        Card(
            colors = CardDefaults.cardColors(
                containerColor = if (isSentByCurrentUser) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.secondaryContainer
            )
        ) {
            Text(
                text = message.message,
                modifier = Modifier.padding(8.dp)
            )
        }
    }
}
