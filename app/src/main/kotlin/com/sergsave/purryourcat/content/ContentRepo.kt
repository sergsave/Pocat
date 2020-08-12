package com.sergsave.purryourcat.content

import android.net.Uri
import androidx.annotation.MainThread
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData

// Save content to file storage available for application
class ContentRepo private constructor(private val storage: IContentStorage)
{
    private val liveData = MutableLiveData<List<Uri>>()

    init {
        liveData.value = storage.read()
    }

    fun read(): LiveData<List<Uri>> {
        return liveData
    }

    // If withName equal null, content will added with same name
    fun add(sourceContent: Uri?, withName: String? = null): Uri? {
        if(sourceContent == null) return null

        val uri =
            if(withName != null) storage.store(sourceContent, withName)
            else storage.store(sourceContent)

        onUpdate()
        return uri
    }

    fun remove(uri: Uri?): Boolean {
        if(uri == null) return false

        val res = storage.remove(uri)
        onUpdate()
        return res
    }

    private fun onUpdate() {
        liveData.value = storage.read()
    }

    companion object {
        var instance: ContentRepo? = null
            private set

        @MainThread
        fun init(storage: IContentStorage): ContentRepo {
            instance = instance ?: ContentRepo(storage)
            return instance!!
        }
    }
}