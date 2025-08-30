package com.example.myapplication.data.model

/**
 * Enum class đại diện cho các danh mục tin tức.
 * Mỗi danh mục bao gồm giá trị API (dùng để gọi API) và tên hiển thị (dùng trong giao diện).
 */
enum class NewsCategory(val apiValue: String, val displayName: String) {
    LATEST("news", "Mới nhất"),
    FOR_YOU("for_you", "Dành cho bạn"),
    TOP("top", "Nổi bật"),
    BUSINESS("business", "Kinh doanh"),
    ENTERTAINMENT("entertainment", "Giải trí"),
    ENVIRONMENT("environment", "Môi trường"),
    FOOD("food", "Ẩm thực"),
    HEALTH("health", "Sức khỏe"),
    POLITICS("politics", "Chính trị"),
    SCIENCE("science", "Khoa học"),
    SPORTS("sports", "Thể thao"),
    TECHNOLOGY("technology", "Công nghệ"),
    WORLD("world", "Thế giới"),
    TRAVEL("travel", "Du lịch"),
    EDUCATION("education", "Giáo dục"),
    FASHION("fashion", "Thời trang"),
    LIFESTYLE("lifestyle", "Phong cách sống"),
    AUTOMOTIVE("automotive", "Ô tô"),
    FINANCE("finance", "Tài chính"),
    GAMING("gaming", "Trò chơi"),
    WEATHER("weather", "Thời tiết"),
    CRIME("crime", "Tội phạm"),
    CULTURE("culture", "Văn hóa"),
    HISTORY("history", "Lịch sử"),
    MUSIC("music", "Âm nhạc"),
    MOVIES("movies", "Phim ảnh");

    companion object {
        /**
         * Lấy danh sách tất cả tên hiển thị của các danh mục.
         */
        fun getAllDisplayNames(): List<String> = NewsCategory.entries.map { it.displayName }

        /**
         * Tìm danh mục theo tên hiển thị.
         */
        fun fromDisplayName(displayName: String): NewsCategory? =
            NewsCategory.entries.find { it.displayName.equals(displayName, ignoreCase = true) }

        /**
         * Tìm danh mục theo giá trị API.
         */
        fun fromApiValue(apiValue: String): NewsCategory? =
            NewsCategory.entries.find { it.apiValue.equals(apiValue, ignoreCase = true) }

        /**
         * Chuyển danh sách tên danh mục thành chuỗi hiển thị.
         */
        fun joinCategories(categories: List<String>): String =
            categories.joinToString(", ")
    }
}