package com.sergsave.pocat.content

import android.net.Uri
import io.reactivex.Completable
import java.io.File

interface SavingStrategy {
    fun save(sourceContent: Uri, outputFile: File): Completable
}