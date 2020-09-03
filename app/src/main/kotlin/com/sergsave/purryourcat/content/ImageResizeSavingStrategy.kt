package com.sergsave.purryourcat.content

import android.content.Context
import android.net.Uri
import io.reactivex.rxjava3.core.Single
import java.io.File
import java.io.IOException
import com.sergsave.purryourcat.helpers.FileUtils
import com.sergsave.purryourcat.helpers.ImageUtils

class ImageResizeSavingStrategy(private val context: Context): SavingStrategy {
    private val width = 1440
    private val height = 2560

    // TODO: DONT resize little images

    override fun save(sourceContent: Uri, outputFile: File): Single<Unit> {
        return Single.create<Unit> { emitter ->
            ImageUtils.loadInto(context, sourceContent, outputFile, width, height) { res ->
                if(res)
                    emitter.onSuccess(Unit)
                else
                    emitter.onError(IOException("Image loading error"))
            }
        }
    }
}