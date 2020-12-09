package com.sergsave.pocat.sharing

import android.content.Intent
import io.reactivex.Single
import io.reactivex.Completable

interface WebSharingManager{
    class InvalidLinkException(message: String, cause: Throwable? = null): Exception(message, cause)
    class NoConnectionException(message: String, cause: Throwable? = null): Exception(message, cause)

    // Expected errors: NoConnectionException, IOException
    fun upload(pack: Pack): Single<Intent>

    // Expected errors: InvalidLinkException, NoConnectionException, IOException
    fun download(intent: Intent): Single<Pack>

    // Release any resources, call only if sharing is not in progress
    // Expected errors: IOException
    fun cleanup(): Completable
}