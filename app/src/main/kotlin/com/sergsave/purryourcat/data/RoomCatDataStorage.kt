package com.sergsave.purryourcat.data

import android.content.Context
import android.net.Uri
import androidx.room.Room
import com.sergsave.purryourcat.data.database.Cat
import com.sergsave.purryourcat.models.CatData
import com.sergsave.purryourcat.data.database.CatDatabase
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers
import io.reactivex.rxjava3.core.Single
import io.reactivex.rxjava3.schedulers.Schedulers

class RoomCatDataStorage(context: Context): CatDataStorage {

    private val database = Room.databaseBuilder(
        context.applicationContext,
        CatDatabase::class.java, "cat_database"
    )
        .build()


    override fun load(): Single<Map<String, CatData>> {
        return Single.fromCallable {
            database.catDao().getAll().associate {
                val catData = CatData(
                    name = it.name,
                    photoUri = it.photoUri?.let { Uri.parse(it) },
                    purrAudioUri = it.audioUri?.let { Uri.parse(it) }
                )
                Pair(it.id, catData)
            }
        }
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
    }

    override fun save(cats: Map<String, CatData>): Single<Unit> {
        return Single.fromCallable {
            val entities = cats.map { (k, v) ->
                Cat(
                    id = k,
                    name = v.name,
                    photoUri = v.photoUri?.toString(),
                    audioUri = v.purrAudioUri?.toString()
                )
            }
            database.catDao().deleteAndInsertAll(entities)
        }
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
    }
}