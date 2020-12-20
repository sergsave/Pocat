package com.sergsave.pocat.content

import android.content.Context
import android.net.Uri
import io.reactivex.Completable
import java.io.File
import java.io.IOException
import com.sergsave.pocat.helpers.FileUtils
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers

class CopySavingStrategy(private val context: Context): SavingStrategy {
    override fun save(sourceContent: Uri, outputFile: File): Completable {
        return Completable.create { emitter ->
            val inputStream = context.contentResolver.openInputStream(sourceContent)
            if(inputStream == null)
                emitter.onError(IOException("Can't open stream"))
            else {
                FileUtils.copyStreamToFile(inputStream, outputFile)
                inputStream.close()
                emitter.onComplete()
            }
        }
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
    }
}