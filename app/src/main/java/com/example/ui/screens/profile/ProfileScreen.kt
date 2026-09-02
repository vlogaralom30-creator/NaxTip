package com.example.ui.screens.profile

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.BookmarkBorder
import androidx.compose.material.icons.filled.Collections
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.GridOn
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Logout
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
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
import com.example.ui.components.PromptDetailBottomSheet
import com.example.ui.theme.TikBlack
import com.example.ui.theme.TikCyan
import com.example.ui.theme.TikDarkSurface
import com.example.ui.theme.TikPink
import com.example.ui.theme.TikPurple
import com.example.ui.theme.TikSurfaceVariant
import com.example.ui.theme.TikWhite

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(
    viewModel: ProfileViewModel,
    onNavigateToAuth: (Boolean) -> Unit,
    modifier: Modifier = Modifier
) {
    val currentUser by viewModel.currentUser.collectAsState()
    val allPosts by viewModel.posts.collectAsState()
    val likedIds by viewModel.likedIds.collectAsState()
    val savedIds by viewModel.savedIds.collectAsState()
    val selectedTab by viewModel.selectedTab.collectAsState()
    val isEditing by viewModel.isEditing.collectAsState()
    val isLoggedIn = viewModel.isLoggedIn

    if (!isLoggedIn || currentUser == null) {
        GuestProfileGateView(
            onNavigateToAuth = onNavigateToAuth,
            modifier = modifier
        )
        return
    }

    var activeDetailPost by remember { mutableStateOf<TikPromPost?>(null) }
    val context = LocalContext.current

    // Filter posts for tabs
    val user = currentUser
    val userId = user?.id.orEmpty()
    val myPosts = allPosts.filter { it.userId == userId || (userId.isEmpty() && it.authorUsername.contains("user", ignoreCase = true)) }
    val savedPosts = allPosts.filter { savedIds.contains(it.id) }
    val likedPosts = allPosts.filter { likedIds.contains(it.id) }

    val displayedPosts = when (selectedTab) {
        ProfileTab.MY_POSTS -> myPosts
        ProfileTab.SAVED_PROMPTS -> savedPosts
        ProfileTab.LIKED_POSTS -> likedPosts
    }

    val totalCopies = myPosts.sumOf { it.copiesCount }
    val totalLikes = myPosts.sumOf { it.likesCount }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(TikBlack)
            .statusBarsPadding()
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(bottom = 60.dp)
        ) {
            // Profile Top Bar
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = user?.username?.let { if (it.startsWith("@")) it else "@$it" } ?: "@creator",
                    color = TikWhite,
                    fontSize = 17.sp,
                    fontWeight = FontWeight.Bold
                )

                Row {
                    IconButton(
                        onClick = { viewModel.openEdit() },
                        modifier = Modifier.testTag("edit_profile_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Edit,
                            contentDescription = "Edit Profile",
                            tint = TikCyan,
                            modifier = Modifier.size(20.dp)
                        )
                    }

                    IconButton(
                        onClick = {
                            viewModel.logout()
                            onNavigateToAuth(false)
                        },
                        modifier = Modifier.testTag("logout_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Logout,
                            contentDescription = "Logout",
                            tint = Color(0xFFEF476F),
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
            }

            // User Info Header
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp)
            ) {
                // Avatar with Gradient Ring
                Box(
                    modifier = Modifier
                        .size(88.dp)
                        .clip(CircleShape)
                        .background(Brush.linearGradient(listOf(TikCyan, TikPink, TikPurple)))
                        .padding(3.dp)
                        .clip(CircleShape)
                ) {
                    AsyncImage(
                        model = user?.avatarUrl?.ifEmpty { "https://api.dicebear.com/7.x/bottts/png?seed=${user.username}" } 
                            ?: "https://api.dicebear.com/7.x/bottts/png?seed=user",
                        contentDescription = "Profile Picture",
                        contentScale = ContentScale.Crop,
                        modifier = Modifier
                            .fillMaxSize()
                            .clip(CircleShape)
                    )
                }

                Spacer(modifier = Modifier.height(10.dp))

                Text(
                    text = user?.fullName?.ifEmpty { user.username } ?: "TikProm Creator",
                    color = TikWhite,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold
                )

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = user?.bio?.ifEmpty { "Creating viral AI prompt videos ✨" } ?: "Creating viral AI prompt videos ✨",
                    color = Color(0xFFA5A5BA),
                    fontSize = 13.sp,
                    textAlign = androidx.compose.ui.text.style.TextAlign.Center
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Stats Row (Posts, Copies, Likes)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    ProfileStatItem(label = "Posts", value = "${myPosts.size}")
                    ProfileStatItem(label = "Prompt Copies", value = "$totalCopies", highlightColor = TikCyan)
                    ProfileStatItem(label = "Likes", value = "$totalLikes", highlightColor = TikPink)
                }

                Spacer(modifier = Modifier.height(16.dp))
            }

            // Profile Tabs
            TabRow(
                selectedTabIndex = selectedTab.ordinal,
                containerColor = TikDarkSurface,
                contentColor = TikWhite,
                indicator = { tabPositions ->
                    TabRowDefaults.SecondaryIndicator(
                        Modifier.tabIndicatorOffset(tabPositions[selectedTab.ordinal]),
                        color = TikCyan,
                        height = 2.dp
                    )
                }
            ) {
                Tab(
                    selected = selectedTab == ProfileTab.MY_POSTS,
                    onClick = { viewModel.selectTab(ProfileTab.MY_POSTS) },
                    text = {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.GridOn, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("My Posts (${myPosts.size})", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        }
                    },
                    selectedContentColor = TikCyan,
                    unselectedContentColor = Color(0xFF7A7A8E)
                )

                Tab(
                    selected = selectedTab == ProfileTab.SAVED_PROMPTS,
                    onClick = { viewModel.selectTab(ProfileTab.SAVED_PROMPTS) },
                    text = {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Bookmark, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Saved (${savedPosts.size})", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        }
                    },
                    selectedContentColor = TikCyan,
                    unselectedContentColor = Color(0xFF7A7A8E)
                )

                Tab(
                    selected = selectedTab == ProfileTab.LIKED_POSTS,
                    onClick = { viewModel.selectTab(ProfileTab.LIKED_POSTS) },
                    text = {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Favorite, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Liked (${likedPosts.size})", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        }
                    },
                    selectedContentColor = TikPink,
                    unselectedContentColor = Color(0xFF7A7A8E)
                )
            }

            // Grid Content
            if (displayedPosts.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(32.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            imageVector = when (selectedTab) {
                                ProfileTab.MY_POSTS -> Icons.Default.GridOn
                                ProfileTab.SAVED_PROMPTS -> Icons.Default.BookmarkBorder
                                ProfileTab.LIKED_POSTS -> Icons.Default.FavoriteBorder
                            },
                            contentDescription = null,
                            tint = Color(0xFF555566),
                            modifier = Modifier.size(48.dp)
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text = when (selectedTab) {
                                ProfileTab.MY_POSTS -> "No posts uploaded yet"
                                ProfileTab.SAVED_PROMPTS -> "No saved AI prompts yet"
                                ProfileTab.LIKED_POSTS -> "No liked videos yet"
                            },
                            color = Color(0xFF8A8A9E),
                            fontSize = 14.sp
                        )
                    }
                }
            } else {
                LazyVerticalGrid(
                    columns = GridCells.Fixed(3),
                    contentPadding = PaddingValues(2.dp),
                    horizontalArrangement = Arrangement.spacedBy(2.dp),
                    verticalArrangement = Arrangement.spacedBy(2.dp),
                    modifier = Modifier.fillMaxSize()
                ) {
                    items(displayedPosts, key = { it.id }) { post ->
                        ProfileVideoGridItem(
                            post = post,
                            onClick = { activeDetailPost = post }
                        )
                    }
                }
            }
        }

        // Edit Profile Modal
        if (isEditing) {
            EditProfileDialog(
                currentUsername = user?.username.orEmpty(),
                currentFullName = user?.fullName.orEmpty(),
                currentBio = user?.bio.orEmpty(),
                onDismiss = { viewModel.closeEdit() },
                onSave = { u, fn, b ->
                    viewModel.saveProfile(u, fn, b)
                    Toast.makeText(context, "Profile updated successfully!", Toast.LENGTH_SHORT).show()
                }
            )
        }

        // Active Post Detail Bottom Sheet
        activeDetailPost?.let { post ->
            PromptDetailBottomSheet(
                post = post,
                onDismiss = { activeDetailPost = null },
                onCopyPrompt = { p ->
                    val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                    clipboard.setPrimaryClip(ClipData.newPlainText("AI Prompt", p.aiPrompt))
                    Toast.makeText(context, "✨ AI Prompt Copied!", Toast.LENGTH_SHORT).show()
                }
            )
        }
    }
}

