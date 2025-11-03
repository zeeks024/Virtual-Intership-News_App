package com.example.mandiri.ui.navigation

sealed class Screen(val route: String) {
    data object Home : Screen("home")
    data object ArticleDetail : Screen("article_detail/{articleUrl}") {
        fun createRoute(articleUrl: String): String {
            return "article_detail/${java.net.URLEncoder.encode(articleUrl, "UTF-8")}"
        }
    }
}
