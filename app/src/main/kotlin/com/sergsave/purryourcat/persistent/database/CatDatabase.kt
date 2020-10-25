package com.sergsave.purryourcat.persistent.database

import androidx.room.Database
import androidx.room.RoomDatabase

@Database(entities = arrayOf(Cat::class), version = 1)
abstract class CatDatabase : RoomDatabase() {
    abstract fun catDao(): CatDao
}