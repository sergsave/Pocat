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
    fun getAll(): Flowable<List<TimedCatEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insert(cat: TimedCatEntity): Completable

    @Update(entity = TimedCatEntity::class)
    fun update(cat: CatEntity): Completable

    @Delete
    fun delete(cat: TimedCatEntity): Completable

    @Query("DELETE FROM cats WHERE id = :catId")
    fun deleteById(catId: String): Completable
}