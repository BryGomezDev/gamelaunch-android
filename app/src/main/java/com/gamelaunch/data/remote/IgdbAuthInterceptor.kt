package com.gamelaunch.data.remote

import com.gamelaunch.BuildConfig
import kotlinx.coroutines.runBlocking
import okhttp3.Interceptor
import okhttp3.Response
import java.io.IOException
import javax.inject.Inject

class IgdbAuthInterceptor @Inject constructor(
    private val tokenManager: IgdbTokenManager
) : Interceptor {
    override fun intercept(chain: Interceptor.Chain): Response {
        val token = try {
            runBlocking { tokenManager.getValidToken() }
        } catch (e: Exception) {
            throw IOException("Failed to obtain IGDB token: ${e.message}", e)
        }
        val request = chain.request().newBuilder()
            .addHeader("Client-ID", BuildConfig.IGDB_CLIENT_ID)
            .addHeader("Authorization", "Bearer $token")
            .addHeader("Accept", "application/json")
            .build()
        return chain.proceed(request)
    }
}
