package com.wa.beastclient

import android.app.Application
import com.wa.beastclient.core.service.WaRealtimeService
import com.wa.beastclient.data.db.WaUiDatabase
import com.wa.beastclient.data.media.EagerMediaVault
import com.wa.beastclient.data.media.SoundPoolManager
import com.wa.beastclient.domain.stealth.StealthMatrixManager

class WaBeastApp : Application() {
    override fun onCreate() {
        super.onCreate()
        EagerMediaVault.ensureDirectories(this)
        StealthMatrixManager.init(this)
        SoundPoolManager.init(this)
        WaUiDatabase.getInstance(this)
    }
}
