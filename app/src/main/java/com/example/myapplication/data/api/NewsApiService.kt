package com.example.myapplication.data.api

import com.example.myapplication.data.model.NewsResponse
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.GET
import retrofit2.http.Query
import java.util.concurrent.TimeUnit

/**
 * Interface để gọi API tin tức từ World News API.
 */
interface NewsApiService {
    @GET("search-news")
    suspend fun getNews(
        @Query("api-key") apiKey: String = "c271c457c2f6444bb22a6f57f8bfc845",
        @Query("category") category: String? = null,
        @Query("language") language: String = "vi",
        @Query("offset") offset: Int = 0,
        @Query("number") number: Int = 100,
        @Query("text") text: String? = "tin tức",
        @Query("sort") sort: String? = "publish-time"
    ): NewsResponse

    companion object {
        private const val BASE_URL = "https://api.worldnewsapi.com/"

        fun create(): NewsApiService {
            val logging = HttpLoggingInterceptor().apply {
                level = HttpLoggingInterceptor.Level.BODY
            }
            val client = OkHttpClient.Builder()
                .addInterceptor(logging)
                .connectTimeout(30, TimeUnit.SECONDS)
                .readTimeout(30, TimeUnit.SECONDS)
                .writeTimeout(30, TimeUnit.SECONDS)
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