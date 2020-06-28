package com.sergsave.purryourcat.data

import android.net.Uri
import com.sergsave.purryourcat.models.CatData
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.annotation.MainThread
import java.util.UUID

class CatDataRepo private constructor(private val storage: ICatDataStorage)
{
    private val cats = mutableMapOf<String, CatData>()
    private val liveData = MutableLiveData<Map<String, CatData>>()

    init {
        storage.load()?.let { cats.putAll(it) }
        liveData.value = cats
    }

    fun read() : LiveData<Map<String, CatData>> {
        return liveData
    }

    fun add(cat: CatData) : String {
        val id = UUID.randomUUID().toString()
        cats.put(id, cat)
        onUpdate()
        return id
    }

    fun update(id: String, cat: CatData) {
        cats.put(id, cat)
        onUpdate()
    }

    fun remove(id: String) {
        cats.remove(id)
        onUpdate()
    }

    private fun onUpdate() {
        storage.save(cats)
        liveData.value = cats.toMap()
    }

    companion object {
        var instance: CatDataRepo? = null
            private set

        @MainThread
        fun init(storage: ICatDataStorage): CatDataRepo {
            instance = instance ?: CatDataRepo(storage)
            return instance!!
        }
    }
}