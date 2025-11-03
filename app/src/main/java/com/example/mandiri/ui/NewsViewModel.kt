package com.example.mandiri.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.mandiri.data.NewsRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class NewsViewModel(private val repository: NewsRepository) : ViewModel() {
    private val _uiState = MutableStateFlow(NewsUiState(isLoading = true))
    val uiState: StateFlow<NewsUiState> = _uiState

    private var currentPage = 1
    private var isLoadingPage = false
    private var currentQuery: String? = null

    init {
        refresh()
    }

    fun refresh() {
        currentQuery = null
        viewModelScope.launch {
            currentPage = 1
            _uiState.value = NewsUiState(isLoading = true)

            val headlineResult = repository.fetchHeadline()
            val articlesResult = repository.fetchNewsPage(page = currentPage)

            val headline = headlineResult.getOrNull()
            val articles = articlesResult.getOrNull().orEmpty()

            val error = articlesResult.exceptionOrNull()?.message

            _uiState.update {
                it.copy(
                    headline = headline,
                    articles = articles,
                    isLoading = false,
                    isPaginating = false,
                    isEndReached = articles.isEmpty(),
                    errorMessage = error
                )
            }
        }
    }

    fun loadNextPage() {
        val currentState = _uiState.value
        if (isLoadingPage || currentState.isEndReached || currentState.isLoading) {
            return
        }
        isLoadingPage = true
        val nextPage = currentPage + 1
        viewModelScope.launch {
            _uiState.update { it.copy(isPaginating = true, errorMessage = null) }
            val result = currentQuery?.let {
                repository.searchNews(query = it, page = nextPage)
            } ?: repository.fetchNewsPage(page = nextPage)

            val newArticles = result.getOrNull().orEmpty()
            _uiState.update { state ->
                val isSuccess = result.isSuccess
                state.copy(
                    articles = if (isSuccess) state.articles + newArticles else state.articles,
                    isPaginating = false,
                    isEndReached = if (isSuccess) newArticles.isEmpty() else state.isEndReached,
                    errorMessage = result.exceptionOrNull()?.message
                )
            }
            if (result.isSuccess && newArticles.isNotEmpty()) {
                currentPage = nextPage
            }
            isLoadingPage = false
        }
    }

    fun search(query: String) {
        currentQuery = query
        viewModelScope.launch {
            currentPage = 1
            _uiState.value = NewsUiState(isLoading = true)

            val searchResult = repository.searchNews(query = query, page = currentPage)
            val articles = searchResult.getOrNull().orEmpty()
            val error = searchResult.exceptionOrNull()?.message

            _uiState.update {
                it.copy(
                    headline = null, // No headline in search results
                    articles = articles,
                    isLoading = false,
                    isPaginating = false,
                    isEndReached = articles.isEmpty(),
                    errorMessage = error
                )
            }
        }
    }

    fun setTheme(theme: String) {
        // TODO: Implement theme preference saving
    }
}

class NewsViewModelFactory(private val repository: NewsRepository) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(NewsViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return NewsViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class ${modelClass.name}")
    }
}
