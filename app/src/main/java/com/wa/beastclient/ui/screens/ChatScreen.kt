package com.wa.beastclient.ui.screens

import android.Manifest
import android.content.pm.PackageManager
import android.graphics.BitmapFactory
import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.automirrored.rounded.Reply
import androidx.compose.material.icons.automirrored.rounded.Send
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.AlternateEmail
import androidx.compose.material.icons.rounded.AttachFile
import androidx.compose.material.icons.rounded.Call
import androidx.compose.material.icons.rounded.CallMade
import androidx.compose.material.icons.rounded.CallMissed
import androidx.compose.material.icons.rounded.CallReceived
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.ContentCopy
import androidx.compose.material.icons.rounded.Delete
import androidx.compose.material.icons.rounded.Download
import androidx.compose.material.icons.rounded.Edit
import androidx.compose.material.icons.rounded.History
import androidx.compose.material.icons.rounded.Image
import androidx.compose.material.icons.rounded.Info
import androidx.compose.material.icons.rounded.Lock
import androidx.compose.material.icons.rounded.MoreVert
import androidx.compose.material.icons.rounded.Notifications
import androidx.compose.material.icons.rounded.NotificationsOff
import androidx.compose.material.icons.rounded.PlayArrow
import androidx.compose.material.icons.rounded.SentimentSatisfiedAlt
import androidx.compose.material.icons.rounded.Shield
import androidx.compose.material.icons.rounded.Stop
import androidx.compose.material.icons.rounded.Videocam
import androidx.compose.material.icons.rounded.Warning
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.paging.compose.collectAsLazyPagingItems
import androidx.paging.compose.itemKey
import com.airbnb.lottie.compose.LottieAnimation
import com.airbnb.lottie.compose.LottieCompositionSpec
import com.airbnb.lottie.compose.LottieConstants
import com.airbnb.lottie.compose.rememberLottieComposition
import com.wa.beastclient.R
import com.wa.beastclient.data.db.MessageEntity
import com.wa.beastclient.data.media.OutboundQualityMode
import com.wa.beastclient.ui.MainViewModel
import com.wa.beastclient.ui.components.DoodleBackground
import com.wa.beastclient.ui.components.FullScreenMediaViewer
import com.wa.beastclient.ui.components.InlineTimestampMessage
import com.wa.beastclient.ui.components.MessageMetaCluster
import com.wa.beastclient.ui.components.SwipeToReplyBox
import com.wa.beastclient.ui.components.VoiceNoteBubble
import com.wa.beastclient.ui.components.WhatsAppBubbleShape
import com.wa.beastclient.ui.components.WhatsAppClusterInnerShape
import com.wa.beastclient.ui.components.formatMessageClock
import com.wa.beastclient.ui.theme.LocalWaColors
import com.wa.beastclient.ui.theme.LocalWaMonoFamily
import kotlinx.coroutines.launch
import java.io.File
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import kotlin.math.abs

