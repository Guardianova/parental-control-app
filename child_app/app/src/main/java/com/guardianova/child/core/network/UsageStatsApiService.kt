package com.guardianova.child.core.network

import retrofit2.http.Body
import retrofit2.http.POST
import retrofit2.http.Path

data class AppUsageEntry(
    val packageName: String,
    val appName: String,
    val totalTimeSeconds: Long,
    val openCount: Int,
    val usageDate: String
)

data class UsageStatsRequest(
    val entries: List<AppUsageEntry>,
    val totalScreenTimeSeconds: Long,
    val reportDate: String
)

interface UsageStatsApiService {
    @POST("/devices/{id}/usage")
    suspend fun sendUsage(
        @Path("id") deviceId: String,
        @Body request: UsageStatsRequest
    )
}
