package com.example.myapplication.repository

import android.util.Log
import com.example.myapplication.data.model.NewsArticle

/**
 * Repository quản lý dữ liệu tin tức từ API.
 */
class NewsRepository {
    private val apiService = NewsApiService.create()
    private val TAG = "NewsRepository"

    /**
     * Lấy danh sách tin tức theo danh mục.
     * @param category Danh mục tin tức (e.g., "Công nghệ", "Khoa học").
     * @return Result chứa danh sách bài viết nếu thành công, hoặc lỗi nếu thất bại.
     */
    suspend fun getNewsByCategory(category: String): Result<List<NewsArticle>> {
        val translatedCategory = mapCategory(category)
        return try {
            val response = apiService.getNews(category = translatedCategory)
            Log.d(TAG, "API Response: ${response.results}")
            Result.success(response.results ?: emptyList())
        } catch (e: Exception) {
            Log.e(TAG, "API Error: ${e.message}", e)
            Result.failure(e)
        }
    }

    /**
     * Ánh xạ danh mục tiếng Việt sang định dạng API.
     * @param category Danh mục tiếng Việt.
     * @return Danh mục tương ứng theo định dạng API, mặc định là "top".
     */
    private fun mapCategory(category: String): String {
        val categoryMap = mapOf(
            "Mới nhất" to "top",
            "Công nghệ" to "technology",
            "Khoa học" to "science",
            "Thể thao" to "sports",
            "Giải trí" to "entertainment",
            "Sức khỏe" to "health"
        )
        return categoryMap[category] ?: "top"
    }
}