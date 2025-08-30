package com.example.myapplication.viewmodel

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.myapplication.ai.GeminiService
import com.example.myapplication.data.model.ChatMessage
import com.example.myapplication.repository.NewsRepository
import com.example.myapplication.repository.NewsResult
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.time.LocalDateTime
import java.util.UUID

class ChatViewModel(
    private val newsRepository: NewsRepository,
    private val geminiService: GeminiService,
    private val context: Context
) : ViewModel() {

    private val _messages = MutableStateFlow<List<ChatMessage>>(emptyList())
    val messages: StateFlow<List<ChatMessage>> = _messages.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    // Lịch sử các cuộc trò chuyện
    private val _chatHistory = MutableStateFlow<List<List<ChatMessage>>>(emptyList())
    val chatHistory: StateFlow<List<List<ChatMessage>>> = _chatHistory.asStateFlow()

    fun sendMessage(userMessage: String) {
        if (userMessage.isBlank()) return

        // Thêm tin nhắn của người dùng
        val userMsg = ChatMessage(
            id = UUID.randomUUID().toString(),
            content = userMessage,
            isUser = true,
            timestamp = LocalDateTime.now()
        )
        _messages.value = _messages.value + userMsg
        _isLoading.value = true

        viewModelScope.launch {
            // Tìm bài viết liên quan trong Room
            val articles = newsRepository.filterArticles(userMessage)
            val context = if (articles.isNotEmpty()) {
                articles.joinToString("\n") { "${it.title}: ${it.content ?: ""}" }
            } else {
                // Nếu không tìm thấy trong Room, gọi WorldNewsAPI
                val apiResult = newsRepository.fetchNewsByKeyword(userMessage)
                when (apiResult) {
                    is NewsResult.Success -> {
                        if (apiResult.articles.isNotEmpty()) {
                            apiResult.articles.joinToString("\n") { "${it.title}: ${it.content ?: ""}" }
                        } else {
                            ""
                        }
                    }
                    is NewsResult.Error -> {
                        "" // Nếu có lỗi, trả về ngữ cảnh rỗng
                    }
                    is NewsResult.Loading -> {
                        "" // Không xử lý trạng thái Loading ở đây
                    }
                }
            }

            // Gọi Gemini API để tạo câu trả lời
            val geminiResult = geminiService.generateResponse(userMessage, context)
            _isLoading.value = false

            val botMessage = when {
                geminiResult.isSuccess -> geminiResult.getOrNull() ?: "Không nhận được phản hồi."
                else -> "Xin lỗi, tôi không thể trả lời ngay bây giờ. Vui lòng thử lại sau."
            }

            // Thêm tin nhắn của chatbot
            val botMsg = ChatMessage(
                id = UUID.randomUUID().toString(),
                content = botMessage,
                isUser = false,
                timestamp = LocalDateTime.now()
            )
            _messages.value = _messages.value + botMsg

            // Lưu cuộc trò chuyện hiện tại vào lịch sử
            saveCurrentChat()
        }
    }

    fun addBotMessage(messageContent: String) {
        val botMsg = ChatMessage(
            id = UUID.randomUUID().toString(),
            content = messageContent,
            isUser = false,
            timestamp = LocalDateTime.now()
        )
        _messages.value = _messages.value + botMsg
        saveCurrentChat()
    }

    // Lưu cuộc trò chuyện hiện tại vào lịch sử
    private fun saveCurrentChat() {
        val currentChat = _messages.value.toList()
        if (currentChat.isNotEmpty()) {
            _chatHistory.value = listOf(currentChat) + _chatHistory.value
        }
    }

    // Reset cuộc trò chuyện hiện tại
    fun resetChat() {
        _messages.value = emptyList()
        addBotMessage("Xin chào! Tôi là Chatbot Tin tức, sẵn sàng trả lời mọi câu hỏi về tin tức của bạn. Hãy hỏi tôi bất cứ điều gì!")
    }

    // Lấy và hiển thị cuộc trò chuyện cuối cùng
    fun loadLastChat() {
        val lastChat = _chatHistory.value.firstOrNull()
        if (lastChat != null) {
            _messages.value = lastChat
        } else {
            resetChat()
        }
    }

    // Lấy một cuộc trò chuyện cụ thể từ lịch sử
    fun loadChat(index: Int) {
        val chat = _chatHistory.value.getOrNull(index)
        if (chat != null) {
            _messages.value = chat
        }
    }
}