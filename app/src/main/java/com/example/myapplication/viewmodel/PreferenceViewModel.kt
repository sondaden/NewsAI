package com.example.myapplication.viewmodel

import android.util.Log
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.myapplication.data.firestore.FirestoreService
import com.example.myapplication.data.firestore.UserPreference
import kotlinx.coroutines.launch

class PreferenceViewModel(private val firestoreService: FirestoreService) : ViewModel() {

    private val TAG = "PreferenceViewModel"

    // State để quản lý thông báo dialog
    val showDialog = mutableStateOf(false)
    val dialogMessage = mutableStateOf("")

    // State để quản lý trạng thái loading
    val isLoading = mutableStateOf(false)

    // Tải sở thích đã lưu từ Firestore
    fun loadUserPreferences(selectedPreferences: MutableState<Set<String>>) {
        viewModelScope.launch {
            Log.d(TAG, "Bắt đầu tải sở thích từ Firestore...")
            isLoading.value = true
            val result = firestoreService.getUserPreferences()
            if (result.isSuccess) {
                val userPrefs = result.getOrNull()
                userPrefs?.categories?.let { categories ->
                    selectedPreferences.value = categories.toSet()
                    Log.d(TAG, "Tải thành công, sở thích: $categories")
                }
            } else {
                Log.e(TAG, "Lỗi khi tải sở thích: ${result.exceptionOrNull()?.message}")
                selectedPreferences.value = emptySet()
            }
            isLoading.value = false
            Log.d(TAG, "Hoàn tất tải sở thích")
        }
    }

    // Lưu sở thích và hiển thị dialog
    fun saveUserPreferences(categories: List<String>) {
        viewModelScope.launch {
            Log.d(TAG, "Bắt đầu lưu sở thích: $categories")
            isLoading.value = true
            val result = firestoreService.updateUserPreferences(categories)
            if (result.isSuccess) {
                dialogMessage.value = "Cập nhật sở thích thành công!"
                showDialog.value = true
                Log.d(TAG, "Lưu sở thích thành công")
            } else {
                dialogMessage.value = "Cập nhật sở thích thất bại: ${result.exceptionOrNull()?.message}"
                showDialog.value = true
                Log.e(TAG, "Lỗi khi lưu sở thích: ${result.exceptionOrNull()?.message}")
            }
            isLoading.value = false
        }
    }
}