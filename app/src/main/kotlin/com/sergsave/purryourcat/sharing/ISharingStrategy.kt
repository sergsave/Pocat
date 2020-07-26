package com.sergsave.purryourcat.sharing

import android.content.Intent
import android.net.Uri
import io.reactivex.rxjava3.core.Single

interface ISharingStrategy {
    fun makePrepareObservable(pack: Pack): Single<Intent>?
    fun makeExtractObservable(intent: Intent): Single<Pack>?
}