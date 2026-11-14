package com.wa.beastclient.ui

import android.app.Application
import android.content.Intent
import android.media.MediaRecorder
import android.net.Uri
import android.os.Build
import android.widget.Toast
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import androidx.paging.cachedIn
import com.wa.beastclient.core.service.BridgeStatus
import com.wa.beastclient.core.service.WaRealtimeService
import com.wa.beastclient.core.update.AppReleaseInfo
import com.wa.beastclient.core.update.UpdateUiState
import com.wa.beastclient.core.update.WuppUpdater
import com.wa.beastclient.data.db.ChatEntity
import com.wa.beastclient.data.db.GroupReceiptEntity
import com.wa.beastclient.data.db.MessageEditEntity
import com.wa.beastclient.data.db.MessageEntity
import com.wa.beastclient.data.db.PresenceLogEntity
import com.wa.beastclient.data.db.ProfileHistoryEntity
import com.wa.beastclient.data.db.StatusEntity
import com.wa.beastclient.data.db.WaUiDatabase
import com.wa.beastclient.data.media.EagerMediaVault
import com.wa.beastclient.data.media.OutboundQualityMode
import com.wa.beastclient.data.media.SoundPoolManager
import com.wa.beastclient.domain.stealth.EffectiveChatPrivacy
import com.wa.beastclient.domain.stealth.GlobalStealthConfig
import com.wa.beastclient.domain.stealth.PrimaryHeartbeatStatus
import com.wa.beastclient.domain.stealth.StealthMatrixManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File

enum class HomeFilterChip(val label: String) {
    ALL("All"),
    UNREAD("Unread"),
    FAVORITES("Favorites"),
    GROUPS("Groups"),
    DELETED("Deleted"),
    EDITED("Edited")
}

class MainViewModel(application: Application) : AndroidViewModel(application) {
    private val appContext = application.applicationContext
    private val dao = WaUiDatabase.getInstance(appContext).waDao()

    val stealthConfig: StateFlow<GlobalStealthConfig> = StealthMatrixManager.config
    val bridgeStatus: StateFlow<BridgeStatus> = WaRealtimeService.bridgeStatus
    val updateState: StateFlow<UpdateUiState> = WuppUpdater.state
    val autoUpdateEnabled: StateFlow<Boolean> = WuppUpdater.autoCheckEnabled
    val showUpdatePrompt: StateFlow<Boolean> = WuppUpdater.showPromptDialog

    init {
        WuppUpdater.init(appContext)
        viewModelScope.launch {
            WuppUpdater.checkForUpdates(appContext, silent = true)
        }
    }

