package com.example.myapplication.ai

import android.content.Context

/**
 * Class giả định để phân loại bài viết bằng AI.
 */
class AiClassifier(private val context: Context) {

    /**
     * Phân loại bài viết thành danh sách danh mục.
     */
    fun classifyArticle(title: String, content: String?): List<String> {
        // Giả định: Logic phân loại AI trả về danh sách danh mục
        val categories = mutableListOf<String>()
        val text = "$title ${content ?: ""}".lowercase()

        if (text.contains("kinh doanh") || text.contains("tài chính")) categories.add("business")
        if (text.contains("giải trí") || text.contains("phim ảnh")) categories.add("entertainment")
        if (text.contains("môi trường") || text.contains("thời tiết")) categories.add("environment")
        if (text.contains("ẩm thực") || text.contains("nấu ăn")) categories.add("food")
        if (text.contains("sức khỏe") || text.contains("y tế")) categories.add("health")
        if (text.contains("chính trị") || text.contains("bầu cử")) categories.add("politics")
        if (text.contains("khoa học") || text.contains("nghiên cứu")) categories.add("science")
        if (text.contains("thể thao") || text.contains("bóng đá")) categories.add("sports")
        if (text.contains("công nghệ") || text.contains("điện thoại")) categories.add("technology")
        if (text.contains("du lịch") || text.contains("đi lại")) categories.add("travel")
        if (text.contains("giáo dục") || text.contains("học tập")) categories.add("education")
        if (text.contains("thời trang") || text.contains("quần áo")) categories.add("fashion")
        if (text.contains("phong cách sống") || text.contains("lifestyle")) categories.add("lifestyle")
        if (text.contains("ô tô") || text.contains("xe hơi")) categories.add("automotive")
        if (text.contains("trò chơi") || text.contains("game")) categories.add("gaming")
        if (text.contains("tội phạm") || text.contains("pháp luật")) categories.add("crime")
        if (text.contains("văn hóa") || text.contains("truyền thống")) categories.add("culture")
        if (text.contains("lịch sử") || text.contains("quá khứ")) categories.add("history")
        if (text.contains("âm nhạc") || text.contains("ca sĩ")) categories.add("music")
        if (text.contains("phim ảnh") || text.contains("điện ảnh")) categories.add("movies")

        return categories.take(3) // Giới hạn tối đa 3 danh mục
    }

    /**
     * Tính độ liên quan của bài viết với từ khóa tìm kiếm.
     */
    fun filterArticle(title: String, content: String?, keyword: String): Int {
        // Giả định: Logic tính độ liên quan
        val text = "$title ${content ?: ""}".lowercase()
        return if (text.contains(keyword.lowercase())) 75 else 0
    }
}