package com.example.data.model

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class TikPromPost(
    @Json(name = "id") val id: String = "",
    @Json(name = "user_id") val userId: String = "",
    @Json(name = "author_name") val authorName: String = "",
    @Json(name = "author_username") val authorUsername: String = "",
    @Json(name = "author_avatar") val authorAvatar: String = "",
    @Json(name = "title") val title: String = "",
    @Json(name = "description") val description: String = "",
    @Json(name = "ai_prompt") val aiPrompt: String = "",
    @Json(name = "prompt_model") val promptModel: String = "AI Prompt",
    @Json(name = "tiktok_url") val tiktokUrl: String = "",
    @Json(name = "video_url") val videoUrl: String = "",
    @Json(name = "thumbnail_url") val thumbnailUrl: String = "",
    @Json(name = "likes_count") val likesCount: Int = 0,
    @Json(name = "copies_count") val copiesCount: Int = 0,
    @Json(name = "shares_count") val sharesCount: Int = 0,
    @Json(name = "created_at") val createdAt: String = ""
) {
    val imageUrls: List<String>
        get() {
            if (thumbnailUrl.isBlank()) return emptyList()
            return when {
                thumbnailUrl.contains("|||") -> {
                    thumbnailUrl.split("|||").map { it.trim() }.filter { it.isNotEmpty() }
                }
                thumbnailUrl.contains(",") && thumbnailUrl.startsWith("http") -> {
                    thumbnailUrl.split(",").map { it.trim() }.filter { it.isNotEmpty() }
                }
                else -> {
                    listOf(thumbnailUrl.trim())
                }
            }
        }

    val isPhotoPost: Boolean
        get() = imageUrls.size > 1 || (imageUrls.isNotEmpty() && (videoUrl.isBlank() || videoUrl.endsWith(".mp3") || videoUrl.contains("music") || !videoUrl.contains(".mp4")))
}
