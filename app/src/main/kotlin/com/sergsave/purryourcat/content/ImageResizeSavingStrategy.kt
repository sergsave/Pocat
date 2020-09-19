package com.sergsave.purryourcat.content

import android.content.Context
import android.net.Uri
import com.sergsave.purryourcat.helpers.ImageUtils
import io.reactivex.Completable
import java.io.File
import java.io.IOException

class ImageResizeSavingStrategy(private val context: Context): SavingStrategy {
    private val width = 1440
    private val height = 2560

    // TODO: DON'T resize little images

    override fun save(sourceContent: Uri, outputFile: File): Completable {
        return Completable.create { emitter ->
            ImageUtils.loadInto(context, sourceContent, outputFile, width, height) { res ->
                if(res)
                    emitter.onComplete()
                else
                    emitter.onError(IOException("Image loading error"))
            }
        }
    }
}