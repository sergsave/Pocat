package com.sergsave.purryourcat.persistent

import com.sergsave.purryourcat.models.Cat
import io.reactivex.Completable
import io.reactivex.Flowable
import java.util.*

interface CatStorage {
    fun read(): Flowable<List<TimedCat>>
    fun add(cat: TimedCat): Completable
    fun update(cat: Cat): Completable
    fun remove(id: UUID): Completable
}