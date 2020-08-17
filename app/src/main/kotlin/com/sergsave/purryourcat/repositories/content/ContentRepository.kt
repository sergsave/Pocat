package com.sergsave.purryourcat.repositories.content

import android.net.Uri
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData

// Save content to file storage available for application
class ContentRepository (private val storage: ContentStorage)
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
}