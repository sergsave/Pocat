package com.sergsave.purryourcat.content

import android.content.Context
import android.net.Uri
import io.reactivex.rxjava3.core.Single
import java.io.File
import java.io.IOException
import com.sergsave.purryourcat.helpers.FileUtils

class CopySavingStrategy(private val context: Context): SavingStrategy {
    override fun save(sourceContent: Uri, outputFile: File): Single<Unit> {
        val inputStream = context.contentResolver.openInputStream(sourceContent)
        if(inputStream == null)
            return Single.error(IOException("Cant open stream"))

        return Single.fromCallable {
            FileUtils.copyStreamToFile(inputStream, outputFile)
            inputStream.close()
        }
    }
}