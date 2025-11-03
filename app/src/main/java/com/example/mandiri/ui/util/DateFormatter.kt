package com.example.mandiri.ui.util

import com.example.mandiri.model.NewsArticle
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Locale

private val displayFormatter: DateTimeFormatter =
    DateTimeFormatter.ofPattern("dd MMM yyyy | HH.mm", Locale("id", "ID"))

fun NewsArticle.formattedPublishedAt(): String? {
    val published = publishedAt ?: return null
    return published.atZoneSameInstant(ZoneId.systemDefault()).format(displayFormatter)
}
