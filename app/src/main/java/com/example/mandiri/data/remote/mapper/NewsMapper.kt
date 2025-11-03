package com.example.mandiri.data.remote.mapper

import com.example.mandiri.data.remote.model.ArticleDto
import com.example.mandiri.model.NewsArticle
import java.time.OffsetDateTime
import java.time.format.DateTimeFormatter
import java.time.format.DateTimeParseException

private val dateParser = DateTimeFormatter.ISO_OFFSET_DATE_TIME

/** Maps network DTOs into domain models that Compose can render. */
fun ArticleDto.toDomain(): NewsArticle? {
    val safeUrl = url ?: return null
    val parsedDate = publishedAt?.let {
        try {
            OffsetDateTime.parse(it, dateParser)
        } catch (_: DateTimeParseException) {
            null
        }
    }
    return NewsArticle(
        title = title.orEmpty(),
        description = description,
        author = author,
        publishedAt = parsedDate,
        imageUrl = imageUrl,
        sourceName = source?.name,
        url = safeUrl
    )
}
