package com.blastscreen.data

import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class GuestAuthResponse(val accessToken: String)

@JsonClass(generateAdapter = true)
data class QueueJoinRequest(val token: String)

@JsonClass(generateAdapter = true)
data class QueueJoinResponse(val status: String, val sessionId: String? = null, val partnerId: String? = null)

@JsonClass(generateAdapter = true)
data class AdminProvisionRequest(val phone: String, val username: String, val password: String)

@JsonClass(generateAdapter = true)
data class AdminProvisionResponse(val accessToken: String, val scopes: List<String>)

@JsonClass(generateAdapter = true)
data class UsageResponse(val remainingMinutes: Int)
