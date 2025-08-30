package com.example.myapplication.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Entity đại diện cho một bài báo tin tức trong cơ sở dữ liệu Room.
 */
@Entity(tableName = "news_articles")
data class NewsArticleEntity(
    @PrimaryKey val id: String,
    val title: String,
    val content: String?,
    val source: String?,
    val author: String?,
    val publishedAt: String?,
    val imageUrl: String?,
    val category: String, // Danh mục từ API
    val categories: String,
    val articleUrl: String?,
    val keywords: String?,
    val language: String?,
    val timestamp: Long,
    val reliabilityScore: Float? = null,
    val aiCategory: String = "", // Danh sách danh mục do AI phân loại, lưu dưới dạng chuỗi phân tách bởi dấu phẩy
    val isSaved: Boolean = false // Cột mới để đánh dấu bài viết đã lưu
)