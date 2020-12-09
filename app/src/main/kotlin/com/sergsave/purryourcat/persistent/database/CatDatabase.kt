package com.sergsave.pocat.persistent.database

import androidx.room.Database
import androidx.room.RoomDatabase

// TODO: Warning "cannot export the schema"
@Database(entities = arrayOf(Cat::class), version = 1)
abstract class CatDatabase : RoomDatabase() {
    abstract fun catDao(): CatDao
}