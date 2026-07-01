package com.guardianova.child.core.boot

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.guardianova.child.core.storage.EncryptedStorage
import com.guardianova.child.service.GuardianService

class BootReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        val action = intent.action ?: return

        if (action == Intent.ACTION_BOOT_COMPLETED ||
            action == "android.intent.action.LOCKED_BOOT_COMPLETED"
        ) {
            val storage = EncryptedStorage(context)
            if (storage.isPairingComplete()) {
                GuardianService.start(context)
            }
        }
    }
}
