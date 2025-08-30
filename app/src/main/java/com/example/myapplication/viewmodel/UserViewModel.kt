package com.example.myapplication.viewmodel

import android.content.Context
import android.content.SharedPreferences
import android.util.Log
import androidx.core.content.edit
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.myapplication.data.firestore.FirestoreService
import com.example.myapplication.data.firestore.UserPreference
import com.example.myapplication.data.model.NewsArticle
import com.example.myapplication.repository.NewsRepository
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

data class UserData(
    val uid: String? = null,
    val displayName: String? = null,
    val email: String? = null,
    val avatarUrl: String? = null
)

class UserViewModel(private val context: Context) : ViewModel() {
    private val newsRepository = NewsRepository(context)
    private val firestoreService = FirestoreService(newsRepository)
    private val preferences: SharedPreferences = context.getSharedPreferences("AppPrefs", Context.MODE_PRIVATE)
    private val TAG = "UserViewModel"

    private val _isDarkTheme = MutableStateFlow(preferences.getBoolean("isDarkTheme", false))
    val isDarkTheme: StateFlow<Boolean> = _isDarkTheme.asStateFlow()

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage.asStateFlow()

    private val _savedArticles = MutableStateFlow<List<NewsArticle>>(emptyList())
    val savedArticles = _savedArticles.asStateFlow()

    private val _readingHistory = MutableStateFlow<List<NewsArticle>>(emptyList())
    val readingHistory = _readingHistory.asStateFlow()

    private val _userPreferences = MutableStateFlow<UserPreference>(UserPreference())
    val userPreferences = _userPreferences.asStateFlow()

    private val _userData = MutableStateFlow<UserData?>(null)
    val userData: StateFlow<UserData?> = _userData.asStateFlow()

    init {
        Log.d(TAG, "Khởi tạo UserViewModel")
        fetchSavedArticles()
        fetchReadingHistory()
        fetchUserPreferences()
        fetchUserData()
    }

    private fun isUserLoggedIn(): Boolean {
        val sharedPreferences: SharedPreferences = context.getSharedPreferences("AppPrefs", Context.MODE_PRIVATE)
        val isLoggedIn = sharedPreferences.getBoolean("isLoggedIn", false)
        Log.d(TAG, "Kiểm tra trạng thái đăng nhập từ SharedPreferences: isLoggedIn=$isLoggedIn")
        return isLoggedIn
    }

    fun fetchUserData() {
        Log.d(TAG, "Gọi fetchUserData")
        if (!isUserLoggedIn()) {
            Log.w(TAG, "Người dùng chưa đăng nhập, bỏ qua fetchUserData")
            _userData.value = null
            return
        }

        viewModelScope.launch {
            try {
                val user = FirebaseAuth.getInstance().currentUser
                if (user == null) {
                    Log.w(TAG, "Không tìm thấy người dùng hiện tại, bỏ qua fetchUserData")
                    _userData.value = null
                    return@launch
                }
                // Đảm bảo người dùng tồn tại và đồng bộ
                user.reload().await()
                if (!user.isEmailVerified) {
                    Log.w(TAG, "Tài khoản chưa xác minh email, bỏ qua fetchUserData")
                    _userData.value = null
                    return@launch
                }
                val userId = user.uid
                Log.d(TAG, "Bắt đầu lấy dữ liệu người dùng với UID: $userId")
                val snapshot = FirebaseFirestore.getInstance()
                    .collection("users")
                    .document(userId)
                    .get()
                    .await()
                if (snapshot.exists()) {
                    val data = snapshot.data
                    Log.d(TAG, "Dữ liệu người dùng từ Firestore: $data")
                    _userData.value = UserData(
                        uid = data?.get("uid") as? String,
                        displayName = data?.get("displayName") as? String,
                        email = data?.get("email") as? String,
                        avatarUrl = data?.get("avatarUrl") as? String
                    )
                    Log.d(TAG, "Fetched user data: ${_userData.value}")
                } else {
                    _errorMessage.value = "Không tìm thấy thông tin người dùng"
                    Log.w(TAG, "User document does not exist for UID: $userId")
                    _userData.value = null
                }
            } catch (e: Exception) {
                _errorMessage.value = "Lỗi khi lấy thông tin người dùng: ${e.message}"
                Log.e(TAG, "Error fetching user data: ${e.message}", e)
                _userData.value = null
            }
        }
    }

