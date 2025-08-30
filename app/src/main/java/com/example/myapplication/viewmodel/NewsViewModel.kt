package com.example.myapplication.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.myapplication.data.model.NewsArticle
import com.example.myapplication.data.model.NewsCategory
import com.example.myapplication.repository.NewsRepository
import com.example.myapplication.repository.NewsResult
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import android.util.Log
import com.example.myapplication.data.firestore.FirestoreService
import com.example.myapplication.ai.GeminiService

/**
 * ViewModel quản lý trạng thái tin tức.
 */
class NewsViewModel(
  private val repository: NewsRepository,
  private val firestoreService: FirestoreService = FirestoreService(repository),
  private val geminiService: GeminiService = GeminiService()
) : ViewModel() {
  private val _newsStates = mutableMapOf<String, MutableStateFlow<NewsResult>>()
  private val _articleStates = mutableMapOf<String, MutableStateFlow<NewsArticle?>>()
  private val _isFetchingMore = mutableMapOf<String, MutableStateFlow<Boolean>>()
  private val _isCalculatingReliability = MutableStateFlow(false)
  val isCalculatingReliability: StateFlow<Boolean> = _isCalculatingReliability
  private val displayedArticleCounts = mutableMapOf<String, Int>()
  private val totalArticleCounts = mutableMapOf<String, Int>()
  private val ARTICLES_PER_PAGE = 15
  private val TAG = "NewsViewModel"

  fun getNewsState(category: NewsCategory?): StateFlow<NewsResult> {
    val key = category?.displayName ?: "Tất cả"
    return _newsStates.getOrPut(key) { MutableStateFlow(NewsResult.Loading) }
  }

  fun getFetchingMoreState(category: NewsCategory?): StateFlow<Boolean> {
    val key = category?.displayName ?: "Tất cả"
    return _isFetchingMore.getOrPut(key) { MutableStateFlow(false) }
  }

  fun getArticleState(articleId: String): StateFlow<NewsArticle?> {
    return _articleStates.getOrPut(articleId) { MutableStateFlow(null) }
  }

  private fun updateNewsState(category: NewsCategory?, result: NewsResult) {
    val key = category?.displayName ?: "Tất cả"
    val stateFlow = _newsStates.getOrPut(key) { MutableStateFlow(NewsResult.Loading) }
    stateFlow.value = result
    Log.d(TAG, "Cập nhật trạng thái danh mục '$key': ${result.javaClass.simpleName}")
  }

  private fun updateFetchingMoreState(category: NewsCategory?, isFetching: Boolean) {
    val key = category?.displayName ?: "Tất cả"
    val stateFlow = _isFetchingMore.getOrPut(key) { MutableStateFlow(false) }
    stateFlow.value = isFetching
    Log.d(TAG, "Cập nhật trạng thái fetch more cho danh mục '$key': $isFetching")
  }

  internal fun updateArticleState(articleId: String, article: NewsArticle?) {
    val stateFlow = _articleStates.getOrPut(articleId) { MutableStateFlow(null) }
    stateFlow.value = article
    Log.d(TAG, "Cập nhật trạng thái bài viết ID '$articleId': ${article?.title ?: "null"}")
  }

  init {
    fetchInitialNews()
  }

  private fun fetchInitialNews() {
    viewModelScope.launch {
      updateNewsState(NewsCategory.LATEST, NewsResult.Loading)
      Log.d(TAG, "Bắt đầu tải tin tức ban đầu cho danh mục 'Mới nhất'")
      val result = repository.fetchInitialNews()
      updateNewsState(NewsCategory.LATEST, when (result) {
        is NewsResult.Success -> {
          if (result.articles.isEmpty()) {
            Log.w(TAG, "Không có bài viết nào từ API cho danh mục 'Mới nhất'")
            NewsResult.Error("Không có tin tức nào để hiển thị", null)
          } else {
            val articles = repository.getArticlesByAiCategory(NewsCategory.LATEST, 0, ARTICLES_PER_PAGE)
            val totalArticles = repository.getArticlesByAiCategory(NewsCategory.LATEST, 0, Int.MAX_VALUE)
            displayedArticleCounts["Tất cả"] = articles.size
            totalArticleCounts["Tất cả"] = totalArticles.size
            Log.d(TAG, "Tải được ${articles.size} bài viết cho danh mục 'Mới nhất', tổng cộng: ${totalArticles.size}")
            if (articles.isEmpty()) {
              NewsResult.Error("Không có tin tức nào để hiển thị", null)
            } else {
              NewsResult.Success(articles, totalArticles.size)
            }
          }
        }
        else -> {
          Log.e(TAG, "Lỗi khi tải tin tức ban đầu: ${(result as? NewsResult.Error)?.message}")
          result
        }
      })
    }
  }

  fun fetchArticleById(articleId: String) {
    viewModelScope.launch {
      Log.d(TAG, "Tải bài viết từ repository với ID: $articleId")
      val article = repository.getArticleById(articleId)
      updateArticleState(articleId, article)
      Log.d(TAG, "Đã tải bài viết: ${article?.title}")
    }
  }

  fun fetchNewsByCategory(category: NewsCategory?) {
    viewModelScope.launch {
      updateNewsState(category, NewsResult.Loading)
      Log.d(TAG, "Bắt đầu tải tin tức cho danh mục '${category?.displayName ?: "Tất cả"}'")
      if (category == NewsCategory.FOR_YOU) {
        // Xử lý tab "Dành cho bạn"
        Log.d(TAG, "Lấy dữ liệu người dùng từ Firestore cho đề xuất")
        val userDataResult = firestoreService.getUserDataForRecommendation()
        if (userDataResult.isSuccess) {
          val (categories, searchHistory) = userDataResult.getOrNull() ?: Pair(emptyList(), emptyList())
          // Lọc bỏ "Dành cho bạn" khỏi danh sách categories trước khi gửi cho Gemini
          val filteredCategories = categories.filter { it != NewsCategory.FOR_YOU.displayName }
          Log.d(TAG, "Dữ liệu người dùng: sở thích=$filteredCategories, lịch sử tìm kiếm=$searchHistory")
          Log.d(TAG, "Gửi yêu cầu đến Gemini API để lấy danh mục đề xuất")
          val recommendedCategoriesResult = geminiService.recommendCategories(filteredCategories, searchHistory)
          if (recommendedCategoriesResult.isSuccess) {
            val recommendedCategories = recommendedCategoriesResult.getOrNull() ?: emptyList()
            Log.d(TAG, "Danh mục đề xuất từ Gemini: $recommendedCategories")
            // Kiểm tra số lượng bài viết trong Room trước khi gọi API
            val roomArticles = repository.getArticlesByAiCategory(null, 0, Int.MAX_VALUE)
              .filter { article ->
                article.category?.any { cat -> recommendedCategories.contains(cat) } ?: false
              }
            Log.d(TAG, "Số bài viết trong Room với danh mục đề xuất: ${roomArticles.size}")
            Log.d(TAG, "Danh mục bài viết trong Room: ${roomArticles.map { it.category }}")
            if (roomArticles.size < ARTICLES_PER_PAGE) {
              Log.d(TAG, "Số bài viết trong Room ít hơn $ARTICLES_PER_PAGE, gửi yêu cầu đến World News API")
              Log.d(TAG, "Gửi yêu cầu đến World News API để lấy bài viết đề xuất")
              val result = repository.fetchRecommendedNews(recommendedCategories, 0, ARTICLES_PER_PAGE)
              updateNewsState(category, when (result) {
                is NewsResult.Success -> {
                  val filteredArticles = result.articles.filter { article ->
                    article.category?.any { cat -> recommendedCategories.contains(cat) } ?: false
                  }
                  displayedArticleCounts[category.displayName] = filteredArticles.size
                  totalArticleCounts[category.displayName] = result.totalResults
                  Log.d(TAG, "Tải được ${filteredArticles.size} bài viết đề xuất, tổng cộng: ${result.totalResults}")
                  Log.d(TAG, "Danh mục bài viết từ API: ${filteredArticles.map { it.category }}")
                  if (filteredArticles.isEmpty()) {
                    Log.w(TAG, "Không có bài viết đề xuất nào từ World News API")
                    NewsResult.Error("Không có bài viết đề xuất nào", null)
                  } else {
                    NewsResult.Success(filteredArticles, result.totalResults)
                  }
                }
                is NewsResult.Error -> {
                  Log.e(TAG, "Lỗi khi lấy bài viết đề xuất: ${result.message}")
                  result
                }
                else -> {
                  Log.e(TAG, "Trạng thái không mong đợi khi lấy bài viết đề xuất")
                  NewsResult.Error("Không thể tải bài viết đề xuất", null)
                }
              })
            } else {
              // Sử dụng bài viết từ Room
              val limitedArticles = roomArticles.take(ARTICLES_PER_PAGE)
              displayedArticleCounts[category.displayName] = limitedArticles.size
              totalArticleCounts[category.displayName] = roomArticles.size
              Log.d(TAG, "Sử dụng ${limitedArticles.size} bài viết từ Room, tổng cộng: ${roomArticles.size}")
              updateNewsState(category, NewsResult.Success(limitedArticles, roomArticles.size))
            }
          } else {
            val error = recommendedCategoriesResult.exceptionOrNull()?.message
            Log.e(TAG, "Lỗi khi lấy danh mục đề xuất từ Gemini: $error")
            updateNewsState(category, NewsResult.Error("Không thể lấy danh mục đề xuất: $error", null))
          }
        } else {
          val error = userDataResult.exceptionOrNull()?.message
          Log.e(TAG, "Lỗi khi lấy dữ liệu người dùng từ Firestore: $error")
          updateNewsState(category, NewsResult.Error("Không thể lấy dữ liệu người dùng: $error", null))
        }
      } else if (category == NewsCategory.TOP) {
        // Xử lý tab "Nổi bật"
        val roomArticles = repository.getArticlesByAiCategory(category, 0, Int.MAX_VALUE)
        Log.d(TAG, "Số bài viết trong Room cho danh mục 'Nổi bật': ${roomArticles.size}")
        if (roomArticles.isEmpty()) {
          Log.d(TAG, "Không có bài viết trong Room cho 'Nổi bật', gửi yêu cầu API với category=hot")
          val result = repository.fetchRecommendedNews(listOf("hot"), 0, ARTICLES_PER_PAGE)
          updateNewsState(category, when (result) {
            is NewsResult.Success -> {
              val filteredArticles = result.articles
              displayedArticleCounts[category.displayName] = filteredArticles.size
              totalArticleCounts[category.displayName] = result.totalResults
              Log.d(TAG, "Tải được ${filteredArticles.size} bài viết từ API với category=hot, tổng cộng: ${result.totalResults}")
              if (filteredArticles.isEmpty()) {
                Log.w(TAG, "Không có bài viết nào từ API với category=hot")
                NewsResult.Error("Không có tin tức nổi bật nào", null)
              } else {
                NewsResult.Success(filteredArticles, result.totalResults)
              }
            }
            is NewsResult.Error -> {
              Log.e(TAG, "Lỗi khi lấy bài viết từ API với category=hot: ${result.message}")
              result
            }
            else -> {
              Log.e(TAG, "Trạng thái không mong đợi khi lấy bài viết từ API với category=hot")
              NewsResult.Error("Không thể tải tin tức nổi bật", null)
            }
          })
        } else {
          val articles = repository.getArticlesByAiCategory(category, 0, ARTICLES_PER_PAGE)
          val totalArticles = roomArticles.size
          displayedArticleCounts[category.displayName] = articles.size
          totalArticleCounts[category.displayName] = totalArticles
          Log.d(TAG, "Tải được ${articles.size} bài viết từ Room cho danh mục 'Nổi bật', tổng cộng: ${totalArticles}")
          updateNewsState(category, if (articles.isEmpty()) {
            Log.w(TAG, "Không có bài viết nào trong danh mục 'Nổi bật'")
            NewsResult.Error("Không có tin tức nổi bật nào", null)
          } else {
            NewsResult.Success(articles, totalArticles)
          })
        }
      } else {
        // Xử lý các danh mục khác
        val articles = repository.getArticlesByAiCategory(category, 0, ARTICLES_PER_PAGE)
        val totalArticles = repository.getArticlesByAiCategory(category, 0, Int.MAX_VALUE)
        val key = category?.displayName ?: "Tất cả"
        displayedArticleCounts[key] = articles.size
        totalArticleCounts[key] = totalArticles.size
        Log.d(TAG, "Tải được ${articles.size} bài viết cho danh mục '$key', tổng cộng: ${totalArticles.size}")

        updateNewsState(category, if (articles.isEmpty()) {
          Log.w(TAG, "Không có bài viết nào trong danh mục '$key'")
          NewsResult.Error("Không có tin tức nào trong danh mục '${category?.displayName ?: "Tất cả"}'", null)
        } else {
          NewsResult.Success(articles, totalArticles.size)
        })
      }
    }
  }

  fun fetchMoreNewsForCategory(category: NewsCategory?) {
    viewModelScope.launch {
      val key = category?.displayName ?: "Tất cả"
      val currentState = getNewsState(category).value
      val currentArticles = (currentState as? NewsResult.Success)?.articles ?: emptyList()
      val currentDisplayedCount = displayedArticleCounts[key] ?: 0
      val totalCount = totalArticleCounts[key] ?: 0
      Log.d(TAG, "Tải thêm bài viết cho danh mục '$key', hiện tại: $currentDisplayedCount, tổng: $totalCount")

      if (currentDisplayedCount >= totalCount) {
        // Hết bài viết trong Room, gọi API lấy thêm
        updateFetchingMoreState(category, true)
        Log.d(TAG, "Hết bài viết trong Room, gọi API để lấy thêm cho danh mục '$key'")
        var recommendedCategories: List<String>? = null
        val result = if (category == NewsCategory.FOR_YOU) {
          // Lấy thêm bài viết đề xuất cho "Dành cho bạn"
          Log.d(TAG, "Lấy dữ liệu người dùng từ Firestore để tải thêm bài viết đề xuất")
          val userDataResult = firestoreService.getUserDataForRecommendation()
          if (userDataResult.isSuccess) {
            val (categories, searchHistory) = userDataResult.getOrNull() ?: Pair(emptyList(), emptyList())
            // Lọc bỏ "Dành cho bạn" khỏi danh sách categories trước khi gửi cho Gemini
            val filteredCategories = categories.filter { it != NewsCategory.FOR_YOU.displayName }
            Log.d(TAG, "Dữ liệu người dùng để tải thêm: sở thích=$filteredCategories, lịch sử tìm kiếm=$searchHistory")
            Log.d(TAG, "Gửi yêu cầu đến Gemini API để lấy danh mục đề xuất cho tải thêm")
            val recommendedCategoriesResult = geminiService.recommendCategories(filteredCategories, searchHistory)
            if (recommendedCategoriesResult.isSuccess) {
              recommendedCategories = recommendedCategoriesResult.getOrNull() ?: emptyList()
              Log.d(TAG, "Danh mục đề xuất để tải thêm: $recommendedCategories")
              // Kiểm tra số lượng bài viết trong Room
              val roomArticles = repository.getArticlesByAiCategory(null, 0, Int.MAX_VALUE)
                .filter { article ->
                  article.category?.any { cat -> recommendedCategories.contains(cat) } ?: false
                }
              Log.d(TAG, "Số bài viết trong Room với danh mục đề xuất: ${roomArticles.size}")
              Log.d(TAG, "Danh mục bài viết trong Room: ${roomArticles.map { it.category }}")
              if (roomArticles.size <= currentDisplayedCount) {
                Log.d(TAG, "Số bài viết trong Room không đủ, gửi yêu cầu đến World News API")
                Log.d(TAG, "Gửi yêu cầu đến World News API để tải thêm bài viết đề xuất")
                repository.fetchRecommendedNews(recommendedCategories, currentDisplayedCount, ARTICLES_PER_PAGE)
              } else {
                Log.d(TAG, "Sử dụng bài viết từ Room để tải thêm")
                val newArticles = roomArticles.drop(currentDisplayedCount).take(ARTICLES_PER_PAGE)
                NewsResult.Success(newArticles, roomArticles.size)
              }
            } else {
              Log.e(TAG, "Lỗi khi lấy danh mục đề xuất để tải thêm: ${recommendedCategoriesResult.exceptionOrNull()?.message}")
              NewsResult.Error("Không thể lấy danh mục đề xuất", null)
            }
          } else {
            Log.e(TAG, "Lỗi khi lấy dữ liệu người dùng để tải thêm: ${userDataResult.exceptionOrNull()?.message}")
            NewsResult.Error("Không thể lấy dữ liệu người dùng", null)
          }
        } else if (category == NewsCategory.TOP) {
          // Lấy thêm bài viết cho "Nổi bật" từ API với category=hot
          Log.d(TAG, "Hết bài viết trong Room cho 'Nổi bật', gửi yêu cầu API với category=hot")
          repository.fetchRecommendedNews(listOf("hot"), currentDisplayedCount, ARTICLES_PER_PAGE)
        } else {
          repository.fetchMoreNews()
        }
        updateFetchingMoreState(category, false)

        when (result) {
          is NewsResult.Success -> {
            if (result.articles.isEmpty()) {
              Log.w(TAG, "Không có thêm bài viết nào từ API cho danh mục '$key'")
              updateNewsState(category, NewsResult.Success(currentArticles, totalCount))
              return@launch
            }
            // Thêm bài viết mới từ API vào danh sách hiện tại
            val newArticles = if (category == NewsCategory.FOR_YOU) {
              result.articles.filter { article ->
                article.category?.any { cat -> recommendedCategories?.contains(cat) == true } ?: false
              }
            } else {
              result.articles
            }
            val updatedArticles = (currentArticles + newArticles).distinctBy { it.id }
            displayedArticleCounts[key] = updatedArticles.size
            totalArticleCounts[key] = result.totalResults // Cập nhật tổng số bài viết từ API
            Log.d(TAG, "Tải thêm được ${newArticles.size} bài viết, tổng cộng hiện tại: ${updatedArticles.size}")
            Log.d(TAG, "Danh mục bài viết từ API: ${newArticles.map { it.category }}")
            updateNewsState(category, NewsResult.Success(updatedArticles, result.totalResults))
          }
          is NewsResult.Error -> {
            Log.e(TAG, "Lỗi khi tải thêm bài viết: ${result.message}")
            updateNewsState(category, result)
          }
          else -> {
            Log.w(TAG, "Trạng thái không mong đợi khi tải thêm bài viết")
            updateNewsState(category, NewsResult.Success(currentArticles, totalCount))
          }
        }
      } else {
        // Còn bài viết trong Room, lấy thêm
        val newArticles = repository.getArticlesByAiCategory(category, currentDisplayedCount, ARTICLES_PER_PAGE)
        val updatedArticles = (currentArticles + newArticles).distinctBy { it.id }
        displayedArticleCounts[key] = updatedArticles.size
        Log.d(TAG, "Tải thêm ${newArticles.size} bài viết từ Room, tổng cộng hiện tại: ${updatedArticles.size}")
        updateNewsState(category, NewsResult.Success(updatedArticles, totalCount))
      }
    }
  }

  fun filterNewsByKeyword(keyword: String) {
    viewModelScope.launch {
      updateNewsState(null, NewsResult.Loading)
      Log.d(TAG, "Lọc bài viết với từ khóa: $keyword")
      val filteredArticles = repository.filterArticles(keyword)
      val limitedArticles = filteredArticles.take(ARTICLES_PER_PAGE)
      displayedArticleCounts["Tất cả"] = limitedArticles.size
      totalArticleCounts["Tất cả"] = filteredArticles.size
      Log.d(TAG, "Lọc được ${limitedArticles.size} bài viết với từ khóa '$keyword', tổng cộng: ${filteredArticles.size}")
      updateNewsState(null, if (limitedArticles.isEmpty()) {
        NewsResult.Error("Không tìm thấy tin tức phù hợp với từ khóa '$keyword'", null)
      } else {
        NewsResult.Success(limitedArticles, filteredArticles.size)
      })
    }
  }

  fun reloadNews() {
    fetchInitialNews()
  }

  fun calculateReliabilityScore(article: NewsArticle) {
    viewModelScope.launch {
      _isCalculatingReliability.value = true
      Log.d(TAG, "Bắt đầu tính toán độ tin cậy cho bài viết: ${article.title}")
      val result = repository.calculateArticleReliability(article)
      result.fold(
        onSuccess = { score ->
          Log.d(TAG, "Độ tin cậy tính toán được: ${score * 100}%")
          val updatedArticle = article.copy(reliabilityScore = score)
          updateArticleState(article.id, updatedArticle)
        },
        onFailure = { error ->
          Log.e(TAG, "Lỗi khi tính toán độ tin cậy: ${error.message}")
        }
      )
      _isCalculatingReliability.value = false
    }
  }
}