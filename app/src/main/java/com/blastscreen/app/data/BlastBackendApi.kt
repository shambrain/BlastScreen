package com.blastscreen.app.data

import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST

interface BlastBackendApi {
    @GET("health")
    suspend fun health(): HealthResponse

    @POST("match")
    suspend fun match(@Body request: MatchRequest): MatchResponse
}
