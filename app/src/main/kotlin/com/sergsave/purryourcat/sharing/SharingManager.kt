package com.sergsave.purryourcat.sharing

import android.content.Intent
import io.reactivex.rxjava3.core.Single
import io.reactivex.rxjava3.core.Completable

interface SharingManager{
    fun makeTakeObservable(pack: Pack): Single<Intent>
    fun makeGiveObservable(intent: Intent): Single<Pack>
    fun cleanup(): Completable // Release any resources, call only if sharing is not in progress
}