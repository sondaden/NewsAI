package com.example.myapplication.data.firestore

import android.util.Log
import com.example.myapplication.data.model.NewsArticle
import com.example.myapplication.repository.NewsRepository
import com.example.myapplication.repository.SearchHistoryEntry
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import kotlinx.coroutines.tasks.await
import java.util.*

data class UserPreference(
    val categories: List<String> = emptyList(),
    val isDarkTheme: Boolean = false
)

class FirestoreService(private val newsRepository: NewsRepository) {
    private val db = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()
    private val TAG = "FirestoreService"

    // Lưu bài viết vào danh sách đã lưu
    suspend fun saveArticle(article: NewsArticle): Result<Unit> {
        val userId = auth.currentUser?.uid ?: return Result.failure(Exception("Chưa đăng nhập"))
        return try {
            // Lưu vào Room trước
            val roomResult = newsRepository.saveArticleToRoom(article)
            if (roomResult.isFailure) {
                Log.e(TAG, "Lỗi khi lưu bài viết vào Room: ${roomResult.exceptionOrNull()?.message}")
                return Result.failure(roomResult.exceptionOrNull() ?: Exception("Lỗi khi lưu vào Room"))
            }

            // Lưu vào Firestore
            val snapshot = db.collection("users").document(userId)
                .collection("savedArticles").document(article.id)
                .get()
                .await()
            if (snapshot.exists()) {
                Log.d(TAG, "Bài viết đã tồn tại trong savedArticles: ${article.id}")
                return Result.success(Unit)
            }

            val savedArticle = hashMapOf(
                "article" to hashMapOf(
                    "id" to article.id,
                    "title" to article.title,
                    "content" to article.content,
                    "source" to article.source,
                    "author" to article.author,
                    "publishedAt" to article.publishedAt,
                    "imageUrl" to article.imageUrl,
                    "category" to article.category,
                    "articleUrl" to article.articleUrl,
                    "keywords" to article.keywords,
                    "language" to article.language,
                    "aiGeneratedSummary" to article.aiGeneratedSummary,
                    "reliabilityScore" to article.reliabilityScore
                ),
                "savedAt" to FieldValue.serverTimestamp()
            )
            db.collection("users").document(userId)
                .collection("savedArticles").document(article.id)
                .set(savedArticle)
                .await()
            Log.d(TAG, "Lưu bài viết thành công: ${article.id}, reliabilityScore=${article.reliabilityScore}")
            Result.success(Unit)
        } catch (e: Exception) {
            Log.e(TAG, "Lỗi khi lưu bài viết: ${e.message}")
            Result.failure(e)
        }
    }

    // Xóa bài viết khỏi danh sách đã lưu
    suspend fun removeSavedArticle(articleId: String): Result<Unit> {
        val userId = auth.currentUser?.uid ?: return Result.failure(Exception("Chưa đăng nhập"))
        return try {
            // Xóa khỏi Room
            val roomResult = newsRepository.removeSavedArticleFromRoom(articleId)
            if (roomResult.isFailure) {
                Log.e(TAG, "Lỗi khi xóa bài viết khỏi Room: ${roomResult.exceptionOrNull()?.message}")
                return Result.failure(roomResult.exceptionOrNull() ?: Exception("Lỗi khi xóa khỏi Room"))
            }

            // Xóa khỏi Firestore
            db.collection("users").document(userId)
                .collection("savedArticles").document(articleId)
                .delete()
                .await()
            Log.d(TAG, "Xóa bài viết đã lưu thành công: $articleId")
            Result.success(Unit)
        } catch (e: Exception) {
            Log.e(TAG, "Lỗi khi xóa bài viết đã lưu: ${e.message}")
            Result.failure(e)
        }
    }

