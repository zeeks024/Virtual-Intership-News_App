package com.example.mandiri.ui

import com.example.mandiri.model.NewsArticle

data class NewsUiState(
    val headline: NewsArticle? = null,
    val articles: List<NewsArticle> = emptyList(),
    val isLoading: Boolean = false,
    val isPaginating: Boolean = false,
    val isEndReached: Boolean = false,
    val errorMessage: String? = null
)
