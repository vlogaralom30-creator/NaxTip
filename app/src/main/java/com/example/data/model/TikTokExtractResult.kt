package com.example.data.model

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

data class TikTokExtractResult(
    val success: Boolean,
    val isPhotoPost: Boolean = false,
    val videoUrl: String = "",
    val coverUrl: String = "",
    val imageUrls: List<String> = emptyList(),
    val images: List<String> = imageUrls,
    val title: String = "",
    val authorName: String = "",
    val authorUsername: String = "",
    val authorAvatar: String = "",
    val musicTitle: String = "",
    val durationSeconds: Int = 0,
    val errorMessage: String? = null
)

@JsonClass(generateAdapter = true)
data class TikwmApiResponse(
    @Json(name = "code") val code: Int? = 0,
    @Json(name = "msg") val msg: String? = null,
    @Json(name = "data") val data: TikwmData? = null
)

@JsonClass(generateAdapter = true)
data class TikwmData(
    @Json(name = "id") val id: String? = null,
    @Json(name = "title") val title: String? = null,
    @Json(name = "cover") val cover: String? = null,
    @Json(name = "origin_cover") val originCover: String? = null,
    @Json(name = "play") val play: String? = null,
    @Json(name = "wmplay") val wmplay: String? = null,
    @Json(name = "hdplay") val hdplay: String? = null,
    @Json(name = "music") val music: String? = null,
    @Json(name = "images") val images: List<String>? = null,
    @Json(name = "duration") val duration: Int? = null,
    @Json(name = "music_info") val musicInfo: TikwmMusic? = null,
    @Json(name = "author") val author: TikwmAuthor? = null
)

@JsonClass(generateAdapter = true)
data class TikwmAuthor(
    @Json(name = "id") val id: String? = null,
    @Json(name = "unique_id") val uniqueId: String? = null,
    @Json(name = "nickname") val nickname: String? = null,
    @Json(name = "avatar") val avatar: String? = null
)

@JsonClass(generateAdapter = true)
data class TikwmMusic(
    @Json(name = "title") val title: String? = null,
    @Json(name = "author") val author: String? = null
)