    // Lấy danh sách bài viết đã lưu
    suspend fun getSavedArticles(): Result<List<NewsArticle>> {
        val userId = auth.currentUser?.uid
        return try {
            if (userId == null) {
                // Nếu chưa đăng nhập, lấy từ Room
                newsRepository.getSavedArticlesFromRoom()
            } else {
                // Thử lấy từ Firestore
                val snapshot = db.collection("users").document(userId)
                    .collection("savedArticles")
                    .get()
                    .await()
                val articles = snapshot.documents.mapNotNull { doc ->
                    doc.get("article") as? Map<*, *>
                }.mapNotNull { map ->
                    NewsArticle(
                        id = map["id"] as String,
                        title = map["title"] as String,
                        content = map["content"] as? String,
                        source = map["source"] as String,
                        author = map["author"] as? String,
                        publishedAt = map["publishedAt"] as String,
                        imageUrl = map["imageUrl"] as? String,
                        category = (map["category"] as List<*>).map { it.toString() },
                        articleUrl = map["articleUrl"] as? String,
                        keywords = (map["keywords"] as? List<*>)?.map { it.toString() },
                        language = map["language"] as? String,
                        aiGeneratedSummary = map["aiGeneratedSummary"] as? String,
                        reliabilityScore = (map["reliabilityScore"] as? Number)?.toFloat()
                    )
                }
                // Đồng bộ Firestore với Room
                articles.forEach { article ->
                    newsRepository.saveArticleToRoom(article)
                }
                Log.d(TAG, "Lấy danh sách bài viết đã lưu thành công: ${articles.size} bài")
                Result.success(articles)
            }
        } catch (e: Exception) {
            Log.e(TAG, "Lỗi khi lấy bài viết đã lưu từ Firestore: ${e.message}")
            // Fallback về Room nếu Firestore thất bại
            newsRepository.getSavedArticlesFromRoom()
        }
    }

    // Lưu lịch sử đọc
    suspend fun addToReadingHistory(article: NewsArticle): Result<Unit> {
        val userId = auth.currentUser?.uid ?: return Result.failure(Exception("Chưa đăng nhập"))
        return try {
            val snapshot = db.collection("users").document(userId)
                .collection("readingHistory").document(article.id)
                .get()
                .await()
            if (snapshot.exists()) {
                Log.d(TAG, "Bài viết đã tồn tại trong readingHistory: ${article.id}")
                return Result.success(Unit)
            }

            val historyEntry = hashMapOf(
                "article" to hashMapOf(
                    "id" to article.id,
                    "title" to article.title,
                    "content" to article.content,
                    "source" to article.source,
                    "author" to article.author,
                    "publishedAt" to article.publishedAt,
                    "imageUrl" to article.imageUrl,
                    "category" to article.category,
                    "articleUrl" to article.articleUrl,
                    "keywords" to article.keywords,
                    "language" to article.language,
                    "aiGeneratedSummary" to article.aiGeneratedSummary,
                    "reliabilityScore" to article.reliabilityScore
                ),
                "readAt" to FieldValue.serverTimestamp()
            )
            db.collection("users").document(userId)
                .collection("readingHistory").document(article.id)
                .set(historyEntry)
                .await()
            Log.d(TAG, "Thêm vào lịch sử đọc thành công: ${article.id}, reliabilityScore=${article.reliabilityScore}")
            Result.success(Unit)
        } catch (e: Exception) {
            Log.e(TAG, "Lỗi khi thêm vào lịch sử đọc: ${e.message}")
            Result.failure(e)
        }
    }

    // Lấy lịch sử đọc
    suspend fun getReadingHistory(): Result<List<NewsArticle>> {
        val userId = auth.currentUser?.uid ?: return Result.failure(Exception("Chưa đăng nhập"))
        return try {
            val snapshot = db.collection("users").document(userId)
                .collection("readingHistory")
                .orderBy("readAt", com.google.firebase.firestore.Query.Direction.DESCENDING)
                .get()
                .await()
            val articles = snapshot.documents.mapNotNull { doc ->
                doc.get("article") as? Map<*, *>
            }.mapNotNull { map ->
                NewsArticle(
                    id = map["id"] as String,
                    title = map["title"] as String,
                    content = map["content"] as? String,
                    source = map["source"] as String,
                    author = map["author"] as? String,
                    publishedAt = map["publishedAt"] as String,
                    imageUrl = map["imageUrl"] as? String,
                    category = (map["category"] as List<*>).map { it.toString() },
                    articleUrl = map["articleUrl"] as? String,
                    keywords = (map["keywords"] as? List<*>)?.map { it.toString() },
                    language = map["language"] as? String,
                    aiGeneratedSummary = map["aiGeneratedSummary"] as? String,
                    reliabilityScore = (map["reliabilityScore"] as? Number)?.toFloat()
                )
            }.distinctBy { it.id }
            Log.d(TAG, "Lấy lịch sử đọc thành công: ${articles.size} bài")
            Result.success(articles)
        } catch (e: Exception) {
            Log.e(TAG, "Lỗi khi lấy lịch sử đọc: ${e.message}")
            Result.failure(e)
        }
    }

