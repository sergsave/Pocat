package com.sergsave.purryourcat.persistent

import com.sergsave.purryourcat.models.Cat
import io.reactivex.Completable
import io.reactivex.Flowable
import java.util.*

class CatRepository(private val storage: CatStorage)
{
    fun read(): Flowable<List<TimedCat>> {
        return storage.read()
    }

    fun add(cat: Cat): Completable {
        return storage.add(TimedCat(Date(), cat))
    }

    fun update(cat: Cat): Completable {
        return storage.update(cat)
    }

    fun remove(id: UUID): Completable {
        return storage.remove(id)
    }
}