package com.blastscreen.data

import com.blastscreen.BuildConfig
import com.blastscreen.core.network.ApiClient

class BackendRepository {
    private val api = ApiClient.retrofit(BuildConfig.BACKEND_URL).create(BackendApi::class.java)

    suspend fun guestToken() = api.guestAuth().accessToken
    suspend fun usage(token: String) = api.usage("Bearer $token")
    suspend fun join(token: String) = api.joinQueue("Bearer $token", QueueJoinRequest(token))
    suspend fun adminProvision(username: String) = api.provisionAdmin(
        AdminProvisionRequest(phone = "0", username = username, password = "deveg")
    )
}