    // Lưu sở thích người dùng
    suspend fun updateUserPreferences(categories: List<String>, isDarkTheme: Boolean = false): Result<Unit> {
        val userId = auth.currentUser?.uid ?: return Result.failure(Exception("Chưa đăng nhập"))
        return try {
            val preferences = hashMapOf(
                "categories" to categories,
                "isDarkTheme" to isDarkTheme,
                "updatedAt" to FieldValue.serverTimestamp()
            )
            db.collection("users").document(userId)
                .collection("preferences").document("userPrefs")
                .set(preferences)
                .await()
            Log.d(TAG, "Cập nhật sở thích thành công: $categories, isDarkTheme=$isDarkTheme")
            Result.success(Unit)
        } catch (e: Exception) {
            Log.e(TAG, "Lỗi khi cập nhật sở thích: ${e.message}")
            Result.failure(e)
        }
    }

    // Lấy sở thích người dùng
    suspend fun getUserPreferences(): Result<UserPreference> {
        val userId = auth.currentUser?.uid ?: return Result.failure(Exception("Chưa đăng nhập"))
        return try {
            val snapshot = db.collection("users").document(userId)
                .collection("preferences").document("userPrefs")
                .get()
                .await()
            if (!snapshot.exists()) {
                val defaultPrefs = hashMapOf(
                    "categories" to emptyList<String>(),
                    "isDarkTheme" to false,
                    "updatedAt" to FieldValue.serverTimestamp()
                )
                db.collection("users").document(userId)
                    .collection("preferences").document("userPrefs")
                    .set(defaultPrefs)
                    .await()
                Log.d(TAG, "Tạo preferences mặc định: isDarkTheme=false")
                Result.success(UserPreference(categories = emptyList(), isDarkTheme = false))
            } else {
                val categories = snapshot.get("categories") as? List<String> ?: emptyList()
                val isDarkTheme = snapshot.getBoolean("isDarkTheme") ?: false
                Log.d(TAG, "Lấy sở thích thành công: $categories, isDarkTheme=$isDarkTheme")
                Result.success(UserPreference(categories, isDarkTheme))
            }
        } catch (e: Exception) {
            Log.e(TAG, "Lỗi khi lấy sở thích: ${e.message}")
            Result.failure(e)
        }
    }

    // Cập nhật trạng thái Dark Mode riêng biệt
    suspend fun updateDarkModePreference(isDarkTheme: Boolean): Result<Unit> {
        val userId = auth.currentUser?.uid ?: return Result.failure(Exception("Chưa đăng nhập"))
        return try {
            val snapshot = db.collection("users").document(userId)
                .collection("preferences").document("userPrefs")
                .get()
                .await()
            val categories = snapshot.get("categories") as? List<String> ?: emptyList()
            val preferences = hashMapOf(
                "categories" to categories,
                "isDarkTheme" to isDarkTheme,
                "updatedAt" to FieldValue.serverTimestamp()
            )
            db.collection("users").document(userId)
                .collection("preferences").document("userPrefs")
                .set(preferences)
                .await()
            Log.d(TAG, "Cập nhật trạng thái Dark Mode thành công: isDarkTheme=$isDarkTheme")
            Result.success(Unit)
        } catch (e: Exception) {
            Log.e(TAG, "Lỗi khi cập nhật trạng thái Dark Mode: ${e.message}")
            Result.failure(e)
        }
    }

    // Lấy sở thích và lịch sử tìm kiếm để đề xuất bài viết
    suspend fun getUserDataForRecommendation(): Result<Pair<List<String>, List<String>>> {
        val userId = auth.currentUser?.uid ?: return Result.failure(Exception("Chưa đăng nhập"))
        return try {
            // Lấy sở thích
            val prefSnapshot = db.collection("users").document(userId)
                .collection("preferences").document("userPrefs")
                .get()
                .await()
            val categories = if (prefSnapshot.exists()) {
                prefSnapshot.get("categories") as? List<String> ?: emptyList()
            } else {
                emptyList()
            }

            // Lấy lịch sử tìm kiếm
            val searchSnapshot = db.collection("users").document(userId)
                .collection("searchHistory")
                .orderBy("timestamp", Query.Direction.DESCENDING)
                .limit(10)
                .get()
                .await()
            val searchHistory = searchSnapshot.documents.mapNotNull { doc ->
                doc.getString("query")
            }

            Log.d(TAG, "Lấy dữ liệu đề xuất thành công: categories=$categories, searchHistory=$searchHistory")
            Result.success(Pair(categories, searchHistory))
        } catch (e: Exception) {
            Log.e(TAG, "Lỗi khi lấy dữ liệu đề xuất: ${e.message}")
            Result.failure(e)
        }
    }
}