@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun ChatScreen(
    chatJid: String,
    viewModel: MainViewModel,
    onBack: () -> Unit
) {
    val colors = LocalWaColors.current
    val mono = LocalWaMonoFamily.current
    val context = LocalContext.current
    val clipboard = LocalClipboardManager.current
    val scope = rememberCoroutineScope()

    DisposableEffect(chatJid) {
        viewModel.setActiveChat(chatJid)
        onDispose { viewModel.setActiveChat(null) }
    }

    val chat by viewModel.observeChat(chatJid).collectAsState(initial = null)
    val isChannel = chatJid.endsWith("@newsletter")
    val isGroupAnnounceLocked by viewModel.isGroupAnnounceLocked(chatJid).collectAsState(initial = false)
    val effectivePrivacy by viewModel.observeEffectivePrivacy(chatJid).collectAsState(initial = null)
    val canSendOutbound by viewModel.canInitiateOutbound(chatJid).collectAsState(initial = true)
    val isRecordingVoice by viewModel.isRecordingVoice.collectAsState()
    val pagedItems = viewModel.pagedMessages(chatJid).collectAsLazyPagingItems()

    var draftText by rememberSaveable(chatJid) { mutableStateOf("") }
    var replyTarget by remember { mutableStateOf<MessageEntity?>(null) }
    var ghostMentionEveryone by remember { mutableStateOf(false) }
    var qualityMode by remember { mutableStateOf(OutboundQualityMode.HD) }
    var showAttachTray by remember { mutableStateOf(false) }

    var showSearchInChat by remember { mutableStateOf(false) }
    var inChatQuery by remember { mutableStateOf("") }
    var inChatMatches by remember { mutableStateOf<List<MessageEntity>>(emptyList()) }
    var showOverflow by remember { mutableStateOf(false) }
    var showSelectionOverflow by remember { mutableStateOf(false) }

    var showGhostOverrideSheet by remember { mutableStateOf(false) }
    var editHistoryTarget by remember { mutableStateOf<MessageEntity?>(null) }
    var groupReceiptsTarget by remember { mutableStateOf<MessageEntity?>(null) }
    var actionMenuTarget by remember { mutableStateOf<MessageEntity?>(null) }
    var showMoreReactionEmojis by remember { mutableStateOf(false) }
    var editMessageDialogTarget by remember { mutableStateOf<MessageEntity?>(null) }
    var mediaLightboxTarget by remember { mutableStateOf<MessageEntity?>(null) }

    BackHandler(enabled = actionMenuTarget != null) {
        actionMenuTarget = null
        showMoreReactionEmojis = false
        showSelectionOverflow = false
    }

    val typingComposition by rememberLottieComposition(
        LottieCompositionSpec.Asset("lottie/typing_indicator_bounce_v3.json")
    )

    val pickMediaLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri ->
        if (uri != null) {
            val mime = context.contentResolver.getType(uri) ?: "image/jpeg"
            showAttachTray = false
            viewModel.sendPickedMedia(chatJid, uri, mime, draftText.trim(), qualityMode)
            draftText = ""
        }
    }

    val micPermLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { granted ->
        viewModel.toggleVoiceRecording(chatJid, granted)
    }

    LaunchedEffect(inChatQuery, showSearchInChat) {
        inChatMatches = if (showSearchInChat && inChatQuery.trim().length >= 2) {
            viewModel.searchInChat(chatJid, inChatQuery)
        } else emptyList()
    }

    Scaffold(
        containerColor = colors.chatCanvas,
        topBar = {
            Column {
                val selectedMsg = actionMenuTarget
                if (selectedMsg != null) {
                    TopAppBar(
                        colors = TopAppBarDefaults.topAppBarColors(
                            containerColor = colors.appSurface,
                            titleContentColor = colors.textPrimary
                        ),
                        navigationIcon = {
                            IconButton(
                                onClick = {
                                    actionMenuTarget = null
                                    showMoreReactionEmojis = false
                                    showSelectionOverflow = false
                                }
                            ) {
                                Icon(Icons.AutoMirrored.Rounded.ArrowBack, "Clear selection", tint = colors.textPrimary)
                            }
                        },
                        title = {
                            Text(
                                text = "1",
                                fontSize = 19.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = colors.textPrimary
                            )
                        },
                        actions = {
                            if (!isChannel && !isGroupAnnounceLocked) {
                                IconButton(
                                    onClick = {
                                        replyTarget = selectedMsg
                                        actionMenuTarget = null
                                        showMoreReactionEmojis = false
                                    }
                                ) {
                                    Icon(Icons.AutoMirrored.Rounded.Reply, "Reply", tint = colors.textPrimary)
                                }
                            }
                            if (selectedMsg.body.isNotBlank()) {
                                IconButton(
                                    onClick = {
                                        clipboard.setText(AnnotatedString(selectedMsg.body))
                                        Toast.makeText(context, "Message copied", Toast.LENGTH_SHORT).show()
                                        actionMenuTarget = null
                                        showMoreReactionEmojis = false
                                    }
                                ) {
                                    Icon(Icons.Rounded.ContentCopy, "Copy", tint = colors.textPrimary)
                                }
                            }
                            if (chat?.isGroup == true || selectedMsg.isEdited) {
                                IconButton(
                                    onClick = {
                                        if (chat?.isGroup == true) {
                                            groupReceiptsTarget = selectedMsg
                                        } else {
                                            editHistoryTarget = selectedMsg
                                        }
                                        actionMenuTarget = null
                                        showMoreReactionEmojis = false
                                    }
                                ) {
                                    Icon(Icons.Rounded.Info, "Message info", tint = colors.textPrimary)
                                }
                            }
                            IconButton(
                                onClick = {
                                    viewModel.revokeOrSimulateAntiDelete(
                                        chatJid,
                                        selectedMsg.id,
                                        simulateIncoming = !selectedMsg.isFromMe
                                    )
                                    actionMenuTarget = null
                                    showMoreReactionEmojis = false
                                }
                            ) {
                                Icon(Icons.Rounded.Delete, "Delete", tint = colors.textPrimary)
                            }
                            Box {
                                IconButton(onClick = { showSelectionOverflow = true }) {
                                    Icon(Icons.Rounded.MoreVert, "More actions", tint = colors.textPrimary)
                                }
                                DropdownMenu(
                                    expanded = showSelectionOverflow,
                                    onDismissRequest = { showSelectionOverflow = false }
                                ) {
                                    if (selectedMsg.isFromMe && selectedMsg.msgType == "TEXT") {
                                        DropdownMenuItem(
                                            text = { Text("Edit") },
                                            onClick = {
                                                showSelectionOverflow = false
                                                editMessageDialogTarget = selectedMsg
                                                actionMenuTarget = null
                                                showMoreReactionEmojis = false
                                            }
                                        )
                                    }
                                    if (selectedMsg.isEdited) {
                                        DropdownMenuItem(
                                            text = { Text("Edit history") },
                                            onClick = {
                                                showSelectionOverflow = false
                                                editHistoryTarget = selectedMsg
                                                actionMenuTarget = null
                                                showMoreReactionEmojis = false
                                            }
                                        )
                                    }
                                    if (!selectedMsg.localMediaPath.isNullOrBlank()) {
                                        DropdownMenuItem(
                                            text = { Text("Save to Gallery") },
                                            onClick = {
                                                showSelectionOverflow = false
                                                viewModel.exportMediaToGallery(
                                                    selectedMsg.localMediaPath,
                                                    "WA_${selectedMsg.msgType}_${selectedMsg.id}.${File(selectedMsg.localMediaPath).extension.ifBlank { "bin" }}",
                                                    selectedMsg.mimeType
                                                )
                                                actionMenuTarget = null
                                                showMoreReactionEmojis = false
                                            }
                                        )
                                    }
                                    if (chat?.isGroup == true) {
                                        DropdownMenuItem(
                                            text = { Text("Message info") },
                                            onClick = {
                                                showSelectionOverflow = false
                                                groupReceiptsTarget = selectedMsg
                                                actionMenuTarget = null
                                                showMoreReactionEmojis = false
                                            }
                                        )
                                    }
                                }
                            }
                        }
                    )
                } else {
                TopAppBar(
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = colors.appSurface,
                        titleContentColor = colors.textPrimary
                    ),
                    navigationIcon = {
                        IconButton(onClick = onBack) {
                            Icon(Icons.AutoMirrored.Rounded.ArrowBack, "Back", tint = colors.textPrimary)
                        }
                    },
                    title = {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            WhatsAppAvatar(
                                jid = chatJid,
                                avatarPath = chat?.avatarPath,
                                isGroup = chat?.isGroup == true,
                                size = 38.dp
                            )
                            Spacer(modifier = Modifier.width(10.dp))
                            Column {
                                Text(
                                    text = chat?.name?.ifBlank { chatJid.substringBefore("@") }
                                        ?: chatJid.substringBefore("@"),
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    val presence = chat?.chatPresence.orEmpty()
                                    if (presence.isNotBlank()) {
                                        if (typingComposition != null) {
                                            LottieAnimation(
                                                composition = typingComposition,
                                                iterations = LottieConstants.IterateForever,
                                                modifier = Modifier.size(16.dp)
                                            )
                                            Spacer(modifier = Modifier.width(4.dp))
                                        }
                                        Text(
                                            text = presence,
                                            fontSize = 12.sp,
                                            color = colors.brightGreen
                                        )
                                    } else {
                                        val sub = when {
                                            isChannel -> "Public channel"
                                            chat?.isOnline == true -> "online"
                                            (chat?.lastSeen ?: 0L) > 0L -> "last seen ${formatMessageClock(chat!!.lastSeen)}"
                                            !chat?.aboutText.isNullOrBlank() -> chat!!.aboutText
                                            else -> ""
                                        }
                                        if (sub.isNotBlank()) {
                                            Text(
                                                text = sub,
                                                fontSize = 12.sp,
                                                color = if (chat?.isOnline == true && !isChannel) colors.brightGreen else colors.textSecondary,
                                                maxLines = 1,
                                                overflow = TextOverflow.Ellipsis
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    },
                    actions = {
                        if (isChannel) {
                            IconButton(
                                onClick = {
                                    viewModel.toggleChannelMute(chatJid, chat?.isMuted == true)
                                }
                            ) {
                                Icon(
                                    imageVector = if (chat?.isMuted == true) Icons.Rounded.NotificationsOff else Icons.Rounded.Notifications,
                                    contentDescription = if (chat?.isMuted == true) "Unmute channel" else "Mute channel",
                                    tint = colors.textPrimary
                                )
                            }
                        } else {
                            IconButton(
                                onClick = {
                                    Toast.makeText(context, "Calls require official WhatsApp app", Toast.LENGTH_SHORT).show()
                                }
                            ) {
                                Icon(Icons.Rounded.Videocam, "Video call", tint = colors.textPrimary)
                            }
                            IconButton(
                                onClick = {
                                    Toast.makeText(context, "Calls require official WhatsApp app", Toast.LENGTH_SHORT).show()
                                }
                            ) {
                                Icon(Icons.Rounded.Call, "Voice call", tint = colors.textPrimary)
                            }
                        }
                        IconButton(onClick = { showOverflow = true }) {
                            Icon(Icons.Rounded.MoreVert, "More", tint = colors.textPrimary)
                        }
                        DropdownMenu(expanded = showOverflow, onDismissRequest = { showOverflow = false }) {
                            DropdownMenuItem(
                                text = { Text("View contact & privacy") },
                                onClick = {
                                    showOverflow = false
                                    showGhostOverrideSheet = true
                                }
                            )
                            DropdownMenuItem(
                                text = { Text("Search") },
                                onClick = {
                                    showOverflow = false
                                    showSearchInChat = !showSearchInChat
                                }
                            )
                            DropdownMenuItem(
                                text = { Text("Mark as read") },
                                onClick = {
                                    showOverflow = false
                                    viewModel.markChatReadExplicitly(chatJid)
                                }
                            )
                            DropdownMenuItem(
                                text = { Text(if (chat?.isPinned == true) "Unpin chat" else "Pin chat") },
                                onClick = {
                                    showOverflow = false
                                    chat?.let { viewModel.togglePinChat(it) }
                                }
                            )
                            DropdownMenuItem(
                                text = { Text(if (chat?.isFavorite == true) "Remove from Favorites" else "Add to Favorites") },
                                onClick = {
                                    showOverflow = false
                                    chat?.let { viewModel.toggleFavoriteChat(it) }
                                }
                            )
                        }
                    }
                )

                AnimatedVisibility(visible = showSearchInChat) {
                    Column(modifier = Modifier.background(colors.elevatedBar).padding(horizontal = 12.dp, vertical = 6.dp)) {
                        OutlinedTextField(
                            value = inChatQuery,
                            onValueChange = { inChatQuery = it },
                            placeholder = { Text("Search messages or voice transcripts...") },
                            singleLine = true,
                            trailingIcon = {
                                IconButton(onClick = { showSearchInChat = false; inChatQuery = "" }) {
                                    Icon(Icons.Rounded.Close, "Close")
                                }
                            },
                            modifier = Modifier.fillMaxWidth()
                        )
                        if (inChatMatches.isNotEmpty()) {
                            Text(
                                text = "${inChatMatches.size} matches: \"${inChatMatches.first().body.ifBlank { inChatMatches.first().audioTranscript.orEmpty() }}\"",
                                fontSize = 11.5.sp,
                                color = colors.brightGreen,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                                modifier = Modifier.padding(top = 4.dp)
                            )
                        }
                    }
                }
                }
            }
        }
    ) { innerPadding ->
        DoodleBackground(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .imePadding()
                .navigationBarsPadding()
        ) {
            Column(modifier = Modifier.fillMaxSize()) {
                // Error 463 Ban Guard Banner on unsaved first-contact chats (not channels or admin-only groups)
                if (!isChannel && !isGroupAnnounceLocked && !canSendOutbound) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(colors.antiDeleteRed.copy(alpha = 0.18f))
                            .padding(horizontal = 12.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Rounded.Warning, null, tint = colors.antiDeleteRed, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Error 463 Guard: 0 prior messages with unsaved contact. Send initial greeting from official primary app first.",
                            color = colors.textPrimary,
                            fontSize = 12.sp,
                            lineHeight = 16.sp
                        )
                    }
                }

                // Paging 3 reverseLayout timeline + Floating WhatsApp Reaction Bar overlay
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth()
                ) {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    reverseLayout = true,
                    contentPadding = PaddingValues(horizontal = 8.dp, vertical = 10.dp)
                ) {
                    items(
                        count = pagedItems.itemCount,
                        key = pagedItems.itemKey { it.id }
                    ) { index ->
                        val msg = pagedItems[index] ?: return@items
                        if (isHiddenPlaceholderMessage(msg)) {
                            return@items
                        }
                        // Remember: reverseLayout=true -> index + 1 is visually ABOVE index
                        var nextVisibleIdx = index + 1
                        var above: MessageEntity? = null
                        while (nextVisibleIdx < pagedItems.itemCount) {
                            val candidate = pagedItems.peek(nextVisibleIdx)
                            if (candidate != null && !isHiddenPlaceholderMessage(candidate)) {
                                above = candidate
                                break
                            }
                            nextVisibleIdx++
                        }
                        val showDateHeader = above == null || !isSameCalendarDay(msg.timestamp, above.timestamp)
                        val isFirstInCluster = above == null ||
                            showDateHeader ||
                            above.isFromMe != msg.isFromMe ||
                            above.senderJid != msg.senderJid
                        val isSelected = actionMenuTarget?.id == msg.id

                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(if (isSelected) colors.brightGreen.copy(alpha = 0.14f) else Color.Transparent)
                        ) {
                            if (showDateHeader && msg.timestamp > 0L) {
                                DateHeaderPill(timestamp = msg.timestamp)
                            }
                            Spacer(modifier = Modifier.height(if (isFirstInCluster || showDateHeader) 8.dp else 2.dp))

                            if (msg.msgType == "SYSTEM") {
                                SystemNoticePill(text = msg.body)
                            } else {
                                val channelDisplayName = chat?.name?.takeIf { it.isNotBlank() } ?: chatJid.substringBefore("@")
                                if (isChannel || isGroupAnnounceLocked) {
                                    MessageBubbleRow(
                                        msg = msg,
                                        isGroup = chat?.isGroup == true && !isChannel,
                                        isChannel = isChannel,
                                        channelDisplayName = channelDisplayName,
                                        showTail = if (isChannel) false else isFirstInCluster,
                                        ghostPlayedActive = effectivePrivacy?.sendPlayedReceipts == false,
                                        onClickEditBadge = { editHistoryTarget = msg },
                                        onManualSendPlayedReceipt = { viewModel.markAudioPlayedExplicitly(msg) },
                                        onRequestDownload = { viewModel.requestMediaDownload(msg) },
                                        onOpenMedia = { mediaLightboxTarget = msg },
                                        onLongPress = {
                                            showMoreReactionEmojis = false
                                            actionMenuTarget = msg
                                        }
                                    )
                                } else {
                                    SwipeToReplyBox(onReply = { replyTarget = msg }) {
                                        MessageBubbleRow(
                                            msg = msg,
                                            isGroup = chat?.isGroup == true,
                                            isChannel = false,
                                            channelDisplayName = channelDisplayName,
                                            showTail = isFirstInCluster,
                                            ghostPlayedActive = effectivePrivacy?.sendPlayedReceipts == false,
                                            onClickEditBadge = { editHistoryTarget = msg },
                                            onManualSendPlayedReceipt = { viewModel.markAudioPlayedExplicitly(msg) },
                                            onRequestDownload = { viewModel.requestMediaDownload(msg) },
                                            onOpenMedia = { mediaLightboxTarget = msg },
                                            onLongPress = {
                                                showMoreReactionEmojis = false
                                                actionMenuTarget = msg
                                            }
                                        )
                                    }
                                }
                            }
                        }
                    }
                }

                // Floating WhatsApp Emoji Reaction Picker Pill when a message is long-pressed
                actionMenuTarget?.let { target ->
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .clickable(
                                interactionSource = remember { androidx.compose.foundation.interaction.MutableInteractionSource() },
                                indication = null
                            ) {
                                actionMenuTarget = null
                                showMoreReactionEmojis = false
                                showSelectionOverflow = false
                            },
                        contentAlignment = Alignment.BottomCenter
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            modifier = Modifier.padding(horizontal = 14.dp, vertical = 18.dp)
                        ) {
                            AnimatedVisibility(visible = showMoreReactionEmojis) {
                                Surface(
                                    shape = RoundedCornerShape(20.dp),
                                    color = if (colors.isDark) Color(0xFF233138) else Color.White,
                                    shadowElevation = 10.dp,
                                    modifier = Modifier.padding(bottom = 8.dp)
                                ) {
                                    Column(
                                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp),
                                        verticalArrangement = Arrangement.spacedBy(8.dp)
                                    ) {
                                        val extraRows = listOf(
                                            listOf("😍", "🔥", "👏", "🎉", "💯", "🤣"),
                                            listOf("🥳", "🤝", "🙌", "✨", "👀", "🫡"),
                                            listOf("😭", "😡", "👌", "✅", "💔", "🤔")
                                        )
                                        extraRows.forEach { rowEmojis ->
                                            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                                                rowEmojis.forEach { emoji ->
                                                    Text(
                                                        text = emoji,
                                                        fontSize = 24.sp,
                                                        modifier = Modifier
                                                            .clip(CircleShape)
                                                            .clickable {
                                                                viewModel.sendReaction(chatJid, target, emoji)
                                                                actionMenuTarget = null
                                                                showMoreReactionEmojis = false
                                                            }
                                                            .padding(4.dp)
                                                    )
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                            Surface(
                                shape = RoundedCornerShape(28.dp),
                                color = if (colors.isDark) Color(0xFF233138) else Color.White,
                                shadowElevation = 10.dp
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                                ) {
                                    listOf("👍", "❤️", "😂", "😮", "😢", "🙏", "😊").forEach { emoji ->
                                        val isCurrent = target.reactionSummary.contains(emoji)
                                        Box(
                                            modifier = Modifier
                                                .size(38.dp)
                                                .clip(CircleShape)
                                                .background(if (isCurrent) colors.brightGreen.copy(alpha = 0.22f) else Color.Transparent)
                                                .clickable {
                                                    viewModel.sendReaction(chatJid, target, emoji)
                                                    actionMenuTarget = null
                                                    showMoreReactionEmojis = false
                                                },
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Text(text = emoji, fontSize = 23.sp)
                                        }
                                    }
                                    Box(
                                        modifier = Modifier
                                            .size(34.dp)
                                            .clip(CircleShape)
                                            .background(colors.quotedBg)
                                            .clickable { showMoreReactionEmojis = !showMoreReactionEmojis },
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(
                                            imageVector = if (showMoreReactionEmojis) Icons.Rounded.Close else Icons.Rounded.Add,
                                            contentDescription = "More reactions",
                                            tint = colors.textSecondary,
                                            modifier = Modifier.size(20.dp)
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
                }

                if (isGroupAnnounceLocked && !isChannel) {
                    Surface(
                        color = colors.appSurface,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp, vertical = 14.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "Only admins can send messages",
                                color = colors.textSecondary,
                                fontSize = 13.5.sp
                            )
                        }
                    }
                } else if (!isChannel) {
                // Attachment & Quality Mode Tray
                AnimatedVisibility(visible = showAttachTray) {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 10.dp, vertical = 4.dp),
                        colors = CardDefaults.cardColors(containerColor = colors.elevatedBar)
                    ) {
                        Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            Text("Outbound Media Quality", fontWeight = FontWeight.Bold, color = colors.textPrimary, fontSize = 13.sp)
                            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                OutboundQualityMode.entries.forEach { mode ->
                                    val selected = mode == qualityMode
                                    Box(
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(14.dp))
                                            .background(if (selected) colors.pillActiveBg else colors.pillInactiveBg)
                                            .clickable { qualityMode = mode }
                                            .padding(horizontal = 10.dp, vertical = 6.dp)
                                    ) {
                                        Text(
                                            text = mode.label,
                                            fontSize = 11.5.sp,
                                            fontWeight = FontWeight.SemiBold,
                                            color = if (selected) colors.pillActiveText else colors.textSecondary
                                        )
                                    }
                                }
                            }
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Button(
                                    onClick = { pickMediaLauncher.launch("*/*") },
                                    colors = ButtonDefaults.buttonColors(containerColor = colors.accentGreen)
                                ) {
                                    Text("Pick File / Photo", fontSize = 12.sp, color = Color.White)
                                }
                                if (chat?.isGroup == true) {
                                    OutlinedButton(onClick = { ghostMentionEveryone = !ghostMentionEveryone }) {
                                        Icon(Icons.Rounded.AlternateEmail, null, modifier = Modifier.size(16.dp))
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text(
                                            text = if (ghostMentionEveryone) "@everyone Silent ON" else "@everyone Ghost Mention",
                                            fontSize = 12.sp
                                        )
                                    }
                                }
                            }
                        }
                    }
                }

                // Reply Context Preview Banner above Composer
                AnimatedVisibility(visible = replyTarget != null) {
                    replyTarget?.let { rep ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 10.dp, vertical = 2.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .background(colors.elevatedBar)
                                .padding(horizontal = 12.dp, vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .width(4.dp)
                                    .height(32.dp)
                                    .background(colors.brightGreen, RoundedCornerShape(2.dp))
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = rep.senderName.ifBlank { rep.senderJid.substringBefore("@") },
                                    color = colors.brightGreen,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 12.sp
                                )
                                Text(
                                    text = rep.body.ifBlank { rep.msgType },
                                    color = colors.textSecondary,
                                    fontSize = 12.sp,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                            IconButton(onClick = { replyTarget = null }, modifier = Modifier.size(24.dp)) {
                                Icon(Icons.Rounded.Close, "Cancel Reply", tint = colors.textSecondary)
                            }
                        }
                    }
                }

                // Floating WhatsApp Composer Pill + Morphing Mic/Send FAB
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 8.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.Bottom
                ) {
                    Row(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(26.dp))
                            .background(colors.elevatedBar)
                            .padding(horizontal = 10.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        if (isRecordingVoice) {
                            Box(
                                modifier = Modifier
                                    .size(10.dp)
                                    .clip(CircleShape)
                                    .background(colors.antiDeleteRed)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Recording voice note... tap stop to send",
                                color = colors.antiDeleteRed,
                                fontSize = 13.5.sp,
                                fontWeight = FontWeight.Medium,
                                modifier = Modifier.weight(1f)
                            )
                            TextButton(onClick = { viewModel.cancelVoiceRecording() }) {
                                Text("Cancel", color = colors.textSecondary)
                            }
                        } else {
                            IconButton(
                                onClick = { },
                                modifier = Modifier.size(34.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Rounded.SentimentSatisfiedAlt,
                                    contentDescription = "Emoji",
                                    tint = colors.textSecondary,
                                    modifier = Modifier.size(23.dp)
                                )
                            }
                            if (ghostMentionEveryone && chat?.isGroup == true) {
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(10.dp))
                                        .background(colors.pillActiveBg)
                                        .padding(horizontal = 6.dp, vertical = 2.dp)
                                ) {
                                    Text("@all", color = colors.pillActiveText, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                }
                                Spacer(modifier = Modifier.width(6.dp))
                            }
                            Box(modifier = Modifier.weight(1f).padding(vertical = 6.dp, horizontal = 4.dp)) {
                                if (draftText.isEmpty()) {
                                    Text(
                                        text = if (canSendOutbound) "Message" else "Blocked by Error 463 guard",
                                        color = colors.textSecondary,
                                        fontSize = 15.5.sp
                                    )
                                }
                                BasicTextField(
                                    value = draftText,
                                    onValueChange = { draftText = it },
                                    enabled = canSendOutbound,
                                    textStyle = TextStyle(
                                        color = colors.textPrimary,
                                        fontSize = 15.5.sp,
                                        lineHeight = 20.sp
                                    ),
                                    cursorBrush = SolidColor(colors.brightGreen),
                                    maxLines = 6,
                                    modifier = Modifier.fillMaxWidth()
                                )
                            }
                            IconButton(
                                onClick = { showAttachTray = !showAttachTray },
                                modifier = Modifier.size(34.dp)
                            ) {
                                Icon(Icons.Rounded.AttachFile, "Attach", tint = colors.textSecondary)
                            }
                            AnimatedVisibility(visible = draftText.isBlank()) {
                                IconButton(
                                    onClick = { pickMediaLauncher.launch("image/*") },
                                    modifier = Modifier.size(34.dp)
                                ) {
                                    Icon(
                                        painter = painterResource(R.drawable.vec_ic_camera_reply),
                                        contentDescription = "Camera / Photo",
                                        tint = colors.textSecondary,
                                        modifier = Modifier.size(20.dp)
                                    )
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.width(6.dp))

                    Box(
                        modifier = Modifier
                            .size(48.dp)
                            .clip(CircleShape)
                            .background(
                                if (!canSendOutbound) colors.textSecondary
                                else if (isRecordingVoice) colors.antiDeleteRed
                                else colors.brightGreen
                            )
                            .clickable(enabled = canSendOutbound) {
                                when {
                                    isRecordingVoice -> viewModel.toggleVoiceRecording(chatJid, true)
                                    draftText.isNotBlank() -> {
                                        viewModel.sendText(chatJid, draftText, replyTarget, ghostMentionEveryone)
                                        draftText = ""
                                        replyTarget = null
                                        ghostMentionEveryone = false
                                    }
                                    else -> {
                                        val hasMic = ContextCompat.checkSelfPermission(
                                            context,
                                            Manifest.permission.RECORD_AUDIO
                                        ) == PackageManager.PERMISSION_GRANTED
                                        if (hasMic) {
                                            viewModel.toggleVoiceRecording(chatJid, true)
                                        } else {
                                            micPermLauncher.launch(Manifest.permission.RECORD_AUDIO)
                                        }
                                    }
                                }
                            },
                        contentAlignment = Alignment.Center
                    ) {
                        AnimatedContent(
                            targetState = when {
                                isRecordingVoice -> 2
                                draftText.isNotBlank() -> 1
                                else -> 0
                            },
                            label = "morph_fab"
                        ) { mode ->
                            when (mode) {
                                2 -> Icon(Icons.Rounded.Stop, "Stop & Send Audio", tint = Color.White)
                                1 -> Icon(Icons.AutoMirrored.Rounded.Send, "Send", tint = Color.White)
                                else -> Icon(
                                    painter = painterResource(R.drawable.msg_status_mic),
                                    contentDescription = "Record Voice Note",
                                    tint = Color.White,
                                    modifier = Modifier.size(22.dp)
                                )
                            }
                        }
                    }
                }
                }
            }
        }
    }

    // Per-Chat Ghost Override Sheet
    if (showGhostOverrideSheet) {
        val currentPrivacy = effectivePrivacy
        var sendRead by remember(currentPrivacy) { mutableStateOf(currentPrivacy?.sendReadReceipts ?: false) }
        var sendPlayed by remember(currentPrivacy) { mutableStateOf(currentPrivacy?.sendPlayedReceipts ?: false) }
        var sendTyping by remember(currentPrivacy) { mutableStateOf(currentPrivacy?.sendChatPresence ?: false) }

        ModalBottomSheet(
            onDismissRequest = { showGhostOverrideSheet = false },
            containerColor = colors.elevatedBar
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 10.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Text(
                    text = "Per-Chat Ghost Matrix (${chat?.name ?: chatJid})",
                    fontWeight = FontWeight.Bold,
                    color = colors.textPrimary,
                    fontSize = 16.sp
                )
                Text(
                    text = "Override global stealth policies exclusively for this thread:",
                    color = colors.textSecondary,
                    fontSize = 12.5.sp
                )
                Row(Modifier.fillMaxWidth(), Arrangement.SpaceBetween, Alignment.CenterVertically) {
                    Text("Send Read Receipts (Blue Ticks)", color = colors.textPrimary)
                    Switch(checked = sendRead, onCheckedChange = { sendRead = it })
                }
                Row(Modifier.fillMaxWidth(), Arrangement.SpaceBetween, Alignment.CenterVertically) {
                    Text("Send Audio Played Receipts", color = colors.textPrimary)
                    Switch(checked = sendPlayed, onCheckedChange = { sendPlayed = it })
                }
                Row(Modifier.fillMaxWidth(), Arrangement.SpaceBetween, Alignment.CenterVertically) {
                    Text("Send Live Typing & Recording", color = colors.textPrimary)
                    Switch(checked = sendTyping, onCheckedChange = { sendTyping = it })
                }
                Row(
                    modifier = Modifier.fillMaxWidth().padding(top = 6.dp, bottom = 18.dp),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    OutlinedButton(
                        onClick = {
                            viewModel.clearChatPrivacyOverride(chatJid)
                            showGhostOverrideSheet = false
                        },
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("Use Global Default")
                    }
                    Button(
                        onClick = {
                            viewModel.saveChatPrivacyOverride(chatJid, sendRead, sendPlayed, sendTyping)
                            showGhostOverrideSheet = false
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = colors.accentGreen),
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("Save Override", color = Color.White)
                    }
                }
            }
        }
    }

    // Unlimited Edit History Bottom Sheet
    editHistoryTarget?.let { target ->
        val edits by viewModel.observeEdits(target.id).collectAsState(initial = emptyList())
        ModalBottomSheet(
            onDismissRequest = { editHistoryTarget = null },
            containerColor = colors.elevatedBar
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 12.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Text(
                    text = "Unlimited Edit History",
                    fontWeight = FontWeight.Bold,
                    color = colors.textPrimary,
                    fontSize = 17.sp
                )
                Card(colors = CardDefaults.cardColors(containerColor = colors.pillActiveBg)) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Text("Current Version · ${formatMessageClock(target.editedAt)}", fontSize = 11.5.sp, fontWeight = FontWeight.Bold, color = colors.pillActiveText)
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(target.body, color = colors.textPrimary, fontSize = 14.sp)
                    }
                }
                if (edits.isEmpty()) {
                    Text("No prior revisions recorded yet.", color = colors.textSecondary, fontSize = 13.sp)
                } else {
                    edits.forEachIndexed { idx, rev ->
                        Card(colors = CardDefaults.cardColors(containerColor = colors.pillInactiveBg)) {
                            Column(modifier = Modifier.padding(12.dp)) {
                                Text(
                                    text = "Revision #${edits.size - idx} · ${formatMessageClock(rev.editedAt)}",
                                    fontSize = 11.sp,
                                    fontFamily = mono,
                                    color = colors.textSecondary
                                )
                                Spacer(modifier = Modifier.height(3.dp))
                                Text(rev.oldText, color = colors.textPrimary, fontSize = 13.5.sp)
                            }
                        }
                    }
                }
                Spacer(modifier = Modifier.height(16.dp))
            }
        }
    }

    // Group Receipts Bottom Sheet
    groupReceiptsTarget?.let { target ->
        val receipts by viewModel.observeGroupReceipts(target.id).collectAsState(initial = emptyList())
        ModalBottomSheet(
            onDismissRequest = { groupReceiptsTarget = null },
            containerColor = colors.elevatedBar
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 12.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text("Group Per-Participant Receipts", fontWeight = FontWeight.Bold, color = colors.textPrimary, fontSize = 16.sp)
                if (receipts.isEmpty()) {
                    Text("No individual receipts recorded for this message yet.", color = colors.textSecondary)
                } else {
                    receipts.forEach { r ->
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(r.participantName.ifBlank { r.participantJid }, color = colors.textPrimary)
                            Text("${r.status} · ${formatMessageClock(r.timestamp)}", color = colors.blueTick, fontFamily = mono, fontSize = 12.sp)
                        }
                    }
                }
                Spacer(modifier = Modifier.height(18.dp))
            }
        }
    }

    // Edit Message Text Dialog
    editMessageDialogTarget?.let { target ->
        var newBody by remember(target.id) { mutableStateOf(target.body) }
        AlertDialog(
            onDismissRequest = { editMessageDialogTarget = null },
            containerColor = colors.elevatedBar,
            title = { Text("Edit Message", color = colors.textPrimary) },
            text = {
                OutlinedTextField(
                    value = newBody,
                    onValueChange = { newBody = it },
                    modifier = Modifier.fillMaxWidth()
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.editMessage(chatJid, target.id, newBody)
                        editMessageDialogTarget = null
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = colors.accentGreen)
                ) {
                    Text("Save Edit", color = Color.White)
                }
            },
            dismissButton = {
                TextButton(onClick = { editMessageDialogTarget = null }) { Text("Cancel") }
            }
        )
    }

    // Full-screen Media / View Once Lightbox
    mediaLightboxTarget?.let { target ->
        val displaySender = if (target.isFromMe) {
            "You"
        } else {
            chat?.name?.takeIf { it.isNotBlank() }
                ?: target.senderName.ifBlank { target.senderJid.substringBefore("@") }
        }
        FullScreenMediaViewer(
            msg = target,
            senderDisplayName = displaySender,
            onDismiss = { mediaLightboxTarget = null },
            onSaveToGallery = {
                val ext = if (target.mimeType.contains("mp4", ignoreCase = true) || target.msgType == "VIDEO") "mp4" else "png"
                val mime = target.mimeType.ifBlank { if (ext == "mp4") "video/mp4" else "image/png" }
                viewModel.exportMediaToGallery(
                    target.localMediaPath,
                    "WA_Vault_${target.id}.$ext",
                    mime
                )
            },
            onReply = if (isChannel) null else {
                { replyTarget = target }
            }
        )
    }
}

