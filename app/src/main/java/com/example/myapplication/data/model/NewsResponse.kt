package com.example.myapplication.data.model

import com.google.gson.annotations.SerializedName

/**
 * Model đại diện cho phản hồi từ API tin tức.
 */
data class NewsResponse(
    @SerializedName("status") val status: String? = null,
    @SerializedName("news", alternate = ["articles"]) val articles: List<NewsArticle> = emptyList(),
    @SerializedName("totalResults", alternate = ["total_results"]) val totalResults: Int = 0
)