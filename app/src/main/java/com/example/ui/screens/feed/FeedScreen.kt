package com.example.ui.screens.feed

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.pager.VerticalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.BookmarkBorder
import androidx.compose.material.icons.filled.ChatBubbleOutline
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Upload
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.data.model.TikPromPost
import com.example.ui.components.PromptCopyButton
import com.example.ui.components.PromptDetailBottomSheet
import com.example.ui.components.PromptPillBanner
import com.example.ui.components.TikTokVideoPlayer
import com.example.ui.theme.TikCyan
import com.example.ui.theme.TikDarkSurface
import com.example.ui.theme.TikPink
import com.example.ui.theme.TikPurple
import com.example.ui.theme.TikWhite

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FeedScreen(
    viewModel: FeedViewModel,
    onNavigateToUpload: () -> Unit,
    onNavigateToAuth: (Boolean) -> Unit = {},
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.uiState.collectAsState()
    val likedIds by viewModel.likedPostIds.collectAsState()
    val savedIds by viewModel.savedPromptIds.collectAsState()
    val currentUser by viewModel.currentUser.collectAsState()

    var selectedPostForDetails by remember { mutableStateOf<TikPromPost?>(null) }
    var authPromptReason by remember { mutableStateOf<String?>(null) }
    val context = LocalContext.current
    val isLoggedIn = viewModel.isLoggedIn

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(Color.Black)
    ) {
        when (val state = uiState) {
            is FeedUiState.Loading -> {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        CircularProgressIndicator(
                            color = TikPink,
                            modifier = Modifier.size(48.dp)
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = "Loading TikProm Feeds...",
                            color = TikWhite.copy(alpha = 0.8f),
                            fontSize = 14.sp
                        )
                    }
                }
            }

            is FeedUiState.Empty -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(24.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Box(
                            modifier = Modifier
                                .size(80.dp)
                                .clip(CircleShape)
                                .background(Brush.linearGradient(listOf(TikPink, TikCyan))),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.AutoAwesome,
                                contentDescription = null,
                                tint = TikWhite,
                                modifier = Modifier.size(42.dp)
                            )
                        }

                        Spacer(modifier = Modifier.height(20.dp))

                        Text(
                            text = "No AI Prompt Posts Yet",
                            color = TikWhite,
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        Text(
                            text = "Be the first to share an AI prompt and TikTok video! Paste any TikTok URL and extract it with 1-click.",
                            color = Color(0xFFA0A0B5),
                            fontSize = 14.sp,
                            textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                            modifier = Modifier.padding(horizontal = 16.dp)
                        )

                        Spacer(modifier = Modifier.height(24.dp))

                        Button(
                            onClick = onNavigateToUpload,
                            colors = ButtonDefaults.buttonColors(containerColor = TikPink),
                            shape = RoundedCornerShape(24.dp),
                            modifier = Modifier.testTag("feed_empty_upload_button")
                        ) {
                            Icon(imageVector = Icons.Default.Upload, contentDescription = null)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(text = "Upload AI Prompt Video", fontWeight = FontWeight.Bold)
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        IconButton(onClick = { viewModel.loadFeed() }) {
                            Icon(
                                imageVector = Icons.Default.Refresh,
                                contentDescription = "Refresh",
                                tint = TikCyan
                            )
                        }
                    }
                }
            }

            is FeedUiState.Error -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(24.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = "Could not load feed",
                            color = TikWhite,
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = state.message,
                            color = Color(0xFFEF476F),
                            fontSize = 13.sp,
                            textAlign = androidx.compose.ui.text.style.TextAlign.Center
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Button(
                            onClick = { viewModel.loadFeed() },
                            colors = ButtonDefaults.buttonColors(containerColor = TikCyan)
                        ) {
                            Text("Retry", color = Color.Black, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }

            is FeedUiState.Success -> {
                val posts = state.posts
                val pagerState = rememberPagerState(pageCount = { posts.size })

                VerticalPager(
                    state = pagerState,
                    modifier = Modifier.fillMaxSize()
                ) { page ->
                    val post = posts[page]
                    val isPageActive = pagerState.currentPage == page
                    val isLiked = likedIds.contains(post.id)
                    val isSaved = savedIds.contains(post.id)

                    Box(modifier = Modifier.fillMaxSize()) {
                        // Video / Photo Carousel Player
                        TikTokVideoPlayer(
                            videoUrl = post.videoUrl,
                            thumbnailUrl = post.thumbnailUrl,
                            imageUrls = post.imageUrls,
                            isPhotoPost = post.isPhotoPost,
                            tiktokUrl = post.tiktokUrl,
                            isActive = isPageActive,
                            onDoubleTapLike = {
                                if (!isLiked) {
                                    viewModel.toggleLike(post.id)
                                }
                            }
                        )

                        // Bottom Gradient Shadow for text readability
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(320.dp)
                                .align(Alignment.BottomCenter)
                                .background(
                                    Brush.verticalGradient(
                                        colors = listOf(Color.Transparent, Color.Black.copy(alpha = 0.85f))
                                    )
                                )
                        )

                        // Top Gradient Shadow
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(120.dp)
                                .align(Alignment.TopCenter)
                                .background(
                                    Brush.verticalGradient(
                                        colors = listOf(Color.Black.copy(alpha = 0.7f), Color.Transparent)
                                    )
                                )
                        )

                        // Right Floating Action Column
                        FeedActionColumn(
                            post = post,
                            isLiked = isLiked,
                            isSaved = isSaved,
                            onLikeClick = {
                                if (!isLoggedIn) {
                                    authPromptReason = "Sign in to like AI prompt posts and save them to your profile favorites."
                                } else {
                                    viewModel.toggleLike(post.id)
                                }
                            },
                            onCopyPromptClick = {
                                viewModel.copyPrompt(post)
                            },
                            onSaveClick = {
                                if (!isLoggedIn) {
                                    authPromptReason = "Sign in to bookmark this AI prompt directly to your saved prompts."
                                } else {
                                    viewModel.toggleSave(post.id) { saved ->
                                        Toast.makeText(
                                            context,
                                            if (saved) "Prompt Saved to Profile!" else "Removed from Saved",
                                            Toast.LENGTH_SHORT
                                        ).show()
                                    }
                                }
                            },
                            onCommentClick = {
                                if (!isLoggedIn) {
                                    authPromptReason = "Sign in to comment and view prompt discussions with other AI creators."
                                } else {
                                    selectedPostForDetails = post
                                }
                            },
                            onShareClick = {
                                val shareIntent = Intent(Intent.ACTION_SEND).apply {
                                    type = "text/plain"
                                    putExtra(
                                        Intent.EXTRA_TEXT,
                                        "Check out this AI Prompt on TikProm:\n${post.aiPrompt}\n${post.title}\n${post.tiktokUrl}"
                                    )
                                }
                                context.startActivity(Intent.createChooser(shareIntent, "Share Prompt"))
                            },
                            modifier = Modifier
                                .align(Alignment.BottomEnd)
                                .padding(end = 12.dp, bottom = 68.dp)
                        )

                        // Bottom Info & Prompt Overlay
                        FeedBottomOverlay(
                            post = post,
                            onCopyPrompt = {
                                val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                                clipboard.setPrimaryClip(ClipData.newPlainText("AI Prompt", post.aiPrompt))
                                viewModel.copyPrompt(post)
                                Toast.makeText(context, "✨ AI Prompt Copied!", Toast.LENGTH_SHORT).show()
                            },
                            onExpandPrompt = {
                                selectedPostForDetails = post
                            },
                            modifier = Modifier
                                .align(Alignment.BottomStart)
                                .padding(start = 14.dp, end = 80.dp, bottom = 68.dp)
                        )
                    }
                }
            }
        }

        // Top Navigation Header (TikProm Brand & Tabs)
        FeedTopHeader(
            onRefresh = { viewModel.loadFeed() },
            modifier = Modifier
                .align(Alignment.TopCenter)
                .statusBarsPadding()
        )

        // Prompt Full Details Bottom Sheet
        selectedPostForDetails?.let { post ->
            PromptDetailBottomSheet(
                post = post,
                onDismiss = { selectedPostForDetails = null },
                onCopyPrompt = { p ->
                    viewModel.copyPrompt(p)
                }
            )
        }

        // Login Required Dialog for Guests
        authPromptReason?.let { reason ->
            AlertDialog(
                onDismissRequest = { authPromptReason = null },
                containerColor = TikDarkSurface,
                shape = RoundedCornerShape(20.dp),
                icon = {
                    Box(
                        modifier = Modifier
                            .size(54.dp)
                            .clip(CircleShape)
                            .background(Brush.linearGradient(listOf(TikPink, TikCyan))),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.AutoAwesome,
                            contentDescription = null,
                            tint = TikWhite,
                            modifier = Modifier.size(28.dp)
                        )
                    }
                },
                title = {
                    Text(
                        text = "Login Required",
                        color = TikWhite,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                        modifier = Modifier.fillMaxWidth()
                    )
                },
                text = {
                    Text(
                        text = reason,
                        color = Color(0xFFA5A5BA),
                        fontSize = 13.sp,
                        textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                        modifier = Modifier.fillMaxWidth()
                    )
                },
                confirmButton = {
                    Button(
                        onClick = {
                            authPromptReason = null
                            onNavigateToAuth(false)
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = TikPink),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 8.dp)
                            .testTag("auth_dialog_signin_button")
                    ) {
                        Text("Sign In / Create Account", fontWeight = FontWeight.Bold, color = TikWhite)
                    }
                },
                dismissButton = {
                    Button(
                        onClick = { authPromptReason = null },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF222230)),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 8.dp)
                            .testTag("auth_dialog_dismiss_button")
                    ) {
                        Text("Not Now", color = Color(0xFFA5A5BA))
                    }
                }
            )
        }
    }
}

