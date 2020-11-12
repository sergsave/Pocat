package com.sergsave.purryourcat.persistent

import com.sergsave.purryourcat.models.CatData
import io.reactivex.Completable
import io.reactivex.Flowable
import io.reactivex.Single
import java.util.*

class CatDataRepository(private val storage: CatDataStorage)
{
    fun read(): Flowable<Map<String, TimedCatData>> {
        return storage.read()
    }

    fun add(cat: CatData): Single<String> {
        val id = UUID.randomUUID().toString()
        val timed = TimedCatData(Date(), cat)
        return storage.add(Pair(id, timed)).toSingle { id }
    }

    fun update(id: String, cat: CatData): Completable {
        return storage.update(Pair(id, cat))
    }

    fun remove(id: String): Completable {
        return storage.remove(id)
    }

    fun remove(ids: List<String>): Completable {
        return storage.remove(ids)
    }
}