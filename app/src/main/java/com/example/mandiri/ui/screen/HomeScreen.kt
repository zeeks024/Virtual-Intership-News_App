package com.example.mandiri.ui.screen

import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberTopAppBarState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.mandiri.model.NewsArticle
import com.example.mandiri.ui.NewsViewModel

@OptIn(ExperimentalMaterialApi::class, ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    viewModel: NewsViewModel,
    onArticleClick: (NewsArticle) -> Unit
) {
    val snackbarHostState = remember { SnackbarHostState() }
    val uiState = viewModel.uiState.collectAsStateWithLifecycle().value

    LaunchedEffect(uiState.errorMessage, uiState.articles.isNotEmpty()) {
        val message = uiState.errorMessage
        if (message != null && uiState.articles.isNotEmpty()) {
            snackbarHostState.showSnackbar(message)
        }
    }

    News(
        state = uiState,
        snackbarHost = { SnackbarHost(snackbarHostState) },
        onRefresh = viewModel::refresh,
        onLoadMore = viewModel::loadNextPage,
        onRetry = viewModel::refresh,
        onArticleClick = onArticleClick,
        scrollBehavior = TopAppBarDefaults.enterAlwaysScrollBehavior(rememberTopAppBarState()),
        onSearch = viewModel::search,
        onSetTheme = { theme -> /* TODO */ }
    )
}