@Composable
private fun ActionSheetRow(
    title: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    onClick: () -> Unit
) {
    val colors = LocalWaColors.current
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 10.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(icon, null, tint = colors.accentGreen, modifier = Modifier.size(20.dp))
        Spacer(modifier = Modifier.width(14.dp))
        Text(title, color = colors.textPrimary, fontSize = 14.5.sp)
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun MessageBubbleRow(
    msg: MessageEntity,
    isGroup: Boolean,
    isChannel: Boolean = false,
    channelDisplayName: String = "",
    showTail: Boolean,
    ghostPlayedActive: Boolean,
    onClickEditBadge: () -> Unit,
    onManualSendPlayedReceipt: () -> Unit,
    onRequestDownload: () -> Unit = {},
    onOpenMedia: () -> Unit,
    onLongPress: () -> Unit
) {
    val colors = LocalWaColors.current
    val mono = LocalWaMonoFamily.current
    val effectiveOutgoing = msg.isFromMe && !isChannel
    val bubbleColor = if (effectiveOutgoing) colors.outgoingBubble else colors.incomingBubble
    val shape = when {
        isChannel -> RoundedCornerShape(12.dp)
        showTail -> WhatsAppBubbleShape(effectiveOutgoing, true)
        else -> WhatsAppClusterInnerShape
    }

    val isMediaCard = (msg.msgType == "IMAGE" || msg.msgType == "VIDEO") &&
        msg.viewOnceState != "STRIPPED" &&
        !(msg.isViewOnce && msg.viewOnceState == "AVAILABLE") &&
        !msg.isDeletedBySender

    val sideStartPad = if (isMediaCard) {
        if (!isChannel && showTail && !effectiveOutgoing) 11.dp else 3.dp
    } else {
        if (!isChannel && showTail && !effectiveOutgoing) 16.dp else 12.dp
    }
    val sideEndPad = if (isMediaCard) {
        if (!isChannel && showTail && effectiveOutgoing) 11.dp else 3.dp
    } else {
        if (!isChannel && showTail && effectiveOutgoing) 16.dp else 12.dp
    }
    val topBottomPad = if (isMediaCard) 3.dp else 8.dp

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(
                start = when {
                    isChannel -> 4.dp
                    effectiveOutgoing -> 52.dp
                    else -> 0.dp
                },
                end = when {
                    isChannel -> 4.dp
                    effectiveOutgoing -> 0.dp
                    else -> 52.dp
                }
            ),
        horizontalArrangement = when {
            isChannel -> Arrangement.Center
            effectiveOutgoing -> Arrangement.End
            else -> Arrangement.Start
        }
    ) {
        Column(
            horizontalAlignment = if (effectiveOutgoing) Alignment.End else Alignment.Start
        ) {
        Box(
            modifier = Modifier
                .then(if (isChannel) Modifier.fillMaxWidth().widthIn(max = 390.dp) else Modifier.widthIn(max = 310.dp))
                .clip(shape)
                .background(bubbleColor)
                .combinedClickable(
                    onClick = {
                        when {
                            msg.isEdited -> onClickEditBadge()
                            msg.isViewOnce && msg.viewOnceState == "AVAILABLE" -> onOpenMedia()
                            !msg.localMediaPath.isNullOrBlank() && msg.msgType != "PTT" && msg.msgType != "AUDIO" -> onOpenMedia()
                            msg.msgType == "IMAGE" || msg.msgType == "VIDEO" -> onRequestDownload()
                        }
                    },
                    onLongClick = onLongPress
                )
                .padding(start = sideStartPad, end = sideEndPad, top = topBottomPad, bottom = topBottomPad)
        ) {
            Column {
                // Sender color label in Groups
                if (isGroup && !effectiveOutgoing && showTail) {
                    val palette = listOf(Color(0xFF53BDEB), Color(0xFFFFB74D), Color(0xFF7CE8A5), Color(0xFFF48FB1))
                    val c = palette[abs(msg.senderJid.hashCode()) % palette.size]
                    Text(
                        text = msg.senderName.ifBlank { msg.senderJid.substringBefore("@") },
                        color = c,
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 12.5.sp,
                        modifier = Modifier.padding(bottom = 2.dp)
                    )
                }

                // Quoted Context Banner
                if (!msg.quotedId.isNullOrBlank()) {
                    val rawQuoted = msg.quotedSender.orEmpty()
                    val resolvedQuotedSender = when {
                        isChannel && (rawQuoted.isBlank() || rawQuoted == msg.chatJid || rawQuoted.endsWith("@newsletter")) ->
                            channelDisplayName.ifBlank { "Channel" }
                        rawQuoted.endsWith("@newsletter") ->
                            channelDisplayName.ifBlank { rawQuoted.substringBefore("@") }
                        rawQuoted.contains("@") -> rawQuoted.substringBefore("@")
                        rawQuoted.isNotBlank() -> rawQuoted
                        else -> "Quoted message"
                    }
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 5.dp)
                            .clip(RoundedCornerShape(6.dp))
                            .background(colors.quotedBg)
                            .padding(6.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .width(3.dp)
                                .height(28.dp)
                                .background(colors.brightGreen, RoundedCornerShape(2.dp))
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Column {
                            Text(
                                text = resolvedQuotedSender,
                                color = colors.brightGreen,
                                fontWeight = FontWeight.Bold,
                                fontSize = 11.5.sp
                            )
                            Text(
                                text = msg.quotedBody.orEmpty(),
                                color = colors.textSecondary,
                                fontSize = 12.sp,
                                maxLines = 2,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }
                }

                when {
                    // Explicit forensic stub when Meta servers strip Key=0B on companion session
                    msg.viewOnceState == "STRIPPED" || msg.msgType == "VIEW_ONCE_STRIPPED" -> {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(8.dp))
                                .background(colors.antiDeleteRed.copy(alpha = 0.14f))
                                .padding(10.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                painter = painterResource(R.drawable.vec_ic_view_once),
                                contentDescription = "View Once Restricted",
                                tint = colors.antiDeleteRed,
                                modifier = Modifier.size(22.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = "View Once (Restricted on Companion)",
                                    color = colors.antiDeleteRed,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 13.sp
                                )
                                Text(
                                    text = msg.body.ifBlank { "Meta stripped MediaKey (Key=0B)" },
                                    color = colors.textSecondary,
                                    fontSize = 11.5.sp
                                )
                            }
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        Row(modifier = Modifier.align(Alignment.End)) {
                            MessageMetaCluster(
                                timeText = formatMessageClock(msg.timestamp),
                                isEdited = false,
                                isEphemeral = msg.isEphemeral,
                                isFromMe = msg.isFromMe,
                                deliveryStatus = msg.deliveryStatus
                            )
                        }
                    }

                    // Vaulted Unstripped View Once Media (Unlimited views + Gallery export)
                    msg.isViewOnce && msg.viewOnceState == "AVAILABLE" -> {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(8.dp))
                                .background(colors.pillActiveBg)
                                .clickable(onClick = onOpenMedia)
                                .padding(10.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                painter = painterResource(R.drawable.vec_ic_view_once),
                                contentDescription = "View Once Vault",
                                tint = colors.pillActiveText,
                                modifier = Modifier.size(22.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = "View Once Unlocked · Vaulted",
                                    color = colors.pillActiveText,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 13.sp
                                )
                                Text(
                                    text = msg.body.ifBlank { "Tap to view anytime or save to Gallery" },
                                    color = colors.textSecondary,
                                    fontSize = 11.5.sp
                                )
                            }
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        Row(modifier = Modifier.align(Alignment.End)) {
                            MessageMetaCluster(
                                timeText = formatMessageClock(msg.timestamp),
                                isEdited = msg.isEdited,
                                isEphemeral = msg.isEphemeral,
                                isFromMe = msg.isFromMe,
                                deliveryStatus = msg.deliveryStatus
                            )
                        }
                    }

                    // Voice Note (PTT) / Audio
                    msg.msgType == "PTT" || msg.msgType == "AUDIO" -> {
                        VoiceNoteBubble(
                            msg = msg,
                            ghostPlayedActive = ghostPlayedActive,
                            onManualSendPlayedReceipt = onManualSendPlayedReceipt,
                            onRequestDownload = onRequestDownload
                        )
                    }

                    // Call Log Bubble ("Voice call · No answer / 1 min / 39 sec")
                    msg.msgType == "CALL" -> {
                        val parts = msg.body.split("|", limit = 2)
                        val callTitle = parts.getOrNull(0)?.ifBlank { "Voice call" } ?: "Voice call"
                        val callSubtitle = parts.getOrNull(1)?.ifBlank {
                            when {
                                msg.durationSec in 1..59 -> "${msg.durationSec} sec"
                                msg.durationSec >= 60 -> "${(msg.durationSec + 30) / 60} min"
                                else -> "No answer"
                            }
                        } ?: "No answer"
                        val isVideoCall = callTitle.contains("Video", ignoreCase = true)
                        val isMissed = callSubtitle.contains("No answer", ignoreCase = true) ||
                            callSubtitle.contains("Missed", ignoreCase = true) ||
                            callSubtitle.contains("Declined", ignoreCase = true)
                        val callCardBg = if (msg.isFromMe) {
                            if (colors.isDark) Color(0xFF0A4D3E) else Color(0xFFC5F2C0)
                        } else {
                            if (colors.isDark) Color(0xFF182229) else Color(0xFFF3F5F7)
                        }
                        val badgeBg = if (msg.isFromMe) {
                            if (colors.isDark) Color(0xFF1B6151) else Color(0xFFB3E6AE)
                        } else {
                            if (colors.isDark) Color(0xFF28353D) else Color(0xFFE1E6EA)
                        }
                        val iconVec = when {
                            isVideoCall -> Icons.Rounded.Videocam
                            isMissed && !msg.isFromMe -> Icons.Rounded.CallMissed
                            msg.isFromMe -> Icons.Rounded.CallMade
                            else -> Icons.Rounded.CallReceived
                        }
                        val iconTint = if (isMissed && !msg.isFromMe) colors.antiDeleteRed else colors.textPrimary

                        Column(modifier = Modifier.widthIn(min = 195.dp, max = 250.dp)) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(10.dp))
                                    .background(callCardBg)
                                    .padding(horizontal = 10.dp, vertical = 10.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(38.dp)
                                        .clip(CircleShape)
                                        .background(badgeBg),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = iconVec,
                                        contentDescription = callTitle,
                                        tint = iconTint,
                                        modifier = Modifier.size(19.dp)
                                    )
                                }
                                Spacer(modifier = Modifier.width(10.dp))
                                Column {
                                    Text(
                                        text = callTitle,
                                        color = colors.textPrimary,
                                        fontWeight = FontWeight.SemiBold,
                                        fontSize = 14.5.sp
                                    )
                                    Text(
                                        text = callSubtitle,
                                        color = colors.textSecondary,
                                        fontSize = 12.5.sp
                                    )
                                }
                            }
                            Spacer(modifier = Modifier.height(3.dp))
                            Row(modifier = Modifier.align(Alignment.End).padding(horizontal = 2.dp)) {
                                MessageMetaCluster(
                                    timeText = formatMessageClock(msg.timestamp),
                                    isEdited = false,
                                    isEphemeral = false,
                                    isFromMe = false,
                                    deliveryStatus = msg.deliveryStatus
                                )
                            }
                        }
                    }

                    // Photo / Video (edge-to-edge card + auto-download when localMediaPath missing)
                    msg.msgType == "IMAGE" || msg.msgType == "VIDEO" -> {
                        val bmp = remember(msg.localMediaPath) {
                            msg.localMediaPath?.takeIf { it.isNotBlank() }?.let { p ->
                                File(p).takeIf { it.exists() }
                                    ?.let { BitmapFactory.decodeFile(it.absolutePath)?.asImageBitmap() }
                            }
                        }
                        LaunchedEffect(msg.id, msg.localMediaPath) {
                            if (bmp == null) {
                                onRequestDownload()
                            }
                        }
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(10.dp))
                                .clickable {
                                    if (bmp != null) onOpenMedia() else onRequestDownload()
                                }
                        ) {
                            if (bmp != null) {
                                Image(
                                    bitmap = bmp,
                                    contentDescription = msg.body,
                                    contentScale = ContentScale.Crop,
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .heightIn(min = 180.dp, max = 300.dp)
                                )
                                if (msg.msgType == "VIDEO") {
                                    Box(
                                        modifier = Modifier
                                            .align(Alignment.Center)
                                            .size(48.dp)
                                            .clip(CircleShape)
                                            .background(Color(0x99000000)),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(
                                            imageVector = Icons.Rounded.PlayArrow,
                                            contentDescription = "Play video",
                                            tint = Color.White,
                                            modifier = Modifier.size(30.dp)
                                        )
                                    }
                                }
                            } else {
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(210.dp)
                                        .background(colors.quotedBg),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Column(
                                        horizontalAlignment = Alignment.CenterHorizontally,
                                        verticalArrangement = Arrangement.spacedBy(6.dp)
                                    ) {
                                        Box(
                                            modifier = Modifier
                                                .size(46.dp)
                                                .clip(CircleShape)
                                                .background(Color(0x66000000)),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Icon(
                                                imageVector = if (msg.fileSize > 0) Icons.Rounded.Download else Icons.Rounded.Image,
                                                contentDescription = "Download ${msg.msgType}",
                                                tint = Color.White,
                                                modifier = Modifier.size(24.dp)
                                            )
                                        }
                                        val sizeLabel = if (msg.fileSize > 0L) " · ${maxOf(1L, msg.fileSize / 1024L)} KB" else ""
                                        Text(
                                            text = (if (msg.msgType == "VIDEO") "Video" else "Photo") + sizeLabel,
                                            color = colors.textSecondary,
                                            fontSize = 12.sp
                                        )
                                    }
                                }
                            }
                            if (msg.body.isBlank()) {
                                Box(
                                    modifier = Modifier
                                        .align(Alignment.BottomEnd)
                                        .padding(6.dp)
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(colors.mediaScrim)
                                        .padding(horizontal = 6.dp, vertical = 2.dp)
                                ) {
                                    MessageMetaCluster(
                                        timeText = formatMessageClock(msg.timestamp),
                                        isEdited = msg.isEdited,
                                        isEphemeral = msg.isEphemeral,
                                        isFromMe = msg.isFromMe,
                                        deliveryStatus = msg.deliveryStatus,
                                        tintOverride = Color.White
                                    )
                                }
                            }
                        }
                        if (msg.body.isNotBlank()) {
                            Box(modifier = Modifier.padding(horizontal = 7.dp, vertical = 4.dp)) {
                                InlineTimestampMessage(msg = msg)
                            }
                        }
                    }

                    // Standard Text, Document, Sticker, or Anti-Deleted text bubble
                    else -> {
                        InlineTimestampMessage(msg = msg)
                    }
                }
            }
        }
        // Reaction & forward pill underneath the bubble/card
        if (msg.reactionSummary.isNotBlank()) {
            Box(
                modifier = Modifier
                    .padding(top = 3.dp, start = 4.dp, end = 4.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(colors.elevatedBar)
                    .padding(horizontal = 8.dp, vertical = 3.dp)
            ) {
                Text(
                    text = msg.reactionSummary,
                    color = colors.textSecondary,
                    fontSize = 12.sp
                )
            }
        }
        }
    }
}

