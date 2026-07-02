package com.guardianova.child.monitoring.location

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.os.BatteryManager
import android.os.Looper
import androidx.core.content.ContextCompat
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import com.guardianova.child.core.network.ApiClient
import com.guardianova.child.core.network.LocationApiService
import com.guardianova.child.core.network.LocationRequest as LocationPayload
import com.guardianova.child.core.storage.EncryptedStorage
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

class LocationTracker(private val context: Context) {

    private val storage = EncryptedStorage(context)
    private val apiService = ApiClient.build(context).create(LocationApiService::class.java)
    private val fusedClient: FusedLocationProviderClient =
        LocationServices.getFusedLocationProviderClient(context)

    companion object {
        const val NORMAL_INTERVAL_MS = 15 * 60 * 1000L   // 15 دقيقة
        const val FAST_INTERVAL_MS   = 30 * 1000L         // 30 ثانية
        const val FAST_MODE_DURATION = 10 * 60 * 1000L    // 10 دقائق
    }

    // ─── التحقق من الصلاحيات ─────────────────────────────────
    fun hasLocationPermission(): Boolean {
        return ContextCompat.checkSelfPermission(
            context, Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED
    }

    fun hasBackgroundPermission(): Boolean {
        return ContextCompat.checkSelfPermission(
            context, Manifest.permission.ACCESS_BACKGROUND_LOCATION
        ) == PackageManager.PERMISSION_GRANTED
    }

    // ─── الوضع العادي (كل 15 دقيقة) ─────────────────────────
    suspend fun startNormalTracking(scope: CoroutineScope) {
        if (!hasLocationPermission()) return

        scope.launch(Dispatchers.IO) {
            while (isActive) {
                try {
                    sendCurrentLocation()
                } catch (_: Exception) { }
                delay(NORMAL_INTERVAL_MS)
            }
        }
    }

    // ─── الوضع السريع (كل 30 ثانية لمدة 10 دقائق) ──────────
    suspend fun startFastTracking(scope: CoroutineScope) {
        if (!hasLocationPermission()) return

        scope.launch(Dispatchers.IO) {
            val endTime = System.currentTimeMillis() + FAST_MODE_DURATION
            while (isActive && System.currentTimeMillis() < endTime) {
                try {
                    sendCurrentLocation()
                } catch (_: Exception) { }
                delay(FAST_INTERVAL_MS)
            }
        }
    }

    // ─── وضع SOS (فوري مرة واحدة) ────────────────────────────
    suspend fun sendSosLocation() {
        if (!hasLocationPermission()) return
        try {
            sendCurrentLocation()
        } catch (_: Exception) { }
    }

    // ─── الإرسال الفعلي ──────────────────────────────────────
    private suspend fun sendCurrentLocation() {
        val deviceId = storage.getDeviceId() ?: return

        @Suppress("MissingPermission")
        val location = fusedClient.lastLocation.await() ?: return

        val battery = getBatteryLevel()

        apiService.sendLocation(
            deviceId = deviceId,
            request = LocationPayload(
                latitude = location.latitude,
                longitude = location.longitude,
                accuracyMeters = location.accuracy,
                batteryLevel = battery,
                recordedAt = System.currentTimeMillis()
            )
        )
    }

    private fun getBatteryLevel(): Int {
        val bm = context.getSystemService(Context.BATTERY_SERVICE) as BatteryManager
        return bm.getIntProperty(BatteryManager.BATTERY_PROPERTY_CAPACITY)
    }
}
