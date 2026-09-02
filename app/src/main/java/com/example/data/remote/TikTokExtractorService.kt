package com.example.data.remote

import android.util.Log
import com.example.data.model.TikTokExtractResult
import com.example.data.model.TikwmApiResponse
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import java.net.URLEncoder
import java.util.concurrent.TimeUnit

class TikTokExtractorService {
    private val TAG = "TikTokExtractor"
    private val okHttpClient = OkHttpClient.Builder()
        .connectTimeout(25, TimeUnit.SECONDS)
        .readTimeout(25, TimeUnit.SECONDS)
        .followRedirects(true)
        .build()

    private val moshi = Moshi.Builder().add(KotlinJsonAdapterFactory()).build()
    private val tikwmAdapter = moshi.adapter(TikwmApiResponse::class.java)

    suspend fun extract(inputUrl: String): TikTokExtractResult = withContext(Dispatchers.IO) {
        val cleanUrl = inputUrl.trim()
        if (cleanUrl.isEmpty()) {
            return@withContext TikTokExtractResult(
                success = false,
                errorMessage = "Please enter a valid TikTok or video URL"
            )
        }

        // Direct video link support
        if (cleanUrl.endsWith(".mp4", ignoreCase = true) || 
            cleanUrl.contains(".mp4?", ignoreCase = true) ||
            cleanUrl.endsWith(".m3u8", ignoreCase = true) ||
            cleanUrl.endsWith(".webm", ignoreCase = true)) {
            val titleFromUrl = cleanUrl.substringAfterLast("/").substringBefore("?").substringBefore(".")
            return@withContext TikTokExtractResult(
                success = true,
                videoUrl = cleanUrl,
                coverUrl = "",
                title = titleFromUrl.replace("_", " ").replace("-", " "),
                authorName = "Creator",
                authorUsername = "@creator",
                authorAvatar = "https://api.dicebear.com/7.x/bottts/png?seed=creator",
                musicTitle = "Original Sound",
                durationSeconds = 15
            )
        }

        // Use TikWM API for TikTok URL extraction
        try {
            val encodedUrl = URLEncoder.encode(cleanUrl, "UTF-8")
            val apiUrl = "https://www.tikwm.com/api/?url=$encodedUrl&hd=1"

            val request = Request.Builder()
                .url(apiUrl)
                .addHeader("User-Agent", "Mozilla/5.0 (Linux; Android 10; K) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/120.0.0.0 Mobile Safari/537.36")
                .get()
                .build()

            val response = okHttpClient.newCall(request).execute()
            val bodyString = response.body?.string().orEmpty()

            if (!response.isSuccessful || bodyString.isEmpty()) {
                return@withContext fallbackExtract(cleanUrl, "API connection failed (${response.code})")
            }

            // Extract using both Moshi model and raw JSONObject for maximum resilience with photo posts
            val rawImagesList = mutableListOf<String>()
            var title = ""
            var authorName = "Creator"
            var uniqueId = "tiktok_creator"
            var avatar = ""
            var musicTitle = "Original Sound"
            var durationSec = 15
            var finalVideoUrl = ""

            try {
                val jsonRoot = org.json.JSONObject(bodyString)
                val code = jsonRoot.optInt("code", -1)
                val dataObj = jsonRoot.optJSONObject("data")

                if (code == 0 && dataObj != null) {
                    title = dataObj.optString("title", "")
                    val hdplay = dataObj.optString("hdplay", "")
                    val play = dataObj.optString("play", "")
                    val music = dataObj.optString("music", "")
                    val wmplay = dataObj.optString("wmplay", "")
                    
                    val rawVid = when {
                        hdplay.isNotEmpty() -> hdplay
                        play.isNotEmpty() -> play
                        music.isNotEmpty() -> music
                        else -> wmplay
                    }
                    finalVideoUrl = if (rawVid.startsWith("/")) "https://www.tikwm.com$rawVid" else rawVid

                    // Collect all photo slides from 'images', 'photos', etc.
                    val imagesArray = dataObj.optJSONArray("images") 
                        ?: dataObj.optJSONArray("photos")
                        ?: dataObj.optJSONArray("image_list")

                    if (imagesArray != null) {
                        for (i in 0 until imagesArray.length()) {
                            val imgItem = imagesArray.opt(i)
                            val imgUrl = when (imgItem) {
                                is String -> imgItem
                                is org.json.JSONObject -> imgItem.optString("url", imgItem.optString("display_image", ""))
                                else -> ""
                            }
                            if (imgUrl.isNotEmpty()) {
                                val fullImgUrl = if (imgUrl.startsWith("/")) "https://www.tikwm.com$imgUrl" else imgUrl
                                rawImagesList.add(fullImgUrl)
                            }
                        }
                    }

                    // Fallback to origin_cover or cover if no images array
                    if (rawImagesList.isEmpty()) {
                        val originCover = dataObj.optString("origin_cover", "")
                        val cover = dataObj.optString("cover", "")
                        val chosenCover = originCover.ifEmpty { cover }
                        if (chosenCover.isNotEmpty()) {
                            val fullCover = if (chosenCover.startsWith("/")) "https://www.tikwm.com$chosenCover" else chosenCover
                            rawImagesList.add(fullCover)
                        }
                    }

                    val authorObj = dataObj.optJSONObject("author")
                    if (authorObj != null) {
                        uniqueId = authorObj.optString("unique_id", "tiktok_creator").ifEmpty { "tiktok_creator" }
                        authorName = authorObj.optString("nickname", uniqueId).ifEmpty { uniqueId }
                        avatar = authorObj.optString("avatar", "")
                    }
                    if (avatar.isEmpty()) {
                        avatar = "https://api.dicebear.com/7.x/bottts/png?seed=$uniqueId"
                    }

                    val musicObj = dataObj.optJSONObject("music_info")
                    if (musicObj != null) {
                        musicTitle = musicObj.optString("title", "Original Sound").ifEmpty { "Original Sound" }
                    }
                    durationSec = dataObj.optInt("duration", 15)
                }
            } catch (jsonEx: Exception) {
                Log.w(TAG, "Raw JSON parse fallback error: ${jsonEx.message}")
            }

            if (rawImagesList.isNotEmpty() || finalVideoUrl.isNotEmpty()) {
                val isPhotoPost = rawImagesList.size > 1 || (rawImagesList.isNotEmpty() && (finalVideoUrl.isBlank() || finalVideoUrl.endsWith(".mp3") || finalVideoUrl.contains("music") || !finalVideoUrl.contains(".mp4")))
                val finalCoverUrl = rawImagesList.joinToString("|||")

                return@withContext TikTokExtractResult(
                    success = true,
                    isPhotoPost = isPhotoPost,
                    videoUrl = finalVideoUrl,
                    coverUrl = finalCoverUrl,
                    imageUrls = rawImagesList,
                    images = rawImagesList,
                    title = title,
                    authorName = authorName,
                    authorUsername = if (uniqueId.startsWith("@")) uniqueId else "@$uniqueId",
                    authorAvatar = avatar,
                    musicTitle = musicTitle,
                    durationSeconds = durationSec
                )
            } else {
                return@withContext fallbackExtract(cleanUrl, "Could not find video or photos in this TikTok post")
            }
        } catch (e: Exception) {
            Log.e(TAG, "Extract error", e)
            return@withContext fallbackExtract(cleanUrl, e.message ?: "Extraction failed")
        }
    }

    private fun fallbackExtract(url: String, reason: String): TikTokExtractResult {
        // Fallback: If extractor service has rate limits or network issues, provide graceful parsing so user can still proceed
        val extractedId = url.substringAfterLast("/").substringBefore("?").ifEmpty { "video" }
        return TikTokExtractResult(
            success = false,
            errorMessage = "Could not automatically fetch from TikTok: $reason. You can paste a direct video link or retry.",
            videoUrl = "",
            title = "",
            authorUsername = "@tiktok_user"
        )
    }
}
