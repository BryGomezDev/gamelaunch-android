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
            Log.d(TAG, "Requesting new IGDB token…")
            val response = authApi.getToken(
                clientId = BuildConfig.IGDB_CLIENT_ID,
                clientSecret = BuildConfig.IGDB_CLIENT_SECRET
            )
            accessToken = response.accessToken
            expiresAt = now + response.expiresIn
            Log.d(TAG, "Token obtained, expires in ${response.expiresIn}s")
        }
        accessToken!!
    }

    companion object {
        private const val TAG = "IgdbTokenManager"
    }
}
