package com.sergsave.purryourcat.sharing

import android.content.Intent
import android.net.Uri
import io.reactivex.rxjava3.core.Single

interface SharingManager{
    fun makeTakeObservable(pack: Pack): Single<Intent>?
    fun makeGiveObservable(intent: Intent): Single<Pack>?
}