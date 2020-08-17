package com.sergsave.purryourcat.repositories.data

import android.net.Uri
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.sergsave.purryourcat.models.CatData
import java.util.UUID

class CatDataRepository(private val storage: CatDataStorage)
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
}