package com.gamelaunch.di

import com.gamelaunch.BuildConfig
import com.gamelaunch.data.remote.IgdbApi
import com.gamelaunch.data.remote.IgdbAuthApi
import com.gamelaunch.data.remote.IgdbAuthInterceptor
import com.squareup.moshi.Moshi
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import javax.inject.Named
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object NetworkModule {

    @Provides
    @Singleton
    fun provideMoshi(): Moshi = Moshi.Builder().build()

    @Provides
    @Singleton
    fun provideLoggingInterceptor(): HttpLoggingInterceptor =
        HttpLoggingInterceptor().apply {
            level = if (BuildConfig.DEBUG) HttpLoggingInterceptor.Level.BASIC
                    else HttpLoggingInterceptor.Level.NONE
        }

    // Auth client — no IGDB auth headers (used to fetch the token itself)
    @Provides
    @Singleton
    @Named("auth")
    fun provideAuthOkHttpClient(logging: HttpLoggingInterceptor): OkHttpClient =
        OkHttpClient.Builder()
            .addInterceptor(logging)
            .build()

    @Provides
    @Singleton
    fun provideIgdbAuthApi(
        moshi: Moshi,
        @Named("auth") client: OkHttpClient
    ): IgdbAuthApi = Retrofit.Builder()
        .baseUrl("https://id.twitch.tv/")
        .client(client)
        .addConverterFactory(MoshiConverterFactory.create(moshi))
        .build()
        .create(IgdbAuthApi::class.java)

    // IGDB client — attaches Client-ID + Bearer token on every request
    @Provides
    @Singleton
    @Named("igdb")
    fun provideIgdbOkHttpClient(
        logging: HttpLoggingInterceptor,
        authInterceptor: IgdbAuthInterceptor
    ): OkHttpClient = OkHttpClient.Builder()
        .addInterceptor(authInterceptor)
        .addInterceptor(logging)
        .build()

    @Provides
    @Singleton
    fun provideIgdbApi(
        moshi: Moshi,
        @Named("igdb") client: OkHttpClient
    ): IgdbApi = Retrofit.Builder()
        .baseUrl("https://api.igdb.com/v4/")
        .client(client)
        .addConverterFactory(MoshiConverterFactory.create(moshi))
        .build()
        .create(IgdbApi::class.java)
}
