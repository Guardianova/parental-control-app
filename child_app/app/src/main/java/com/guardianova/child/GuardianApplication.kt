package com.guardianova.child

import android.app.Application
import com.guardianova.child.core.storage.EncryptedStorage
import com.guardianova.child.monitoring.usage.UsageReportScheduler

class GuardianApplication : Application() {

    override fun onCreate() {
        super.onCreate()

        val storage = EncryptedStorage(this)
        if (storage.isPairingComplete()) {
            UsageReportScheduler.schedule(this)
        }
    }
}
