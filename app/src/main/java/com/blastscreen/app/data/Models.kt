package com.blastscreen.app.data

import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class MatchRequest(
    val userId: String,
    val region: String = "global"
)

@JsonClass(generateAdapter = true)
data class MatchResponse(
    val roomId: String,
    val roomUrl: String,
    val partnerId: String,
    val ttlSeconds: Int
)

@JsonClass(generateAdapter = true)
data class HealthResponse(
    val status: String,
    val version: String
)
