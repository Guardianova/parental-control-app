package com.guardianova.child.monitoring.usage

import android.app.usage.UsageStatsManager
import android.content.Context
import android.content.pm.PackageManager
import com.guardianova.child.core.network.AppUsageEntry
import com.guardianova.child.core.network.ApiClient
import com.guardianova.child.core.network.UsageStatsApiService
import com.guardianova.child.core.network.UsageStatsRequest
import com.guardianova.child.core.storage.EncryptedStorage
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

class UsageStatsCollector(private val context: Context) {

    private val storage = EncryptedStorage(context)
    private val apiService = ApiClient.build(context).create(UsageStatsApiService::class.java)
    private val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())

    // ─── التحقق من الصلاحية ───────────────────────────────────
    fun hasPermission(): Boolean {
        return try {
            val usageStatsManager = context.getSystemService(
                Context.USAGE_STATS_SERVICE
            ) as UsageStatsManager
            val now = System.currentTimeMillis()
            val stats = usageStatsManager.queryUsageStats(
                UsageStatsManager.INTERVAL_DAILY,
                now - 1000 * 60,
                now
            )
            stats != null && stats.isNotEmpty()
        } catch (_: Exception) {
            false
        }
    }

    // ─── جمع وإرسال إحصائيات اليوم ───────────────────────────
    suspend fun collectAndSend() {
        if (!hasPermission()) return

        val deviceId = storage.getDeviceId() ?: return

        val calendar = Calendar.getInstance()
        val endTime = calendar.timeInMillis

        calendar.set(Calendar.HOUR_OF_DAY, 0)
        calendar.set(Calendar.MINUTE, 0)
        calendar.set(Calendar.SECOND, 0)
        calendar.set(Calendar.MILLISECOND, 0)
        val startTime = calendar.timeInMillis

        val usageStatsManager = context.getSystemService(
            Context.USAGE_STATS_SERVICE
        ) as UsageStatsManager

        val statsMap = usageStatsManager.queryAndAggregateUsageStats(startTime, endTime)

        val entries = mutableListOf<AppUsageEntry>()
        var totalScreenTime = 0L

        for ((packageName, stats) in statsMap) {
            val timeInSeconds = stats.totalTimeInForeground / 1000
            if (timeInSeconds < 5) continue // تجاهل التطبيقات التي فُتحت أقل من 5 ثوانٍ

            val appName = getAppName(packageName)
            entries.add(
                AppUsageEntry(
                    packageName = packageName,
                    appName = appName,
                    totalTimeSeconds = timeInSeconds,
                    openCount = 0, // queryAndAggregateUsageStats لا تُرجع عدد مرات الفتح
                    usageDate = dateFormat.format(Date(startTime))
                )
            )
            totalScreenTime += timeInSeconds
        }

        if (entries.isEmpty()) return

        val request = UsageStatsRequest(
            entries = entries.sortedByDescending { it.totalTimeSeconds },
            totalScreenTimeSeconds = totalScreenTime,
            reportDate = dateFormat.format(Date(startTime))
        )

        apiService.sendUsage(deviceId, request)
    }

    // ─── جلب اسم التطبيق ─────────────────────────────────────
    private fun getAppName(packageName: String): String {
        return try {
            val pm = context.packageManager
            val info = pm.getApplicationInfo(packageName, 0)
            pm.getApplicationLabel(info).toString()
        } catch (_: PackageManager.NameNotFoundException) {
            packageName
        }
    }
}
