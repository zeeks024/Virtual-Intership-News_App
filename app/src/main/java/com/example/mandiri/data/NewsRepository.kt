package com.example.mandiri.data

import com.example.mandiri.BuildConfig
import com.example.mandiri.data.remote.NewsApiService
import com.example.mandiri.data.remote.mapper.toDomain
import com.example.mandiri.model.NewsArticle

private const val DEFAULT_QUERY = "bank mandiri OR ekonomi"
private const val DEFAULT_PAGE_SIZE = 20

class NewsRepository(private val apiService: NewsApiService) {
    suspend fun fetchHeadline(): Result<NewsArticle> {
        if (BuildConfig.NEWS_API_KEY.isBlank()) {
            return Result.failure(IllegalStateException("NEWS_API_KEY belum diatur"))
        }
        return runCatching {
            val response = apiService.getTopHeadlines()
            val article = response.articles.firstOrNull()?.toDomain()
            article ?: throw IllegalStateException("Headline tidak tersedia")
        }
    }

    suspend fun fetchNewsPage(page: Int, pageSize: Int = DEFAULT_PAGE_SIZE): Result<List<NewsArticle>> {
        if (BuildConfig.NEWS_API_KEY.isBlank()) {
            return Result.failure(IllegalStateException("NEWS_API_KEY belum diatur"))
        }
        return runCatching {
            val response = apiService.getEverything(
                query = DEFAULT_QUERY,
                page = page,
                pageSize = pageSize
            )
            response.articles.mapNotNull { it.toDomain() }
        }
    }

    suspend fun searchNews(query: String, page: Int, pageSize: Int = DEFAULT_PAGE_SIZE): Result<List<NewsArticle>> {
        if (BuildConfig.NEWS_API_KEY.isBlank()) {
            return Result.failure(IllegalStateException("NEWS_API_KEY belum diatur"))
        }
        return runCatching {
            val response = apiService.getEverything(
                query = query,
                page = page,
                pageSize = pageSize
            )
            response.articles.mapNotNull { it.toDomain() }
        }
    }
}
