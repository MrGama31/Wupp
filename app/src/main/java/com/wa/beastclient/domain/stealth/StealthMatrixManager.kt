package com.wa.beastclient.domain.stealth

import android.content.Context
import android.content.SharedPreferences
import com.wa.beastclient.data.db.PrivacyRuleEntity
import com.wa.beastclient.data.db.WaDao
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import java.util.concurrent.TimeUnit
import kotlin.math.max

data class GlobalStealthConfig(
    val ghostPresence: Boolean = true,
    val hideReadReceipts: Boolean = false,
    val hidePlayedReceipts: Boolean = true,
    val hideTypingRecording: Boolean = true,
    val antiDeleteEnabled: Boolean = true,
    val viewOnceVaultEnabled: Boolean = true,
    val disappearingImmunity: Boolean = true,
    val darkTheme: Boolean = true,
    val primaryLastSeenEpochMs: Long = System.currentTimeMillis()
)

data class EffectiveChatPrivacy(
    val sendReadReceipts: Boolean,
    val sendPlayedReceipts: Boolean,
    val sendChatPresence: Boolean,
    val hasCustomOverride: Boolean
)

data class PrimaryHeartbeatStatus(
    val lastPrimarySeenMs: Long,
    val elapsedDays: Int,
    val remainingDays: Int,
    val remainingHours: Long,
    val isWarningActive: Boolean,
    val isExpiredRisk: Boolean
)

/**
 * Manages Global & per-chat Ghost/Stealth rules, Error 463 unsaved-number outbound guard,
 * and Meta's 14-day primary phone companion heartbeat reminder.
 */
object StealthMatrixManager {
    private const val PREFS_NAME = "wa_stealth_prefs"
    private const val KEY_GHOST_PRESENCE = "ghost_presence"
    private const val KEY_HIDE_READ = "hide_read_receipts"
    private const val KEY_HIDE_PLAYED = "hide_played_receipts"
    private const val KEY_HIDE_TYPING = "hide_typing_recording"
    private const val KEY_ANTI_DELETE = "anti_delete_enabled"
    private const val KEY_VIEW_ONCE_VAULT = "view_once_vault_enabled"
    private const val KEY_DISAPPEARING_IMMUNITY = "disappearing_immunity"
    private const val KEY_DARK_THEME = "dark_theme"
    private const val KEY_PRIMARY_HEARTBEAT_MS = "primary_last_seen_ms"

    private const val FOURTEEN_DAYS_MS = 14L * 24L * 60L * 60L * 1000L
    private const val WARNING_THRESHOLD_DAYS = 10

    private lateinit var prefs: SharedPreferences
    private val _config = MutableStateFlow(GlobalStealthConfig())
    val config: StateFlow<GlobalStealthConfig> = _config.asStateFlow()

    @Volatile
    private var initialized = false

    fun init(context: Context) {
        if (initialized) return
        synchronized(this) {
            if (initialized) return
            prefs = context.applicationContext.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            val now = System.currentTimeMillis()
            if (!prefs.contains(KEY_PRIMARY_HEARTBEAT_MS)) {
                prefs.edit().putLong(KEY_PRIMARY_HEARTBEAT_MS, now).apply()
            }
            _config.value = GlobalStealthConfig(
                ghostPresence = prefs.getBoolean(KEY_GHOST_PRESENCE, true),
                hideReadReceipts = prefs.getBoolean(KEY_HIDE_READ, false),
                hidePlayedReceipts = prefs.getBoolean(KEY_HIDE_PLAYED, true),
                hideTypingRecording = prefs.getBoolean(KEY_HIDE_TYPING, true),
                antiDeleteEnabled = prefs.getBoolean(KEY_ANTI_DELETE, true),
                viewOnceVaultEnabled = prefs.getBoolean(KEY_VIEW_ONCE_VAULT, true),
                disappearingImmunity = prefs.getBoolean(KEY_DISAPPEARING_IMMUNITY, true),
                darkTheme = prefs.getBoolean(KEY_DARK_THEME, true),
                primaryLastSeenEpochMs = prefs.getLong(KEY_PRIMARY_HEARTBEAT_MS, now)
            )
            initialized = true
        }
    }

