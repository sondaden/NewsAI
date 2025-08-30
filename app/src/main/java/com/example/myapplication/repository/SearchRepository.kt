package com.example.myapplication.repository

import android.util.Log
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import kotlinx.coroutines.tasks.await
import java.util.*

data class SearchHistoryEntry(
    val query: String,
    val timestamp: Long
)

class SearchRepository {
    private val db = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()
    private val TAG = "SearchRepository"
    private val MAX_HISTORY_ITEMS = 20 // Giới hạn 20 từ khóa tìm kiếm

    suspend fun saveSearchQuery(query: String): Result<Unit> {
        val userId = auth.currentUser?.uid ?: return Result.failure(Exception("Chưa đăng nhập"))
        return try {
            val snapshot = db.collection("users").document(userId)
                .collection("searchHistory").whereEqualTo("query", query)
                .get().await()

            if (!snapshot.isEmpty) {
                snapshot.documents.first().reference.update("timestamp", FieldValue.serverTimestamp()).await()
                Log.d(TAG, "Cập nhật timestamp cho từ khóa: $query")
                return Result.success(Unit)
            }

            val searchEntry = hashMapOf(
                "query" to query,
                "timestamp" to FieldValue.serverTimestamp()
            )
            db.collection("users").document(userId)
                .collection("searchHistory").document(UUID.randomUUID().toString())
                .set(searchEntry)
                .await()
            Log.d(TAG, "Lưu từ khóa tìm kiếm thành công: $query")

            limitSearchHistory(userId)
            Result.success(Unit)
        } catch (e: Exception) {
            Log.e(TAG, "Lỗi khi lưu từ khóa tìm kiếm: ${e.message}")
            Result.failure(e)
        }
    }

    suspend fun getSearchHistory(): Result<Pair<List<SearchHistoryEntry>, Int>> {
        val userId = auth.currentUser?.uid ?: return Result.failure(Exception("Chưa đăng nhập"))
        return try {
            val snapshot = db.collection("users").document(userId)
                .collection("searchHistory")
                .orderBy("timestamp", Query.Direction.DESCENDING)
                .limit(MAX_HISTORY_ITEMS.toLong())
                .get().await()

            val history = snapshot.documents.mapNotNull { doc ->
                val query = doc.getString("query") ?: return@mapNotNull null
                val timestamp = doc.getTimestamp("timestamp")?.toDate()?.time ?: System.currentTimeMillis()
                SearchHistoryEntry(query, timestamp)
            }
            val totalSize = history.size // Tổng số mục thực sự lấy được
            Log.d(TAG, "Lấy lịch sử tìm kiếm thành công: ${history.size} từ khóa (tổng số từ Firestore: ${snapshot.size()})")
            Result.success(Pair(history, totalSize))
        } catch (e: Exception) {
            Log.e(TAG, "Lỗi khi lấy lịch sử tìm kiếm: ${e.message}")
            Result.failure(e)
        }
    }

    suspend fun clearSearchHistory(): Result<Unit> {
        val userId = auth.currentUser?.uid ?: return Result.failure(Exception("Chưa đăng nhập"))
        return try {
            val snapshot = db.collection("users").document(userId)
                .collection("searchHistory").get().await()

            snapshot.documents.forEach { doc ->
                doc.reference.delete().await()
            }
            Log.d(TAG, "Xóa toàn bộ lịch sử tìm kiếm thành công")
            Result.success(Unit)
        } catch (e: Exception) {
            Log.e(TAG, "Lỗi khi xóa lịch sử tìm kiếm: ${e.message}")
            Result.failure(e)
        }
    }

    suspend fun deleteSearchHistoryItem(query: String): Result<Unit> {
        val userId = auth.currentUser?.uid ?: return Result.failure(Exception("Chưa đăng nhập"))
        return try {
            val snapshot = db.collection("users").document(userId)
                .collection("searchHistory").whereEqualTo("query", query)
                .get().await()

            if (!snapshot.isEmpty) {
                snapshot.documents.first().reference.delete().await()
                Log.d(TAG, "Xóa từ khóa tìm kiếm thành công: $query")
                return Result.success(Unit)
            }
            Result.failure(Exception("Không tìm thấy từ khóa để xóa"))
        } catch (e: Exception) {
            Log.e(TAG, "Lỗi khi xóa từ khóa tìm kiếm: ${e.message}")
            Result.failure(e)
        }
    }

    private suspend fun limitSearchHistory(userId: String) {
        try {
            val snapshot = db.collection("users").document(userId)
                .collection("searchHistory")
                .orderBy("timestamp", Query.Direction.DESCENDING)
                .get().await()

            if (snapshot.size() > MAX_HISTORY_ITEMS) {
                val documentsToDelete = snapshot.documents.drop(MAX_HISTORY_ITEMS)
                documentsToDelete.forEach { doc ->
                    doc.reference.delete().await()
                }
                Log.d(TAG, "Xóa ${documentsToDelete.size} từ khóa cũ để giới hạn lịch sử")
            }
        } catch (e: Exception) {
            Log.e(TAG, "Lỗi khi giới hạn lịch sử tìm kiếm: ${e.message}")
        }
    }
}