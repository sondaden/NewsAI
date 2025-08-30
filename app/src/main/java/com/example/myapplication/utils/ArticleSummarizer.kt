package com.example.myapplication.utils

import android.util.Log
import com.example.myapplication.UiState
import com.example.myapplication.ai.GeminiService
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

object ArticleSummarizer {
    private val geminiService = GeminiService()
    private const val TAG = "ArticleSummarizer"

    suspend fun summarizeWithGemini(content: String?): UiState = withContext(Dispatchers.IO) {
        if (content.isNullOrBlank()) {
            Log.w(TAG, "Nội dung bài viết rỗng, không thể tóm tắt")
            return@withContext UiState.Error("Nội dung bài viết rỗng")
        }

        val prompt = """
            Tóm tắt bài viết sau thành 2-3 câu ngắn gọn, súc tích bằng tiếng Việt. 
            Chỉ trả về nội dung tóm tắt, không thêm giải thích hoặc tiêu đề.
            
            **Nội dung bài viết**:
            $content
        """.trimIndent()

        Log.d(TAG, "Gửi yêu cầu tóm tắt tới Gemini API")
        val result = geminiService.generateResponse(prompt)
        result.fold(
            onSuccess = { summary ->
                Log.d(TAG, "Tóm tắt thành công: $summary")
                UiState.Success(summary.trim())
            },
            onFailure = { error ->
                Log.e(TAG, "Lỗi khi tóm tắt: ${error.message}")
                UiState.Error("Không thể tóm tắt: ${error.message}")
            }
        )
    }
}