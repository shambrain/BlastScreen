package com.blastscreen.app.data

import com.blastscreen.app.BuildConfig
import com.squareup.moshi.Moshi
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory

class BackendRepository(
    private val api: BlastBackendApi = createApi()
) {
    suspend fun checkHealth(): HealthResponse = api.health()

    suspend fun createMatch(userId: String): MatchResponse =
        api.match(MatchRequest(userId = userId))

    companion object {
        private fun createApi(): BlastBackendApi {
            val moshi = Moshi.Builder().build()
            val retrofit = Retrofit.Builder()
                .baseUrl(BuildConfig.BACKEND_URL)
                .addConverterFactory(MoshiConverterFactory.create(moshi))
                .build()
            return retrofit.create(BlastBackendApi::class.java)
        }
    }
}
