package com.example.myapplication.ai

import android.util.Log
import com.example.myapplication.BuildConfig
import com.example.myapplication.data.model.NewsCategory
import com.google.ai.client.generativeai.GenerativeModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class GeminiService {
    private val TAG = "GeminiService"
    private val generativeModel = GenerativeModel(
        modelName = "gemini-2.0-flash",
        apiKey = BuildConfig.GEMINI_API_KEY
    )

    suspend fun generateResponse(prompt: String, context: String = ""): Result<String> = withContext(Dispatchers.IO) {
        try {
            val fullPrompt = """
                Bạn là một trợ lý AI thông minh, có khả năng trả lời các câu hỏi đa dạng bằng tiếng Việt, bao gồm cả câu hỏi về tin tức và các câu hỏi cơ bản khác (ví dụ: toán học, lịch sử, khoa học, đời sống, v.v.). Dựa trên thông tin ngữ cảnh (nếu có) và câu hỏi của người dùng, hãy trả lời một cách tự nhiên, chính xác và đúng trọng tâm.

                **Thông tin ngữ cảnh** (nếu có):
                $context
                
                **Câu hỏi người dùng**:
                $prompt
                
                - Nếu câu hỏi liên quan đến tin tức và có ngữ cảnh, hãy sử dụng thông tin ngữ cảnh để trả lời.
                - Nếu không có thông tin liên quan, hãy trả lời dựa trên kiến thức chung hoặc thông báo rằng bạn không có dữ liệu và đề xuất tìm kiếm thêm.
                - Trả lời ngắn gọn, rõ ràng, và bằng tiếng Việt.
            """.trimIndent()

            Log.d(TAG, "Gửi yêu cầu tới Gemini API với prompt: $fullPrompt")
            val response = generativeModel.generateContent(fullPrompt)
            val responseText = response.text ?: return@withContext Result.failure(Exception("Không nhận được phản hồi từ Gemini"))
            Log.d(TAG, "Nhận phản hồi từ Gemini: $responseText")
            Result.success(responseText)
        } catch (e: Exception) {
            Log.e(TAG, "Lỗi khi gọi Gemini API: ${e.message}", e)
            Result.failure(Exception("Lỗi khi xử lý câu hỏi: ${e.message}"))
        }
    }

    // Đề xuất danh mục dựa trên sở thích và lịch sử tìm kiếm
    suspend fun recommendCategories(categories: List<String>, searchHistory: List<String>): Result<List<String>> = withContext(Dispatchers.IO) {
        try {
            // Loại bỏ LATEST và FOR_YOU khỏi danh sách danh mục
            val allowedCategories = NewsCategory.entries
                .filter { it != NewsCategory.LATEST && it != NewsCategory.FOR_YOU }
                .joinToString(", ") { it.apiValue }

            val prompt = """
                Dựa trên sở thích và lịch sử tìm kiếm của người dùng, hãy đề xuất tối đa 5 danh mục tin tức phù hợp nhất từ danh sách sau: 
                $allowedCategories.
                
                **Sở thích người dùng**: ${categories.joinToString(", ")}
                **Lịch sử tìm kiếm**: ${searchHistory.joinToString(", ")}
                
                Trả về danh sách các danh mục dưới dạng chuỗi, phân tách bằng dấu phẩy, ví dụ: "technology,science,health".
                Nếu không có dữ liệu phù hợp, trả về danh sách mặc định: "top,world,business".
            """.trimIndent()

            Log.d(TAG, "Gửi yêu cầu đề xuất danh mục tới Gemini API với danh sách: $allowedCategories")
            val response = generativeModel.generateContent(prompt)
            val responseText = response.text?.trim() ?: return@withContext Result.failure(Exception("Không nhận được phản hồi từ Gemini"))

            val recommendedCategories = responseText.split(",")
                .map { it.trim() }
                .filter { category ->
                    NewsCategory.entries.any { it.apiValue == category } &&
                            category != NewsCategory.LATEST.apiValue &&
                            category != NewsCategory.FOR_YOU.apiValue
                }
                .distinct()

            Log.d(TAG, "Danh mục đề xuất từ Gemini sau khi lọc: $recommendedCategories")
            Result.success(if (recommendedCategories.isEmpty()) listOf("top", "world", "business") else recommendedCategories)
        } catch (e: Exception) {
            Log.e(TAG, "Lỗi khi đề xuất danh mục: ${e.message}", e)
            Result.success(listOf("top", "world", "business")) // Mặc định nếu lỗi
        }
    }
}