private fun isHiddenPlaceholderMessage(msg: MessageEntity): Boolean {
    return msg.msgType == "UNKNOWN" &&
        msg.body.isBlank() &&
        msg.localMediaPath.isNullOrBlank() &&
        !msg.isViewOnce &&
        !msg.isDeletedBySender
}

private fun isSameCalendarDay(ts1: Long, ts2: Long): Boolean {
    if (ts1 <= 0L || ts2 <= 0L) return false
    val c1 = Calendar.getInstance().apply { timeInMillis = ts1 }
    val c2 = Calendar.getInstance().apply { timeInMillis = ts2 }
    return c1.get(Calendar.YEAR) == c2.get(Calendar.YEAR) &&
        c1.get(Calendar.DAY_OF_YEAR) == c2.get(Calendar.DAY_OF_YEAR)
}

private fun formatChatDateHeader(timestamp: Long): String {
    val now = System.currentTimeMillis()
    val today = Calendar.getInstance().apply { timeInMillis = now }
    val yesterday = Calendar.getInstance().apply {
        timeInMillis = now
        add(Calendar.DAY_OF_YEAR, -1)
    }
    val target = Calendar.getInstance().apply { timeInMillis = timestamp }
    return when {
        target.get(Calendar.YEAR) == today.get(Calendar.YEAR) &&
            target.get(Calendar.DAY_OF_YEAR) == today.get(Calendar.DAY_OF_YEAR) -> "Today"
        target.get(Calendar.YEAR) == yesterday.get(Calendar.YEAR) &&
            target.get(Calendar.DAY_OF_YEAR) == yesterday.get(Calendar.DAY_OF_YEAR) -> "Yesterday"
        else -> SimpleDateFormat("MMMM d, yyyy", Locale.getDefault()).format(Date(timestamp))
    }
}

