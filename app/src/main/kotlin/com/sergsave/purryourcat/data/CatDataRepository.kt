package com.sergsave.purryourcat.data

import com.sergsave.purryourcat.models.CatData
import io.reactivex.rxjava3.core.Completable
import io.reactivex.rxjava3.core.Flowable
import io.reactivex.rxjava3.core.Single
import java.util.*

class CatDataRepository(private val storage: CatDataStorage)
{
    fun read(): Flowable<Map<String, TimedCatData>> {
        return storage.read()
    }

    fun add(cat: CatData): Single<String> {
        val timestamp = System.currentTimeMillis()
        val id = UUID.randomUUID().toString()
        val timed = TimedCatData(timestamp, cat)
        return storage.add(Pair(id, timed)).toSingle { id }
    }

    fun update(id: String, cat: CatData): Completable {
        return storage.update(Pair(id, cat))
    }

    fun remove(id: String): Completable {
        return storage.remove(id)
    }
}