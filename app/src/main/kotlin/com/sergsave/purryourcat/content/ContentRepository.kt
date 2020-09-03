package com.sergsave.purryourcat.content

import android.net.Uri
import io.reactivex.rxjava3.core.Observable
import io.reactivex.rxjava3.core.Single
import io.reactivex.rxjava3.kotlin.Singles
import io.reactivex.rxjava3.subjects.BehaviorSubject
import java.io.IOException

// Save content to file storage available for application
class ContentRepository (
    private val imageStorage: ContentStorage,
    private val audioStorage: ContentStorage,
    val maxImageFileSize: Long,
    val maxAudioFileSize: Long)
{
    private val contentListSubject = BehaviorSubject.create<List<Uri>>()

    private fun sendNotification() {
        contentListSubject.onNext(null)
    }

    fun read(): Observable<List<Uri>> {
        return contentListSubject.flatMapSingle {
            Singles.zip(audioStorage.read(), imageStorage.read()).map { (audios, images) ->
                audios + images
            }
        }
    }

    private fun add(storage: ContentStorage, sourceContent: Uri?, withName: String? = null): Single<Uri> {
        if(sourceContent == null)
            return Single.error(IOException("Null content"))

        return storage.store(sourceContent, withName).doOnSuccess { sendNotification() }
    }

    // If withName equal null, content will added with same name
    fun addAudio(sourceContent: Uri?, withName: String? = null): Single<Uri> {
        return add(audioStorage, sourceContent, withName)
    }

    fun addImage(sourceContent: Uri?, withName: String? = null): Single<Uri> {
        return add(imageStorage, sourceContent, withName)
    }

    fun remove(uri: Uri?): Single<Unit> {
        if(uri == null)
            return Single.error(IOException("Null uri"))

        return audioStorage.remove(uri)
            .onErrorResumeNext { imageStorage.remove(uri) }
            .doOnSuccess{ sendNotification() }
    }
}