package com.example.myapplication.repository

import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.util.Log
import com.example.myapplication.ai.AiClassifier
import com.example.myapplication.ai.GeminiService
import com.example.myapplication.data.api.NewsApiService
import com.example.myapplication.data.db.AppDatabase
import com.example.myapplication.data.model.NewsArticle
import com.example.myapplication.data.model.NewsArticleEntity
import com.example.myapplication.data.model.NewsCategory
import com.example.myapplication.data.model.NewsResponse
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.withContext
import retrofit2.HttpException
import java.io.IOException
import java.net.UnknownHostException
import java.text.SimpleDateFormat
import java.util.Locale
import androidx.core.net.toUri

/**
 * Kết quả trả về từ các thao tác lấy tin tức.
 */
sealed class NewsResult {
    data class Success(val articles: List<NewsArticle>, val totalResults: Int) : NewsResult()
    data class Error(val message: String, val code: Int? = null) : NewsResult()
    object Loading : NewsResult()
}

/**
 * Repository quản lý dữ liệu tin tức.
 */
class NewsRepository(private val context: Context) {
    private val apiService = NewsApiService.create()
    private val dao = AppDatabase.getDatabase(context).newsArticleDao()
    private val aiClassifier = AiClassifier(context)
    private val geminiService = GeminiService()
    private val TAG = "NewsRepository"
    private val EXPIRATION_TIME = 6 * 60 * 60 * 1000L // Cache 6 giờ
    private val API_LIMIT = 100 // Số bài viết tối đa mỗi lần gọi API
    private val REQUEST_DELAY = 1000L // Delay 1 giây giữa các yêu cầu API
    private var currentOffset = 0 // Theo dõi offset hiện tại
    private companion object {
        private const val SUCCESS_STATUS = "success"
    }

    /**
     * Lấy 100 bài viết mới nhất từ API khi khởi động ứng dụng.
     */
    suspend fun fetchInitialNews(): NewsResult = withContext(Dispatchers.IO) {
        try {
            val currentTime = System.currentTimeMillis()
            val expirationTime = currentTime - EXPIRATION_TIME

            try {
                dao.deleteExpiredArticles(expirationTime)
                Log.i(TAG, "Đã xóa dữ liệu hết hạn từ Room")
            } catch (e: Exception) {
                Log.e(TAG, "Lỗi khi xóa dữ liệu hết hạn từ Room: ${e.message}", e)
            }

            if (!isNetworkAvailable(context)) {
                Log.e(TAG, "Không có kết nối mạng")
                return@withContext NewsResult.Error(
                    "Không có kết nối mạng, vui lòng kiểm tra Wi-Fi hoặc dữ liệu di động",
                    null
                )
            }

            Log.i(TAG, "Gửi yêu cầu API với offset=0, number=$API_LIMIT")
            val response = apiService.getNews(
                language = "vi",
                category = "",
                text = "tin tức",
                offset = 0,
                number = API_LIMIT,
                sort = "publish-time"
            )
            processApiResponse(response, currentTime, API_LIMIT, true)
        } catch (e: Exception) {
            handleError(e)
        }
    }

    /**
     * Lấy thêm 100 bài viết từ API khi cần.
     */
    suspend fun fetchMoreNews(): NewsResult = withContext(Dispatchers.IO) {
        try {
            if (!isNetworkAvailable(context)) {
                Log.e(TAG, "Không có kết nối mạng")
                return@withContext NewsResult.Error(
                    "Không có kết nối mạng, vui lòng kiểm tra Wi-Fi hoặc dữ liệu di động",
                    null
                )
            }

            delay(REQUEST_DELAY)

            Log.i(TAG, "Gửi yêu cầu API lấy thêm với offset=$currentOffset, number=$API_LIMIT")
            val response = apiService.getNews(
                language = "vi",
                category = "",
                text = "tin tức",
                offset = currentOffset,
                number = API_LIMIT,
                sort = "publish-time"
            )
            processApiResponse(response, System.currentTimeMillis(), API_LIMIT, false)
        } catch (e: Exception) {
            handleError(e)
        }
    }

