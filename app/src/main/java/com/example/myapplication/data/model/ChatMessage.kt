package com.example.myapplication.data.model

import java.time.LocalDateTime

data class ChatMessage(
    val id: String,
    val content: String,
    val isUser: Boolean,
    val timestamp: LocalDateTime
)