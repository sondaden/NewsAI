package com.example.myapplication.data.model

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query

/**
 * Data Access Object (DAO) để quản lý bài báo tin tức trong cơ sở dữ liệu Room.
 */
@Dao
interface NewsArticleDao {

    /**
     * Chèn danh sách bài báo vào cơ sở dữ liệu.
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertArticles(articles: List<NewsArticleEntity>)

    /**
     * Lấy tất cả bài báo từ cơ sở dữ liệu.
     */
    @Query("SELECT * FROM news_articles WHERE timestamp > :expirationTime ORDER BY timestamp DESC")
    suspend fun getAllArticles(expirationTime: Long): List<NewsArticleEntity>

    /**
     * Lấy danh sách bài báo theo danh mục AI.
     * Sử dụng LIKE để tìm kiếm vì aiCategory là chuỗi danh mục.
     */
    @Query("SELECT * FROM news_articles WHERE aiCategory LIKE '%' || :aiCategory || '%' AND timestamp > :expirationTime ORDER BY timestamp DESC")
    suspend fun getArticlesByAiCategory(aiCategory: String, expirationTime: Long): List<NewsArticleEntity>

    /**
     * Lấy bài báo theo ID.
     */
    @Query("SELECT * FROM news_articles WHERE id = :articleId LIMIT 1")
    suspend fun getArticleById(articleId: String): NewsArticleEntity?

    /**
     * Xóa các bài báo đã hết hạn.
     */
    @Query("DELETE FROM news_articles WHERE timestamp < :expirationTime")
    suspend fun deleteExpiredArticles(expirationTime: Long)

    /**
     * Lấy danh sách bài viết đã lưu.
     */
    @Query("SELECT * FROM news_articles WHERE isSaved = 1 ORDER BY timestamp DESC")
    suspend fun getSavedArticles(): List<NewsArticleEntity>

    /**
     * Cập nhật trạng thái đã lưu của bài viết.
     */
    @Query("UPDATE news_articles SET isSaved = :isSaved WHERE id = :articleId")
    suspend fun updateSavedStatus(articleId: String, isSaved: Boolean)
}