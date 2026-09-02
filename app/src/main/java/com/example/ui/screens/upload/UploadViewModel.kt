package com.example.ui.screens.upload

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.model.TikTokExtractResult
import com.example.data.model.TikPromPost
import com.example.data.repository.TikPromRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

sealed class UploadStatus {
    object Idle : UploadStatus()
    object Extracting : UploadStatus()
    data class Extracted(val result: TikTokExtractResult) : UploadStatus()
    object Publishing : UploadStatus()
    data class Success(val post: TikPromPost) : UploadStatus()
    data class Error(val message: String) : UploadStatus()
}

class UploadViewModel(
    private val repository: TikPromRepository
) : ViewModel() {

    val currentUser = repository.currentUser
    val isLoggedIn: Boolean
        get() = repository.isLoggedIn

    private val _status = MutableStateFlow<UploadStatus>(UploadStatus.Idle)
    val status: StateFlow<UploadStatus> = _status.asStateFlow()

    var tiktokUrl = MutableStateFlow("")
    var title = MutableStateFlow("")
    var description = MutableStateFlow("")
    var aiPrompt = MutableStateFlow("")
    var selectedModel = MutableStateFlow("Midjourney v6")
    var extractedVideoUrl = MutableStateFlow("")
    var extractedThumbnailUrl = MutableStateFlow("")

    val availableModels = listOf(
        "Midjourney v6",
        "OpenAI Sora",
        "Runway Gen-3",
        "Flux 1.1",
        "Kling AI",
        "Luma Dream Machine",
        "ChatGPT-4o",
        "Stable Diffusion 3",
        "Pika 2.0"
    )

    fun extractTikTokLink() {
        val url = tiktokUrl.value.trim()
        if (url.isEmpty()) {
            _status.value = UploadStatus.Error("Please enter a TikTok or video URL first")
            return
        }

        viewModelScope.launch {
            _status.value = UploadStatus.Extracting
            val res = repository.extractTikTok(url)
            if (res.success) {
                extractedVideoUrl.value = res.videoUrl
                extractedThumbnailUrl.value = res.coverUrl
                if (title.value.isBlank()) {
                    title.value = res.title
                }
                if (description.value.isBlank()) {
                    description.value = res.title
                }
                _status.value = UploadStatus.Extracted(res)
            } else {
                _status.value = UploadStatus.Error(res.errorMessage ?: "Extraction failed. Check the link or try another.")
            }
        }
    }

    fun publishPost(onSuccess: (TikPromPost) -> Unit) {
        val vUrl = extractedVideoUrl.value.ifEmpty { tiktokUrl.value.trim() }
        val prompt = aiPrompt.value.trim()

        if (vUrl.isEmpty()) {
            _status.value = UploadStatus.Error("Please provide a video URL or extract a TikTok link first")
            return
        }

        if (prompt.isEmpty()) {
            _status.value = UploadStatus.Error("Please enter the AI Prompt used for this video")
            return
        }

        viewModelScope.launch {
            _status.value = UploadStatus.Publishing
            val result = repository.createPost(
                title = title.value.ifEmpty { "AI Generated TikTok" },
                description = description.value.ifEmpty { title.value },
                aiPrompt = prompt,
                promptModel = selectedModel.value,
                tiktokUrl = tiktokUrl.value.trim(),
                videoUrl = vUrl,
                thumbnailUrl = extractedThumbnailUrl.value
            )

            result.fold(
                onSuccess = { createdPost ->
                    _status.value = UploadStatus.Success(createdPost)
                    resetForm()
                    onSuccess(createdPost)
                },
                onFailure = { err ->
                    _status.value = UploadStatus.Error(err.message ?: "Failed to publish to Supabase")
                }
            )
        }
    }

    fun resetForm() {
        tiktokUrl.value = ""
        title.value = ""
        description.value = ""
        aiPrompt.value = ""
        extractedVideoUrl.value = ""
        extractedThumbnailUrl.value = ""
        _status.value = UploadStatus.Idle
    }
}
