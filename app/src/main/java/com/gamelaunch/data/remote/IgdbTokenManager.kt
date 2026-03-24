package com.gamelaunch.data.remote

import android.util.Log
import com.gamelaunch.BuildConfig
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class IgdbTokenManager @Inject constructor(
    private val authApi: IgdbAuthApi
) {
    private var accessToken: String? = null
    private var expiresAt: Long = 0L
    private val mutex = Mutex()

    suspend fun getValidToken(): String = mutex.withLock {
        val now = System.currentTimeMillis() / 1000
        if (accessToken == null || now >= expiresAt - 60) {
            Log.d(TAG, "Token missing or expiring — requesting new one. clientId=${BuildConfig.IGDB_CLIENT_ID.take(8)}…")
            val response = authApi.getToken(
                clientId = BuildConfig.IGDB_CLIENT_ID,
                clientSecret = BuildConfig.IGDB_CLIENT_SECRET
            )
            accessToken = response.accessToken
            expiresAt = now + response.expiresIn
            Log.d(TAG, "Token obtained. type=${response.tokenType} expiresIn=${response.expiresIn}s token=${response.accessToken.take(8)}…")
        } else {
            Log.d(TAG, "Using cached token (valid for ${expiresAt - now}s). token=${accessToken!!.take(8)}…")
        }
        accessToken!!
    }

    companion object {
        private const val TAG = "IgdbTokenManager"
    }
}
