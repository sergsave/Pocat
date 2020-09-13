package com.sergsave.purryourcat.content

import android.content.Context
import android.net.Uri
import io.reactivex.rxjava3.core.Completable
import java.io.File
import java.io.IOException
import com.sergsave.purryourcat.helpers.FileUtils

class CopySavingStrategy(private val context: Context): SavingStrategy {
    override fun save(sourceContent: Uri, outputFile: File): Completable {
        val inputStream = context.contentResolver.openInputStream(sourceContent)
        if(inputStream == null)
            return Completable.error(IOException("Cant open stream"))

        return Completable.fromCallable {
            FileUtils.copyStreamToFile(inputStream, outputFile)
            inputStream.close()
        }
    }
}