package com.example.ui.screens.profile

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.model.TikPromPost
import com.example.data.model.User
import com.example.data.repository.TikPromRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

enum class ProfileTab {
    MY_POSTS,
    SAVED_PROMPTS,
    LIKED_POSTS
}

class ProfileViewModel(
    private val repository: TikPromRepository
) : ViewModel() {

    val currentUser = repository.currentUser
    val posts = repository.posts
    val likedIds = repository.likedPostIds
    val savedIds = repository.savedPromptIds

    val isLoggedIn: Boolean
        get() = repository.isLoggedIn

    private val _selectedTab = MutableStateFlow(ProfileTab.MY_POSTS)
    val selectedTab: StateFlow<ProfileTab> = _selectedTab.asStateFlow()

    private val _isEditing = MutableStateFlow(false)
    val isEditing: StateFlow<Boolean> = _isEditing.asStateFlow()

    fun selectTab(tab: ProfileTab) {
        _selectedTab.value = tab
    }

    fun openEdit() {
        _isEditing.value = true
    }

    fun closeEdit() {
        _isEditing.value = false
    }

    fun saveProfile(username: String, fullName: String, bio: String) {
        val current = currentUser.value ?: return
        val updated = current.copy(
            username = username.trim(),
            fullName = fullName.trim(),
            bio = bio.trim(),
            avatarUrl = "https://api.dicebear.com/7.x/bottts/png?seed=$username"
        )
        viewModelScope.launch {
            repository.updateProfile(updated)
            _isEditing.value = false
        }
    }

    fun logout() {
        repository.logout()
    }
}
