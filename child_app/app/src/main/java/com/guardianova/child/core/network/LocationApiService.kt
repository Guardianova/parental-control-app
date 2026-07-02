package com.guardianova.child.core.network

import retrofit2.http.Body
import retrofit2.http.POST
import retrofit2.http.Path

data class LocationRequest(
    val latitude: Double,
    val longitude: Double,
    val accuracyMeters: Float,
    val batteryLevel: Int,
    val recordedAt: Long
)

interface LocationApiService {
    @POST("/devices/{id}/location")
    suspend fun sendLocation(
        @Path("id") deviceId: String,
        @Body request: LocationRequest
    )
}
