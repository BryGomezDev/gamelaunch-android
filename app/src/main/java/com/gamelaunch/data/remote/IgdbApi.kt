package com.gamelaunch.data.remote

import com.gamelaunch.data.remote.dto.GameDto
import com.gamelaunch.data.remote.dto.ReleaseDateDto
import retrofit2.http.Body
import retrofit2.http.POST

interface IgdbApi {

    @POST("release_dates")
    suspend fun getReleaseDates(@Body body: String): List<ReleaseDateDto>

    @POST("games")
    suspend fun searchGames(@Body body: String): List<GameDto>

    @POST("games")
    suspend fun getGameById(@Body body: String): List<GameDto>
}
