package com.example.data.remote

import android.util.Log
import com.example.data.model.AuthResponse
import com.example.data.model.TikPromPost
import com.example.data.model.User
import com.squareup.moshi.Moshi
import com.squareup.moshi.Types
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import okhttp3.logging.HttpLoggingInterceptor
import org.json.JSONObject
import java.util.UUID
import java.util.concurrent.TimeUnit

class SupabaseClient(
    private val supabaseUrl: String = "https://wxrtmczgwzmljjelarlg.supabase.co",
    private val supabaseKey: String = "sb_publishable_ieRx3f9JUtx1eeqah7Xasw_M-m9odpN"
) {
    private val TAG = "SupabaseClient"
    private val jsonMediaType = "application/json; charset=utf-8".toMediaType()

    private val okHttpClient = OkHttpClient.Builder()
        .connectTimeout(30, TimeUnit.SECONDS)
        .readTimeout(30, TimeUnit.SECONDS)
        .writeTimeout(30, TimeUnit.SECONDS)
        .addInterceptor(HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.BODY
        })
        .build()

    private val moshi = Moshi.Builder().add(KotlinJsonAdapterFactory()).build()
    private val postAdapter = moshi.adapter(TikPromPost::class.java)
    private val postListAdapter = moshi.adapter<List<TikPromPost>>(
        Types.newParameterizedType(List::class.java, TikPromPost::class.java)
    )
    private val userAdapter = moshi.adapter(User::class.java)
    private val authResponseAdapter = moshi.adapter(AuthResponse::class.java)

    private fun buildRequest(
        path: String,
        authToken: String? = null
    ): Request.Builder {
        val url = if (path.startsWith("http")) path else "$supabaseUrl$path"
        val token = authToken ?: supabaseKey
        return Request.Builder()
            .url(url)
            .addHeader("apikey", supabaseKey)
            .addHeader("Authorization", "Bearer $token")
            .addHeader("Content-Type", "application/json")
    }

    suspend fun signUp(email: String, password: String, username: String, fullName: String): Result<Pair<User, String?>> = withContext(Dispatchers.IO) {
        try {
            val bodyJson = JSONObject().apply {
                put("email", email.trim())
                put("password", password)
                put("data", JSONObject().apply {
                    put("username", username.trim())
                    put("full_name", fullName.trim())
                })
            }
            val req = buildRequest("/auth/v1/signup")
                .post(bodyJson.toString().toRequestBody(jsonMediaType))
                .build()

            val resp = okHttpClient.newCall(req).execute()
            val respBody = resp.body?.string().orEmpty()
            if (!resp.isSuccessful) {
                val errorMsg = try {
                    JSONObject(respBody).optString("msg", JSONObject(respBody).optString("error_description", "Sign up failed: ${resp.code}"))
                } catch (e: Exception) {
                    "Sign up error: HTTP ${resp.code}"
                }
                return@withContext Result.failure(Exception(errorMsg))
            }

            val authResp = authResponseAdapter.fromJson(respBody)
            val userId = authResp?.user?.id ?: UUID.randomUUID().toString()
            val createdUser = User(
                id = userId,
                email = email.trim(),
                username = username.ifEmpty { email.substringBefore("@") },
                fullName = fullName.ifEmpty { username },
                avatarUrl = "https://api.dicebear.com/7.x/bottts/png?seed=$username",
                bio = "AI Prompt Creator on TikProm ✨",
                createdAt = System.currentTimeMillis().toString()
            )

            // Save profile to profiles table
            try {
                upsertProfile(createdUser, authResp?.accessToken)
            } catch (e: Exception) {
                Log.w(TAG, "Profile upsert note: ${e.message}")
            }

            Result.success(Pair(createdUser, authResp?.accessToken))
        } catch (e: Exception) {
            Log.e(TAG, "SignUp error", e)
            Result.failure(e)
        }
    }

    suspend fun signIn(email: String, password: String): Result<Pair<User, String?>> = withContext(Dispatchers.IO) {
        try {
            val bodyJson = JSONObject().apply {
                put("email", email.trim())
                put("password", password)
            }
            val req = buildRequest("/auth/v1/token?grant_type=password")
                .post(bodyJson.toString().toRequestBody(jsonMediaType))
                .build()

            val resp = okHttpClient.newCall(req).execute()
            val respBody = resp.body?.string().orEmpty()
            if (!resp.isSuccessful) {
                val errorMsg = try {
                    JSONObject(respBody).optString("error_description", JSONObject(respBody).optString("msg", "Invalid credentials"))
                } catch (e: Exception) {
                    "Login error: HTTP ${resp.code}"
                }
                return@withContext Result.failure(Exception(errorMsg))
            }

            val authResp = authResponseAdapter.fromJson(respBody)
            val userId = authResp?.user?.id ?: ""
            val userMeta = authResp?.user?.userMetadata
            val username = userMeta?.get("username")?.toString() ?: email.substringBefore("@")
            val fullName = userMeta?.get("full_name")?.toString() ?: username

            // Try to fetch profile from DB or use metadata
            val profile = getProfile(userId, authResp?.accessToken).getOrNull() ?: User(
                id = userId,
                email = email,
                username = username,
                fullName = fullName,
                avatarUrl = "https://api.dicebear.com/7.x/bottts/png?seed=$username",
                bio = "AI Prompt Creator on TikProm ✨"
            )

            Result.success(Pair(profile, authResp?.accessToken))
        } catch (e: Exception) {
            Log.e(TAG, "SignIn error", e)
            Result.failure(e)
        }
    }

    suspend fun getProfile(userId: String, authToken: String? = null): Result<User> = withContext(Dispatchers.IO) {
        try {
            val req = buildRequest("/rest/v1/profiles?id=eq.$userId&select=*", authToken)
                .get()
                .build()

            val resp = okHttpClient.newCall(req).execute()
            val respBody = resp.body?.string().orEmpty()
            if (resp.isSuccessful) {
                val userList = moshi.adapter<List<User>>(Types.newParameterizedType(List::class.java, User::class.java)).fromJson(respBody)
                val user = userList?.firstOrNull()
                if (user != null) {
                    return@withContext Result.success(user)
                }
            }
            Result.failure(Exception("Profile not found"))
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun upsertProfile(user: User, authToken: String? = null): Result<User> = withContext(Dispatchers.IO) {
        try {
            val body = userAdapter.toJson(user)
            val req = buildRequest("/rest/v1/profiles", authToken)
                .addHeader("Prefer", "resolution=merge-duplicates,return=representation")
                .post(body.toRequestBody(jsonMediaType))
                .build()

            val resp = okHttpClient.newCall(req).execute()
            if (resp.isSuccessful) {
                Result.success(user)
            } else {
                Result.failure(Exception("Failed to save profile: ${resp.code}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun fetchPosts(userIdFilter: String? = null, authToken: String? = null): Result<List<TikPromPost>> = withContext(Dispatchers.IO) {
        try {
            val path = if (userIdFilter.isNullOrEmpty()) {
                "/rest/v1/posts?select=*&order=created_at.desc"
            } else {
                "/rest/v1/posts?user_id=eq.$userIdFilter&select=*&order=created_at.desc"
            }

            val req = buildRequest(path, authToken)
                .get()
                .build()

            val resp = okHttpClient.newCall(req).execute()
            val respBody = resp.body?.string().orEmpty()
            if (resp.isSuccessful) {
                val posts = postListAdapter.fromJson(respBody) ?: emptyList()
                Result.success(posts)
            } else {
                Result.failure(Exception("Fetch posts failed: HTTP ${resp.code}"))
            }
        } catch (e: Exception) {
            Log.e(TAG, "Fetch posts error", e)
            Result.failure(e)
        }
    }

    suspend fun createPost(post: TikPromPost, authToken: String? = null): Result<TikPromPost> = withContext(Dispatchers.IO) {
        try {
            val json = postAdapter.toJson(post)
            val req = buildRequest("/rest/v1/posts", authToken)
                .addHeader("Prefer", "return=representation")
                .post(json.toRequestBody(jsonMediaType))
                .build()

            val resp = okHttpClient.newCall(req).execute()
            val respBody = resp.body?.string().orEmpty()
            if (resp.isSuccessful) {
                val createdList = postListAdapter.fromJson(respBody)
                val created = createdList?.firstOrNull() ?: post
                Result.success(created)
            } else {
                val errorMsg = try {
                    JSONObject(respBody).optString("message", "Failed to upload post: HTTP ${resp.code}")
                } catch (e: Exception) {
                    "Upload failed: HTTP ${resp.code}"
                }
                Result.failure(Exception(errorMsg))
            }
        } catch (e: Exception) {
            Log.e(TAG, "Create post error", e)
            Result.failure(e)
        }
    }

    suspend fun incrementCopyCount(postId: String, currentCount: Int, authToken: String? = null): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            val json = JSONObject().apply {
                put("copies_count", currentCount + 1)
            }.toString()

            val req = buildRequest("/rest/v1/posts?id=eq.$postId", authToken)
                .patch(json.toRequestBody(jsonMediaType))
                .build()

            okHttpClient.newCall(req).execute()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun updateLikesCount(postId: String, newCount: Int, authToken: String? = null): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            val json = JSONObject().apply {
                put("likes_count", newCount)
            }.toString()

            val req = buildRequest("/rest/v1/posts?id=eq.$postId", authToken)
                .patch(json.toRequestBody(jsonMediaType))
                .build()

            okHttpClient.newCall(req).execute()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // --- Relational Likes Operations ---
    suspend fun syncLike(userId: String, postId: String, isLiked: Boolean, authToken: String? = null): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            if (isLiked) {
                val json = JSONObject().apply {
                    put("user_id", userId)
                    put("post_id", postId)
                }.toString()

                val req = buildRequest("/rest/v1/likes", authToken)
                    .addHeader("Prefer", "resolution=merge-duplicates")
                    .post(json.toRequestBody(jsonMediaType))
                    .build()
                okHttpClient.newCall(req).execute()
            } else {
                val req = buildRequest("/rest/v1/likes?user_id=eq.$userId&post_id=eq.$postId", authToken)
                    .delete()
                    .build()
                okHttpClient.newCall(req).execute()
            }
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // --- Relational Bookmarks Operations ---
    suspend fun syncBookmark(userId: String, postId: String, isSaved: Boolean, authToken: String? = null): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            if (isSaved) {
                val json = JSONObject().apply {
                    put("user_id", userId)
                    put("post_id", postId)
                }.toString()

                val req = buildRequest("/rest/v1/bookmarks", authToken)
                    .addHeader("Prefer", "resolution=merge-duplicates")
                    .post(json.toRequestBody(jsonMediaType))
                    .build()
                okHttpClient.newCall(req).execute()
            } else {
                val req = buildRequest("/rest/v1/bookmarks?user_id=eq.$userId&post_id=eq.$postId", authToken)
                    .delete()
                    .build()
                okHttpClient.newCall(req).execute()
            }
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun fetchUserLikedPostIds(userId: String, authToken: String? = null): Result<List<String>> = withContext(Dispatchers.IO) {
        try {
            val req = buildRequest("/rest/v1/likes?user_id=eq.$userId&select=post_id", authToken)
                .get()
                .build()
            val resp = okHttpClient.newCall(req).execute()
            val respBody = resp.body?.string().orEmpty()
            if (resp.isSuccessful) {
                val list = mutableListOf<String>()
                val arr = org.json.JSONArray(respBody)
                for (i in 0 until arr.length()) {
                    val item = arr.getJSONObject(i)
                    item.optString("post_id").takeIf { it.isNotEmpty() }?.let { list.add(it) }
                }
                Result.success(list)
            } else {
                Result.failure(Exception("Failed to fetch user likes"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun fetchUserBookmarkPostIds(userId: String, authToken: String? = null): Result<List<String>> = withContext(Dispatchers.IO) {
        try {
            val req = buildRequest("/rest/v1/bookmarks?user_id=eq.$userId&select=post_id", authToken)
                .get()
                .build()
            val resp = okHttpClient.newCall(req).execute()
            val respBody = resp.body?.string().orEmpty()
            if (resp.isSuccessful) {
                val list = mutableListOf<String>()
                val arr = org.json.JSONArray(respBody)
                for (i in 0 until arr.length()) {
                    val item = arr.getJSONObject(i)
                    item.optString("post_id").takeIf { it.isNotEmpty() }?.let { list.add(it) }
                }
                Result.success(list)
            } else {
                Result.failure(Exception("Failed to fetch user bookmarks"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