    fun clearUserData() {
        Log.d(TAG, "Xóa dữ liệu người dùng")
        _userData.value = null
        _savedArticles.value = emptyList()
        _readingHistory.value = emptyList()
        _userPreferences.value = UserPreference()
        _isDarkTheme.value = false
        _errorMessage.value = null
        preferences.edit {
            putBoolean("isDarkTheme", false)
        }
        Log.d(TAG, "Đã xóa trạng thái UserViewModel")
    }

    fun updateUsername(newUsername: String, password: String, onResult: (Result<Unit>) -> Unit) {
        Log.d(TAG, "Gọi updateUsername với newUsername: $newUsername")
        viewModelScope.launch {
            try {
                val user = FirebaseAuth.getInstance().currentUser
                if (user == null || user.email == null) {
                    Log.w(TAG, "Chưa đăng nhập, không thể cập nhật tên người dùng")
                    onResult(Result.failure(Exception("Chưa đăng nhập")))
                    return@launch
                }
                Log.d(TAG, "Xác minh mật khẩu để cập nhật tên người dùng")
                val credential = com.google.firebase.auth.EmailAuthProvider.getCredential(user.email!!, password)
                user.reauthenticate(credential).await()

                Log.d(TAG, "Cập nhật displayName trong Firestore: $newUsername")
                FirebaseFirestore.getInstance()
                    .collection("users")
                    .document(user.uid)
                    .update("displayName", newUsername)
                    .await()

                _userData.value = _userData.value?.copy(displayName = newUsername)
                Log.d(TAG, "Updated username to: $newUsername, userData hiện tại: ${_userData.value}")
                onResult(Result.success(Unit))
            } catch (e: Exception) {
                Log.e(TAG, "Error updating username: ${e.message}", e)
                onResult(Result.failure(Exception("Lỗi khi cập nhật tên người dùng: ${e.message}")))
            }
        }
    }

    fun changePassword(currentPassword: String, newPassword: String, onResult: (Result<Unit>) -> Unit) {
        Log.d(TAG, "Gọi changePassword")
        viewModelScope.launch {
            try {
                val user = FirebaseAuth.getInstance().currentUser
                if (user == null || user.email == null) {
                    Log.w(TAG, "Chưa đăng nhập, không thể đổi mật khẩu")
                    onResult(Result.failure(Exception("Chưa đăng nhập")))
                    return@launch
                }
                Log.d(TAG, "Xác minh mật khẩu hiện tại")
                val credential = com.google.firebase.auth.EmailAuthProvider.getCredential(user.email!!, currentPassword)
                user.reauthenticate(credential).await()

                Log.d(TAG, "Cập nhật mật khẩu mới")
                user.updatePassword(newPassword).await()
                Log.d(TAG, "Changed password successfully")
                onResult(Result.success(Unit))
            } catch (e: Exception) {
                Log.e(TAG, "Error changing password: ${e.message}", e)
                onResult(Result.failure(Exception("Lỗi khi đổi mật khẩu: ${e.message}")))
            }
        }
    }

    fun toggleDarkMode() {
        Log.d(TAG, "Gọi toggleDarkMode")
        viewModelScope.launch {
            val newDarkModeState = !_isDarkTheme.value
            _isDarkTheme.value = newDarkModeState
            preferences.edit { putBoolean("isDarkTheme", newDarkModeState) }
            firestoreService.updateDarkModePreference(newDarkModeState).fold(
                onSuccess = {
                    Log.d(TAG, "Chuyển đổi chế độ tối thành công: $newDarkModeState")
                },
                onFailure = { error ->
                    _errorMessage.value = "Lỗi khi cập nhật chế độ tối: ${error.message}"
                    Log.e(TAG, "Lỗi khi cập nhật chế độ tối: ${error.message}")
                }
            )
        }
    }

    fun clearErrorMessage() {
        Log.d(TAG, "Xóa thông báo lỗi")
        _errorMessage.value = null
    }

    fun fetchSavedArticles() {
        Log.d(TAG, "Gọi fetchSavedArticles")
        viewModelScope.launch {
            Log.d(TAG, "Bắt đầu lấy bài viết đã lưu")
            firestoreService.getSavedArticles().fold(
                onSuccess = { articles ->
                    _savedArticles.value = articles
                    Log.d(TAG, "Lấy bài viết đã lưu thành công: ${articles.size} bài")
                },
                onFailure = { error ->
                    _errorMessage.value = "Lỗi khi lấy bài viết đã lưu: ${error.message}"
                    Log.e(TAG, "Lỗi khi lấy bài viết đã lưu: ${error.message}")
                }
            )
        }
    }

