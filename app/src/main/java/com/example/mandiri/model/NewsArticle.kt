package com.example.mandiri.model

import java.time.OffsetDateTime

/** Lightweight domain model for rendering news articles in the UI. */
data class NewsArticle(
    val title: String,
    val description: String?,
    val author: String?,
    val publishedAt: OffsetDateTime?,
    val imageUrl: String?,
    val sourceName: String?,
    val url: String
)