    /**
     * Lấy bài viết từ API dựa trên từ khóa tìm kiếm.
     */
    suspend fun fetchNewsByKeyword(keyword: String): NewsResult = withContext(Dispatchers.IO) {
        try {
            if (!isNetworkAvailable(context)) {
                Log.e(TAG, "Không có kết nối mạng")
                return@withContext NewsResult.Error(
                    "Không có kết nối mạng, vui lòng kiểm tra Wi-Fi hoặc dữ liệu di động",
                    null
                )
            }

            delay(REQUEST_DELAY)

            Log.i(TAG, "Gửi yêu cầu API với từ khóa: $keyword")
            val response = apiService.getNews(
                language = "vi",
                category = "",
                text = keyword,
                offset = 0,
                number = API_LIMIT,
                sort = "publish-time"
            )
            processApiResponse(response, System.currentTimeMillis(), API_LIMIT, true)
        } catch (e: Exception) {
            handleError(e)
        }
    }

    /**
     * Lấy bài viết đề xuất dựa trên danh mục từ Gemini API.
     */
    suspend fun fetchRecommendedNews(categories: List<String>, offset: Int = 0, limit: Int = 15): NewsResult = withContext(Dispatchers.IO) {
        try {
            if (!isNetworkAvailable(context)) {
                Log.e(TAG, "Không có kết nối mạng")
                return@withContext NewsResult.Error(
                    "Không có kết nối mạng, vui lòng kiểm tra Wi-Fi hoặc dữ liệu di động",
                    null
                )
            }

            delay(REQUEST_DELAY)

            val articles = mutableListOf<NewsArticle>()
            var totalResults = 0
            categories.forEach { category ->
                Log.i(TAG, "Gửi yêu cầu API cho danh mục đề xuất: $category")
                val response = apiService.getNews(
                    language = "vi",
                    category = category,
                    text = null,
                    offset = offset,
                    number = limit / categories.size + 1, // Chia đều số bài viết
                    sort = "publish-time"
                )
                val result = processApiResponse(response, System.currentTimeMillis(), limit, true)
                if (result is NewsResult.Success) {
                    articles.addAll(result.articles)
                    totalResults += result.totalResults
                }
            }

            if (articles.isEmpty()) {
                Log.w(TAG, "Không tìm thấy bài viết đề xuất nào")
                return@withContext NewsResult.Success(emptyList(), 0)
            }

            // Lưu vào Room
            val entities = articles.map { article ->
                val aiCategories = aiClassifier.classifyArticle(article.title, article.content)
                article.toNewsArticleEntity("recommended", System.currentTimeMillis(), aiCategories)
            }
            try {
                dao.insertArticles(entities)
                Log.i(TAG, "Lưu ${entities.size} bài báo đề xuất vào Room")
            } catch (e: Exception) {
                Log.e(TAG, "Lỗi khi lưu bài báo đề xuất vào Room: ${e.message}", e)
            }

            Log.i(TAG, "Lấy được ${articles.size} bài viết đề xuất")
            NewsResult.Success(articles.distinctBy { it.id }.take(limit), totalResults)
        } catch (e: Exception) {
            handleError(e)
        }
    }

    /**
     * Xử lý phản hồi từ API và lưu dữ liệu vào Room.
     */
    private suspend fun processApiResponse(
        response: NewsResponse,
        currentTime: Long,
        limit: Int,
        isInitialFetch: Boolean
    ): NewsResult {
        if (response.status == null || response.status != SUCCESS_STATUS) {
            Log.w(TAG, "API trả về status=${response.status}, không khớp với '$SUCCESS_STATUS'")
            if (response.articles.isEmpty()) {
                return NewsResult.Error(
                    "Phản hồi từ API không hợp lệ: status=${response.status ?: "không có"}, không có bài viết",
                    null
                )
            }
            Log.i(TAG, "Tiếp tục xử lý dữ liệu vì có ${response.articles.size} bài viết")
        }

        val articles = response.articles

        return if (articles.isNotEmpty()) {
            val entities = articles.map { article ->
                val aiCategories = aiClassifier.classifyArticle(article.title, article.content)
                Log.d(TAG, "Phân loại bài viết: title=${article.title}, aiCategories=$aiCategories")
                article.toNewsArticleEntity("general", currentTime, aiCategories)
            }
            try {
                dao.insertArticles(entities)
                Log.i(TAG, "Lưu ${entities.size} bài báo vào Room")
            } catch (e: Exception) {
                Log.e(TAG, "Lỗi khi lưu dữ liệu vào Room: ${e.message}", e)
            }
            if (isInitialFetch) {
                currentOffset = limit
            } else {
                currentOffset += limit
            }
            Log.i(TAG, "Đã tải thêm ${articles.size} bài viết mới")
            NewsResult.Success(articles, response.totalResults)
        } else {
            Log.w(TAG, "Không còn bài viết nào để lấy tại offset=$currentOffset")
            NewsResult.Success(emptyList(), response.totalResults)
        }
    }

