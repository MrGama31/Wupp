package com.wa.beastclient.ui.components

import android.graphics.BitmapFactory
import android.net.Uri
import android.view.TextureView
import android.widget.Toast
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.rounded.Favorite
import androidx.compose.material.icons.rounded.FavoriteBorder
import androidx.compose.material.icons.rounded.MoreVert
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import androidx.media3.exoplayer.ExoPlayer
import com.wa.beastclient.data.db.StatusEntity
import com.wa.beastclient.ui.screens.WhatsAppAvatar
import kotlinx.coroutines.delay
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun FullScreenStatusViewer(
    statuses: List<StatusEntity>,
    initialIndex: Int = 0,
    senderName: String,
    senderAvatarPath: String?,
    onDismiss: () -> Unit,
    onSaveToGallery: (StatusEntity) -> Unit,
    onReply: ((StatusEntity) -> Unit)? = null
) {
    if (statuses.isEmpty()) {
        onDismiss()
        return
    }
    val context = LocalContext.current
    var currentIndex by remember(statuses) {
        mutableIntStateOf(initialIndex.coerceIn(0, statuses.lastIndex))
    }
    var progress by remember(currentIndex) { mutableFloatStateOf(0f) }
    var isPaused by remember { mutableStateOf(false) }
    var showOverflow by remember { mutableStateOf(false) }
    var liked by remember(currentIndex) { mutableStateOf(false) }

    val current = statuses[currentIndex.coerceIn(0, statuses.lastIndex)]
    val mediaFile = remember(current.id, current.localMediaPath) {
        current.localMediaPath?.takeIf { it.isNotBlank() }?.let { path ->
            File(path).takeIf { it.exists() && it.length() > 0L }
        }
    }
    val isVideo = current.msgType.uppercase() == "VIDEO" || current.mimeType.startsWith("video/", ignoreCase = true)
    val bitmap = remember(mediaFile, isVideo) {
        if (!isVideo && mediaFile != null) {
            runCatching { BitmapFactory.decodeFile(mediaFile.absolutePath)?.asImageBitmap() }.getOrNull()
        } else {
            null
        }
    }

    LaunchedEffect(currentIndex, isPaused, showOverflow) {
        if (isPaused || showOverflow || isVideo) return@LaunchedEffect
        val totalMs = 5500L
        val stepMs = 40L
        while (progress < 1f) {
            delay(stepMs)
            progress = (progress + stepMs.toFloat() / totalMs.toFloat()).coerceAtMost(1f)
        }
        if (currentIndex < statuses.lastIndex) {
            currentIndex++
        } else {
            onDismiss()
        }
    }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(
            usePlatformDefaultWidth = false,
            decorFitsSystemWindows = false
        )
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black)
        ) {
            // Story Media / Text Canvas + Tap & Hold Gestures
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .pointerInput(currentIndex, statuses.size) {
                        detectTapGestures(
                            onPress = {
                                isPaused = true
                                tryAwaitRelease()
                                isPaused = false
                            },
                            onTap = { offset ->
                                val width = size.width.toFloat().coerceAtLeast(1f)
                                if (offset.x < width * 0.30f) {
                                    if (currentIndex > 0) {
                                        currentIndex--
                                    } else {
                                        progress = 0f
                                    }
                                } else {
                                    if (currentIndex < statuses.lastIndex) {
                                        currentIndex++
                                    } else {
                                        onDismiss()
                                    }
                                }
                            }
                        )
                    },
                contentAlignment = Alignment.Center
            ) {
                if (isVideo && mediaFile != null) {
                    StatusVideoPlayer(
                        file = mediaFile,
                        isPaused = isPaused || showOverflow,
                        onProgress = { progress = it },
                        onEnded = {
                            if (currentIndex < statuses.lastIndex) {
                                currentIndex++
                            } else {
                                onDismiss()
                            }
                        }
                    )
                } else if (bitmap != null) {
                    Image(
                        bitmap = bitmap,
                        contentDescription = current.caption,
                        contentScale = ContentScale.Fit,
                        modifier = Modifier.fillMaxSize()
                    )
                } else {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(Color(0xFF1F384A)),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = current.caption.ifBlank { "Status update" },
                            color = Color.White,
                            fontSize = 24.sp,
                            lineHeight = 32.sp,
                            fontWeight = FontWeight.SemiBold,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.padding(horizontal = 28.dp)
                        )
                    }
                }
            }

            // Top Segmented Progress Bars + Header
            Column(
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .fillMaxWidth()
                    .background(
                        Brush.verticalGradient(
                            listOf(Color.Black.copy(alpha = 0.72f), Color.Transparent)
                        )
                    )
                    .statusBarsPadding()
                    .padding(top = 8.dp, bottom = 12.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 10.dp),
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    statuses.forEachIndexed { idx, _ ->
                        val barFraction = when {
                            idx < currentIndex -> 1f
                            idx == currentIndex -> progress.coerceIn(0f, 1f)
                            else -> 0f
                        }
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .height(2.5.dp)
                                .clip(RoundedCornerShape(2.dp))
                                .background(Color.White.copy(alpha = 0.34f))
                        ) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth(barFraction)
                                    .height(2.5.dp)
                                    .background(Color.White)
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(onClick = onDismiss) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Rounded.ArrowBack,
                            contentDescription = "Back",
                            tint = Color.White
                        )
                    }
                    WhatsAppAvatar(
                        jid = current.senderJid,
                        avatarPath = senderAvatarPath,
                        isGroup = false,
                        size = 38.dp
                    )
                    Column(
                        modifier = Modifier
                            .weight(1f)
                            .padding(horizontal = 10.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Text(
                                text = senderName.ifBlank { current.senderName },
                                color = Color.White,
                                fontSize = 16.sp,
                                fontWeight = FontWeight.SemiBold,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                            if (current.isRevoked) {
                                Text(
                                    text = "Deleted",
                                    color = Color(0xFFFF6B6B),
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    modifier = Modifier
                                        .background(Color(0xFF44181A), RoundedCornerShape(6.dp))
                                        .padding(horizontal = 6.dp, vertical = 1.dp)
                                )
                            }
                        }
                        Text(
                            text = formatStatusRelativeTime(current.timestamp),
                            color = Color(0xFFD1D7DB),
                            fontSize = 12.sp
                        )
                    }
                    Box {
                        IconButton(onClick = { showOverflow = true }) {
                            Icon(
                                imageVector = Icons.Rounded.MoreVert,
                                contentDescription = "More",
                                tint = Color.White
                            )
                        }
                        DropdownMenu(
                            expanded = showOverflow,
                            onDismissRequest = { showOverflow = false },
                            containerColor = Color(0xFF233138)
                        ) {
                            if (mediaFile != null) {
                                DropdownMenuItem(
                                    text = { Text("Save to gallery", color = Color.White) },
                                    onClick = {
                                        showOverflow = false
                                        onSaveToGallery(current)
                                    }
                                )
                            }
                            if (onReply != null) {
                                DropdownMenuItem(
                                    text = { Text("Open chat", color = Color.White) },
                                    onClick = {
                                        showOverflow = false
                                        onReply(current)
                                        onDismiss()
                                    }
                                )
                            }
                        }
                    }
                }
            }

            // Bottom Caption + Reply Pill + Heart Button
            Column(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .fillMaxWidth()
                    .background(
                        Brush.verticalGradient(
                            listOf(Color.Transparent, Color.Black.copy(alpha = 0.78f))
                        )
                    )
                    .navigationBarsPadding()
                    .padding(horizontal = 14.dp, vertical = 12.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                if ((bitmap != null || isVideo) && current.caption.isNotBlank()) {
                    Text(
                        text = current.caption,
                        color = Color.White,
                        fontSize = 14.5.sp,
                        lineHeight = 20.sp,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .height(46.dp)
                            .clip(RoundedCornerShape(23.dp))
                            .background(Color(0xFF202C33).copy(alpha = 0.92f))
                            .clickable {
                                if (onReply != null) {
                                    onReply(current)
                                    onDismiss()
                                }
                            }
                            .padding(horizontal = 18.dp),
                        contentAlignment = Alignment.CenterStart
                    ) {
                        Text(
                            text = "Reply",
                            color = Color(0xFFD1D7DB),
                            fontSize = 15.sp
                        )
                    }
                    listOf("❤️", "👍", "😮").forEach { emoji ->
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .clip(CircleShape)
                                .background(Color(0xFF202C33).copy(alpha = 0.92f))
                                .clickable {
                                    Toast.makeText(context, "Reacted $emoji to status", Toast.LENGTH_SHORT).show()
                                },
                            contentAlignment = Alignment.Center
                        ) {
                            Text(text = emoji, fontSize = 17.sp)
                        }
                    }
                    Box(
                        modifier = Modifier
                            .size(46.dp)
                            .clip(CircleShape)
                            .background(Color(0xFF202C33).copy(alpha = 0.92f))
                            .clickable { liked = !liked },
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = if (liked) Icons.Rounded.Favorite else Icons.Rounded.FavoriteBorder,
                            contentDescription = "Like status",
                            tint = if (liked) Color(0xFFFF4D6D) else Color.White,
                            modifier = Modifier.size(22.dp)
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun StatusVideoPlayer(
    file: File,
    isPaused: Boolean,
    onProgress: (Float) -> Unit,
    onEnded: () -> Unit
) {
    val context = LocalContext.current
    val player = remember(file.absolutePath) {
        ExoPlayer.Builder(context).build().apply {
            setMediaItem(MediaItem.fromUri(Uri.fromFile(file)))
            prepare()
            playWhenReady = true
        }
    }
    LaunchedEffect(isPaused) {
        if (isPaused) player.pause() else player.play()
    }
    DisposableEffect(player) {
        val listener = object : Player.Listener {
            override fun onPlaybackStateChanged(playbackState: Int) {
                if (playbackState == Player.STATE_ENDED) {
                    onEnded()
                }
            }
        }
        player.addListener(listener)
        onDispose {
            player.removeListener(listener)
            player.release()
        }
    }
    LaunchedEffect(player) {
        while (true) {
            val dur = player.duration.coerceAtLeast(1L)
            val pos = player.currentPosition.coerceAtLeast(0L)
            onProgress((pos.toFloat() / dur.toFloat()).coerceIn(0f, 1f))
            delay(50L)
        }
    }
    AndroidView(
        factory = { ctx ->
            TextureView(ctx).also { player.setVideoTextureView(it) }
        },
        modifier = Modifier.fillMaxSize()
    )
}

private fun formatStatusRelativeTime(epochMs: Long): String {
    if (epochMs <= 0L) return ""
    val diffMin = ((System.currentTimeMillis() - epochMs) / 60_000L).coerceAtLeast(0L)
    return when {
        diffMin < 1L -> "Just now"
        diffMin == 1L -> "1 minute ago"
        diffMin < 60L -> "$diffMin minutes ago"
        diffMin < 24 * 60L -> "Today, " + SimpleDateFormat("h:mm a", Locale.getDefault()).format(Date(epochMs))
        else -> SimpleDateFormat("MMM d, h:mm a", Locale.getDefault()).format(Date(epochMs))
    }
}
