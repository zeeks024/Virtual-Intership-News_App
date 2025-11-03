package com.example.mandiri.ui.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.example.mandiri.ui.NewsViewModel
import com.example.mandiri.ui.screen.ArticleDetailScreen
import com.example.mandiri.ui.screen.HomeScreen
import java.net.URLDecoder

@Composable
fun MandiriNavHost(
    navController: NavHostController,
    viewModel: NewsViewModel
) {

    NavHost(
        navController = navController,
        startDestination = Screen.Home.route
    ) {
        composable(Screen.Home.route) {
            HomeScreen(
                viewModel = viewModel,
                onArticleClick = { article ->
                    navController.navigate(Screen.ArticleDetail.createRoute(article.url))
                }
            )
        }

        composable(
            route = Screen.ArticleDetail.route,
            arguments = listOf(
                navArgument("articleUrl") { type = NavType.StringType }
            )
        ) { backStackEntry ->
            val encodedUrl = backStackEntry.arguments?.getString("articleUrl") ?: ""
            val articleUrl = URLDecoder.decode(encodedUrl, "UTF-8")
            
            ArticleDetailScreen(
                articleUrl = articleUrl,
                onNavigateBack = { navController.popBackStack() }
            )
        }
    }
}
