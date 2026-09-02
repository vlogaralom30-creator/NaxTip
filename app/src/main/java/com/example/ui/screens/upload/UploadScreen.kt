package com.example.ui.screens.upload

import android.content.ClipboardManager
import android.content.Context
import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.ContentPaste
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.Link
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.PlayCircleOutline
import androidx.compose.material.icons.filled.Upload
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.components.TikTokVideoPlayer
import com.example.ui.theme.TikBlack
import com.example.ui.theme.TikCyan
import com.example.ui.theme.TikDarkSurface
import com.example.ui.theme.TikPink
import com.example.ui.theme.TikPurple
import com.example.ui.theme.TikSurfaceVariant
import com.example.ui.theme.TikWhite

@Composable
fun UploadScreen(
    viewModel: UploadViewModel,
    onUploadSuccess: () -> Unit,
    onNavigateToAuth: (Boolean) -> Unit = {},
    modifier: Modifier = Modifier
) {
    val isLoggedIn = viewModel.isLoggedIn
    val currentUser by viewModel.currentUser.collectAsState()

    if (!isLoggedIn || currentUser == null) {
        GuestUploadGateView(
            onNavigateToAuth = onNavigateToAuth,
            modifier = modifier
        )
        return
    }

    val context = LocalContext.current
    val status by viewModel.status.collectAsState()

    val tiktokUrl by viewModel.tiktokUrl.collectAsState()
    val title by viewModel.title.collectAsState()
    val description by viewModel.description.collectAsState()
    val aiPrompt by viewModel.aiPrompt.collectAsState()
    val selectedModel by viewModel.selectedModel.collectAsState()
    val extractedVideoUrl by viewModel.extractedVideoUrl.collectAsState()
    val extractedThumbnailUrl by viewModel.extractedThumbnailUrl.collectAsState()

    fun pasteFromClipboard() {
        val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
        val clip = clipboard.primaryClip
        if (clip != null && clip.itemCount > 0) {
            val text = clip.getItemAt(0).text?.toString().orEmpty()
            if (text.isNotEmpty()) {
                viewModel.tiktokUrl.value = text
                Toast.makeText(context, "Link pasted from clipboard!", Toast.LENGTH_SHORT).show()
                viewModel.extractTikTokLink()
            }
        }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(TikBlack)
            .statusBarsPadding()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 16.dp, vertical = 12.dp)
            .padding(bottom = 72.dp)
    ) {
        // Header
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(bottom = 16.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .clip(CircleShape)
                    .background(Brush.linearGradient(listOf(TikPink, TikCyan))),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Upload,
                    contentDescription = null,
                    tint = TikWhite,
                    modifier = Modifier.size(20.dp)
                )
            }
            Spacer(modifier = Modifier.width(10.dp))
            Column {
                Text(
                    text = "Upload AI Video Post",
                    color = TikWhite,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "Extract TikTok link & attach your AI prompt",
                    color = Color(0xFFA0A0B5),
                    fontSize = 12.sp
                )
            }
        }

        // 1. TikTok Link Input Card
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(16.dp))
                .background(TikDarkSurface)
                .border(1.dp, Color(0xFF262635), RoundedCornerShape(16.dp))
                .padding(14.dp)
        ) {
            Column {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Link,
                            contentDescription = null,
                            tint = TikCyan,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "TikTok Video URL",
                            color = TikWhite,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    // Paste Button
                    Button(
                        onClick = { pasteFromClipboard() },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = TikSurfaceVariant,
                            contentColor = TikCyan
                        ),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier
                            .height(32.dp)
                            .testTag("paste_link_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.ContentPaste,
                            contentDescription = "Paste",
                            modifier = Modifier.size(14.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(text = "Paste", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                OutlinedTextField(
                    value = tiktokUrl,
                    onValueChange = { viewModel.tiktokUrl.value = it },
                    placeholder = {
                        Text(
                            text = "https://www.tiktok.com/@... or https://vt.tiktok.com/...",
                            color = Color(0xFF6B6B7F),
                            fontSize = 13.sp
                        )
                    },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = TikCyan,
                        unfocusedBorderColor = Color(0xFF333345),
                        focusedTextColor = TikWhite,
                        unfocusedTextColor = TikWhite,
                        cursorColor = TikCyan
                    ),
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Uri,
                        imeAction = ImeAction.Done
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("tiktok_url_input")
                )

                Spacer(modifier = Modifier.height(12.dp))

                // Extract Button
                Button(
                    onClick = { viewModel.extractTikTokLink() },
                    enabled = status !is UploadStatus.Extracting,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = TikCyan,
                        contentColor = Color.Black
                    ),
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(44.dp)
                        .testTag("extract_video_button")
                ) {
                    if (status is UploadStatus.Extracting) {
                        CircularProgressIndicator(
                            color = Color.Black,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Extracting Video...",
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp
                        )
                    } else {
                        Icon(
                            imageVector = Icons.Default.Download,
                            contentDescription = null,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "Extract TikTok Video & Info",
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Video/Photo Player Preview (if extracted or provided)
        if (extractedVideoUrl.isNotEmpty() || extractedThumbnailUrl.isNotEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(260.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .background(Color.Black)
                    .border(2.dp, TikCyan, RoundedCornerShape(16.dp))
            ) {
                TikTokVideoPlayer(
                    videoUrl = extractedVideoUrl,
                    thumbnailUrl = extractedThumbnailUrl,
                    tiktokUrl = tiktokUrl,
                    isActive = true,
                    onDoubleTapLike = {}
                )

                // Extracted Badge
                Box(
                    modifier = Modifier
                        .align(Alignment.TopStart)
                        .padding(10.dp)
                        .clip(RoundedCornerShape(6.dp))
                        .background(TikCyan)
                        .padding(horizontal = 8.dp, vertical = 3.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.CheckCircle,
                            contentDescription = null,
                            tint = Color.Black,
                            modifier = Modifier.size(14.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = if (extractedVideoUrl.isNotEmpty()) "Extracted & Ready" else "Photo Post Ready",
                            color = Color.Black,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))
        }

        // 2. Extracted Title & Description Auto-inputs
        Text(
            text = "POST TITLE & DESCRIPTION (AUTO-EXTRACTED)",
            color = Color(0xFFA0A0B5),
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            letterSpacing = 0.5.sp
        )
        Spacer(modifier = Modifier.height(6.dp))

        OutlinedTextField(
            value = title,
            onValueChange = { viewModel.title.value = it },
            label = { Text("Title", color = Color(0xFF8E8EA0)) },
            placeholder = { Text("Auto-filled from TikTok video", color = Color(0xFF6B6B7F)) },
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = TikPink,
                unfocusedBorderColor = Color(0xFF333345),
                focusedTextColor = TikWhite,
                unfocusedTextColor = TikWhite,
                cursorColor = TikPink
            ),
            modifier = Modifier
                .fillMaxWidth()
                .testTag("upload_title_input")
        )

        Spacer(modifier = Modifier.height(10.dp))

        OutlinedTextField(
            value = description,
            onValueChange = { viewModel.description.value = it },
            label = { Text("Description / Hashtags", color = Color(0xFF8E8EA0)) },
            placeholder = { Text("Auto-filled caption", color = Color(0xFF6B6B7F)) },
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = TikPink,
                unfocusedBorderColor = Color(0xFF333345),
                focusedTextColor = TikWhite,
                unfocusedTextColor = TikWhite,
                cursorColor = TikPink
            ),
            maxLines = 3,
            modifier = Modifier
                .fillMaxWidth()
                .testTag("upload_description_input")
        )

        Spacer(modifier = Modifier.height(20.dp))

        // 3. AI Model Selector
        Text(
            text = "AI MODEL / TOOL USED",
            color = TikCyan,
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            letterSpacing = 0.5.sp
        )
        Spacer(modifier = Modifier.height(8.dp))

        Row(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState())
        ) {
            viewModel.availableModels.forEach { modelName ->
                val isSelected = selectedModel == modelName
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(20.dp))
                        .background(if (isSelected) TikPurple else TikSurfaceVariant)
                        .border(
                            1.dp,
                            if (isSelected) TikCyan else Color(0xFF333345),
                            RoundedCornerShape(20.dp)
                        )
                        .clickable { viewModel.selectedModel.value = modelName }
                        .padding(horizontal = 12.dp, vertical = 6.dp)
                ) {
                    Text(
                        text = modelName,
                        color = if (isSelected) TikWhite else Color(0xFFA0A0B5),
                        fontSize = 12.sp,
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        // 4. Dedicated AI Prompt Input Field (Key Feature)
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(16.dp))
                .background(TikDarkSurface)
                .border(1.5.dp, TikPurple, RoundedCornerShape(16.dp))
                .padding(14.dp)
        ) {
            Column {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(24.dp)
                                .clip(CircleShape)
                                .background(TikPurple),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.AutoAwesome,
                                contentDescription = null,
                                tint = TikWhite,
                                modifier = Modifier.size(14.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "AI Prompt (1-Click Copyable)",
                            color = TikWhite,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    Text(
                        text = "Required",
                        color = TikPink,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = "Enter the exact prompt used to generate this video so viewers can 1-click copy it from the feed.",
                    color = Color(0xFFA0A0B5),
                    fontSize = 11.sp
                )

                Spacer(modifier = Modifier.height(10.dp))

                OutlinedTextField(
                    value = aiPrompt,
                    onValueChange = { viewModel.aiPrompt.value = it },
                    placeholder = {
                        Text(
                            text = "e.g. Cinematic 8k drone shot of futuristic Tokyo in rain, neon glowing reflections, hyper-realistic, volumetric lighting, photorealistic --v 6.0 --ar 9:16",
                            color = Color(0xFF6B6B7F),
                            fontSize = 13.sp,
                            fontFamily = FontFamily.Monospace
                        )
                    },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = TikPurple,
                        unfocusedBorderColor = Color(0xFF3B3B4E),
                        focusedTextColor = TikWhite,
                        unfocusedTextColor = TikWhite,
                        cursorColor = TikCyan
                    ),
                    minLines = 4,
                    maxLines = 8,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("ai_prompt_input")
                )
            }
        }

        // Status Error banner
        if (status is UploadStatus.Error) {
            Spacer(modifier = Modifier.height(12.dp))
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(8.dp))
                    .background(Color(0xFF3D141C))
                    .padding(10.dp)
            ) {
                Text(
                    text = (status as UploadStatus.Error).message,
                    color = Color(0xFFFF7B92),
                    fontSize = 12.sp
                )
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // 5. Publish to Supabase Button
        Button(
            onClick = {
                viewModel.publishPost { createdPost ->
                    Toast.makeText(context, "🎉 Post Published to TikProm successfully!", Toast.LENGTH_LONG).show()
                    onUploadSuccess()
                }
            },
            enabled = status !is UploadStatus.Publishing,
            colors = ButtonDefaults.buttonColors(
                containerColor = TikPink,
                contentColor = TikWhite
            ),
            shape = RoundedCornerShape(14.dp),
            modifier = Modifier
                .fillMaxWidth()
                .height(52.dp)
                .testTag("publish_post_button")
        ) {
            if (status is UploadStatus.Publishing) {
                CircularProgressIndicator(
                    color = TikWhite,
                    modifier = Modifier.size(24.dp)
                )
                Spacer(modifier = Modifier.width(10.dp))
                Text(
                    text = "Publishing to Supabase...",
                    fontWeight = FontWeight.Bold,
                    fontSize = 15.sp
                )
            } else {
                Icon(
                    imageVector = Icons.Default.Upload,
                    contentDescription = null,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Publish to TikProm Feed",
                    fontWeight = FontWeight.Bold,
                    fontSize = 15.sp
                )
            }
        }
    }
}

@Composable
fun GuestUploadGateView(
    onNavigateToAuth: (Boolean) -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(TikBlack)
            .statusBarsPadding()
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 24.dp, vertical = 20.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            // Upload Hero Icon
            Box(
                modifier = Modifier
                    .size(100.dp)
                    .clip(CircleShape)
                    .background(Brush.linearGradient(listOf(TikPink, TikCyan)))
                    .padding(3.dp)
                    .clip(CircleShape)
                    .background(Color(0xFF161622)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Upload,
                    contentDescription = null,
                    tint = TikPink,
                    modifier = Modifier.size(46.dp)
                )
            }

            Spacer(modifier = Modifier.height(20.dp))

            Text(
                text = "Creator Login Required",
                color = TikWhite,
                fontSize = 22.sp,
                fontWeight = FontWeight.Bold,
                textAlign = androidx.compose.ui.text.style.TextAlign.Center
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "Publishing AI prompt videos requires a verified creator account. Sign in or register to share your prompts with thousands of creators.",
                color = Color(0xFFA5A5BA),
                fontSize = 14.sp,
                textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                lineHeight = 20.sp,
                modifier = Modifier.padding(horizontal = 8.dp)
            )

            Spacer(modifier = Modifier.height(28.dp))

            // Action Buttons
            Button(
                onClick = { onNavigateToAuth(false) },
                colors = ButtonDefaults.buttonColors(containerColor = TikPink),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp)
                    .testTag("guest_upload_login_button")
            ) {
                Text(
                    text = "Sign In to Upload",
                    fontWeight = FontWeight.Bold,
                    fontSize = 15.sp,
                    color = TikWhite
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            Button(
                onClick = { onNavigateToAuth(true) },
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1E1E2C)),
                shape = RoundedCornerShape(12.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, TikCyan.copy(alpha = 0.6f)),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp)
                    .testTag("guest_upload_signup_button")
            ) {
                Text(
                    text = "Create Creator Account",
                    fontWeight = FontWeight.Bold,
                    fontSize = 15.sp,
                    color = TikCyan
                )
            }

            Spacer(modifier = Modifier.height(32.dp))

            // Info Box
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp))
                    .background(TikDarkSurface)
                    .border(1.dp, Color(0xFF222232), RoundedCornerShape(16.dp))
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.CheckCircle, contentDescription = null, tint = TikCyan, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(10.dp))
                    Text(
                        text = "1-Click TikTok Video Extraction",
                        color = TikWhite,
                        fontSize = 13.sp
                    )
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.AutoAwesome, contentDescription = null, tint = TikPink, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(10.dp))
                    Text(
                        text = "Tag AI Models (Midjourney, Sora, Flux, etc.)",
                        color = TikWhite,
                        fontSize = 13.sp
                    )
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Lock, contentDescription = null, tint = TikPurple, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(10.dp))
                    Text(
                        text = "Securely linked with Supabase Auth & RLS",
                        color = TikWhite,
                        fontSize = 13.sp
                    )
                }
            }
        }
    }
}
