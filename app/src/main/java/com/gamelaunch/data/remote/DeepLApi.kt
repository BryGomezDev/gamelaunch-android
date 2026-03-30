package com.gamelaunch.data.remote

import com.gamelaunch.data.remote.dto.DeepLRequest
import com.gamelaunch.data.remote.dto.DeepLResponse
import retrofit2.http.Body
import retrofit2.http.POST

interface DeepLApi {
    @POST("v2/translate")
    suspend fun translate(@Body request: DeepLRequest): DeepLResponse
}