    fun saveArticle(article: NewsArticle) {
        Log.d(TAG, "Gọi saveArticle với article ID: ${article.id}")
        viewModelScope.launch {
            val previousList = _savedArticles.value
            _savedArticles.value = previousList + article
            Log.d(TAG, "Cập nhật UI tạm thời: Đã thêm bài viết ${article.id}, danh sách hiện tại: ${_savedArticles.value.size} bài")

            firestoreService.saveArticle(article).fold(
                onSuccess = {
                    Log.d(TAG, "Lưu bài viết thành công: ${article.id}")
                },
                onFailure = { error ->
                    _savedArticles.value = previousList
                    _errorMessage.value = "Lỗi khi lưu bài viết: ${error.message}"
                    Log.e(TAG, "Lỗi khi lưu bài viết: ${error.message}, rollback danh sách bài viết")
                }
            )
        }
    }

    fun removeSavedArticle(articleId: String) {
        Log.d(TAG, "Gọi removeSavedArticle với articleId: $articleId")
        viewModelScope.launch {
            val previousList = _savedArticles.value
            val updatedList = previousList.filterNot { it.id == articleId }
            _savedArticles.value = updatedList
            Log.d(TAG, "Cập nhật UI tạm thời: Đã xóa bài viết $articleId, danh sách hiện tại: ${_savedArticles.value.size} bài")

            firestoreService.removeSavedArticle(articleId).fold(
                onSuccess = {
                    Log.d(TAG, "Xóa bài viết thành công: $articleId")
                },
                onFailure = { error ->
                    _savedArticles.value = previousList
                    _errorMessage.value = "Lỗi khi xóa bài viết: ${error.message}"
                    Log.e(TAG, "Lỗi khi xóa bài viết: ${error.message}, rollback danh sách bài viết")
                }
            )
        }
    }

    fun deleteAllSavedArticles() {
        Log.d(TAG, "Gọi deleteAllSavedArticles")
        viewModelScope.launch {
            Log.d(TAG, "Bắt đầu xóa toàn bộ bài viết đã lưu")
            val userId = FirebaseAuth.getInstance().currentUser?.uid
            if (userId == null) {
                _errorMessage.value = "Chưa đăng nhập, không thể xóa bài viết"
                Log.e(TAG, "Chưa đăng nhập, không thể xóa bài viết")
                return@launch
            }
            try {
                val snapshot = FirebaseFirestore.getInstance()
                    .collection("users").document(userId)
                    .collection("savedArticles")
                    .get()
                    .await()

                for (doc in snapshot.documents) {
                    doc.reference.delete().await()
                }
                Log.d(TAG, "Xóa toàn bộ bài viết đã lưu thành công")
                fetchSavedArticles()
            } catch (e: Exception) {
                _errorMessage.value = "Lỗi khi xóa bài viết: ${e.message}"
                Log.e(TAG, "Lỗi khi xóa bài viết: ${e.message}", e)
            }
        }
    }

    fun fetchReadingHistory() {
        Log.d(TAG, "Gọi fetchReadingHistory")
        viewModelScope.launch {
            Log.d(TAG, "Bắt đầu lấy lịch sử đọc")
            firestoreService.getReadingHistory().fold(
                onSuccess = { articles ->
                    _readingHistory.value = articles
                    Log.d(TAG, "Lấy lịch sử đọc thành công: ${articles.size} bài")
                },
                onFailure = { error ->
                    _errorMessage.value = "Lỗi khi lấy lịch sử đọc: ${error.message}"
                    Log.e(TAG, "Lỗi khi lấy lịch sử đọc: ${error.message}")
                }
            )
        }
    }

    fun addToReadingHistory(article: NewsArticle) {
        Log.d(TAG, "Gọi addToReadingHistory với article ID: ${article.id}")
        viewModelScope.launch {
            Log.d(TAG, "Kiểm tra thêm bài viết vào lịch sử đọc: ${article.id}")
            if (_readingHistory.value.any { it.id == article.id }) {
                Log.d(TAG, "Bài viết đã có trong lịch sử đọc: ${article.id}")
                return@launch
            }
            firestoreService.addToReadingHistory(article).fold(
                onSuccess = {
                    fetchReadingHistory()
                    Log.d(TAG, "Thêm lịch sử đọc thành công: ${article.id}")
                },
                onFailure = { error ->
                    _errorMessage.value = "Lỗi khi thêm vào lịch sử đọc: ${error.message}"
                    Log.e(TAG, "Lỗi khi thêm vào lịch sử đọc: ${error.message}")
                }
            )
        }
    }

