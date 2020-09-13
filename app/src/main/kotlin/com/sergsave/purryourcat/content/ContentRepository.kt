package com.sergsave.purryourcat.content

import android.net.Uri
import io.reactivex.rxjava3.core.Flowable
import io.reactivex.rxjava3.core.Single
import io.reactivex.rxjava3.core.Completable
import io.reactivex.rxjava3.kotlin.Flowables
import java.io.IOException

// Save content to file storage available for application
// Files will added with same names
class ContentRepository (
    private val imageStorage: ContentStorage,
    private val audioStorage: ContentStorage,
    val maxImageFileSize: Long,
    val maxAudioFileSize: Long)
{
    fun read(): Flowable<List<Uri>> {
        return Flowables.zip(audioStorage.read(), imageStorage.read())
            .map { (audios, images) -> audios + images }
    }

    private fun add(storage: ContentStorage, sourceContent: Uri?): Single<Uri> {
        if(sourceContent == null)
            return Single.error(IOException("Null content"))

        return storage.add(sourceContent, true)
    }

    fun addAudio(sourceContent: Uri?): Single<Uri> {
        return add(audioStorage, sourceContent)
    }

    fun addImage(sourceContent: Uri?): Single<Uri> {
        return add(imageStorage, sourceContent)
    }

    fun remove(uri: Uri?): Completable {
        if(uri == null)
            return Completable.error(IOException("Null uri"))

        return audioStorage.remove(uri)
            .onErrorResumeNext { imageStorage.remove(uri) }
    }
}