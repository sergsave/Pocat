package com.sergsave.purryourcat.data

import android.content.Context
import android.net.Uri
import androidx.room.Room
import com.sergsave.purryourcat.data.database.BaseCatEntity
import com.sergsave.purryourcat.data.database.Cat
import com.sergsave.purryourcat.data.database.CatDatabase
import com.sergsave.purryourcat.data.database.CatWithoutTime
import com.sergsave.purryourcat.models.CatData
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers
import io.reactivex.rxjava3.core.Completable
import io.reactivex.rxjava3.core.Flowable
import io.reactivex.rxjava3.schedulers.Schedulers

class RoomCatDataStorage(context: Context): CatDataStorage {

    private val database = Room.databaseBuilder(
        context.applicationContext,
        CatDatabase::class.java, "cat_database"
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

                Pair(cat.id, TimedCatData(cat.createdTime, catData))
            }
        }
            .observeOn(AndroidSchedulers.mainThread())
    }

    override fun add(cat: Pair<String, TimedCatData>): Completable {
        val catEntity = Cat(
            id = cat.first,
            createdTime = cat.second.timeOfCreateMillis,
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

    private fun baseEntityFrom(catData: CatData) = BaseCatEntity(
        catData.name,
        catData.photoUri?.toString(),
        catData.purrAudioUri?.toString()
    )
}