    fun removeReadingHistory(articleId: String) {
        Log.d(TAG, "Gọi removeReadingHistory với articleId: $articleId")
        viewModelScope.launch {
            Log.d(TAG, "Bắt đầu xóa bài viết khỏi lịch sử đọc: $articleId")
            val userId = com.google.firebase.auth.FirebaseAuth.getInstance().currentUser?.uid
            if (userId == null) {
                _errorMessage.value = "Chưa đăng nhập, không thể xóa lịch sử đọc"
                Log.e(TAG, "Chưa đăng nhập, không thể xóa lịch sử đọc")
                return@launch
            }
            try {
                val snapshot = com.google.firebase.firestore.FirebaseFirestore.getInstance()
                    .collection("users").document(userId)
                    .collection("readingHistory")
                    .whereEqualTo("article.id", articleId)
                    .get()
                    .await()

                for (doc in snapshot.documents) {
                    doc.reference.delete().await()
                }
                Log.d(TAG, "Xóa bài viết khỏi lịch sử đọc thành công: $articleId")
                fetchReadingHistory()
            } catch (e: Exception) {
                _errorMessage.value = "Lỗi khi xóa bài viết khỏi lịch sử đọc: ${e.message}"
                Log.e(TAG, "Lỗi khi xóa bài viết khỏi lịch sử đọc: ${e.message}")
            }
        }
    }

    fun deleteAllReadingHistory() {
        Log.d(TAG, "Gọi deleteAllReadingHistory")
        viewModelScope.launch {
            Log.d(TAG, "Bắt đầu xóa toàn bộ lịch sử đọc")
            val userId = com.google.firebase.auth.FirebaseAuth.getInstance().currentUser?.uid
            if (userId == null) {
                _errorMessage.value = "Chưa đăng nhập, không thể xóa lịch sử đọc"
                Log.e(TAG, "Chưa đăng nhập, không thể xóa lịch sử đọc")
                return@launch
            }
            try {
                val snapshot = com.google.firebase.firestore.FirebaseFirestore.getInstance()
                    .collection("users").document(userId)
                    .collection("readingHistory")
                    .get()
                    .await()

                for (doc in snapshot.documents) {
                    doc.reference.delete().await()
                }
                Log.d(TAG, "Xóa toàn bộ lịch sử đọc thành công")
                fetchReadingHistory()
            } catch (e: Exception) {
                _errorMessage.value = "Lỗi khi xóa toàn bộ lịch sử đọc: ${e.message}"
                Log.e(TAG, "Lỗi khi xóa toàn bộ lịch sử đọc: ${e.message}")
            }
        }
    }

    private fun fetchUserPreferences() {
        Log.d(TAG, "Gọi fetchUserPreferences")
        viewModelScope.launch {
            firestoreService.getUserPreferences().fold(
                onSuccess = { preferences ->
                    _userPreferences.value = preferences
                    _isDarkTheme.value = preferences.isDarkTheme
                    this@UserViewModel.preferences.edit {
                        putBoolean(
                            "isDarkTheme",
                            preferences.isDarkTheme
                        )
                    }
                    Log.d(TAG, "Lấy sở thích thành công: ${preferences.categories}, isDarkTheme=${preferences.isDarkTheme}")
                },
                onFailure = { error ->
                    _errorMessage.value = "Lỗi khi lấy sở thích: ${error.message}"
                    Log.e(TAG, "Lỗi khi lấy sở thích: ${error.message}")
                }
            )
        }
    }

    fun updateUserPreferences(categories: List<String>) {
        Log.d(TAG, "Gọi updateUserPreferences với categories: $categories")
        viewModelScope.launch {
            firestoreService.updateUserPreferences(categories, _isDarkTheme.value).fold(
                onSuccess = {
                    fetchUserPreferences()
                    Log.d(TAG, "Cập nhật sở thích thành công: $categories")
                },
                onFailure = { error ->
                    _errorMessage.value = "Lỗi khi cập nhật sở thích: ${error.message}"
                    Log.e(TAG, "Lỗi khi cập nhật sở thích: ${error.message}")
                }
            )
        }
    }
}