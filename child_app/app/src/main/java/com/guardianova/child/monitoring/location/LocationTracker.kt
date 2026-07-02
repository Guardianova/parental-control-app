package com.guardianova.child.monitoring.location

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.location.Location
import android.os.BatteryManager
import android.os.CancellationSignal
import androidx.core.content.ContextCompat
import com.google.android.gms.location.CurrentLocationRequest
import com.google.android.gms.location.FusedLocationProviderClient
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
import kotlinx.coroutines.withTimeoutOrNull

class LocationTracker(private val context: Context) {

    private val storage = EncryptedStorage(context)
    private val apiService = ApiClient.build(context).create(LocationApiService::class.java)
    private val fusedClient: FusedLocationProviderClient =
        LocationServices.getFusedLocationProviderClient(context)

    companion object {
        const val NORMAL_INTERVAL_MS    = 15 * 60 * 1000L  // 15 دقيقة
        const val FAST_INTERVAL_MS      = 30 * 1000L        // 30 ثانية
        const val FAST_MODE_DURATION    = 10 * 60 * 1000L   // 10 دقائق
        const val LOCATION_TIMEOUT_MS   = 10_000L           // 10 ثوانٍ كحد أقصى للانتظار
        const val LOCATION_MAX_AGE_MS   = 5 * 60 * 1000L   // اعتبر الموقع قديماً إذا تجاوز 5 دقائق
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

    // ─── الوضع العادي — ج.1 (بدون تعديل) ────────────────────
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

    // ─── الوضع السريع — ج.3 (placeholder) ───────────────────
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

    // ─── وضع SOS (بدون تعديل) ────────────────────────────────
    suspend fun sendSosLocation() {
        if (!hasLocationPermission()) return
        try { sendCurrentLocation() } catch (_: Exception) { }
    }

    // ─── الإرسال الفعلي — مُحدَّث في ج.2 ────────────────────
    // الترتيب:
    // 1. حاول lastLocation (سريع، بدون GPS)
    // 2. إذا null أو قديم → اطلب موقعاً جديداً عبر getCurrentLocationFallback()
    // 3. إذا فشل كلاهما → تجاهل الدورة
    private suspend fun sendCurrentLocation() {
        val deviceId = storage.getDeviceId() ?: return

        @Suppress("MissingPermission")
        val lastLocation = fusedClient.lastLocation.await()

        val location: Location = when {
            // ج.1: موقع حديث وصالح → استخدمه مباشرة
            lastLocation != null &&
            (System.currentTimeMillis() - lastLocation.time) < LOCATION_MAX_AGE_MS -> {
                lastLocation
            }
            // ج.2: null أو قديم → اطلب موقعاً جديداً
            else -> {
                getCurrentLocationFallback() ?: return
            }
        } 

        apiService.sendLocation(
            deviceId = deviceId,
            request = LocationPayload(
                latitude      = location.latitude,
                longitude     = location.longitude,
                accuracyMeters = location.accuracy,
                batteryLevel  = getBatteryLevel(),
                recordedAt    = System.currentTimeMillis()
            )
        )
    }

    // ─── Fallback — ج.2 (جديد) ───────────────────────────────
    // يطلب موقعاً واحداً جديداً ويتوقف — مع timeout 10 ثوانٍ
    @Suppress("MissingPermission")
    private suspend fun getCurrentLocationFallback(): Location? {
        return withTimeoutOrNull(LOCATION_TIMEOUT_MS) {
            val request = CurrentLocationRequest.Builder()
                .setPriority(Priority.PRIORITY_BALANCED_POWER_ACCURACY)
                .setMaxUpdateAgeMillis(LOCATION_MAX_AGE_MS)
                .build()

            fusedClient.getCurrentLocation(request, null).await()
        }
    }

    // ─── بيانات مساعدة ────────────────────────────────────────
    private fun getBatteryLevel(): Int {
        val bm = context.getSystemService(Context.BATTERY_SERVICE) as BatteryManager
        return bm.getIntProperty(BatteryManager.BATTERY_PROPERTY_CAPACITY)
    }
}
