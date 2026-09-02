package com.example.ui.components

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.OpenInBrowser
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.SheetState
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.TikPromPost
import com.example.ui.theme.TikCyan
import com.example.ui.theme.TikDarkSurface
import com.example.ui.theme.TikPink
import com.example.ui.theme.TikPurple
import com.example.ui.theme.TikSurfaceVariant
import com.example.ui.theme.TikWhite

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PromptDetailBottomSheet(
    post: TikPromPost,
    onDismiss: () -> Unit,
    onCopyPrompt: (TikPromPost) -> Unit,
    sheetState: SheetState = rememberModalBottomSheetState()
) {
    val context = LocalContext.current

    fun copyToClipboard() {
        val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
        val clip = ClipData.newPlainText("AI Prompt", post.aiPrompt)
        clipboard.setPrimaryClip(clip)
        onCopyPrompt(post)
        Toast.makeText(context, "✨ AI Prompt copied to clipboard!", Toast.LENGTH_SHORT).show()
    }

    fun sharePrompt() {
        val shareIntent = Intent(Intent.ACTION_SEND).apply {
            type = "text/plain"
            putExtra(
                Intent.EXTRA_TEXT,
                "TikProm AI Prompt (${post.promptModel}):\n\n${post.aiPrompt}\n\nTitle: ${post.title}\nSource: ${post.tiktokUrl}"
            )
        }
        context.startActivity(Intent.createChooser(shareIntent, "Share AI Prompt via"))
    }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = TikDarkSurface,
        dragHandle = {
            Box(
                modifier = Modifier
                    .padding(vertical = 10.dp)
                    .width(40.dp)
                    .height(4.dp)
                    .clip(CircleShape)
                    .background(Color(0xFF3E3E50))
            )
        }
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 8.dp)
                .verticalScroll(rememberScrollState())
        ) {
            // Header
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(32.dp)
                            .clip(CircleShape)
                            .background(Brush.linearGradient(listOf(TikPink, TikPurple))),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.AutoAwesome,
                            contentDescription = null,
                            tint = TikWhite,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text(
                            text = "AI Prompt Details",
                            color = TikWhite,
                            fontSize = 17.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = post.authorUsername,
                            color = TikCyan,
                            fontSize = 12.sp
                        )
                    }
                }

                IconButton(
                    onClick = onDismiss,
                    modifier = Modifier.testTag("close_prompt_sheet")
                ) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "Close",
                        tint = TikWhite.copy(alpha = 0.7f)
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Model Badge & Copy Stats
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .background(TikPink.copy(alpha = 0.2f))
                        .border(1.dp, TikPink.copy(alpha = 0.5f), RoundedCornerShape(8.dp))
                        .padding(horizontal = 10.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = post.promptModel.ifEmpty { "Generative AI" },
                        color = TikPink,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }

                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .background(TikSurfaceVariant)
                        .padding(horizontal = 10.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = "🔥 ${post.copiesCount} Copies",
                        color = TikWhite.copy(alpha = 0.8f),
                        fontSize = 12.sp
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // AI Prompt Box
            Text(
                text = "PROMPT CODE / TEXT",
                color = TikCyan,
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 1.sp
            )
            Spacer(modifier = Modifier.height(6.dp))

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(min = 100.dp, max = 260.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(Color(0xFF0B0B10))
                    .border(1.dp, Color(0xFF262635), RoundedCornerShape(12.dp))
                    .padding(14.dp)
            ) {
                Text(
                    text = post.aiPrompt.ifEmpty { "(No prompt text provided for this post)" },
                    color = TikWhite,
                    fontSize = 14.sp,
                    lineHeight = 20.sp,
                    fontFamily = FontFamily.Monospace,
                    modifier = Modifier.verticalScroll(rememberScrollState())
                )
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Video Description if present
            if (post.description.isNotEmpty() && post.description != post.title) {
                Text(
                    text = "Description",
                    color = Color(0xFFA0A0B5),
                    fontSize = 12.sp,
                    fontWeight = FontWeight.SemiBold
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = post.description,
                    color = Color(0xFFD5D5E0),
                    fontSize = 13.sp
                )
                Spacer(modifier = Modifier.height(14.dp))
            }

            // Action Buttons
            Row(
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 24.dp)
            ) {
                // Primary 1-Click Copy
                Button(
                    onClick = { copyToClipboard() },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = TikCyan,
                        contentColor = Color.Black
                    ),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier
                        .weight(1f)
                        .height(48.dp)
                        .testTag("dialog_copy_button")
                ) {
                    Icon(
                        imageVector = Icons.Default.ContentCopy,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "1-Click Copy",
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp
                    )
                }

                // Share Button
                Button(
                    onClick = { sharePrompt() },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = TikSurfaceVariant,
                        contentColor = TikWhite
                    ),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier
                        .height(48.dp)
                        .testTag("dialog_share_button")
                ) {
                    Icon(
                        imageVector = Icons.Default.Share,
                        contentDescription = "Share",
                        modifier = Modifier.size(18.dp)
                    )
                }
            }
        }
    }
}