@Composable
fun FeedTopHeader(
    onRefresh: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        // TikProm Logo
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                text = "Tik",
                color = TikCyan,
                fontSize = 22.sp,
                fontWeight = FontWeight.Black
            )
            Text(
                text = "Prom",
                color = TikPink,
                fontSize = 22.sp,
                fontWeight = FontWeight.Black
            )
            Spacer(modifier = Modifier.width(4.dp))
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(4.dp))
                    .background(TikPurple)
                    .padding(horizontal = 4.dp, vertical = 1.dp)
            ) {
                Text(
                    text = "AI",
                    color = TikWhite,
                    fontSize = 9.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }

        // Center tabs
        Row(
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "For You",
                color = TikWhite,
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold
            )
        }

        IconButton(
            onClick = onRefresh,
            modifier = Modifier.size(36.dp)
        ) {
            Icon(
                imageVector = Icons.Default.Refresh,
                contentDescription = "Refresh Feed",
                tint = TikWhite.copy(alpha = 0.8f),
                modifier = Modifier.size(20.dp)
            )
        }
    }
}

@Composable
fun FeedActionColumn(
    post: TikPromPost,
    isLiked: Boolean,
    isSaved: Boolean,
    onLikeClick: () -> Unit,
    onCopyPromptClick: () -> Unit,
    onSaveClick: () -> Unit,
    onCommentClick: () -> Unit,
    onShareClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val infiniteTransition = rememberInfiniteTransition(label = "disc_anim")
    val discRotation by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(4000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "disc_rotation"
    )

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp),
        modifier = modifier
    ) {
        // Author Avatar with + button
        Box(contentAlignment = Alignment.BottomCenter) {
            AsyncImage(
                model = post.authorAvatar.ifEmpty { "https://api.dicebear.com/7.x/bottts/png?seed=${post.authorUsername}" },
                contentDescription = "Author Avatar",
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .size(46.dp)
                    .clip(CircleShape)
                    .border(2.dp, TikWhite, CircleShape)
            )
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .size(18.dp)
                    .clip(CircleShape)
                    .background(TikPink)
            ) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = null,
                    tint = TikWhite,
                    modifier = Modifier.size(12.dp)
                )
            }
        }

        // ⭐ 1-CLICK COPY AI PROMPT BUTTON (Placed right above Like button as requested!)
        PromptCopyButton(
            promptText = post.aiPrompt,
            copiesCount = post.copiesCount,
            onCopied = onCopyPromptClick
        )

        // Like Button
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier
                .testTag("action_like_button")
                .clickable { onLikeClick() }
        ) {
            Icon(
                imageVector = if (isLiked) Icons.Filled.Favorite else Icons.Default.FavoriteBorder,
                contentDescription = "Like",
                tint = if (isLiked) TikPink else TikWhite,
                modifier = Modifier.size(32.dp)
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = "${post.likesCount}",
                color = TikWhite,
                fontSize = 12.sp,
                fontWeight = FontWeight.SemiBold
            )
        }

        // Prompt Info / Comments Button
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier
                .testTag("action_comment_button")
                .clickable { onCommentClick() }
        ) {
            Icon(
                imageVector = Icons.Default.ChatBubbleOutline,
                contentDescription = "Prompt Info",
                tint = TikWhite,
                modifier = Modifier.size(30.dp)
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = "Prompt",
                color = TikWhite,
                fontSize = 11.sp
            )
        }

        // Save / Bookmark Button
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier
                .testTag("action_save_button")
                .clickable { onSaveClick() }
        ) {
            Icon(
                imageVector = if (isSaved) Icons.Filled.Bookmark else Icons.Default.BookmarkBorder,
                contentDescription = "Save Prompt",
                tint = if (isSaved) Color(0xFFFFD166) else TikWhite,
                modifier = Modifier.size(30.dp)
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = if (isSaved) "Saved" else "Save",
                color = TikWhite,
                fontSize = 11.sp
            )
        }

        // Share Button
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier
                .testTag("action_share_button")
                .clickable { onShareClick() }
        ) {
            Icon(
                imageVector = Icons.Default.Share,
                contentDescription = "Share",
                tint = TikWhite,
                modifier = Modifier.size(28.dp)
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = "Share",
                color = TikWhite,
                fontSize = 11.sp
            )
        }

        // Music Vinyl Disc
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier
                .size(42.dp)
                .rotate(discRotation)
                .clip(CircleShape)
                .background(Color(0xFF202026))
                .border(2.dp, Color(0xFF383845), CircleShape)
        ) {
            Icon(
                imageVector = Icons.Default.MusicNote,
                contentDescription = "Audio Sound",
                tint = TikWhite.copy(alpha = 0.7f),
                modifier = Modifier.size(20.dp)
            )
        }
    }
}

