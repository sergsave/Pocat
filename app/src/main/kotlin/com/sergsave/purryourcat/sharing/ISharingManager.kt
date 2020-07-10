package com.sergsave.purryourcat.sharing

import android.content.Intent
import android.net.Uri
import com.sergsave.purryourcat.models.CatData

interface ISharingListener<T> {
    fun onSuccessed(data: T)
    fun onFailed(error: String)
    fun onCanceled()
    fun onProgressChanged(progress: Int)
}

interface ISharingTask<T> {
    fun start()
    fun cancel()
    fun setListener(listener: ISharingListener<T>?)
}

// Note. All content from CatData should have "file" Uri scheme
interface ISharingManager {
    fun makePrepareTask(pack: Pack): ISharingTask<Intent>?
    fun makeExtractTask(intent: Intent): ISharingTask<Pack>?
}