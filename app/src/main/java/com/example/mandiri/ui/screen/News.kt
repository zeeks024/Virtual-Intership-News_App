package com.example.mandiri.ui.screen

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.pullrefresh.PullRefreshIndicator
import androidx.compose.material.pullrefresh.pullRefresh
import androidx.compose.material.pullrefresh.rememberPullRefreshState
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextField
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarScrollBehavior
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import com.example.mandiri.R
import com.example.mandiri.model.NewsArticle
import com.example.mandiri.ui.NewsUiState
import com.example.mandiri.ui.components.ArticleListItem
import com.example.mandiri.ui.components.FullScreenError
import com.example.mandiri.ui.components.FullScreenLoading
import com.example.mandiri.ui.components.HeadlineCard
import com.example.mandiri.ui.components.PaginateFooter
import com.example.mandiri.ui.components.SectionLabel
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.distinctUntilChanged

@OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterialApi::class)
@Composable
fun News(
    state: NewsUiState,
    snackbarHost: @Composable () -> Unit,
    onRefresh: () -> Unit,
    onLoadMore: () -> Unit,
    onRetry: () -> Unit,
    onArticleClick: (NewsArticle) -> Unit,
    scrollBehavior: TopAppBarScrollBehavior,
    onSearch: (String) -> Unit,
    onSetTheme: (String) -> Unit
) {
    val listState = rememberLazyListState()
    val isRefreshing = state.isLoading && (state.articles.isNotEmpty() || state.headline != null)
    val pullRefreshState = rememberPullRefreshState(refreshing = isRefreshing, onRefresh = onRefresh)
    var showThemeDialog by remember { mutableStateOf(false) }

    if (showThemeDialog) {
        ThemeDialog(
            onDismiss = { showThemeDialog = false },
            onConfirm = {
                onSetTheme(it)
                showThemeDialog = false
            }
        )
    }

    LaunchedEffect(listState, state.isPaginating, state.isEndReached, state.isLoading, onLoadMore) {
        snapshotFlow {
            val layoutInfo = listState.layoutInfo
            layoutInfo.visibleItemsInfo.lastOrNull()?.index to layoutInfo.totalItemsCount
        }
            .distinctUntilChanged()
            .collectLatest { (lastVisible, total) ->
                if (!state.isLoading && !state.isPaginating && !state.isEndReached) {
                    if (lastVisible != null && total > 0 && lastVisible >= total - 2) {
                        onLoadMore()
                    }
                }
            }
    }
    Scaffold(
        modifier = Modifier.nestedScroll(scrollBehavior.nestedScrollConnection),
        topBar = {
            TopAppBar(
                title = { Text(text = stringResource(id = R.string.app_name)) },
                scrollBehavior = scrollBehavior,
                actions = {
                    IconButton(onClick = { showThemeDialog = true }) {
                        Icon(Icons.Filled.Settings, contentDescription = "Theme settings")
                    }
                }
            )
        },
        snackbarHost = snackbarHost
    ) { innerPadding ->
        Column(modifier = Modifier.padding(innerPadding)) {
            SearchBar(onSearch = onSearch)
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .pullRefresh(pullRefreshState)
            ) {
                when {
                    state.isLoading && state.articles.isEmpty() && state.headline == null -> {
                        FullScreenLoading()
                    }
                    state.errorMessage != null && state.articles.isEmpty() && state.headline == null -> {
                        FullScreenError(
                            message = state.errorMessage,
                            onRetry = onRetry
                        )
                    }
                    else -> {
                        LazyColumn(
                            state = listState,
                            modifier = Modifier.fillMaxSize(),
                            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 24.dp)
                        ) {
                            state.headline?.let { article ->
                                item(key = "headline_header") {
                                    SectionLabel(text = stringResource(R.string.headline_news))
                                    Spacer(modifier = Modifier.height(12.dp))
                                }
                                item(key = "headline_card") {
                                    HeadlineCard(
                                        article = article,
                                        onClick = { onArticleClick(article) }
                                    )
                                    Spacer(modifier = Modifier.height(24.dp))
                                }
                            }

                            item(key = "all_news_header") {
                                SectionLabel(text = stringResource(id = R.string.all_news))
                                Spacer(modifier = Modifier.height(12.dp))
                            }

                            items(
                                items = state.articles,
                                key = { it.url }
                            ) { article ->
                                ArticleListItem(
                                    article = article,
                                    onClick = { onArticleClick(article) }
                                )
                                Spacer(modifier = Modifier.height(12.dp))
                            }

                            if (state.isPaginating) {
                                item(key = "pagination") {
                                    PaginateFooter()
                                }
                            }
                        }
                    }
                }
                PullRefreshIndicator(
                    refreshing = isRefreshing,
                    state = pullRefreshState,
                    modifier = Modifier
                        .align(Alignment.TopCenter)
                        .padding(top = 16.dp)
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SearchBar(
    onSearch: (String) -> Unit
) {
    var text by remember { mutableStateOf("") }
    val keyboardController = LocalSoftwareKeyboardController.current
    TextField(
        value = text,
        onValueChange = { text = it },
        placeholder = { Text("Search News...") },
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        keyboardOptions = KeyboardOptions.Default.copy(
            imeAction = ImeAction.Search
        ),
        keyboardActions = KeyboardActions(
            onSearch = {
                onSearch(text)
                keyboardController?.hide()
            }
        )
    )
}

@Composable
private fun ThemeDialog(
    onDismiss: () -> Unit,
    onConfirm: (String) -> Unit
) {
    val themes = listOf("system", "light", "dark")
    var selectedTheme by remember { mutableStateOf(themes.first()) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Choose Theme") },
        text = {
            Column {
                themes.forEach { theme ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { selectedTheme = theme },
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        RadioButton(
                            selected = (theme == selectedTheme),
                            onClick = { selectedTheme = theme }
                        )
                        Text(
                            text = theme.replaceFirstChar { it.uppercase() },
                            modifier = Modifier.padding(start = 8.dp)
                        )
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = { onConfirm(selectedTheme) }) {
                Text("OK")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}