@Composable
fun FeedBottomOverlay(
    post: TikPromPost,
    onCopyPrompt: () -> Unit,
    onExpandPrompt: () -> Unit,
    modifier: Modifier = Modifier
) {
    var isExpanded by remember { mutableStateOf(false) }

    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        // Author Username & Verified Tag
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Text(
                text = post.authorUsername.ifEmpty { "@tikprom_ai" },
                color = TikWhite,
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold
            )

            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(4.dp))
                    .background(TikCyan.copy(alpha = 0.2f))
                    .padding(horizontal = 6.dp, vertical = 2.dp)
            ) {
                Text(
                    text = post.promptModel.ifEmpty { "AI Video" },
                    color = TikCyan,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }

        // Video Title & Caption
        if (post.title.isNotEmpty()) {
            Text(
                text = post.title,
                color = TikWhite.copy(alpha = 0.95f),
                fontSize = 13.sp,
                maxLines = if (isExpanded) 5 else 2,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.clickable { isExpanded = !isExpanded }
            )
        }

        // AI Prompt Banner with 1-Click Copy Pill
        PromptPillBanner(
            promptText = post.aiPrompt,
            promptModel = post.promptModel,
            onCopyClick = onCopyPrompt,
            onExpandClick = onExpandPrompt
        )

        // Sound Track Line
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(top = 2.dp)
        ) {
            Icon(
                imageVector = Icons.Default.MusicNote,
                contentDescription = null,
                tint = TikWhite.copy(alpha = 0.8f),
                modifier = Modifier.size(13.dp)
            )
            Spacer(modifier = Modifier.width(4.dp))
            Text(
                text = "Original AI Audio - ${post.authorName}",
                color = TikWhite.copy(alpha = 0.8f),
                fontSize = 11.sp,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}
