package com.example.myapplication.data.model

import java.io.Serializable

data class UserProfile(
    val uid: String = "", // ID người dùng từ Firebase Auth
    val displayName: String = "",
    val email: String = "",
    val avatarUrl: String? = null,
    val readingHistory: List<String> = emptyList(), // Danh sách ID bài viết đã đọc
    val savedArticles: List<String> = emptyList(), // Danh sách ID bài viết đã lưu
    val preferences: List<String> = emptyList(), // Danh sách danh mục yêu thích
    val settings: UserSettings = UserSettings()
) : Serializable

data class UserSettings(
    val isDarkTheme: Boolean = false,
    val language: String = "vi", // Mặc định là tiếng Việt
    val notificationsEnabled: Boolean = true
) : Serializable