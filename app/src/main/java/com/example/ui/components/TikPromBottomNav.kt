package com.example.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.TikCyan
import com.example.ui.theme.TikPink
import com.example.ui.theme.TikWhite

enum class TikPromNavDestination {
    FEED,
    UPLOAD,
    PROFILE
}

@Composable
fun TikPromBottomNav(
    currentDestination: TikPromNavDestination,
    onNavigate: (TikPromNavDestination) -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .background(Color.Black.copy(alpha = 0.92f))
            .windowInsetsPadding(WindowInsets.navigationBars)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(58.dp)
                .padding(horizontal = 24.dp),
            horizontalArrangement = Arrangement.SpaceAround,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Home / Feed
            val homeSelected = currentDestination == TikPromNavDestination.FEED
            val homeColor by animateColorAsState(if (homeSelected) TikWhite else Color(0xFF7A7A8E), label = "nav_home")
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier
                    .testTag("nav_item_feed")
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null
                    ) { onNavigate(TikPromNavDestination.FEED) }
                    .padding(8.dp)
            ) {
                Icon(
                    imageVector = if (homeSelected) Icons.Filled.Home else Icons.Outlined.Home,
                    contentDescription = "Feed",
                    tint = homeColor,
                    modifier = Modifier.size(24.dp)
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = "Feed",
                    color = homeColor,
                    fontSize = 10.sp,
                    fontWeight = if (homeSelected) FontWeight.Bold else FontWeight.Normal
                )
            }

            // TikTok Special Upload (+) Button
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .testTag("nav_item_upload")
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null
                    ) { onNavigate(TikPromNavDestination.UPLOAD) }
            ) {
                // TikTok 3D effect layer
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .height(30.dp)
                        .clip(RoundedCornerShape(8.dp))
                ) {
                    Box(
                        modifier = Modifier
                            .size(width = 38.dp, height = 30.dp)
                            .background(TikCyan)
                    )
                    Box(
                        modifier = Modifier
                            .size(width = 38.dp, height = 30.dp)
                            .background(TikPink)
                    )
                }

                // White Center Block
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier
                        .size(width = 40.dp, height = 30.dp)
                        .clip(RoundedCornerShape(7.dp))
                        .background(TikWhite)
                ) {
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = "Upload Prompt",
                        tint = Color.Black,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }

            // Profile
            val profileSelected = currentDestination == TikPromNavDestination.PROFILE
            val profileColor by animateColorAsState(if (profileSelected) TikWhite else Color(0xFF7A7A8E), label = "nav_profile")
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier
                    .testTag("nav_item_profile")
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null
                    ) { onNavigate(TikPromNavDestination.PROFILE) }
                    .padding(8.dp)
            ) {
                Icon(
                    imageVector = if (profileSelected) Icons.Filled.Person else Icons.Outlined.Person,
                    contentDescription = "Profile",
                    tint = profileColor,
                    modifier = Modifier.size(24.dp)
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = "Profile",
                    color = profileColor,
                    fontSize = 10.sp,
                    fontWeight = if (profileSelected) FontWeight.Bold else FontWeight.Normal
                )
            }
        }
    }
}