    /**
     * Lấy bài viết từ Room theo danh mục AI, hỗ trợ phân trang.
     */
    suspend fun getArticlesByAiCategory(category: NewsCategory?, offset: Int, limit: Int): List<NewsArticle> = withContext(Dispatchers.IO) {
        try {
            val expirationTime = System.currentTimeMillis() - EXPIRATION_TIME
            val articles = if (category == null || category == NewsCategory.LATEST) {
                dao.getAllArticles(expirationTime)
            } else {
                dao.getArticlesByAiCategory(category.apiValue, expirationTime)
            }

            if (articles.isEmpty()) {
                Log.w(TAG, "Không tìm thấy bài viết nào cho danh mục: ${category?.displayName ?: "Tất cả"}")
            } else {
                Log.i(TAG, "Tìm thấy ${articles.size} bài viết cho danh mục: ${category?.displayName ?: "Tất cả"}")
            }

            articles.map { it.toNewsArticle() }
                .sortedByDescending { parsePublishedDate(it.publishedAt) }
                .drop(offset)
                .take(limit)
        } catch (e: Exception) {
            Log.e(TAG, "Lỗi khi lấy bài viết theo danh mục '${category?.displayName ?: "Tất cả"}': ${e.message}", e)
            emptyList()
        }
    }

    /**
     * Lấy bài viết từ Room theo ID.
     */
    suspend fun getArticleById(articleId: String): NewsArticle? = withContext(Dispatchers.IO) {
        try {
            val articleEntity = dao.getArticleById(articleId)
            if (articleEntity != null) {
                Log.i(TAG, "Tìm thấy bài viết với ID: $articleId")
                articleEntity.toNewsArticle()
            } else {
                Log.w(TAG, "Không tìm thấy bài viết với ID: $articleId")
                null
            }
        } catch (e: Exception) {
            Log.e(TAG, "Lỗi khi lấy bài viết với ID '$articleId': ${e.message}", e)
            null
        }
    }

    /**
     * Lọc bài viết dựa trên từ khóa tìm kiếm.
     */
    suspend fun filterArticles(keyword: String): List<NewsArticle> = withContext(Dispatchers.IO) {
        try {
            val expirationTime = System.currentTimeMillis() - EXPIRATION_TIME
            val allArticles = dao.getAllArticles(expirationTime)
            if (allArticles.isEmpty()) {
                Log.w(TAG, "Không có bài viết nào để lọc với từ khóa '$keyword' trong Room.")
                return@withContext emptyList()
            }

            val filteredArticles = allArticles.map { entity ->
                val article = entity.toNewsArticle()
                val relevance = aiClassifier.filterArticle(entity.title, entity.content, keyword)
                article to relevance
            }.filter { it.second >= 50 }
                .sortedByDescending { it.second }
                .map { it.first }

            if (filteredArticles.isEmpty()) {
                Log.w(TAG, "Không tìm thấy bài viết nào phù hợp với từ khóa '$keyword'")
            } else {
                Log.i(TAG, "Tìm thấy ${filteredArticles.size} bài viết phù hợp với từ khóa '$keyword'")
            }
            filteredArticles
        } catch (e: Exception) {
            Log.e(TAG, "Lỗi khi lọc bài viết với từ khóa '$keyword': ${e.message}", e)
            emptyList()
        }
    }

