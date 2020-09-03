package com.sergsave.purryourcat.data.database

import androidx.room.*

@Dao
interface CatDao {
    @Query("SELECT * FROM cat")
    fun getAll(): List<Cat>

    @Insert
    fun insertAll(cats: List<Cat>)

    @Query("DELETE FROM cat")
    fun delete()

    @Transaction
    fun deleteAndInsertAll(cats: List<Cat>) {
        delete()
        insertAll(cats)
    }

    fun insert(cat: Cat)
    fun remove(id: String)
    fun update()
    fun getAll()
    fun get(id: String)
}