package com.sergsave.purryourcat.persistent.database

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Delete
import androidx.room.Update
import androidx.room.OnConflictStrategy
import io.reactivex.Completable
import io.reactivex.Flowable

@Dao
interface CatDao {
    @Query("SELECT * FROM cats")
    fun getAll(): Flowable<List<Cat>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insert(cat: Cat): Completable

    @Update(entity = Cat::class)
    fun update(cat: CatWithoutTime): Completable

    @Delete
    fun delete(cat: Cat): Completable

    @Query("DELETE FROM cats WHERE id = :catId")
    fun deleteById(catId: String): Completable
}