@Composable
private fun DateHeaderPill(timestamp: Long) {
    val colors = LocalWaColors.current
    val pillBg = if (colors.isDark) Color(0xFF182229) else Color(0xFFE7EFF3)
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        contentAlignment = Alignment.Center
    ) {
        Box(
            modifier = Modifier
                .clip(RoundedCornerShape(10.dp))
                .background(pillBg)
                .padding(horizontal = 12.dp, vertical = 5.dp)
        ) {
            Text(
                text = formatChatDateHeader(timestamp),
                color = colors.textSecondary,
                fontSize = 12.5.sp,
                fontWeight = FontWeight.Medium
            )
        }
    }
}

@Composable
private fun SystemNoticePill(text: String) {
    if (text.isBlank()) return
    val colors = LocalWaColors.current
    val pillBg = if (colors.isDark) Color(0xFF182229) else Color(0xFFE7EFF3)
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp, vertical = 4.dp),
        contentAlignment = Alignment.Center
    ) {
        Box(
            modifier = Modifier
                .clip(RoundedCornerShape(10.dp))
                .background(pillBg)
                .padding(horizontal = 12.dp, vertical = 6.dp)
        ) {
            Text(
                text = text,
                color = colors.textSecondary,
                fontSize = 12.5.sp
            )
        }
    }
}
