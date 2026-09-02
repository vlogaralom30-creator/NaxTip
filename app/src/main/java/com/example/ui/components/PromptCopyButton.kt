package com.example.ui.components

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.TikCyan
import com.example.ui.theme.TikGreen
import com.example.ui.theme.TikPink
import com.example.ui.theme.TikPurple
import com.example.ui.theme.TikWhite
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@Composable
fun PromptCopyButton(
    promptText: String,
    copiesCount: Int,
    onCopied: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    var isCopied by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()

    val scale by animateFloatAsState(
        targetValue = if (isCopied) 1.25f else 1f,
        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy),
        label = "copy_scale"
    )

    val buttonBgColor by animateColorAsState(
        targetValue = if (isCopied) TikGreen else Color(0xFF1E1E2E).copy(alpha = 0.85f),
        label = "copy_bg"
    )

    fun performCopy() {
        if (promptText.isBlank()) {
            Toast.makeText(context, "No prompt available for this video", Toast.LENGTH_SHORT).show()
            return
        }
        val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
        val clip = ClipData.newPlainText("AI Prompt", promptText)
        clipboard.setPrimaryClip(clip)
        isCopied = true
        onCopied()
        Toast.makeText(context, "✨ AI Prompt Copied to Clipboard!", Toast.LENGTH_SHORT).show()

        scope.launch {
            delay(1800)
            isCopied = false
        }
    }

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = modifier
            .testTag("action_copy_prompt_button")
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null
            ) {
                performCopy()
            }
    ) {
        // Glowing Badge Above or Button
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier
                .size(48.dp)
                .scale(scale)
                .clip(CircleShape)
                .background(
                    brush = Brush.linearGradient(
                        colors = if (isCopied) {
                            listOf(TikGreen, Color(0xFF00B4D8))
                        } else {
                            listOf(TikPurple, TikPink)
                        }
                    )
                )
                .border(
                    width = 2.dp,
                    brush = Brush.sweepGradient(
                        colors = listOf(TikCyan, TikPink, TikPurple, TikCyan)
                    ),
                    shape = CircleShape
                )
                .shadow(elevation = 8.dp, shape = CircleShape)
        ) {
            if (isCopied) {
                Icon(
                    imageVector = Icons.Default.Check,
                    contentDescription = "Copied",
                    tint = TikWhite,
                    modifier = Modifier.size(24.dp)
                )
            } else {
                Icon(
                    imageVector = Icons.Default.ContentCopy,
                    contentDescription = "1-Click Copy AI Prompt",
                    tint = TikWhite,
                    modifier = Modifier.size(22.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(4.dp))

        // Badge Text
        Text(
            text = if (isCopied) "Copied!" else "Copy AI",
            color = if (isCopied) TikGreen else TikCyan,
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.shadow(2.dp)
        )

        Text(
            text = "$copiesCount",
            color = TikWhite,
            fontSize = 11.sp,
            fontWeight = FontWeight.SemiBold
        )
    }
}

@Composable
fun PromptPillBanner(
    promptText: String,
    promptModel: String,
    onCopyClick: () -> Unit,
    onExpandClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    if (promptText.isBlank()) return

    Box(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(Color(0xFF101018).copy(alpha = 0.82f))
            .border(
                width = 1.dp,
                brush = Brush.horizontalGradient(listOf(TikCyan.copy(alpha = 0.6f), TikPink.copy(alpha = 0.6f))),
                shape = RoundedCornerShape(12.dp)
            )
            .clickable { onExpandClick() }
            .padding(horizontal = 10.dp, vertical = 8.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.weight(1f)
            ) {
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(6.dp))
                        .background(Brush.horizontalGradient(listOf(TikPink, TikPurple)))
                        .padding(horizontal = 6.dp, vertical = 2.dp)
                ) {
                    Text(
                        text = promptModel.ifEmpty { "AI PROMPT" },
                        color = TikWhite,
                        fontSize = 9.sp,
                        fontWeight = FontWeight.Black
                    )
                }

                Spacer(modifier = Modifier.width(6.dp))

                Text(
                    text = promptText,
                    color = TikWhite,
                    fontSize = 12.sp,
                    maxLines = 1,
                    fontWeight = FontWeight.Medium
                )
            }

            Spacer(modifier = Modifier.width(6.dp))

            // Quick Copy Pill
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(8.dp))
                    .background(TikCyan.copy(alpha = 0.25f))
                    .clickable { onCopyClick() }
                    .padding(horizontal = 8.dp, vertical = 4.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.ContentCopy,
                        contentDescription = "Copy Prompt",
                        tint = TikCyan,
                        modifier = Modifier.size(13.dp)
                    )
                    Spacer(modifier = Modifier.width(3.dp))
                    Text(
                        text = "Copy",
                        color = TikCyan,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}

