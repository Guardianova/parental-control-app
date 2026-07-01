package com.guardianova.child.core.network

import retrofit2.http.Body
import retrofit2.http.POST

data class PairDeviceRequest(
    val qrPayload: String,
    val platform: String,
    val deviceModel: String?,
    val publicKey: String
)

data class PairDeviceResponse(
    val id: String,
    val childId: String,
    val platform: String,
    val pairedAt: String
)

interface PairingApiService {
    @POST("/devices/pair")
    suspend fun pairDevice(@Body request: PairDeviceRequest): PairDeviceResponse
}
