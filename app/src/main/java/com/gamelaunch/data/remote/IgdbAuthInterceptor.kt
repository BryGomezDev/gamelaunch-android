package com.gamelaunch.data.remote

import com.gamelaunch.BuildConfig
import kotlinx.coroutines.runBlocking
import okhttp3.Interceptor
import okhttp3.Response
import javax.inject.Inject

class IgdbAuthInterceptor @Inject constructor(
    private val tokenManager: IgdbTokenManager
) : Interceptor {
    override fun intercept(chain: Interceptor.Chain): Response {
        val token = runBlocking { tokenManager.getValidToken() }
        val request = chain.request().newBuilder()
            .addHeader("Client-ID", BuildConfig.IGDB_CLIENT_ID)
            .addHeader("Authorization", "Bearer $token")
            .addHeader("Accept", "application/json")
            .build()
        return chain.proceed(request)
    }
}
