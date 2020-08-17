package com.sergsave.purryourcat.sharing

import java.io.File
import java.net.URL
import io.reactivex.rxjava3.core.Single

interface NetworkService {
    fun makeUploadObservable(file: File): Single<URL>
    fun makeDownloadObservable(url: URL, destDir: File): Single<File>
}