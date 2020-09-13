package com.sergsave.purryourcat.data

import com.sergsave.purryourcat.models.CatData
import io.reactivex.rxjava3.core.Completable
import io.reactivex.rxjava3.core.Flowable

interface CatDataStorage {
    fun read(): Flowable<Map<String, TimedCatData>>
    fun add(cat: Pair<String, TimedCatData>): Completable
    fun update(cat: Pair<String, CatData>): Completable
    fun remove(id: String): Completable
}