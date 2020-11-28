package com.sergsave.purryourcat.sharing

import android.content.Intent
import android.net.Uri
import io.reactivex.Single
import io.reactivex.Completable

interface WebSharingManager{
    class InvalidLinkException(message: String, cause: Throwable? = null): Exception(message, cause)
    class NoConnectionException(message: String, cause: Throwable? = null): Exception(message, cause)

    // Expected errors: NoConnectionException, IOException
    fun upload(pack: Pack): Single<Uri>

    // Expected errors: InvalidLinkException, NoConnectionException, IOException
    fun download(link: Uri): Single<Pack>

    // Expected errors: InvalidLinkException
    fun extractLink(intent: Intent): Single<Uri>

    fun createIntent(link: Uri): Single<Intent>

    // Release any resources, call only if sharing is not in progress
    // Expected errors: IOException
    fun cleanup(): Completable
}