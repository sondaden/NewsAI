package com.example.myapplication.data.model

import com.google.gson.annotations.SerializedName

/**
 * Model đại diện cho một bài báo tin tức từ API.
 */
data class NewsArticle(
    @SerializedName("id") val id: String,
    @SerializedName("title") val title: String,
    @SerializedName(value = "text", alternate = ["content"]) val content: String?,
    @SerializedName(value = "source_country", alternate = ["source", "publisher"]) val source: String?,
    @SerializedName(value = "author", alternate = ["creator"]) val author: String?,
    @SerializedName(value = "publish_date", alternate = ["pubDate", "published_at", "date"]) val publishedAt: String?,
    @SerializedName(value = "image", alternate = ["image_url"]) val imageUrl: String?,
    @SerializedName(value = "category", alternate = ["categories"]) val category: List<String>?,
    @SerializedName(value = "url", alternate = ["link"]) val articleUrl: String?,
    @SerializedName("keywords") val keywords: List<String>?,
    @SerializedName("language") val language: String?,
    @SerializedName("summary") val aiGeneratedSummary: String?,
    val reliabilityScore: Float? = null
) {
    override fun toString(): String {
        return "NewsArticle(id=$id, title=$title, content=$content, source=$source, author=$author, " +
                "publishedAt=$publishedAt, imageUrl=$imageUrl, category=$category, articleUrl=$articleUrl, " +
                "keywords=$keywords, language=$language, aiGeneratedSummary=$aiGeneratedSummary, " +
                "reliabilityScore=$reliabilityScore)"
    }
}