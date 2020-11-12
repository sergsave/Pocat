package com.sergsave.purryourcat.persistent.database

import androidx.room.*
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

    // Warning! Limits for query is 999 items
    // https://raw.githubusercontent.com/android/platform_external_sqlite/master/dist/sqlite3.c
    @Query("DELETE FROM cats WHERE id in (:catIds)")
    fun deleteMultipleById(catIds: List<String>): Completable
}