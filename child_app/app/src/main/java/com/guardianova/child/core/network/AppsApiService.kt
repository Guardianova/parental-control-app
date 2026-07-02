package com.guardianova.child.core.network

import retrofit2.http.Body
import retrofit2.http.POST
import retrofit2.http.Path

data class AppEventRequest(
    val packageName: String,
    val appName: String,
    val eventType: String,  // "installed" أو "removed"
    val eventTime: Long
)

interface AppsApiService {
    @POST("/devices/{id}/apps")
    suspend fun sendAppEvent(
        @Path("id") deviceId: String,
        @Body request: AppEventRequest
    )
}
