package com.gamelaunch.data.remote

import com.gamelaunch.data.remote.dto.IgdbTokenDto
import retrofit2.http.POST
import retrofit2.http.Query

interface IgdbAuthApi {
    @POST("oauth2/token")
    suspend fun getToken(
        @Query("client_id") clientId: String,
        @Query("client_secret") clientSecret: String,
        @Query("grant_type") grantType: String = "client_credentials"
    ): IgdbTokenDto
}
