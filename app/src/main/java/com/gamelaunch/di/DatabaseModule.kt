package com.gamelaunch.di

import android.content.Context
import androidx.room.Room
import com.gamelaunch.data.local.GameDatabase
import com.gamelaunch.data.local.dao.GameDao
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun provideDatabase(@ApplicationContext context: Context): GameDatabase =
        Room.databaseBuilder(context, GameDatabase::class.java, "gamelaunch.db")
            .build()

    @Provides
    fun provideGameDao(db: GameDatabase): GameDao = db.gameDao()
}
