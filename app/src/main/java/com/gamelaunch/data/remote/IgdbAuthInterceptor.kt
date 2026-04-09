package com.gamelaunch.data.remote

import com.gamelaunch.BuildConfig
import kotlinx.coroutines.Dispatchers
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
            runBlocking(Dispatchers.IO) { tokenManager.getValidToken() }
        } catch (e: Exception) {
            throw IOException("Failed to obtain IGDB token: ${e.message}", e)
        }
        val response = chain.proceed(authenticatedRequest(chain.request(), token))
        if (response.code == 401 || response.code == 403) {
            response.close()
            tokenManager.clearToken()
            val freshToken = try {
                runBlocking(Dispatchers.IO) { tokenManager.getValidToken() }
            } catch (e: Exception) {
                throw IOException("Failed to refresh IGDB token: ${e.message}", e)
            }
            return chain.proceed(authenticatedRequest(chain.request(), freshToken))
        }
        return response
    }

    private fun authenticatedRequest(original: okhttp3.Request, token: String) =
        original.newBuilder()
            .addHeader("Client-ID", BuildConfig.IGDB_CLIENT_ID)
            .addHeader("Authorization", "Bearer $token")
            .addHeader("Accept", "application/json")
            .build()
}
