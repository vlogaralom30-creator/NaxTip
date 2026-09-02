package com.example.data.model

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class User(
    @Json(name = "id") val id: String = "",
    @Json(name = "email") val email: String = "",
    @Json(name = "username") val username: String = "",
    @Json(name = "full_name") val fullName: String = "",
    @Json(name = "avatar_url") val avatarUrl: String = "",
    @Json(name = "bio") val bio: String = "",
    @Json(name = "created_at") val createdAt: String = ""
)

@JsonClass(generateAdapter = true)
data class AuthResponse(
    @Json(name = "access_token") val accessToken: String? = null,
    @Json(name = "token_type") val tokenType: String? = null,
    @Json(name = "user") val user: SupabaseUserObject? = null
)

@JsonClass(generateAdapter = true)
data class SupabaseUserObject(
    @Json(name = "id") val id: String = "",
    @Json(name = "email") val email: String = "",
    @Json(name = "user_metadata") val userMetadata: Map<String, Any?>? = null
)
