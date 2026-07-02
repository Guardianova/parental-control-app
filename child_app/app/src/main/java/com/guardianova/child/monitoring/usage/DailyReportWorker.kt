package com.guardianova.child.monitoring.usage

import android.app.usage.UsageStatsManager
import android.content.Context
import android.content.pm.PackageManager
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.guardianova.child.core.network.ApiClient
import com.guardianova.child.core.network.AppUsageEntry
import com.guardianova.child.core.network.UsageStatsApiService
import com.guardianova.child.core.network.UsageStatsRequest
import com.guardianova.child.core.storage.EncryptedStorage
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

class DailyReportWorker(
    context: Context,
    params: WorkerParameters
) : CoroutineWorker(context, params) {

    private val storage = EncryptedStorage(applicationContext)
    private val apiService = ApiClient.build(applicationContext)
        .create(UsageStatsApiService::class.java)
    private val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())

    override suspend fun doWork(): Result {
        val deviceId = storage.getDeviceId() ?: return Result.failure()

        return try {
            // تقرير أمس (يومي كامل)
            sendDailyReport(deviceId)
            Result.success()
        } catch (_: Exception) {
            Result.retry()
        }
    }

    private suspend fun sendDailyReport(deviceId: String) {
        val calendar = Calendar.getInstance()

        // نهاية أمس (منتصف الليل)
        calendar.set(Calendar.HOUR_OF_DAY, 0)
        calendar.set(Calendar.MINUTE, 0)
        calendar.set(Calendar.SECOND, 0)
        calendar.set(Calendar.MILLISECOND, 0)
        val endTime = calendar.timeInMillis

        // بداية أمس
        calendar.add(Calendar.DAY_OF_YEAR, -1)
        val startTime = calendar.timeInMillis
        val reportDate = dateFormat.format(Date(startTime))

        val entries = collectUsageForPeriod(startTime, endTime, reportDate)
        if (entries.isEmpty()) return

        val totalScreenTime = entries.sumOf { it.totalTimeSeconds }

        apiService.sendUsage(
            deviceId = deviceId,
            request = UsageStatsRequest(
                entries = entries.sortedByDescending { it.totalTimeSeconds },
                totalScreenTimeSeconds = totalScreenTime,
                reportDate = reportDate
            )
        )
    }

    private fun collectUsageForPeriod(
        startTime: Long,
        endTime: Long,
        reportDate: String
    ): List<AppUsageEntry> {
        val usageStatsManager = applicationContext.getSystemService(
            Context.USAGE_STATS_SERVICE
        ) as UsageStatsManager

        val statsMap = usageStatsManager.queryAndAggregateUsageStats(startTime, endTime)
        val entries = mutableListOf<AppUsageEntry>()

        for ((packageName, stats) in statsMap) {
            val timeInSeconds = stats.totalTimeInForeground / 1000
            if (timeInSeconds < 5) continue

            entries.add(
                AppUsageEntry(
                    packageName = packageName,
                    appName = getAppName(packageName),
                    totalTimeSeconds = timeInSeconds,
                    openCount = 0,
                    usageDate = reportDate
                )
            )
        }

        return entries
    }

    private fun getAppName(packageName: String): String {
        return try {
            val pm = applicationContext.packageManager
            val info = pm.getApplicationInfo(packageName, 0)
            pm.getApplicationLabel(info).toString()
        } catch (_: PackageManager.NameNotFoundException) {
            packageName
        }
    }
}
