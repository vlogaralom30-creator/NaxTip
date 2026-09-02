package com.example.data.repository

import com.example.data.local.AuthPreferences
import com.example.data.model.TikTokExtractResult
import com.example.data.model.TikPromPost
import com.example.data.model.User
import com.example.data.remote.SupabaseClient
import com.example.data.remote.TikTokExtractorService
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.util.UUID

class TikPromRepository(
    private val supabaseClient: SupabaseClient,
    private val extractorService: TikTokExtractorService,
    private val authPreferences: AuthPreferences
) {
    private val _currentUser = MutableStateFlow<User?>(authPreferences.currentUser)
    val currentUser: StateFlow<User?> = _currentUser.asStateFlow()

    private val _posts = MutableStateFlow<List<TikPromPost>>(emptyList())
    val posts: StateFlow<List<TikPromPost>> = _posts.asStateFlow()

    private val _likedPostIds = MutableStateFlow<Set<String>>(authPreferences.getLikedPostIds())
    val likedPostIds: StateFlow<Set<String>> = _likedPostIds.asStateFlow()

    private val _savedPromptIds = MutableStateFlow<Set<String>>(authPreferences.getSavedPromptIds())
    val savedPromptIds: StateFlow<Set<String>> = _savedPromptIds.asStateFlow()

    val isLoggedIn: Boolean
        get() = authPreferences.isLoggedIn && _currentUser.value != null

    suspend fun loadFeedPosts(): Result<List<TikPromPost>> {
        val result = supabaseClient.fetchPosts(authToken = authPreferences.authToken)
        result.onSuccess { fetched ->
            _posts.value = fetched
        }
        return result
    }

    suspend fun extractTikTok(url: String): TikTokExtractResult {
        return extractorService.extract(url)
    }

    suspend fun createPost(
        title: String,
        description: String,
        aiPrompt: String,
        promptModel: String,
        tiktokUrl: String,
        videoUrl: String,
        thumbnailUrl: String
    ): Result<TikPromPost> {
        val user = _currentUser.value
        if (user == null || !authPreferences.isLoggedIn) {
            return Result.failure(Exception("Please log in to upload AI videos and prompts"))
        }

        val newPost = TikPromPost(
            id = UUID.randomUUID().toString(),
            userId = user.id,
            authorName = user.fullName.ifEmpty { user.username },
            authorUsername = if (user.username.startsWith("@")) user.username else "@${user.username}",
            authorAvatar = user.avatarUrl.ifEmpty { "https://api.dicebear.com/7.x/bottts/png?seed=${user.username}" },
            title = title,
            description = description,
            aiPrompt = aiPrompt,
            promptModel = promptModel,
            tiktokUrl = tiktokUrl,
            videoUrl = videoUrl,
            thumbnailUrl = thumbnailUrl,
            likesCount = 0,
            copiesCount = 0,
            sharesCount = 0,
            createdAt = java.text.SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", java.util.Locale.US).apply {
                timeZone = java.util.TimeZone.getTimeZone("UTC")
            }.format(java.util.Date())
        )

        val uploadResult = supabaseClient.createPost(newPost, authToken = authPreferences.authToken)
        uploadResult.onSuccess { createdPost ->
            _posts.value = listOf(createdPost) + _posts.value
        }
        return uploadResult
    }

    suspend fun toggleLike(postId: String): Boolean {
        val currentlyLiked = _likedPostIds.value.contains(postId)
        val newLiked = !currentlyLiked
        
        authPreferences.setPostLiked(postId, newLiked)
        _likedPostIds.value = authPreferences.getLikedPostIds()

        // Update post like count in memory
        val updatedList = _posts.value.map { p ->
            if (p.id == postId) {
                val newCount = if (newLiked) p.likesCount + 1 else maxOf(0, p.likesCount - 1)
                p.copy(likesCount = newCount)
            } else p
        }
        _posts.value = updatedList

        // Sync to Supabase
        val targetPost = updatedList.find { it.id == postId }
        if (targetPost != null) {
            supabaseClient.updateLikesCount(postId, targetPost.likesCount, authPreferences.authToken)
        }
        val user = _currentUser.value
        if (user != null && user.id.isNotEmpty()) {
            supabaseClient.syncLike(user.id, postId, newLiked, authPreferences.authToken)
        }
        return newLiked
    }

    suspend fun toggleSavePrompt(postId: String): Boolean {
        val currentlySaved = _savedPromptIds.value.contains(postId)
        val newSaved = !currentlySaved
        authPreferences.setPromptSaved(postId, newSaved)
        _savedPromptIds.value = authPreferences.getSavedPromptIds()
        
        val user = _currentUser.value
        if (user != null && user.id.isNotEmpty()) {
            supabaseClient.syncBookmark(user.id, postId, newSaved, authPreferences.authToken)
        }
        return newSaved
    }

    suspend fun recordPromptCopy(postId: String) {
        val updatedList = _posts.value.map { p ->
            if (p.id == postId) {
                val newCount = p.copiesCount + 1
                p.copy(copiesCount = newCount)
            } else p
        }
        _posts.value = updatedList

        val targetPost = updatedList.find { it.id == postId }
        if (targetPost != null) {
            supabaseClient.incrementCopyCount(postId, targetPost.copiesCount, authPreferences.authToken)
        }
    }

    suspend fun signUp(email: String, pass: String, username: String, fullName: String): Result<User> {
        val res = supabaseClient.signUp(email, pass, username, fullName)
        return if (res.isSuccess) {
            val (user, token) = res.getOrThrow()
            authPreferences.authToken = token
            authPreferences.currentUser = user
            _currentUser.value = user
            Result.success(user)
        } else {
            Result.failure(res.exceptionOrNull() ?: Exception("Sign up failed"))
        }
    }

    suspend fun signIn(email: String, pass: String): Result<User> {
        val res = supabaseClient.signIn(email, pass)
        return if (res.isSuccess) {
            val (user, token) = res.getOrThrow()
            authPreferences.authToken = token
            authPreferences.currentUser = user
            _currentUser.value = user
            Result.success(user)
        } else {
            Result.failure(res.exceptionOrNull() ?: Exception("Sign in failed"))
        }
    }

    suspend fun updateProfile(updatedUser: User): Result<User> {
        val res = supabaseClient.upsertProfile(updatedUser, authPreferences.authToken)
        return if (res.isSuccess) {
            authPreferences.currentUser = updatedUser
            _currentUser.value = updatedUser
            Result.success(updatedUser)
        } else {
            // Update locally even if network has quirks
            authPreferences.currentUser = updatedUser
            _currentUser.value = updatedUser
            Result.success(updatedUser)
        }
    }

    fun logout() {
        authPreferences.clearSession()
        _currentUser.value = null
        _likedPostIds.value = emptySet()
        _savedPromptIds.value = emptySet()
    }
}