    val heartbeatStatus: StateFlow<PrimaryHeartbeatStatus> = stealthConfig
        .map { StealthMatrixManager.computeHeartbeatStatus(it) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), StealthMatrixManager.computeHeartbeatStatus())

    private val _includeArchived = MutableStateFlow(false)
    val includeArchived: StateFlow<Boolean> = _includeArchived.asStateFlow()

    private val _selectedFilter = MutableStateFlow(HomeFilterChip.ALL)
    val selectedFilter: StateFlow<HomeFilterChip> = _selectedFilter.asStateFlow()

    private val _homeSearchQuery = MutableStateFlow("")
    val homeSearchQuery: StateFlow<String> = _homeSearchQuery.asStateFlow()

    private val _ftsSearchResults = MutableStateFlow<List<MessageEntity>>(emptyList())
    val ftsSearchResults: StateFlow<List<MessageEntity>> = _ftsSearchResults.asStateFlow()

    private val _bulkVerifyOutput = MutableStateFlow<String?>(null)
    val bulkVerifyOutput: StateFlow<String?> = _bulkVerifyOutput.asStateFlow()

    private val _isRecordingVoice = MutableStateFlow(false)
    val isRecordingVoice: StateFlow<Boolean> = _isRecordingVoice.asStateFlow()
    private var activeRecorder: MediaRecorder? = null
    private var activeRecordingFile: File? = null
    private var recordingStartMs: Long = 0L

    @OptIn(ExperimentalCoroutinesApi::class)
    val allChats: StateFlow<List<ChatEntity>> = _includeArchived
        .flatMapLatest { dao.observeChats(it) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val filteredChats: StateFlow<List<ChatEntity>> = combine(
        allChats,
        _selectedFilter,
        _homeSearchQuery
    ) { chats, chip, query ->
        val chatThreads = chats.filter {
            !it.isCommunityParent &&
                !it.jid.endsWith("@newsletter") &&
                !(it.name.startsWith("@") && it.lastMessagePreview.isBlank() && (it.lastMessageType == "UNKNOWN" || it.lastMessageType.isBlank()))
        }
        val byChip = when (chip) {
            HomeFilterChip.ALL -> chatThreads
            HomeFilterChip.UNREAD -> chatThreads.filter { it.unreadCount > 0 }
            HomeFilterChip.FAVORITES -> chatThreads.filter { it.isFavorite }
            HomeFilterChip.GROUPS -> chatThreads.filter { it.isGroup }
            HomeFilterChip.DELETED -> chatThreads.filter { it.hasDeletedMessages || it.lastMessageIsDeleted }
            HomeFilterChip.EDITED -> chatThreads.filter { it.hasEditedMessages }
        }
        val q = query.trim().lowercase()
        if (q.isEmpty()) byChip
        else byChip.filter {
            it.name.lowercase().contains(q) ||
                it.jid.lowercase().contains(q) ||
                it.lastMessagePreview.lowercase().contains(q)
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val channels: StateFlow<List<ChatEntity>> = allChats
        .map { chats ->
            chats.filter { it.jid.endsWith("@newsletter") }
                .sortedWith(
                    compareByDescending<ChatEntity> { it.isPinned }
                        .thenByDescending { it.pinOrder }
                        .thenByDescending { it.lastMessageTimestamp }
                        .thenBy { it.name.lowercase() }
                )
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val statuses: StateFlow<List<StatusEntity>> = dao.observeStatuses()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val viewOnceVault: StateFlow<List<MessageEntity>> = dao.observeViewOnceVault()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val presenceLogs: StateFlow<List<PresenceLogEntity>> = dao.observeAllPresenceLogs()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val profileHistory: StateFlow<List<ProfileHistoryEntity>> = dao.observeAllProfileHistory()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun selectFilter(chip: HomeFilterChip) {
        _selectedFilter.value = chip
    }

    fun toggleArchivedIncluded() {
        _includeArchived.value = !_includeArchived.value
    }

    fun updateHomeSearch(query: String) {
        _homeSearchQuery.value = query
        viewModelScope.launch(Dispatchers.IO) {
            val cleaned = query.trim().replace("\"", "")
            if (cleaned.length < 2) {
                _ftsSearchResults.value = emptyList()
            } else {
                val results = runCatching {
                    dao.searchMessagesFts("$cleaned*")
                }.getOrDefault(emptyList())
                _ftsSearchResults.value = results
            }
        }
    }

    fun setActiveChat(chatJid: String?) {
        WaRealtimeService.activeChatJid.value = chatJid
        if (!chatJid.isNullOrBlank()) {
            WaRealtimeService.onChatOpened(appContext, chatJid)
        }
    }

    fun requestMediaDownload(msg: MessageEntity) {
        WaRealtimeService.requestMediaDownload(appContext, msg)
    }

    fun observeChat(chatJid: String): Flow<ChatEntity?> = dao.observeChat(chatJid)

    fun observeEffectivePrivacy(chatJid: String): Flow<EffectiveChatPrivacy> =
        StealthMatrixManager.observeEffectivePrivacy(dao, chatJid)

    fun canInitiateOutbound(chatJid: String): Flow<Boolean> {
        return if (chatJid.endsWith("@g.us")) {
            MutableStateFlow(true)
        } else {
            dao.observeRecentMessages(chatJid, 1).map { it.isNotEmpty() }
        }
    }

    fun pagedMessages(chatJid: String): Flow<PagingData<MessageEntity>> {
        return Pager(
            config = PagingConfig(
                pageSize = 60,
                initialLoadSize = 120,
                prefetchDistance = 20,
                enablePlaceholders = false
            ),
            pagingSourceFactory = { dao.pagingMessages(chatJid) }
        ).flow.cachedIn(viewModelScope)
    }

    suspend fun searchInChat(chatJid: String, query: String): List<MessageEntity> =
        withContext(Dispatchers.IO) {
            if (query.trim().length < 2) emptyList()
            else dao.searchInChat(chatJid, query.trim())
        }

    fun observeEdits(messageId: String): Flow<List<MessageEditEntity>> =
        dao.observeMessageEdits(messageId)

    fun observeGroupReceipts(messageId: String): Flow<List<GroupReceiptEntity>> =
        dao.observeGroupReceipts(messageId)

    fun toggleGhostPresence() {
        val next = !stealthConfig.value.ghostPresence
        viewModelScope.launch { WaRealtimeService.setGhostPresence(next) }
    }

    fun toggleDarkTheme() {
        StealthMatrixManager.updateConfig { it.copy(darkTheme = !it.darkTheme) }
    }

    fun updateGlobalStealth(transform: (GlobalStealthConfig) -> GlobalStealthConfig) {
        val prevGhost = stealthConfig.value.ghostPresence
        StealthMatrixManager.updateConfig(transform)
        val newGhost = stealthConfig.value.ghostPresence
        if (prevGhost != newGhost) {
            viewModelScope.launch { WaRealtimeService.setGhostPresence(newGhost) }
        }
    }

    fun saveChatPrivacyOverride(
        chatJid: String,
        sendRead: Boolean,
        sendPlayed: Boolean,
        sendTyping: Boolean
    ) {
        viewModelScope.launch(Dispatchers.IO) {
            StealthMatrixManager.setChatPrivacyOverride(dao, chatJid, sendRead, sendPlayed, sendTyping)
        }
    }

    fun clearChatPrivacyOverride(chatJid: String) {
        viewModelScope.launch(Dispatchers.IO) {
            StealthMatrixManager.clearChatPrivacyOverride(dao, chatJid)
        }
    }

    fun toggleFavoriteChat(chat: ChatEntity) {
        viewModelScope.launch(Dispatchers.IO) {
            dao.updateFavorite(chat.jid, !chat.isFavorite)
        }
    }

    fun togglePinChat(chat: ChatEntity) {
        viewModelScope.launch(Dispatchers.IO) {
            dao.updatePinned(chat.jid, !chat.isPinned, System.currentTimeMillis())
        }
    }

    fun toggleChannelMute(chatJid: String, currentlyMuted: Boolean) {
        viewModelScope.launch {
            val targetMuted = !currentlyMuted
            WaRealtimeService.toggleChannelMute(appContext, chatJid, targetMuted)
                .onSuccess {
                    showToast(if (targetMuted) "Channel muted" else "Channel notifications on")
                }
                .onFailure {
                    showToast(if (targetMuted) "Channel muted locally" else "Channel unmuted locally")
                }
        }
    }

    fun isGroupAnnounceLocked(chatJid: String): Flow<Boolean> =
        WaRealtimeService.announceLockedGroups.map { it.contains(chatJid) }

    fun sendReaction(chatJid: String, target: MessageEntity, emoji: String) {
        viewModelScope.launch {
            WaRealtimeService.sendReaction(appContext, chatJid, target, emoji)
                .onFailure { err ->
                    showToast("Reaction saved locally (${err.message ?: "offline"})")
                }
        }
    }

    fun pairWithPhoneNumber(phone: String, onResult: (String) -> Unit) {
        viewModelScope.launch {
            val digits = phone.filter { it.isDigit() }
            if (digits.length < 7) {
                showToast("Enter country code + phone digits")
                return@launch
            }
            WaRealtimeService.pairWithPhoneNumber(digits)
                .onSuccess { code ->
                    showToast("Pairing Code: $code")
                    onResult(code)
                }
                .onFailure { err ->
                    showToast("Pair error: ${err.message}")
                    onResult("")
                }
        }
    }

    fun reconnectSocket() {
        val intent = Intent(appContext, WaRealtimeService::class.java).apply {
            action = WaRealtimeService.ACTION_RECONNECT
        }
        appContext.startService(intent)
    }

    fun sendText(
        chatJid: String,
        text: String,
        quoted: MessageEntity?,
        ghostMentionEveryone: Boolean
    ) {
        if (text.isBlank()) return
        viewModelScope.launch {
            WaRealtimeService.sendTextMessage(appContext, chatJid, text.trim(), quoted, ghostMentionEveryone)
                .onFailure { err ->
                    showToast(err.message ?: "Send failed")
                }
        }
    }

    fun editMessage(chatJid: String, messageId: String, newText: String) {
        if (newText.isBlank()) return
        viewModelScope.launch {
            WaRealtimeService.editOwnMessage(appContext, chatJid, messageId, newText.trim())
                .onFailure { showToast("Edit error: ${it.message}") }
        }
    }

    fun revokeOrSimulateAntiDelete(chatJid: String, messageId: String, simulateIncoming: Boolean) {
        viewModelScope.launch {
            WaRealtimeService.revokeMessageRemoteOrSimulateAntiDelete(
                appContext,
                chatJid,
                messageId,
                simulateIncoming
            ).onSuccess {
                showToast(
                    if (simulateIncoming) "Anti-Delete captured: original bubble kept intact!"
                    else "Revoke sent & local copy preserved"
                )
            }
        }
    }

    fun markChatReadExplicitly(chatJid: String) {
        viewModelScope.launch {
            WaRealtimeService.markChatReadExplicitly(appContext, chatJid)
                .onSuccess { count ->
                    showToast("Sent manual Read receipts ($count msgs)")
                }
        }
    }

    fun markAudioPlayedExplicitly(msg: MessageEntity) {
        viewModelScope.launch {
            WaRealtimeService.sendManualPlayedReceipt(appContext, msg)
                .onSuccess { showToast("Sent manual Played receipt") }
        }
    }

    fun sendPickedMedia(
        chatJid: String,
        uri: Uri,
        mimeType: String,
        caption: String,
        qualityMode: OutboundQualityMode
    ) {
        viewModelScope.launch {
            val ext = when {
                mimeType.contains("png") -> "png"
                mimeType.contains("webp") -> "webp"
                mimeType.contains("mp4") -> "mp4"
                mimeType.contains("pdf") -> "pdf"
                mimeType.startsWith("image/") -> "jpg"
                else -> "bin"
            }
            val copied = EagerMediaVault.copyUriToCacheFile(appContext, uri, ext)
            if (copied == null) {
                showToast("Unable to read selected file")
                return@launch
            }
            WaRealtimeService.sendMediaMessage(
                context = appContext,
                chatJid = chatJid,
                sourceFile = copied,
                mimeType = mimeType,
                caption = caption,
                qualityMode = qualityMode,
                isVoiceNote = false,
                durationSec = 0
            ).onFailure { err ->
                showToast(err.message ?: "Media send failed")
            }
        }
    }

    fun toggleVoiceRecording(chatJid: String, hasRecordPermission: Boolean) {
        if (_isRecordingVoice.value) {
            stopAndSendVoiceRecording(chatJid)
        } else {
            startVoiceRecording(hasRecordPermission)
        }
    }

    private fun startVoiceRecording(hasRecordPermission: Boolean) {
        SoundPoolManager.playPttStart()
        val target = File(EagerMediaVault.mediaDir(appContext), "ptt_rec_${System.currentTimeMillis()}.m4a")
        activeRecordingFile = target
        recordingStartMs = System.currentTimeMillis()
        _isRecordingVoice.value = true

        if (hasRecordPermission) {
            runCatching {
                val rec = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                    MediaRecorder(appContext)
                } else {
                    @Suppress("DEPRECATION")
                    MediaRecorder()
                }
                rec.apply {
                    setAudioSource(MediaRecorder.AudioSource.MIC)
                    setOutputFormat(MediaRecorder.OutputFormat.MPEG_4)
                    setAudioEncoder(MediaRecorder.AudioEncoder.AAC)
                    setAudioEncodingBitRate(64000)
                    setAudioSamplingRate(16000)
                    setOutputFile(target.absolutePath)
                    prepare()
                    start()
                }
                activeRecorder = rec
            }.onFailure {
                activeRecorder = null
            }
        }
    }

    fun cancelVoiceRecording() {
        SoundPoolManager.playPttCancel()
        runCatching { activeRecorder?.stop() }
        runCatching { activeRecorder?.release() }
        activeRecorder = null
        activeRecordingFile?.delete()
        activeRecordingFile = null
        _isRecordingVoice.value = false
    }

    private fun stopAndSendVoiceRecording(chatJid: String) {
        SoundPoolManager.playPttEnd()
        val rec = activeRecorder
        activeRecorder = null
        if (rec != null) {
            runCatching { rec.stop() }
            runCatching { rec.release() }
        }
        val elapsedSec = (((System.currentTimeMillis() - recordingStartMs) / 1000L).toInt()).coerceIn(1, 300)
        val file = activeRecordingFile
        activeRecordingFile = null
        _isRecordingVoice.value = false

        viewModelScope.launch {
            val audioFile = if (file != null && file.exists() && file.length() > 1024L) {
                file
            } else {
                // Clean synthesized voice note fallback when mic is muted or permission declined
                File(EagerMediaVault.mediaDir(appContext), "ptt_voice_${System.currentTimeMillis()}.wav").also { wav ->
                    EagerMediaVault.generateSpeechCadenceWav(wav, durationSec = elapsedSec.coerceAtLeast(3))
                }
            }
            val mime = if (audioFile.extension == "wav") "audio/wav" else "audio/mp4"
            WaRealtimeService.sendMediaMessage(
                context = appContext,
                chatJid = chatJid,
                sourceFile = audioFile,
                mimeType = mime,
                caption = "",
                qualityMode = OutboundQualityMode.ORIGINAL,
                isVoiceNote = true,
                durationSec = elapsedSec.coerceAtLeast(2)
            ).onFailure { err ->
                showToast(err.message ?: "Voice send failed")
            }
        }
    }

    fun exportMediaToGallery(localPath: String?, displayName: String, mimeType: String) {
        if (localPath.isNullOrBlank()) {
            showToast("No decrypted file on disk for this item")
            return
        }
        viewModelScope.launch {
            EagerMediaVault.exportToGallery(appContext, File(localPath), displayName, mimeType)
                .onSuccess { showToast("Saved to Gallery ($displayName)") }
                .onFailure { showToast("Export failed: ${it.message}") }
        }
    }

    fun seedShowcaseData() {
        viewModelScope.launch {
            WaRealtimeService.seedBenchmarkAndShowcase(appContext)
                .onSuccess { showToast("Seeded 1,220-msg thread + all Mod showcases") }
                .onFailure { showToast("Seed failed: ${it.message}") }
        }
    }

    fun recordPrimaryHeartbeatNow() {
        StealthMatrixManager.recordPrimaryHeartbeat()
        showToast("Primary phone heartbeat reset (14d window refreshed)")
    }

    fun simulate11DayHeartbeatGap() {
        StealthMatrixManager.simulateElapsedDays(11)
        showToast("Simulated 11 days elapsed -> 3 days until companion unlink warning active")
    }

    fun verifyBulkNumbers(phonesCsv: String) {
        if (phonesCsv.isBlank()) return
        viewModelScope.launch {
            WaRealtimeService.verifyBulkNumbers(phonesCsv)
                .onSuccess { _bulkVerifyOutput.value = it }
                .onFailure { _bulkVerifyOutput.value = "Error: ${it.message}" }
        }
    }

    fun checkForUpdates(silent: Boolean = false) {
        viewModelScope.launch {
            val result = WuppUpdater.checkForUpdates(appContext, silent = silent)
            if (!silent) {
                when (result) {
                    is UpdateUiState.UpToDate -> showToast("You're on the latest Wupp (v${result.currentVersion})")
                    is UpdateUiState.Error -> showToast(result.message)
                    else -> Unit
                }
            }
        }
    }

    fun downloadUpdate(release: AppReleaseInfo) {
        viewModelScope.launch {
            WuppUpdater.downloadUpdate(appContext, release)
            val current = WuppUpdater.state.value
            if (current is UpdateUiState.Error) {
                showToast(current.message)
            }
        }
    }

    fun installDownloadedApk(apkFile: File) {
        WuppUpdater.installApk(appContext, apkFile).onFailure { err ->
            showToast(err.message ?: "Unable to launch APK installer")
        }
    }

    fun openUpdatePrompt() {
        WuppUpdater.openPromptDialog()
    }

    fun dismissUpdatePrompt(skipVersion: Boolean = false) {
        WuppUpdater.dismissPromptDialog(appContext, skipVersion = skipVersion)
    }

    fun setAutoUpdateEnabled(enabled: Boolean) {
        WuppUpdater.setAutoCheckEnabled(appContext, enabled)
    }

    private fun showToast(text: String) {
        viewModelScope.launch(Dispatchers.Main) {
            Toast.makeText(appContext, text, Toast.LENGTH_SHORT).show()
        }
    }
}
