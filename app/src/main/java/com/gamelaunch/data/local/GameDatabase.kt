package com.gamelaunch.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import com.gamelaunch.data.local.dao.GameDao
import com.gamelaunch.data.local.entity.GameEntity
import com.gamelaunch.data.local.entity.ReleaseEntity

@Database(
    entities = [GameEntity::class, ReleaseEntity::class],
    version = 1,
    exportSchema = true
)
abstract class GameDatabase : RoomDatabase() {
    abstract fun gameDao(): GameDao
}
