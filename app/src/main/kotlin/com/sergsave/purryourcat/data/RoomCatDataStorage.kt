package com.sergsave.purryourcat.persistent

import android.content.Context
import android.net.Uri
import androidx.room.Room
import com.sergsave.purryourcat.persistent.database.TimedCatEntity
import com.sergsave.purryourcat.persistent.database.CatEntity
import com.sergsave.purryourcat.persistent.database.CatDatabase
import com.sergsave.purryourcat.models.Cat
import com.sergsave.purryourcat.models.CatData
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.Completable
import io.reactivex.Flowable
import io.reactivex.schedulers.Schedulers
import java.util.*

class RoomCatStorage(context: Context): CatStorage {

    private val database = Room.databaseBuilder(
        context.applicationContext,
        CatDatabase::class.java, "cat_database"
    )
        .build()

    override fun read(): Flowable<List<TimedCat>> {
        return database.catDao().getAll().map { entities ->
            entities.map { timedCatFrom(it) }
        }
            .observeOn(AndroidSchedulers.mainThread())
    }

    override fun add(cat: TimedCat): Completable {
        return database.catDao().insert(timedCatEntityFrom(cat))
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
    }

    override fun update(cat: Cat): Completable {
        return database.catDao().update(catEntityFrom(cat))
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
    }

    override fun remove(id: UUID): Completable {
        return database.catDao().deleteById(id.toString())
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
    }

    private fun catEntityFrom(cat: Cat) = CatEntity(
        id = cat.id.toString(),
        name = cat.data.name,
        photoUri = cat.data.photoUri?.toString(),
        audioUri = cat.data.purrAudioUri?.toString()
    )

    private fun timedCatEntityFrom(timedCat: TimedCat) = TimedCatEntity (
        createdTime = timedCat.timestamp.time,
        base = catEntityFrom(timedCat.cat)
    )

    private fun catFrom(entity: CatEntity): Cat {
        val data = CatData(
            name = entity.name,
            photoUri = entity.photoUri?.let { Uri.parse(it) },
            purrAudioUri = entity.audioUri?.let { Uri.parse(it) }
        )
        return Cat(UUID.fromString(entity.id), data)
    }

    private fun timedCatFrom(entity: TimedCatEntity) = TimedCat (
        timestamp = Date(entity.createdTime),
        cat = catFrom(entity.base)
    )
}