package com.example.ui.components

import android.net.Uri
import android.view.ViewGroup
import android.widget.FrameLayout
import androidx.annotation.OptIn
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BrokenImage
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.VolumeOff
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.media3.common.AudioAttributes
import androidx.media3.common.C
import androidx.media3.common.MediaItem
import androidx.media3.common.PlaybackException
import androidx.media3.common.Player
import androidx.media3.common.util.UnstableApi
import androidx.media3.exoplayer.DefaultRenderersFactory
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.ui.AspectRatioFrameLayout
import androidx.media3.ui.PlayerView
import coil.compose.AsyncImage
import coil.compose.SubcomposeAsyncImage
import coil.request.ImageRequest
import com.example.ui.theme.TikCyan
import com.example.ui.theme.TikPink
import com.example.ui.theme.TikWhite
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.math.roundToInt

@OptIn(UnstableApi::class)
@Composable
fun TikTokVideoPlayer(
    videoUrl: String,
    thumbnailUrl: String,
    isActive: Boolean,
    onDoubleTapLike: () -> Unit,
    modifier: Modifier = Modifier,
    imageUrls: List<String> = emptyList(),
    isPhotoPost: Boolean = false,
    tiktokUrl: String = ""
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()

    // State for dynamically enriched photos if post had only 1 thumbnail saved previously
    var dynamicPhotos by remember(thumbnailUrl, tiktokUrl) { mutableStateOf<List<String>>(emptyList()) }

    // Resolve complete list of photos from imageUrls parameter or parse thumbnailUrl
    val parsedPhotos = remember(imageUrls, thumbnailUrl) {
        if (imageUrls.isNotEmpty()) {
            imageUrls.filter { it.isNotBlank() }
        } else if (thumbnailUrl.isBlank()) {
            emptyList()
        } else if (thumbnailUrl.contains("|||")) {
            thumbnailUrl.split("|||").map { it.trim() }.filter { it.isNotEmpty() }
        } else if (thumbnailUrl.contains(",") && thumbnailUrl.startsWith("http")) {
            thumbnailUrl.split(",").map { it.trim() }.filter { it.isNotEmpty() }
        } else {
            listOf(thumbnailUrl.trim())
        }
    }

    val photoList = remember(parsedPhotos, dynamicPhotos) {
        if (dynamicPhotos.size > parsedPhotos.size) dynamicPhotos else parsedPhotos
    }

    // If only 1 photo was recorded and we have a TikTok URL, asynchronously extract full photo slides in background
    LaunchedEffect(thumbnailUrl, tiktokUrl) {
        if (parsedPhotos.size <= 1 && tiktokUrl.isNotBlank() && (tiktokUrl.contains("tiktok.com") || tiktokUrl.contains("vt.tiktok"))) {
            try {
                val extractor = com.example.data.remote.TikTokExtractorService()
                val res = extractor.extract(tiktokUrl)
                if (res.success && res.imageUrls.size > 1) {
                    dynamicPhotos = res.imageUrls
                }
            } catch (e: Exception) {
                // Ignore background enrich errors
            }
        }
    }

    val isPhoto = isPhotoPost || photoList.size > 1 || (photoList.isNotEmpty() && (videoUrl.isBlank() || videoUrl.endsWith(".mp3") || videoUrl.contains("music") || !videoUrl.contains(".mp4")))
    val isMultiPhoto = photoList.size > 1
    val photoPagerState = rememberPagerState(pageCount = { photoList.size.coerceAtLeast(1) })

    var isPlaying by remember { mutableStateOf(false) }
    var isBuffering by remember { mutableStateOf(true) }
    var hasError by remember { mutableStateOf(false) }
    var hasVideoTrack by remember { mutableStateOf(false) }
    var isMuted by remember { mutableStateOf(false) }
    var showPlayPauseIcon by remember { mutableStateOf(false) }
    var progress by remember { mutableFloatStateOf(0f) }
    var durationMs by remember { mutableLongStateOf(0L) }

    // Heart explosion animations on double tap
    var showDoubleTapHeart by remember { mutableStateOf(false) }
    val heartScale = remember { Animatable(0.2f) }
    val heartAlpha = remember { Animatable(1f) }

    var exoPlayer by remember { mutableStateOf<ExoPlayer?>(null) }

    // Create player only when active with video/audio URL, and release immediately on inactive
    DisposableEffect(isActive, videoUrl, isPhoto) {
        val hasPlayableVideo = videoUrl.isNotBlank() && (!isPhoto || videoUrl.endsWith(".mp4") || videoUrl.contains("video"))
        if (isActive && hasPlayableVideo) {
            hasError = false
            isBuffering = true

            val renderersFactory = DefaultRenderersFactory(context)
                .setExtensionRendererMode(DefaultRenderersFactory.EXTENSION_RENDERER_MODE_OFF)
                .setEnableDecoderFallback(true)

            val player = ExoPlayer.Builder(context, renderersFactory)
                .setAudioAttributes(
                    AudioAttributes.Builder()
                        .setContentType(C.AUDIO_CONTENT_TYPE_MOVIE)
                        .setUsage(C.USAGE_MEDIA)
                        .build(),
                    true
                )
                .build().apply {
                    repeatMode = Player.REPEAT_MODE_ONE
                    volume = if (isMuted) 0f else 1f
                }

            val listener = object : Player.Listener {
                override fun onPlaybackStateChanged(playbackState: Int) {
                    when (playbackState) {
                        Player.STATE_BUFFERING -> {
                            isBuffering = true
                            hasError = false
                        }
                        Player.STATE_READY -> {
                            isBuffering = false
                            hasError = false
                            durationMs = player.duration.coerceAtLeast(1L)
                        }
                        Player.STATE_ENDED -> {
                            isBuffering = false
                        }
                        Player.STATE_IDLE -> {
                            isBuffering = false
                        }
                    }
                }

                override fun onVideoSizeChanged(videoSize: androidx.media3.common.VideoSize) {
                    if (videoSize.width > 0 && videoSize.height > 0) {
                        hasVideoTrack = true
                    }
                }

                override fun onTracksChanged(tracks: androidx.media3.common.Tracks) {
                    val hasVideo = tracks.groups.any { group ->
                        group.type == C.TRACK_TYPE_VIDEO && group.isSelected
                    }
                    if (hasVideo) {
                        hasVideoTrack = true
                    }
                }

                override fun onIsPlayingChanged(playing: Boolean) {
                    isPlaying = playing
                }

                override fun onPlayerError(error: PlaybackException) {
                    isBuffering = false
                    hasError = true
                }
            }

            player.addListener(listener)

            try {
                val mediaItem = MediaItem.fromUri(Uri.parse(videoUrl))
                player.setMediaItem(mediaItem)
                player.prepare()
                player.playWhenReady = true
            } catch (e: Exception) {
                hasError = true
                isBuffering = false
            }

            exoPlayer = player

            onDispose {
                try {
                    player.removeListener(listener)
                    player.stop()
                    player.release()
                } catch (e: Exception) {
                    // Ignore release errors
                }
                exoPlayer = null
                isPlaying = false
                isBuffering = false
            }
        } else {
            isBuffering = false
            onDispose { }
        }
    }

    // Progress tracker
    LaunchedEffect(isActive, isPlaying, exoPlayer) {
        val player = exoPlayer
        while (isActive && isPlaying && player != null) {
            val current = player.currentPosition
            val total = player.duration
            if (total > 0) {
                progress = (current.toFloat() / total.toFloat()).coerceIn(0f, 1f)
            }
            delay(250)
        }
    }

    // Handle Mute
    LaunchedEffect(isMuted, exoPlayer) {
        exoPlayer?.volume = if (isMuted) 0f else 1f
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(Color.Black)
            .pointerInput(Unit) {
                detectTapGestures(
                    onTap = {
                        val player = exoPlayer
                        if (player != null) {
                            if (isPlaying) {
                                player.pause()
                                showPlayPauseIcon = true
                            } else {
                                player.play()
                                showPlayPauseIcon = false
                            }
                        }
                    },
                    onDoubleTap = {
                        onDoubleTapLike()
                        coroutineScope.launch {
                            showDoubleTapHeart = true
                            heartScale.snapTo(0.2f)
                            heartAlpha.snapTo(1f)
                            heartScale.animateTo(
                                targetValue = 1.3f,
                                animationSpec = tween(300, easing = FastOutSlowInEasing)
                            )
                            heartAlpha.animateTo(
                                targetValue = 0f,
                                animationSpec = tween(200)
                            )
                            showDoubleTapHeart = false
                        }
                    }
                )
            }
    ) {
        // Photo Image Layer: If photoList is present, render single image or horizontal swipeable carousel
        if (photoList.isNotEmpty()) {
            if (isMultiPhoto) {
                HorizontalPager(
                    state = photoPagerState,
                    modifier = Modifier.fillMaxSize()
                ) { pageIndex ->
                    val currentImgUrl = photoList[pageIndex]
                    SubcomposeAsyncImage(
                        model = ImageRequest.Builder(context)
                            .data(currentImgUrl)
                            .crossfade(true)
                            .build(),
                        contentDescription = "TikTok Photo Slide ${pageIndex + 1} of ${photoList.size}",
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize(),
                        loading = {
                            Box(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .background(Color(0xFF14141E)),
                                contentAlignment = Alignment.Center
                            ) {
                                CircularProgressIndicator(
                                    color = TikCyan,
                                    modifier = Modifier.size(36.dp)
                                )
                            }
                        },
                        error = {
                            Box(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .background(Color(0xFF191924)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.BrokenImage,
                                    contentDescription = "Failed to load image",
                                    tint = Color.White.copy(alpha = 0.4f),
                                    modifier = Modifier.size(48.dp)
                                )
                            }
                        }
                    )
                }
            } else {
                val singleImg = photoList.first()
                SubcomposeAsyncImage(
                    model = ImageRequest.Builder(context)
                        .data(singleImg)
                        .crossfade(true)
                        .build(),
                    contentDescription = "TikTok Post Photo",
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize(),
                    loading = {
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .background(Color(0xFF14141E)),
                            contentAlignment = Alignment.Center
                        ) {
                            CircularProgressIndicator(
                                color = TikCyan,
                                modifier = Modifier.size(36.dp)
                            )
                        }
                    },
                    error = {
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .background(Color(0xFF191924)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.BrokenImage,
                                contentDescription = "Failed to load image",
                                tint = Color.White.copy(alpha = 0.4f),
                                modifier = Modifier.size(48.dp)
                            )
                        }
                    }
                )
            }
        }

        // Video Surface Layer: Rendered with transparent background so photo shows beneath if video has no visual track
        if (videoUrl.isNotEmpty() && isActive) {
            AndroidView(
                factory = { ctx ->
                    PlayerView(ctx).apply {
                        player = exoPlayer
                        useController = false
                        resizeMode = AspectRatioFrameLayout.RESIZE_MODE_ZOOM
                        setShutterBackgroundColor(android.graphics.Color.TRANSPARENT)
                        setBackgroundColor(android.graphics.Color.TRANSPARENT)
                        layoutParams = FrameLayout.LayoutParams(
                            ViewGroup.LayoutParams.MATCH_PARENT,
                            ViewGroup.LayoutParams.MATCH_PARENT
                        )
                    }
                },
                update = { view ->
                    if (view.player != exoPlayer) {
                        view.player = exoPlayer
                    }
                },
                modifier = Modifier
                    .fillMaxSize()
                    .alpha(if (hasVideoTrack) 1f else 0f)
            )
        }

        // Photo Mode Badge & Multi-photo counter
        if (!hasVideoTrack && photoList.isNotEmpty()) {
            Box(
                modifier = Modifier
                    .align(Alignment.TopStart)
                    .padding(top = 44.dp, start = 16.dp)
                    .clip(CircleShape)
                    .background(Color.Black.copy(alpha = 0.55f))
                    .padding(horizontal = 10.dp, vertical = 5.dp)
            ) {
                Text(
                    text = if (isMultiPhoto) "📸 ${photoPagerState.currentPage + 1}/${photoList.size} Photos" else "📸 Photo Post",
                    color = TikWhite,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }

        // Multi-photo pagination dot indicators on bottom
        if (isMultiPhoto) {
            Row(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(bottom = 12.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(Color.Black.copy(alpha = 0.45f))
                    .padding(horizontal = 8.dp, vertical = 4.dp),
                horizontalArrangement = Arrangement.spacedBy(5.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                photoList.forEachIndexed { idx, _ ->
                    val isCurrent = photoPagerState.currentPage == idx
                    Box(
                        modifier = Modifier
                            .size(if (isCurrent) 6.dp else 4.dp)
                            .clip(CircleShape)
                            .background(if (isCurrent) TikCyan else Color.White.copy(alpha = 0.5f))
                    )
                }
            }
        }

        // Buffering Spinner (only when video is loading and no thumbnail is ready)
        if (isBuffering && !hasError && photoList.isEmpty()) {
            CircularProgressIndicator(
                color = TikCyan,
                modifier = Modifier
                    .size(42.dp)
                    .align(Alignment.Center)
            )
        }

        // Error Retry
        if (hasError && photoList.isEmpty()) {
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.6f))
            ) {
                IconButton(
                    onClick = {
                        hasError = false
                        isBuffering = true
                        exoPlayer?.prepare()
                        exoPlayer?.play()
                    },
                    modifier = Modifier
                        .size(56.dp)
                        .clip(CircleShape)
                        .background(Color.White.copy(alpha = 0.2f))
                ) {
                    Icon(
                        imageVector = Icons.Default.Refresh,
                        contentDescription = "Retry playback",
                        tint = TikWhite,
                        modifier = Modifier.size(32.dp)
                    )
                }
            }
        }

        // Paused Indicator Overlay
        AnimatedVisibility(
            visible = !isPlaying && !isBuffering && !hasError,
            enter = fadeIn() + scaleIn(initialScale = 0.8f),
            exit = fadeOut() + scaleOut(targetScale = 0.8f),
            modifier = Modifier.align(Alignment.Center)
        ) {
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .size(72.dp)
                    .clip(CircleShape)
                    .background(Color.Black.copy(alpha = 0.5f))
            ) {
                Icon(
                    imageVector = Icons.Default.PlayArrow,
                    contentDescription = "Paused",
                    tint = TikWhite.copy(alpha = 0.9f),
                    modifier = Modifier.size(48.dp)
                )
            }
        }

        // Double Tap Flying Heart Animation
        if (showDoubleTapHeart) {
            Icon(
                imageVector = Icons.Default.Favorite,
                contentDescription = "Liked",
                tint = TikPink,
                modifier = Modifier
                    .size(110.dp)
                    .align(Alignment.Center)
                    .scale(heartScale.value)
                    .offset { IntOffset(0, ((1f - heartAlpha.value) * -40).roundToInt()) }
            )
        }

        // Top Mute Control Button
        IconButton(
            onClick = { isMuted = !isMuted },
            modifier = Modifier
                .align(Alignment.TopEnd)
                .padding(top = 44.dp, end = 16.dp)
                .size(36.dp)
                .clip(CircleShape)
                .background(Color.Black.copy(alpha = 0.4f))
        ) {
            Icon(
                imageVector = if (isMuted) Icons.Default.VolumeOff else Icons.Default.VolumeUp,
                contentDescription = if (isMuted) "Unmute" else "Mute",
                tint = TikWhite,
                modifier = Modifier.size(20.dp)
            )
        }

        // Bottom Progress Bar
        LinearProgressIndicator(
            progress = { progress },
            modifier = Modifier
                .fillMaxWidth()
                .height(2.5.dp)
                .align(Alignment.BottomCenter),
            color = TikWhite.copy(alpha = 0.8f),
            trackColor = Color.White.copy(alpha = 0.2f),
        )
    }
}

