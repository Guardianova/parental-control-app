package com.guardianova.child.monitoring.apps

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import com.guardianova.child.core.network.ApiClient
import com.guardianova.child.core.network.AppEventRequest
import com.guardianova.child.core.network.AppsApiService
import com.guardianova.child.core.storage.EncryptedStorage
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class AppInstallReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        val action = intent.action ?: return
        val packageName = intent.data?.schemeSpecificPart ?: return

        val eventType = when (action) {
            Intent.ACTION_PACKAGE_ADDED -> "installed"
            Intent.ACTION_PACKAGE_REMOVED -> "removed"
            else -> return
        }

        val appName = getAppName(context, packageName)
        val storage = EncryptedStorage(context)
        val deviceId = storage.getDeviceId() ?: return

        val apiService = ApiClient.build(context).create(AppsApiService::class.java)

        CoroutineScope(Dispatchers.IO).launch {
            try {
                apiService.sendAppEvent(
                    deviceId = deviceId,
                    request = AppEventRequest(
                        packageName = packageName,
                        appName = appName,
                        eventType = eventType,
                        eventTime = System.currentTimeMillis()
                    )
                )
            } catch (_: Exception) {
                // الحدث غير حرج — لا retry هنا
            }
        }
    }

    private fun getAppName(context: Context, packageName: String): String {
        return try {
            val pm = context.packageManager
            val info = pm.getApplicationInfo(packageName, 0)
            pm.getApplicationLabel(info).toString()
        } catch (_: PackageManager.NameNotFoundException) {
            packageName
        }
    }
}
