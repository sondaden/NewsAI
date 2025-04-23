package com.example.myapplication.data.remote

import com.example.myapplication.data.model.NewsResponse
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.GET
import retrofit2.http.Query

interface NewsApiService {
    @GET("search-news")
    suspend fun getNews(
        @Query("api-token") apiKey: String = "a3a7d654712d4fd8b979a7248f31ae60", // Thay bằng key của bạn
        @Query("category") category: String? = null,
        @Query("language") language: String = "vi", // Ngôn ngữ mặc định: Tiếng Việt
        @Query("offset") offset: Int = 0, // Hỗ trợ phân trang
        @Query("number") number: Int = 10 // Số bài viết mỗi lần gọi
    ): NewsResponse

    companion object {
        private const val BASE_URL = "https://api.worldnewsapi.com/"

        fun create(): NewsApiService {
            val logging = HttpLoggingInterceptor().apply {
                level = HttpLoggingInterceptor.Level.BODY // Log chi tiết cho debug
            }
            val client = OkHttpClient.Builder()
                .addInterceptor(logging)
                .build()

            return Retrofit.Builder()
                .baseUrl(BASE_URL)
                .client(client)
                .addConverterFactory(GsonConverterFactory.create())
                .build()
                .create(NewsApiService::class.java)
        }
    }
}