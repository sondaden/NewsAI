package com.example.myapplication.repository

import android.content.Context
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.core.content.edit
import com.example.myapplication.data.model.UserProfile
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext

/**
 * Repository quản lý dữ liệu người dùng trên Firestore và SharedPreferences.
 */
class UserRepository(private val context: Context) {
    private val firestore = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()
    private val prefs = context.getSharedPreferences("user_prefs", Context.MODE_PRIVATE)

    /**
     * Lấy thông tin hồ sơ người dùng từ Firestore.
     * @return Result chứa UserProfile nếu thành công, hoặc lỗi nếu thất bại.
     */
    suspend fun getUserProfile(): Result<UserProfile> = withContext(Dispatchers.IO) {
        val user = auth.currentUser ?: return@withContext Result.failure(Exception("Chưa đăng nhập"))
        try {
            val document = firestore.collection("users").document(user.uid).get().await()
            document.toObject(UserProfile::class.java)?.copy(uid = user.uid)
                ?.let { Result.success(it) }
                ?: Result.failure(Exception("Không tìm thấy hồ sơ người dùng"))
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Cập nhật thông tin hồ sơ người dùng lên Firestore.
     */
    suspend fun updateUserProfile(userProfile: UserProfile): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            firestore.collection("users").document(userProfile.uid).set(userProfile).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Thêm bài viết vào lịch sử đọc.
     */
    @RequiresApi(Build.VERSION_CODES.VANILLA_ICE_CREAM)
    suspend fun addToReadingHistory(articleId: String): Result<Unit> = updateProfileList { profile ->
        val updatedHistory = profile.readingHistory.toMutableList().apply {
            if (!contains(articleId)) add(0, articleId)
            if (size > 100) removeLast()
        }
        profile.copy(readingHistory = updatedHistory)
    }

    /**
     * Xóa bài viết khỏi lịch sử đọc.
     */
    suspend fun removeFromReadingHistory(articleId: String): Result<Unit> = updateProfileList { profile ->
        val updatedHistory = profile.readingHistory.toMutableList().apply { remove(articleId) }
        profile.copy(readingHistory = updatedHistory)
    }

    /**
     * Thêm bài viết vào danh sách đã lưu.
     */
    suspend fun addToSavedArticles(articleId: String): Result<Unit> = updateProfileList { profile ->
        val updatedSaved = profile.savedArticles.toMutableList().apply {
            if (!contains(articleId)) add(articleId)
        }
        profile.copy(savedArticles = updatedSaved)
    }

    /**
     * Xóa bài viết khỏi danh sách đã lưu.
     */
    suspend fun removeFromSavedArticles(articleId: String): Result<Unit> = updateProfileList { profile ->
        val updatedSaved = profile.savedArticles.toMutableList().apply { remove(articleId) }
        profile.copy(savedArticles = updatedSaved)
    }

    /**
     * Cập nhật sở thích danh mục của người dùng.
     */
    suspend fun updatePreferences(preferences: List<String>): Result<Unit> = updateProfileList { profile ->
        profile.copy(preferences = preferences)
    }

    /**
     * Lưu cài đặt chế độ tối vào SharedPreferences.
     */
    fun saveDarkThemePreference(isDarkTheme: Boolean): Result<Unit> = try {
        prefs.edit { putBoolean("is_dark_theme", isDarkTheme) }
        Result.success(Unit)
    } catch (e: Exception) {
        Result.failure(e)
    }

    /**
     * Lấy cài đặt chế độ tối từ SharedPreferences.
     */
    fun getDarkThemePreference(): Result<Boolean> = try {
        Result.success(prefs.getBoolean("is_dark_theme", false))
    } catch (e: Exception) {
        Result.failure(e)
    }

    // Hàm tiện ích để cập nhật danh sách trong UserProfile
    private suspend fun updateProfileList(transform: (UserProfile) -> UserProfile): Result<Unit> = withContext(Dispatchers.IO) {
        val user = auth.currentUser ?: return@withContext Result.failure(Exception("Chưa đăng nhập"))
        return@withContext getUserProfile().fold(
            onSuccess = { profile ->
                val updatedProfile = transform(profile)
                updateUserProfile(updatedProfile)
            },
            onFailure = { Result.failure(it) }
        )
    }
}