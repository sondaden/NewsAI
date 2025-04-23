package com.example.myapplication.data.model

import com.google.gson.annotations.SerializedName

data class NewsArticle(
    @SerializedName("article_id") val id: String, // ID bài viết
    @SerializedName("title") val title: String, // Tiêu đề bài viết
    @SerializedName("content") val content: String?, // Nội dung bài viết
    @SerializedName("source_id") val source: String, // Nguồn bài viết
    @SerializedName("creator") val author: List<String>?, // Tác giả bài viết
    @SerializedName("pubDate") val publishedAt: String, // Ngày xuất bản
    val publishedAtTimestamp: Long? = null, // Thời gian xuất bản dưới dạng timestamp
    @SerializedName("image_url") val imageUrl: String?, // URL hình ảnh
    @SerializedName("category") val category: List<String>, // Danh mục bài viết
    @SerializedName("link") val articleUrl: String? = null, // URL bài viết
    @SerializedName("keywords") val keywords: List<String>? = null, // Từ khóa bài viết
    @SerializedName("language") val language: String? = null, // Ngôn ngữ bài viết

    val aiGeneratedSummary: String? = null, // Tóm tắt bài viết do AI tạo
    val sentiment: String? = null, // Tình cảm bài viết
    val relevanceScore: Float? = null, // Điểm liên quan bài viết
    val reliabilityScore: Float? = null // Điểm độ tin cậy bài viết
)