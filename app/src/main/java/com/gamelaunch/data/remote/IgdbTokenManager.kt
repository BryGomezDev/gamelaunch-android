package com.gamelaunch.data.remote

import android.content.SharedPreferences
import android.util.Log
import com.gamelaunch.BuildConfig
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import javax.inject.Inject
import javax.inject.Named
import javax.inject.Singleton

@Singleton
class IgdbTokenManager @Inject constructor(
    private val authApi: IgdbAuthApi,
    @Named("encrypted") private val securePrefs: SharedPreferences
) {
    private val mutex = Mutex()

    suspend fun getValidToken(): String = mutex.withLock {
        val now = System.currentTimeMillis() / 1000
        val cached = securePrefs.getString(KEY_TOKEN, null)
        val expiresAt = securePrefs.getLong(KEY_EXPIRES_AT, 0L)

        if (cached != null && now < expiresAt - 60) {
            return@withLock cached
        }

        if (BuildConfig.DEBUG) Log.d(TAG, "Requesting new IGDB token…")
        val response = authApi.getToken(
            clientId = BuildConfig.IGDB_CLIENT_ID,
            clientSecret = BuildConfig.IGDB_CLIENT_SECRET
        )
        securePrefs.edit()
            .putString(KEY_TOKEN, response.accessToken)
            .putLong(KEY_EXPIRES_AT, now + response.expiresIn)
            .apply()
        if (BuildConfig.DEBUG) Log.d(TAG, "Token obtained, expires in ${response.expiresIn}s")
        response.accessToken
    }

    fun clearToken() {
        securePrefs.edit()
            .remove(KEY_TOKEN)
            .remove(KEY_EXPIRES_AT)
            .apply()
        if (BuildConfig.DEBUG) Log.d(TAG, "Token cache cleared")
    }

    companion object {
        private const val TAG = "IgdbTokenManager"
        private const val KEY_TOKEN = "igdb_access_token"
        private const val KEY_EXPIRES_AT = "igdb_token_expires_at"
    }
}
