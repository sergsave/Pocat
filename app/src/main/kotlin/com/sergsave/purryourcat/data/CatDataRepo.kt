package com.sergsave.purryourcat.data

import com.sergsave.purryourcat.models.CatData
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Transformations
import androidx.annotation.MainThread

interface ICatStorage {
    fun save(cats: List<CatData>?)
    fun load() : List<CatData>?
}

class CatDataRepo private constructor(private val storage: ICatStorage) {
    private val cats = mutableMapOf<String, CatData>()
    private val liveData = MutableLiveData<Map<String, CatData>>()
    private var idCounter = 0

    init {
        liveData.value = cats
        storage.load()?.forEach { add(it) }
    }

    fun read() : LiveData<Map<String, CatData>> {
        return liveData
    }

    fun update(id: String, cat: CatData) {
        cats.put(id, cat)
        onUpdate()
    }

    fun add(cat: CatData) : String {
        val id = (idCounter++).toString()
        cats.put(id, cat)
        onUpdate()
        return id
    }

    fun remove(id: String) {
        cats.remove(id)
        onUpdate()
    }

    private fun onUpdate() {
        // Notify
        liveData.value = cats.toMap()
        storage.save(cats.values.toList())
    }

    companion object {
        var instance: CatDataRepo? = null
            private set

        @MainThread
        fun init(storage: ICatStorage): CatDataRepo {
            instance = instance ?: CatDataRepo(storage)
            return instance!!
        }
    }
}