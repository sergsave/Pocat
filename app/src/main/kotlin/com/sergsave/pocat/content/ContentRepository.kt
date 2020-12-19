package com.sergsave.pocat.content

import android.net.Uri
import io.reactivex.Flowable
import io.reactivex.Single
import io.reactivex.Completable
import io.reactivex.rxkotlin.Flowables

// Save content to file storage available for application
// Files will added with same names
class ContentRepository (
    private val imageStorage: ContentStorage,
    private val audioStorage: ContentStorage)
{
    fun read(): Flowable<List<Uri>> {
        return Flowables.zip(audioStorage.read(), imageStorage.read())
            .map { (audios, images) -> audios + images }
    }

    private fun add(storage: ContentStorage, sourceContent: Uri): Single<Uri> {
        return storage.add(sourceContent, true)
    }

    fun addAudio(sourceContent: Uri): Single<Uri> {
        return add(audioStorage, sourceContent)
    }

    fun addImage(sourceContent: Uri): Single<Uri> {
        return add(imageStorage, sourceContent)
    }

    fun remove(uri: Uri): Completable {
        return audioStorage.remove(uri)
            .onErrorResumeNext { imageStorage.remove(uri) }
    }
}