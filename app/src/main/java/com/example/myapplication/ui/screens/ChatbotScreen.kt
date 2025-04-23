package com.example.myapplication.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.myapplication.ui.components.AppScaffold
import com.example.myapplication.ui.components.BottomNavigationBar
import com.example.myapplication.ui.components.TopBar
import com.example.myapplication.viewmodel.ChatMessage
import com.example.myapplication.viewmodel.ChatViewModel
import kotlinx.coroutines.launch

@Composable
fun ChatbotScreen(navController: NavController, chatViewModel: ChatViewModel) {
    val chatHistory by chatViewModel.chatHistory.collectAsState()
    var userInput by remember { mutableStateOf(TextFieldValue("")) }
    val listState = rememberLazyListState()
    val scope = rememberCoroutineScope()

    LaunchedEffect(chatHistory) {
        if (chatHistory.isNotEmpty()) listState.animateScrollToItem(chatHistory.size - 1)
    }

    AppScaffold(
        topBar = { TopBar(titleContent = { Text("Chatbot", style = MaterialTheme.typography.titleLarge) }) },
        bottomBar = {
            BottomNavigationBar(
                selectedItem = "Chatbot",
                onItemSelected = { route -> navController.navigate(route.lowercase()) }
            )
        }
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            LazyColumn(
                state = listState,
                modifier = Modifier.weight(1f).padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(chatHistory) { message ->
                    ChatBubble(message)
                }
            }
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                TextField(
                    value = userInput,
                    onValueChange = { userInput = it },
                    modifier = Modifier.weight(1f),
                    placeholder = { Text("Nhập tin nhắn...") },
                    trailingIcon = {
                        if (userInput.text.isBlank()) {
                            IconButton(onClick = { /* TODO: Micro */ }) {
                                Icon(Icons.Default.Mic, contentDescription = "Micro")
                            }
                        } else {
                            IconButton(onClick = {
                                chatViewModel.sendMessage(userInput.text)
                                userInput = TextFieldValue("")
                                scope.launch { listState.animateScrollToItem(chatHistory.size) }
                            }) {
                                Icon(Icons.AutoMirrored.Filled.Send, contentDescription = "Gửi")
                            }
                        }
                    }
                )
            }
        }
    }
}

@Composable
fun ChatBubble(message: ChatMessage) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = if (message.isUser) Arrangement.End else Arrangement.Start
    ) {
        Card(
            modifier = Modifier
                .widthIn(max = 280.dp)
                .padding(4.dp),
            shape = RoundedCornerShape(12.dp),
            colors = CardDefaults.cardColors(
                containerColor = if (message.isUser) MaterialTheme.colorScheme.primary
                else MaterialTheme.colorScheme.surface
            )
        ) {
            Text(
                text = message.text,
                modifier = Modifier.padding(12.dp),
                style = MaterialTheme.typography.bodyMedium,
                color = if (message.isUser) MaterialTheme.colorScheme.onPrimary
                else MaterialTheme.colorScheme.onSurface
            )
        }
    }
}