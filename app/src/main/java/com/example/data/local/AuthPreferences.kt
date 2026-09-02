package com.example.data.local

import android.content.Context
import android.content.SharedPreferences
import com.example.data.model.User
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory

class AuthPreferences(context: Context) {
    private val prefs: SharedPreferences = context.getSharedPreferences("tikprom_auth_prefs", Context.MODE_PRIVATE)
    private val moshi = Moshi.Builder().add(KotlinJsonAdapterFactory()).build()
    private val userAdapter = moshi.adapter(User::class.java)

    companion object {
        private const val KEY_AUTH_TOKEN = "auth_token"
        private const val KEY_USER_ID = "user_id"
        private const val KEY_USER_JSON = "user_json"
        private const val KEY_LIKED_POSTS = "liked_posts_set"
        private const val KEY_SAVED_PROMPTS = "saved_prompts_set"
    }

    var authToken: String?
        get() = prefs.getString(KEY_AUTH_TOKEN, null)
        set(value) = prefs.edit().putString(KEY_AUTH_TOKEN, value).apply()

    var currentUserId: String?
        get() = prefs.getString(KEY_USER_ID, null)
        set(value) = prefs.edit().putString(KEY_USER_ID, value).apply()

    var currentUser: User?
        get() {
            val json = prefs.getString(KEY_USER_JSON, null) ?: return null
            return try {
                userAdapter.fromJson(json)
            } catch (e: Exception) {
                null
            }
        }
        set(value) {
            if (value != null) {
                prefs.edit().putString(KEY_USER_JSON, userAdapter.toJson(value)).apply()
                currentUserId = value.id
            } else {
                prefs.edit().remove(KEY_USER_JSON).remove(KEY_USER_ID).remove(KEY_AUTH_TOKEN).apply()
            }
        }

    val isLoggedIn: Boolean
        get() {
            val user = currentUser
            val token = authToken
            return !token.isNullOrEmpty() && user != null && user.id.isNotEmpty() && user.email != "guest@tikprom.app"
        }

    fun isPostLiked(postId: String): Boolean {
        val set = prefs.getStringSet(KEY_LIKED_POSTS, emptySet()) ?: emptySet()
        return set.contains(postId)
    }

    fun setPostLiked(postId: String, liked: Boolean) {
        val currentSet = prefs.getStringSet(KEY_LIKED_POSTS, emptySet())?.toMutableSet() ?: mutableSetOf()
        if (liked) {
            currentSet.add(postId)
        } else {
            currentSet.remove(postId)
        }
        prefs.edit().putStringSet(KEY_LIKED_POSTS, currentSet).apply()
    }

    fun isPromptSaved(postId: String): Boolean {
        val set = prefs.getStringSet(KEY_SAVED_PROMPTS, emptySet()) ?: emptySet()
        return set.contains(postId)
    }

    fun setPromptSaved(postId: String, saved: Boolean) {
        val currentSet = prefs.getStringSet(KEY_SAVED_PROMPTS, emptySet())?.toMutableSet() ?: mutableSetOf()
        if (saved) {
            currentSet.add(postId)
        } else {
            currentSet.remove(postId)
        }
        prefs.edit().putStringSet(KEY_SAVED_PROMPTS, currentSet).apply()
    }

    fun getLikedPostIds(): Set<String> {
        return prefs.getStringSet(KEY_LIKED_POSTS, emptySet()) ?: emptySet()
    }

    fun getSavedPromptIds(): Set<String> {
        return prefs.getStringSet(KEY_SAVED_PROMPTS, emptySet()) ?: emptySet()
    }

    fun clearSession() {
        prefs.edit()
            .remove(KEY_AUTH_TOKEN)
            .remove(KEY_USER_ID)
            .remove(KEY_USER_JSON)
            .apply()
    }
}
