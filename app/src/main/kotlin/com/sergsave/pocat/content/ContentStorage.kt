package com.sergsave.pocat.content

import android.net.Uri
import io.reactivex.Flowable
import io.reactivex.Single
import io.reactivex.Completable

interface ContentStorage {
    // No expected errors, returns empty list
    fun read(): Flowable<List<Uri>>
    fun add(sourceContent: Uri, keepFileName: Boolean): Single<Uri>
    fun remove(uri: Uri): Completable
}