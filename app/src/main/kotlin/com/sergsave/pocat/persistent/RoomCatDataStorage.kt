package com.sergsave.pocat.persistent

import android.content.Context
import android.net.Uri
import androidx.room.Room
import com.sergsave.pocat.Constants
import com.sergsave.pocat.persistent.database.BaseCatEntity
import com.sergsave.pocat.persistent.database.Cat
import com.sergsave.pocat.persistent.database.CatDatabase
import com.sergsave.pocat.persistent.database.CatWithoutTime
import com.sergsave.pocat.models.CatData
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.Completable
import io.reactivex.Flowable
import io.reactivex.Observable
import io.reactivex.schedulers.Schedulers
import java.util.*

class RoomCatDataStorage(context: Context): CatDataStorage {

    private val database = Room.databaseBuilder(
        context.applicationContext,
        CatDatabase::class.java, Constants.PERSISTENT_DATABASE_NAME
    )
        .build()

    override fun read(): Flowable<Map<String, TimedCatData>> {
        return database.catDao().getAll().map { cats ->
            cats.associate { cat ->
                val catData = CatData(
                    name = cat.entity.name,
                    photoUri = cat.entity.photoUri?.let { Uri.parse(it) },
                    purrAudioUri = cat.entity.audioUri?.let { Uri.parse(it) }
                )

                Pair(cat.id, TimedCatData(Date(cat.createdTime), catData))
            }
        }
            .observeOn(AndroidSchedulers.mainThread())
    }

    override fun add(cat: Pair<String, TimedCatData>): Completable {
        val catEntity = Cat(
            id = cat.first,
            createdTime = cat.second.timestamp.time,
            entity = baseEntityFrom(cat.second.data)
        )
        return database.catDao().insert(catEntity)
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
    }

    override fun update(cat: Pair<String, CatData>): Completable {
        val catEntity = CatWithoutTime(
            id = cat.first,
            entity = baseEntityFrom(cat.second)
        )
        return database.catDao().update(catEntity)
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
    }

    override fun remove(id: String): Completable {
        return database.catDao().deleteById(id)
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
    }

    override fun remove(ids: List<String>): Completable {
        val sqliteSizeLimit = 999
        return Observable.fromIterable(ids.chunked(sqliteSizeLimit))
            .concatMapCompletable { database.catDao().deleteMultipleById(it) }
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
    }

    private fun baseEntityFrom(catData: CatData) = BaseCatEntity(
        catData.name,
        catData.photoUri?.toString(),
        catData.purrAudioUri?.toString()
    )
}