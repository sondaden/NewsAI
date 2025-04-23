package com.example.myapplication.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

/**
 * ViewModel quản lý cuộc trò chuyện với chatbot.
 */
class ChatViewModel : ViewModel() {
    private val _chatHistory = MutableStateFlow(
        listOf(ChatMessage("Xin chào! Tôi có thể giúp gì cho bạn?", isUser = false))
    )
    val chatHistory: StateFlow<List<ChatMessage>> = _chatHistory.asStateFlow()

    /**
     * Gửi tin nhắn và nhận phản hồi từ chatbot.
     * @param message Tin nhắn của người dùng
     */
    fun sendMessage(message: String) {
        if (message.isBlank()) return

        viewModelScope.launch {
            _chatHistory.value = _chatHistory.value + ChatMessage(message, isUser = true)
            val response = generateBotResponse(message)
            _chatHistory.value = _chatHistory.value + ChatMessage(response, isUser = false)
        }
    }

    // Tạo phản hồi giả lập từ chatbot
    private suspend fun generateBotResponse(userMessage: String): String {
        kotlinx.coroutines.delay(500) // Giả lập thời gian phản hồi
        return when {
            userMessage.contains("news", ignoreCase = true) && userMessage.contains("AI", ignoreCase = true) ->
                "The latest breakthrough in AI technology involves advanced language models that can now understand and generate human-like text with improved accuracy."
            userMessage.contains("hello", ignoreCase = true) || userMessage.contains("hi", ignoreCase = true) ->
                "Hello! How can I assist you today?"
            else -> "I'm sorry, I don't have information on that topic. Can you ask something else?"
        }
    }
}

/**
 * Dữ liệu tin nhắn trong cuộc trò chuyện.
 */
data class ChatMessage(val text: String, val isUser: Boolean)