    /**
     * Tính toán độ tin cậy của bài viết sử dụng Gemini API.
     */
    suspend fun calculateArticleReliability(article: NewsArticle): Result<Float> = withContext(Dispatchers.IO) {
        try {
            if (!isNetworkAvailable(context)) {
                Log.e(TAG, "Không có kết nối mạng để tính độ tin cậy")
                return@withContext Result.failure(Exception("Không có kết nối mạng"))
            }

            val prompt = """
                Đánh giá độ tin cậy của bài viết sau theo thang điểm từ 0 đến 100, trả về chỉ số phần trăm (0-100). 
                Hãy xem xét các yếu tố như nguồn bài viết, tính chính xác của thông tin, và cách trình bày. 
                Trả về chỉ một số nguyên (ví dụ: 85) thay vì văn bản chi tiết.
                
                **Tiêu đề**: ${article.title}
                **Nội dung**: ${article.content ?: "Không có nội dung"}
                **Nguồn**: ${article.source ?: "Không rõ nguồn"}
                **Tác giả**: ${article.author ?: "Không rõ tác giả"}
                **Ngày đăng**: ${article.publishedAt ?: "Không rõ ngày"}
            """.trimIndent()

            Log.d(TAG, "Gửi yêu cầu tính độ tin cậy tới Gemini API cho bài viết: ${article.title}")
            val response = geminiService.generateResponse(prompt)
            response.fold(
                onSuccess = { scoreText ->
                    val score = scoreText.trim().toFloatOrNull()?.div(100) ?: return@fold Result.failure(Exception("Phản hồi không hợp lệ từ Gemini"))
                    if (score < 0 || score > 1) {
                        Log.e(TAG, "Điểm số không hợp lệ: $score")
                        return@fold Result.failure(Exception("Điểm số không hợp lệ"))
                    }

                    val entity = dao.getArticleById(article.id)?.copy(reliabilityScore = score)
                    if (entity != null) {
                        dao.insertArticles(listOf(entity))
                        Log.i(TAG, "Cập nhật reliabilityScore=$score cho bài viết ${article.id} trong Room")
                    } else {
                        Log.w(TAG, "Không tìm thấy bài viết ${article.id} trong Room để cập nhật")
                    }

                    Result.success(score)
                },
                onFailure = { error ->
                    Log.e(TAG, "Lỗi khi gọi Gemini API: ${error.message}")
                    Result.failure(error)
                }
            )
        } catch (e: Exception) {
            Log.e(TAG, "Lỗi không xác định khi tính độ tin cậy: ${e.message}", e)
            Result.failure(e)
        }
    }

