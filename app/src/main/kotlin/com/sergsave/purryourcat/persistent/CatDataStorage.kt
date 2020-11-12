package com.sergsave.purryourcat.persistent

import com.sergsave.purryourcat.models.CatData
import io.reactivex.Completable
import io.reactivex.Flowable

interface CatDataStorage {
    fun read(): Flowable<Map<String, TimedCatData>>
    fun add(cat: Pair<String, TimedCatData>): Completable
    fun update(cat: Pair<String, CatData>): Completable
    fun remove(id: String): Completable
    fun remove(ids: List<String>): Completable
}