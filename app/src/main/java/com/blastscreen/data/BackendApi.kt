package com.blastscreen.data

import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.POST

interface BackendApi {
    @POST("/auth/guest")
    suspend fun guestAuth(): GuestAuthResponse

    @POST("/queue/join")
    suspend fun joinQueue(@Header("Authorization") bearer: String, @Body req: QueueJoinRequest): QueueJoinResponse

    @POST("/admin/provision")
    suspend fun provisionAdmin(@Body req: AdminProvisionRequest): AdminProvisionResponse

    @GET("/usage/me")
    suspend fun usage(@Header("Authorization") bearer: String): UsageResponse
}
