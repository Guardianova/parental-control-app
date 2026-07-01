package com.guardianova.child.core.storage

import android.content.Context
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey

class EncryptedStorage(context: Context) {

    private val masterKey = MasterKey.Builder(context)
        .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
        .build()

    private val prefs = EncryptedSharedPreferences.create(
        context,
        "guardian_secure_prefs",
        masterKey,
        EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
        EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
    )

    // ─── Device ───────────────────────────────────────────────
    fun saveDeviceId(deviceId: String) =
        prefs.edit().putString(KEY_DEVICE_ID, deviceId).apply()

    fun getDeviceId(): String? =
        prefs.getString(KEY_DEVICE_ID, null)

    // ─── Tokens ───────────────────────────────────────────────
    fun saveTokens(accessToken: String, refreshToken: String) {
        prefs.edit()
            .putString(KEY_ACCESS_TOKEN, accessToken)
            .putString(KEY_REFRESH_TOKEN, refreshToken)
            .apply()
    }

    fun getAccessToken(): String? =
        prefs.getString(KEY_ACCESS_TOKEN, null)

    fun getRefreshToken(): String? =
        prefs.getString(KEY_REFRESH_TOKEN, null)

    // ─── Pairing State ────────────────────────────────────────
    fun savePairingComplete(complete: Boolean) =
        prefs.edit().putBoolean(KEY_PAIRING_COMPLETE, complete).apply()

    fun isPairingComplete(): Boolean =
        prefs.getBoolean(KEY_PAIRING_COMPLETE, false)

    // ─── Camera Feature ───────────────────────────────────────
    fun saveCameraFeatureEnabled(enabled: Boolean) =
        prefs.edit().putBoolean(KEY_CAMERA_ENABLED, enabled).apply()

    fun isCameraFeatureEnabled(): Boolean =
        prefs.getBoolean(KEY_CAMERA_ENABLED, false)

    // ─── Clear All (عند إلغاء الربط) ─────────────────────────
    fun clearAll() = prefs.edit().clear().apply()

    companion object {
        private const val KEY_DEVICE_ID       = "device_id"
        private const val KEY_ACCESS_TOKEN    = "access_token"
        private const val KEY_REFRESH_TOKEN   = "refresh_token"
        private const val KEY_PAIRING_COMPLETE = "pairing_complete"
        private const val KEY_CAMERA_ENABLED  = "camera_feature_enabled"
    }
}
