package com.wa.beastclient

import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.lifecycle.viewmodel.compose.viewModel
import com.wa.beastclient.core.service.WaRealtimeService
import com.wa.beastclient.core.update.WuppUpdater
import com.wa.beastclient.ui.MainViewModel
import com.wa.beastclient.ui.screens.ChatScreen
import com.wa.beastclient.ui.screens.FirstInstallSetupScreen
import com.wa.beastclient.ui.screens.HomeScreen
import com.wa.beastclient.ui.theme.WhatsAppTheme

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        val setupPrefs = getSharedPreferences("wupp_setup_prefs", Context.MODE_PRIVATE)
        val initialSetupDone = setupPrefs.getBoolean("setup_completed_v1", false)
        if (initialSetupDone) {
            WaRealtimeService.ensureStarted(this)
        }

        setContent {
            val vm: MainViewModel = viewModel()
            val stealth by vm.stealthConfig.collectAsState()
            var setupDone by remember { mutableStateOf(initialSetupDone) }
            var selectedChatJid by rememberSaveable { mutableStateOf<String?>(null) }

            LaunchedEffect(intent?.getBooleanExtra("open_update_prompt", false)) {
                if (intent?.getBooleanExtra("open_update_prompt", false) == true) {
                    intent?.removeExtra("open_update_prompt")
                    vm.openUpdatePrompt()
                }
            }

            BackHandler(enabled = setupDone && selectedChatJid != null) {
                selectedChatJid = null
            }

            WhatsAppTheme(
                darkTheme = stealth.darkTheme,
                assetManager = assets
            ) {
                if (!setupDone) {
                    FirstInstallSetupScreen(
                        onCompleteSetup = {
                            setupPrefs.edit().putBoolean("setup_completed_v1", true).apply()
                            setupDone = true
                            WaRealtimeService.ensureStarted(this@MainActivity)
                        }
                    )
                } else {
                    val activeJid = selectedChatJid
                    if (activeJid == null) {
                        HomeScreen(
                            viewModel = vm,
                            onOpenChat = { jid -> selectedChatJid = jid }
                        )
                    } else {
                        ChatScreen(
                            chatJid = activeJid,
                            viewModel = vm,
                            onBack = { selectedChatJid = null }
                        )
                    }
                }
            }
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        if (intent.getBooleanExtra("open_update_prompt", false)) {
            WuppUpdater.openPromptDialog()
        }
    }

    override fun onStart() {
        super.onStart()
        val setupDone = getSharedPreferences("wupp_setup_prefs", Context.MODE_PRIVATE)
            .getBoolean("setup_completed_v1", false)
        if (setupDone) {
            WaRealtimeService.ensureStarted(this)
        }
    }
}
