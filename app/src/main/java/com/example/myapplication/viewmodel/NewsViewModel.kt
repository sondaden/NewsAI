package com.example.myapplication.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.myapplication.data.model.NewsArticle
import com.example.myapplication.repository.NewsRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

/**
 * ViewModel quản lý dữ liệu tin tức theo mô hình MVVM.
 * @param repository Repository cung cấp dữ liệu tin tức từ API.
 */
class NewsViewModel(private val repository: NewsRepository) : ViewModel() {

  // Trạng thái UI cho danh sách bài viết
  private val _newsState = MutableStateFlow<NewsUiState>(NewsUiState.Initial)
  val newsState: StateFlow<NewsUiState> = _newsState.asStateFlow()

  // Cache lưu trữ bài viết
  private val articleCache = mutableMapOf<String, NewsArticle>()

  // Thông tin phân trang
  private var currentCategory: String? = null
  private var currentPage: Int = 1
  private var isLoadingMore = false

  init {
    fetchNews("Mới nhất") // Tải dữ liệu mặc định
  }

  /**
   * Tải tin tức theo danh mục.
   * @param category Danh mục tin tức (e.g., "Mới nhất", "Công nghệ").
   * @param loadMore Nếu true, tải thêm trang tiếp theo thay vì làm mới.
   */
  fun fetchNews(category: String, loadMore: Boolean = false) {
    if (isLoadingMore || (loadMore && _newsState.value !is NewsUiState.Success)) return
    if (!loadMore && category == currentCategory && _newsState.value is NewsUiState.Success) return

    viewModelScope.launch {
      isLoadingMore = true
      _newsState.value = if (loadMore) {
        (_newsState.value as? NewsUiState.Success)?.copy(isLoadingMore = true) ?: NewsUiState.Loading
      } else {
        NewsUiState.Loading
      }

      repository.getNewsByCategory(category).fold(
        onSuccess = { articles ->
          if (!loadMore) {
            currentCategory = category
            currentPage = 1
            articleCache.clear()
          }
          updateCache(articles)
          val currentArticles = if (loadMore) {
            (_newsState.value as? NewsUiState.Success)?.articles ?: emptyList()
          } else {
            emptyList()
          }
          _newsState.value = NewsUiState.Success(
            articles = currentArticles + articles,
            isLoadingMore = false
          )
          if (loadMore) currentPage++
        },
        onFailure = { error ->
          _newsState.value = NewsUiState.Error("Lỗi khi tải tin tức: ${error.message}")
        }
      )
      isLoadingMore = false
    }
  }

  /**
   * Lấy bài viết theo ID từ cache hoặc tải từ API nếu cần.
   * @param articleId ID của bài viết.
   * @return Bài viết nếu tìm thấy, null nếu không.
   */
  fun getArticleById(articleId: String): NewsArticle? {
    return articleCache[articleId]
  }

  /**
   * Thêm bài viết vào cache và cập nhật danh sách nếu phù hợp.
   * @param article Bài viết cần thêm.
   */
  fun addArticle(article: NewsArticle) {
    articleCache[article.id] = article
    val currentState = _newsState.value as? NewsUiState.Success
    if (currentState != null && currentCategory != null && article.category.contains(currentCategory!!)) {
      _newsState.value = currentState.copy(articles = currentState.articles + article)
    }
  }

  // Cập nhật cache với danh sách bài viết mới
  private fun updateCache(articles: List<NewsArticle>) {
    articles.forEach { articleCache[it.id] = it }
  }
}

/**
 * Trạng thái UI cho tin tức.
 */
sealed class NewsUiState {
  object Initial : NewsUiState() // Trạng thái ban đầu
  object Loading : NewsUiState() // Đang tải dữ liệu
  data class Success(
    val articles: List<NewsArticle>,
    val isLoadingMore: Boolean = false // Đang tải thêm trang
  ) : NewsUiState()
  data class Error(val message: String) : NewsUiState() // Lỗi với thông điệp
}