    fun updateConfig(transform: (GlobalStealthConfig) -> GlobalStealthConfig) {
        val updated = transform(_config.value)
        _config.value = updated
        if (::prefs.isInitialized) {
            prefs.edit()
                .putBoolean(KEY_GHOST_PRESENCE, updated.ghostPresence)
                .putBoolean(KEY_HIDE_READ, updated.hideReadReceipts)
                .putBoolean(KEY_HIDE_PLAYED, updated.hidePlayedReceipts)
                .putBoolean(KEY_HIDE_TYPING, updated.hideTypingRecording)
                .putBoolean(KEY_ANTI_DELETE, updated.antiDeleteEnabled)
                .putBoolean(KEY_VIEW_ONCE_VAULT, updated.viewOnceVaultEnabled)
                .putBoolean(KEY_DISAPPEARING_IMMUNITY, updated.disappearingImmunity)
                .putBoolean(KEY_DARK_THEME, updated.darkTheme)
                .putLong(KEY_PRIMARY_HEARTBEAT_MS, updated.primaryLastSeenEpochMs)
                .apply()
        }
    }

    fun recordPrimaryHeartbeat(timestampMs: Long = System.currentTimeMillis()) {
        updateConfig { it.copy(primaryLastSeenEpochMs = timestampMs) }
    }

    fun simulateElapsedDays(days: Int) {
        val offsetMs = TimeUnit.DAYS.toMillis(days.toLong())
        updateConfig { it.copy(primaryLastSeenEpochMs = System.currentTimeMillis() - offsetMs) }
    }

    fun computeHeartbeatStatus(cfg: GlobalStealthConfig = _config.value): PrimaryHeartbeatStatus {
        val now = System.currentTimeMillis()
        val elapsedMs = max(0L, now - cfg.primaryLastSeenEpochMs)
        val elapsedDays = TimeUnit.MILLISECONDS.toDays(elapsedMs).toInt()
        val remainingMs = max(0L, FOURTEEN_DAYS_MS - elapsedMs)
        val remainingDays = TimeUnit.MILLISECONDS.toDays(remainingMs).toInt()
        val remainingHours = TimeUnit.MILLISECONDS.toHours(remainingMs)
        return PrimaryHeartbeatStatus(
            lastPrimarySeenMs = cfg.primaryLastSeenEpochMs,
            elapsedDays = elapsedDays,
            remainingDays = remainingDays,
            remainingHours = remainingHours,
            isWarningActive = elapsedDays >= WARNING_THRESHOLD_DAYS,
            isExpiredRisk = elapsedMs >= FOURTEEN_DAYS_MS
        )
    }

    fun observeEffectivePrivacy(dao: WaDao, chatJid: String): Flow<EffectiveChatPrivacy> {
        return combine(config, dao.observePrivacyRule(chatJid)) { cfg, rule ->
            resolvePrivacy(cfg, rule)
        }
    }

    suspend fun getEffectivePrivacy(dao: WaDao, chatJid: String): EffectiveChatPrivacy {
        val rule = dao.getPrivacyRuleOnce(chatJid)
        return resolvePrivacy(_config.value, rule)
    }

    suspend fun setChatPrivacyOverride(
        dao: WaDao,
        chatJid: String,
        sendReadReceipts: Boolean,
        sendPlayedReceipts: Boolean,
        sendChatPresence: Boolean
    ) {
        dao.upsertPrivacyRule(
            PrivacyRuleEntity(
                jid = chatJid,
                sendReadReceipts = sendReadReceipts,
                sendPlayedReceipts = sendPlayedReceipts,
                sendChatPresence = sendChatPresence
            )
        )
    }

    suspend fun clearChatPrivacyOverride(dao: WaDao, chatJid: String) {
        dao.deletePrivacyRule(chatJid)
    }

    private fun resolvePrivacy(
        cfg: GlobalStealthConfig,
        rule: PrivacyRuleEntity?
    ): EffectiveChatPrivacy {
        if (rule != null) {
            return EffectiveChatPrivacy(
                sendReadReceipts = rule.sendReadReceipts,
                sendPlayedReceipts = rule.sendPlayedReceipts,
                sendChatPresence = rule.sendChatPresence,
                hasCustomOverride = true
            )
        }
        return EffectiveChatPrivacy(
            sendReadReceipts = !cfg.hideReadReceipts,
            sendPlayedReceipts = !cfg.hidePlayedReceipts,
            sendChatPresence = !cfg.hideTypingRecording,
            hasCustomOverride = false
        )
    }

    /**
     * Error 463 Ban Guard: Blocks sending an initial companion message to an unsaved/unknown
     * 1-on-1 contact that has 0 prior messages in local Room history.
     */
    suspend fun canInitiateOutboundMessage(dao: WaDao, chatJid: String): Boolean {
        if (chatJid.endsWith("@g.us") || chatJid == "status@broadcast" || chatJid.endsWith("@newsletter")) {
            return true
        }
        return dao.countMessagesInChat(chatJid) > 0
    }
}