@Composable
fun ProfileStatItem(
    label: String,
    value: String,
    highlightColor: Color = TikWhite
) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text = value,
            color = highlightColor,
            fontSize = 18.sp,
            fontWeight = FontWeight.Black
        )
        Text(
            text = label,
            color = Color(0xFF8E8EA0),
            fontSize = 11.sp
        )
    }
}

@Composable
fun ProfileVideoGridItem(
    post: TikPromPost,
    onClick: () -> Unit
) {
    val displayCover = remember(post.thumbnailUrl) {
        if (post.thumbnailUrl.contains("|||")) {
            post.thumbnailUrl.substringBefore("|||").trim()
        } else if (post.thumbnailUrl.contains(",") && post.thumbnailUrl.startsWith("http")) {
            post.thumbnailUrl.substringBefore(",").trim()
        } else {
            post.thumbnailUrl.trim()
        }
    }

    val photoCount = remember(post.thumbnailUrl) {
        if (post.thumbnailUrl.contains("|||")) {
            post.thumbnailUrl.split("|||").size
        } else if (post.thumbnailUrl.contains(",") && post.thumbnailUrl.startsWith("http")) {
            post.thumbnailUrl.split(",").size
        } else if (post.thumbnailUrl.isNotEmpty()) {
            1
        } else {
            0
        }
    }

    Box(
        modifier = Modifier
            .aspectRatio(0.75f)
            .background(TikSurfaceVariant)
            .clickable { onClick() }
    ) {
        if (displayCover.isNotEmpty()) {
            AsyncImage(
                model = displayCover,
                contentDescription = post.title,
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize()
            )
        } else {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Brush.verticalGradient(listOf(Color(0xFF1E1E2E), Color(0xFF0F0F18)))),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.PlayArrow,
                    contentDescription = null,
                    tint = TikWhite.copy(alpha = 0.5f),
                    modifier = Modifier.size(32.dp)
                )
            }
        }

        // Multi-photo indicator icon in top right
        if (photoCount > 1) {
            Box(
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(6.dp)
                    .clip(RoundedCornerShape(4.dp))
                    .background(Color.Black.copy(alpha = 0.6f))
                    .padding(horizontal = 5.dp, vertical = 2.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Collections,
                        contentDescription = "Multiple photos",
                        tint = TikWhite,
                        modifier = Modifier.size(11.dp)
                    )
                    Spacer(modifier = Modifier.width(3.dp))
                    Text(
                        text = "$photoCount",
                        color = TikWhite,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }

        // Bottom Shadow with Like count & copies count
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(36.dp)
                .align(Alignment.BottomCenter)
                .background(Brush.verticalGradient(listOf(Color.Transparent, Color.Black.copy(alpha = 0.8f))))
                .padding(horizontal = 6.dp, vertical = 4.dp),
            contentAlignment = Alignment.BottomStart
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Favorite,
                        contentDescription = null,
                        tint = TikPink,
                        modifier = Modifier.size(11.dp)
                    )
                    Spacer(modifier = Modifier.width(3.dp))
                    Text(
                        text = "${post.likesCount}",
                        color = TikWhite,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.ContentCopy,
                        contentDescription = null,
                        tint = TikCyan,
                        modifier = Modifier.size(10.dp)
                    )
                    Spacer(modifier = Modifier.width(2.dp))
                    Text(
                        text = "${post.copiesCount}",
                        color = TikCyan,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}

@Composable
fun EditProfileDialog(
    currentUsername: String,
    currentFullName: String,
    currentBio: String,
    onDismiss: () -> Unit,
    onSave: (username: String, fullName: String, bio: String) -> Unit
) {
    var username by remember { mutableStateOf(currentUsername) }
    var fullName by remember { mutableStateOf(currentFullName) }
    var bio by remember { mutableStateOf(currentBio) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text("Edit Profile", color = TikWhite, fontWeight = FontWeight.Bold)
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                OutlinedTextField(
                    value = username,
                    onValueChange = { username = it },
                    label = { Text("Username") },
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = TikCyan,
                        focusedTextColor = TikWhite,
                        unfocusedTextColor = TikWhite
                    )
                )

                OutlinedTextField(
                    value = fullName,
                    onValueChange = { fullName = it },
                    label = { Text("Full Name") },
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = TikPink,
                        focusedTextColor = TikWhite,
                        unfocusedTextColor = TikWhite
                    )
                )

                OutlinedTextField(
                    value = bio,
                    onValueChange = { bio = it },
                    label = { Text("Bio") },
                    maxLines = 3,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = TikPurple,
                        focusedTextColor = TikWhite,
                        unfocusedTextColor = TikWhite
                    )
                )
            }
        },
        confirmButton = {
            Button(
                onClick = { onSave(username, fullName, bio) },
                colors = ButtonDefaults.buttonColors(containerColor = TikCyan, contentColor = Color.Black)
            ) {
                Text("Save", fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            Button(
                onClick = onDismiss,
                colors = ButtonDefaults.buttonColors(containerColor = TikSurfaceVariant, contentColor = TikWhite)
            ) {
                Text("Cancel")
            }
        },
        containerColor = TikDarkSurface
    )
}

