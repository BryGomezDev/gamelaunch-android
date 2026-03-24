package com.gamelaunch.data.remote

import com.gamelaunch.data.remote.dto.GameDto
import com.gamelaunch.data.remote.dto.ReleaseDateDto
import okhttp3.RequestBody
import retrofit2.http.Body
import retrofit2.http.POST

interface IgdbApi {

    @POST("release_dates")
    suspend fun getReleaseDates(@Body body: RequestBody): List<ReleaseDateDto>

    @POST("games")
    suspend fun searchGames(@Body body: RequestBody): List<GameDto>

    @POST("games")
    suspend fun getGameById(@Body body: RequestBody): List<GameDto>
}
