package com.guardianova.child.service

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Context
import android.content.Intent
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.os.BatteryManager
import android.os.Build
import android.os.IBinder
import androidx.core.app.NotificationCompat
import com.guardianova.child.BuildConfig
import com.guardianova.child.core.network.ApiClient
import com.guardianova.child.core.network.DeviceStatusApiService
import com.guardianova.child.core.network.DeviceStatusRequest
import com.guardianova.child.core.storage.EncryptedStorage
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

class GuardianService : Service() {

    private lateinit var storage: EncryptedStorage
    private lateinit var apiService: DeviceStatusApiService
    private val serviceScope = CoroutineScope(Dispatchers.IO + Job())

    companion object {
        const val CHANNEL_ID = "guardian_channel"
        const val NOTIFICATION_ID = 1
        const val STATUS_INTERVAL_MS = 15 * 60 * 1000L

        fun start(context: Context) {
            val intent = Intent(context, GuardianService::class.java)
            context.startForegroundService(intent)
        }

        fun stop(context: Context) {
            context.stopService(Intent(context, GuardianService::class.java))
        }
    }

    override fun onCreate() {
        super.onCreate()
        storage = EncryptedStorage(this)
        apiService = ApiClient.build(this).create(DeviceStatusApiService::class.java)
        createNotificationChannel()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        startForeground(NOTIFICATION_ID, buildNotification())
        startStatusReporting()
        return START_STICKY
    }

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onDestroy() {
        super.onDestroy()
        serviceScope.launch { }.cancel()
    }

    // ─── إشعار Foreground إلزامي ──────────────────────────────
    private fun createNotificationChannel() {
        val channel = NotificationChannel(
            CHANNEL_ID,
            "Guardianova",
            NotificationManager.IMPORTANCE_LOW
        ).apply {
            description = "خدمة الحماية الأبوية"
            setShowBadge(false)
        }
        val manager = getSystemService(NotificationManager::class.java)
        manager.createNotificationChannel(channel)
    }

    private fun buildNotification(): Notification {
        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle("Guardianova")
            .setContentText("وضع الرقابة الأبوية مفعّل")
            .setSmallIcon(android.R.drawable.ic_dialog_info)
            .setOngoing(true)
            .setSilent(true)
            .build()
    }

    // ─── إرسال حالة الجهاز دورياً ────────────────────────────
    private fun startStatusReporting() {
        serviceScope.launch {
            while (isActive) {
                try {
                    val deviceId = storage.getDeviceId()
                    if (deviceId != null) {
                        val status = DeviceStatusRequest(
                            batteryLevel = getBatteryLevel(),
                            isCharging = isCharging(),
                            networkType = getNetworkType(),
                            deviceModel = getDeviceModel(),
                            osVersion = Build.VERSION.SDK_INT,
                            appVersion = BuildConfig.VERSION_NAME
                        )
                        apiService.sendStatus(deviceId, status)
                    }
                } catch (_: Exception) {
                    // سيعيد المحاولة في الدورة القادمة
                }
                delay(STATUS_INTERVAL_MS)
            }
        }
    }

    // ─── بيانات الجهاز ────────────────────────────────────────
    private fun getBatteryLevel(): Int {
        val bm = getSystemService(BATTERY_SERVICE) as BatteryManager
        return bm.getIntProperty(BatteryManager.BATTERY_PROPERTY_CAPACITY)
    }

    private fun isCharging(): Boolean {
        val bm = getSystemService(BATTERY_SERVICE) as BatteryManager
        return bm.isCharging
    }

    private fun getNetworkType(): String {
        val cm = getSystemService(CONNECTIVITY_SERVICE) as ConnectivityManager
        val network = cm.activeNetwork ?: return "none"
        val caps = cm.getNetworkCapabilities(network) ?: return "none"
        return when {
            caps.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) -> "wifi"
            caps.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) -> "cellular"
            else -> "other"
        }
    }

    private fun getDeviceModel(): String {
        return "${Build.MANUFACTURER} ${Build.MODEL}"
    }
}
