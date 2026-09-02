package com.example.ui.screens.feed

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.model.TikPromPost
import com.example.data.repository.TikPromRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

sealed class FeedUiState {
    object Loading : FeedUiState()
    data class Success(val posts: List<TikPromPost>) : FeedUiState()
    data class Empty(val message: String = "No AI prompt videos yet. Be the first to upload!") : FeedUiState()
    data class Error(val message: String) : FeedUiState()
}

class FeedViewModel(
    private val repository: TikPromRepository
) : ViewModel() {

    val currentUser = repository.currentUser
    val isLoggedIn: Boolean
        get() = repository.isLoggedIn

    private val _uiState = MutableStateFlow<FeedUiState>(FeedUiState.Loading)
    val uiState: StateFlow<FeedUiState> = _uiState.asStateFlow()

    val likedPostIds = repository.likedPostIds
    val savedPromptIds = repository.savedPromptIds

    init {
        loadFeed()
    }

    fun loadFeed() {
        viewModelScope.launch {
            _uiState.value = FeedUiState.Loading
            val result = repository.loadFeedPosts()
            result.fold(
                onSuccess = { posts ->
                    if (posts.isEmpty()) {
                        _uiState.value = FeedUiState.Empty()
                    } else {
                        _uiState.value = FeedUiState.Success(posts)
                    }
                },
                onFailure = { error ->
                    _uiState.value = FeedUiState.Error(error.message ?: "Failed to load posts from Supabase")
                }
            )
        }
    }

    fun toggleLike(postId: String) {
        viewModelScope.launch {
            repository.toggleLike(postId)
            // Refresh posts state if current is Success
            val current = _uiState.value
            if (current is FeedUiState.Success) {
                _uiState.value = FeedUiState.Success(repository.posts.value)
            }
        }
    }

    fun toggleSave(postId: String, onResult: (Boolean) -> Unit = {}) {
        viewModelScope.launch {
            val saved = repository.toggleSavePrompt(postId)
            onResult(saved)
        }
    }

    fun copyPrompt(post: TikPromPost) {
        viewModelScope.launch {
            repository.recordPromptCopy(post.id)
            val current = _uiState.value
            if (current is FeedUiState.Success) {
                _uiState.value = FeedUiState.Success(repository.posts.value)
            }
        }
    }
}
