package com.sergsave.purryourcat.content

import android.net.Uri
import io.reactivex.rxjava3.core.Single

interface ContentStorage {
    fun store(sourceContent: Uri, fileName: String? = null): Single<Uri>
    fun read(): Single<List<Uri>>
    fun remove(uri: Uri): Single<Unit>
}