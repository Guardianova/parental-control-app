package com.guardianova.child.core.network

import retrofit2.http.Body
import retrofit2.http.POST
import retrofit2.http.Path

data class DeviceStatusRequest(
    val batteryLevel: Int,
    val isCharging: Boolean,
    val networkType: String,
    val deviceModel: String,
    val osVersion: Int,
    val appVersion: String
)

interface DeviceStatusApiService {
    @POST("/devices/{id}/status")
    suspend fun sendStatus(
        @Path("id") deviceId: String,
        @Body status: DeviceStatusRequest
    )
}