@Composable
fun GuestProfileGateView(
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
            // Profile Glow Lock Avatar
            Box(
                modifier = Modifier
                    .size(100.dp)
                    .clip(CircleShape)
                    .background(Brush.linearGradient(listOf(TikPink, TikPurple, TikCyan)))
                    .padding(3.dp)
                    .clip(CircleShape)
                    .background(Color(0xFF161622)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Lock,
                    contentDescription = null,
                    tint = TikCyan,
                    modifier = Modifier.size(44.dp)
                )
            }

            Spacer(modifier = Modifier.height(20.dp))

            Text(
                text = "Join TikProm Community",
                color = TikWhite,
                fontSize = 22.sp,
                fontWeight = FontWeight.Bold,
                textAlign = androidx.compose.ui.text.style.TextAlign.Center
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "Sign in or create an account to view your AI video uploads, access saved prompts, and track your creator stats.",
                color = Color(0xFFA5A5BA),
                fontSize = 14.sp,
                textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                lineHeight = 20.sp,
                modifier = Modifier.padding(horizontal = 12.dp)
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
                    .testTag("guest_profile_login_button")
            ) {
                Text(
                    text = "Sign In to Your Account",
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
                    .testTag("guest_profile_signup_button")
            ) {
                Text(
                    text = "Create Creator Account",
                    fontWeight = FontWeight.Bold,
                    fontSize = 15.sp,
                    color = TikCyan
                )
            }

            Spacer(modifier = Modifier.height(32.dp))

            // Value Props
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp))
                    .background(TikDarkSurface)
                    .border(1.dp, Color(0xFF222232), RoundedCornerShape(16.dp))
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Bookmark, contentDescription = null, tint = TikCyan, modifier = Modifier.size(20.dp))
                    Spacer(modifier = Modifier.width(12.dp))
                    Text(
                        text = "Bookmark & organize viral AI prompts",
                        color = TikWhite,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Medium
                    )
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.AutoAwesome, contentDescription = null, tint = TikPink, modifier = Modifier.size(20.dp))
                    Spacer(modifier = Modifier.width(12.dp))
                    Text(
                        text = "Publish TikTok videos with AI prompts",
                        color = TikWhite,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Medium
                    )
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Favorite, contentDescription = null, tint = TikPurple, modifier = Modifier.size(20.dp))
                    Spacer(modifier = Modifier.width(12.dp))
                    Text(
                        text = "Build prompt creator reputation & stats",
                        color = TikWhite,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Medium
                    )
                }
            }
        }
    }
}
