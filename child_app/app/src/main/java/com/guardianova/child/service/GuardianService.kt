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
import com.guardianova.child.monitoring.location.LocationTracker
import com.guardianova.child.monitoring.usage.UsageStatsCollector
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

class GuardianService : Service() {

    private lateinit var storage: EncryptedStorage
    private lateinit var apiService: DeviceStatusApiService
    private lateinit var usageCollector: UsageStatsCollector
    private lateinit var locationTracker: LocationTracker
    private val serviceScope = CoroutineScope(Dispatchers.IO + Job())

    companion object {
        const val CHANNEL_ID         = "guardian_channel"
        const val NOTIFICATION_ID    = 1
        const val STATUS_INTERVAL_MS = 15 * 60 * 1000L
        const val USAGE_INTERVAL_MS  = 15 * 60 * 1000L

        fun start(context: Context) {
            context.startForegroundService(Intent(context, GuardianService::class.java))
        }

        fun stop(context: Context) {
            context.stopService(Intent(context, GuardianService::class.java))
        }
    }

    override fun onCreate() {
        super.onCreate()
        storage         = EncryptedStorage(this)
        apiService      = ApiClient.build(this).create(DeviceStatusApiService::class.java)
        usageCollector  = UsageStatsCollector(this)
        locationTracker = LocationTracker(this)
        createNotificationChannel()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        startForeground(NOTIFICATION_ID, buildNotification())
        startStatusReporting()
        startUsageReporting()
        startLocationTracking()
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
        getSystemService(NotificationManager::class.java)
            .createNotificationChannel(channel)
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

    // ─── حالة الجهاز ──────────────────────────────────────────
    private fun startStatusReporting() {
        serviceScope.launch {
            while (isActive) {
                try {
                    storage.getDeviceId()?.let { deviceId ->
                        apiService.sendStatus(
                            deviceId, DeviceStatusRequest(
                                batteryLevel = getBatteryLevel(),
                                isCharging   = isCharging(),
                                networkType  = getNetworkType(),
                                deviceModel  = getDeviceModel(),
                                osVersion    = Build.VERSION.SDK_INT,
                                appVersion   = BuildConfig.VERSION_NAME
                            )
                        )
                    }
                } catch (_: Exception) { }
                delay(STATUS_INTERVAL_MS)
            }
        }
    }

    // ─── إحصائيات الاستخدام ───────────────────────────────────
    private fun startUsageReporting() {
        serviceScope.launch {
            while (isActive) {
                try { usageCollector.collectAndSend() } catch (_: Exception) { }
                delay(USAGE_INTERVAL_MS)
            }
        }
    }

    // ─── الموقع ───────────────────────────────────────────────
    private fun startLocationTracking() {
        if (!locationTracker.hasLocationPermission()) return
        serviceScope.launch {
            locationTracker.startNormalTracking(serviceScope)
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
        val caps = cm.getNetworkCapabilities(cm.activeNetwork ?: return "none") ?: return "none"
        return when {
            caps.hasTransport(NetworkCapabilities.TRANSPORT_WIFI)     -> "wifi"
            caps.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) -> "cellular"
            else -> "other"
        }
    }

    private fun getDeviceModel() = "${Build.MANUFACTURER} ${Build.MODEL}"
}
