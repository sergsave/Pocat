package com.sergsave.purryourcat.data

import com.sergsave.purryourcat.models.CatData
import io.reactivex.rxjava3.subjects.BehaviorSubject
import io.reactivex.rxjava3.core.Observable
import io.reactivex.rxjava3.core.Single
import java.util.*

class CatDataRepository(private val storage: CatDataStorage)
{
    private val catsSubject = BehaviorSubject.create<Map<String, CatData>>()

    private fun sendNotification() {
        catsSubject.onNext(emptyMap())
    }

    fun read(): Observable<Map<String, CatData>> {
        return catsSubject.flatMapSingle { _ -> storage.load() }
            .startWith(storage.load())
    }

    fun add(cat: CatData): Single<String> {
        val id = UUID.randomUUID().toString()
        return updateStorage({ it.put(id, cat) })
            .map{ id }
            .doOnSuccess { sendNotification() }
    }

    fun update(id: String, cat: CatData): Single<Unit> {
        return updateStorage({ it.put(id, cat) }).doOnSuccess { sendNotification() }
    }

    fun remove(id: String): Single<Unit> {
        return updateStorage({ it.remove(id) }).doOnSuccess { sendNotification() }
    }

    private fun updateStorage(updater: (MutableMap<String, CatData>) -> Unit): Single<Unit> {
        return storage.load()
            .flatMap { cats ->
                val copy = cats.toMutableMap()
                updater(copy)
                storage.save(copy)
            }
    }
}