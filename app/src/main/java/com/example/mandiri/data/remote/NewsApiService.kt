package com.example.mandiri.data.remote

import com.example.mandiri.BuildConfig
import com.example.mandiri.data.remote.model.NewsResponseDto
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.GET
import retrofit2.http.Query

interface NewsApiService {
    @GET("top-headlines")
    suspend fun getTopHeadlines(
        @Query("country") country: String = "id",
        @Query("pageSize") pageSize: Int = 1
    ): NewsResponseDto

    @GET("everything")
    suspend fun getEverything(
        @Query("q") query: String,
        @Query("page") page: Int,
        @Query("pageSize") pageSize: Int,
        @Query("sortBy") sortBy: String = "publishedAt",
        @Query("language") language: String = "id"
    ): NewsResponseDto

    companion object {
        private const val BASE_URL = "https://newsapi.org/v2/"

        fun create(): NewsApiService {
            val apiKeyInterceptor = Interceptor { chain ->
                val original = chain.request()
                val authorised: Request = original.newBuilder()
                    .addHeader("X-Api-Key", BuildConfig.NEWS_API_KEY)
                    .build()
                chain.proceed(authorised)
            }

            val logging = HttpLoggingInterceptor().apply {
                level = HttpLoggingInterceptor.Level.BASIC
            }

            val client = OkHttpClient.Builder()
                .addInterceptor(apiKeyInterceptor)
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