    /**
     * Lưu bài viết vào danh sách đã lưu trong Room.
     */
    suspend fun saveArticleToRoom(article: NewsArticle): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            val entity = dao.getArticleById(article.id)
            if (entity != null) {
                dao.updateSavedStatus(article.id, true)
                Log.i(TAG, "Cập nhật trạng thái đã lưu cho bài viết ${article.id} trong Room")
                Result.success(Unit)
            } else {
                val aiCategories = aiClassifier.classifyArticle(article.title, article.content)
                val newEntity = article.toNewsArticleEntity("general", System.currentTimeMillis(), aiCategories).copy(isSaved = true)
                dao.insertArticles(listOf(newEntity))
                Log.i(TAG, "Lưu bài viết ${article.id} vào Room với trạng thái đã lưu")
                Result.success(Unit)
            }
        } catch (e: Exception) {
            Log.e(TAG, "Lỗi khi lưu bài viết ${article.id} vào Room: ${e.message}", e)
            Result.failure(e)
        }
    }

    /**
     * Xóa bài viết khỏi danh sách đã lưu trong Room.
     */
    suspend fun removeSavedArticleFromRoom(articleId: String): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            dao.updateSavedStatus(articleId, false)
            Log.i(TAG, "Xóa trạng thái đã lưu cho bài viết $articleId trong Room")
            Result.success(Unit)
        } catch (e: Exception) {
            Log.e(TAG, "Lỗi khi xóa bài viết $articleId khỏi Room: ${e.message}", e)
            Result.failure(e)
        }
    }

    /**
     * Lấy danh sách bài viết đã lưu từ Room.
     */
    suspend fun getSavedArticlesFromRoom(): Result<List<NewsArticle>> = withContext(Dispatchers.IO) {
        try {
            val articles = dao.getSavedArticles().map { it.toNewsArticle() }
            Log.i(TAG, "Lấy ${articles.size} bài viết đã lưu từ Room")
            Result.success(articles)
        } catch (e: Exception) {
            Log.e(TAG, "Lỗi khi lấy bài viết đã lưu từ Room: ${e.message}", e)
            Result.failure(e)
        }
    }

    /**
     * Kiểm tra kết nối mạng.
     */
    private fun isNetworkAvailable(context: Context): Boolean {
        val connectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val network = connectivityManager.activeNetwork ?: return false
        val capabilities = connectivityManager.getNetworkCapabilities(network) ?: return false
        return capabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) ||
                capabilities.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR)
    }

    /**
     * Xử lý lỗi API.
     */
    private fun handleError(e: Exception): NewsResult {
        return when (e) {
            is UnknownHostException -> {
                Log.e(TAG, "Không thể phân giải tên miền: ${e.message}")
                NewsResult.Error("Không thể kết nối đến server, vui lòng kiểm tra kết nối mạng hoặc DNS", null)
            }
            is HttpException -> {
                Log.e(TAG, "Lỗi HTTP: code=${e.code()}, message=${e.message()}")
                val errorBody = e.response()?.errorBody()?.string()
                Log.e(TAG, "Chi tiết lỗi: $errorBody")
                when (e.code()) {
                    401 -> NewsResult.Error("API key không hợp lệ hoặc hết hạn. Vui lòng kiểm tra API key.", e.code())
                    429 -> NewsResult.Error("Quá nhiều yêu cầu, vui lòng thử lại sau", e.code())
                    400 -> NewsResult.Error("Yêu cầu không hợp lệ. Chi tiết: $errorBody", e.code())
                    else -> NewsResult.Error("Lỗi HTTP: ${e.message()}", e.code())
                }
            }
            is IOException -> {
                Log.e(TAG, "Lỗi kết nối mạng: ${e.message}")
                NewsResult.Error("Lỗi kết nối mạng, vui lòng kiểm tra kết nối của bạn", null)
            }
            else -> {
                Log.e(TAG, "Lỗi không xác định: ${e.message}", e)
                NewsResult.Error("Lỗi không xác định: ${e.message ?: "Không xác định"}", null)
            }
        }
    }

    /**
     * Chuyển đổi ngày đăng thành định dạng có thể so sánh.
     */
    private fun parsePublishedDate(date: String?): Long {
        if (date.isNullOrBlank()) return 0L
        return try {
            val format = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
            format.parse(date)?.time ?: 0L
        } catch (_: Exception) {
            0L
        }
    }
}

/**
 * Chuyển NewsArticleEntity thành NewsArticle.
 */
fun NewsArticleEntity.toNewsArticle(): NewsArticle {
    val aiCategories = if (aiCategory.isNotBlank()) {
        aiCategory.split(",").map { it.trim() }.filter { it.isNotBlank() }
    } else {
        emptyList()
    }
    return NewsArticle(
        id = id,
        title = title,
        content = content,
        source = source,
        author = author,
        publishedAt = publishedAt,
        imageUrl = imageUrl,
        category = aiCategories,
        articleUrl = articleUrl,
        keywords = if (keywords.isNullOrBlank()) null else keywords.split(",").filter { it.isNotBlank() },
        language = language,
        aiGeneratedSummary = null,
        reliabilityScore = reliabilityScore
    )
}

/**
 * Chuyển NewsArticle thành NewsArticleEntity.
 */
fun NewsArticle.toNewsArticleEntity(category: String, timestamp: Long, aiCategories: List<String>? = null): NewsArticleEntity {
    val derivedSource = if (articleUrl != null) {
        try {
            val uri = articleUrl.toUri()
            val host = uri.host?.removePrefix("www.")
            host?.split(".")?.firstOrNull()?.replaceFirstChar { if (it.isLowerCase()) it.titlecase() else it.toString() }
                ?: source
        } catch (_: Exception) {
            source
        }
    } else {
        source
    }

    return NewsArticleEntity(
        id = id,
        title = title,
        content = content,
        source = derivedSource,
        author = author,
        publishedAt = publishedAt,
        imageUrl = imageUrl,
        category = category,
        categories = category,
        articleUrl = articleUrl,
        keywords = keywords?.filter { it.isNotBlank() }?.joinToString(","),
        language = language,
        timestamp = timestamp,
        reliabilityScore = reliabilityScore,
        aiCategory = aiCategories?.joinToString(",") ?: ""
    )
}