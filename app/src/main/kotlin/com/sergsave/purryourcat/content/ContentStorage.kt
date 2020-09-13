package com.sergsave.purryourcat.content

import android.net.Uri
import io.reactivex.rxjava3.core.Flowable
import io.reactivex.rxjava3.core.Single
import io.reactivex.rxjava3.core.Completable

interface ContentStorage {
    fun read(): Flowable<List<Uri>>
    fun add(sourceContent: Uri, keepFileName: Boolean): Single<Uri>
    fun remove(uri: Uri): Completable
}