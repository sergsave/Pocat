package com.sergsave.purryourcat.content

import android.content.Context
import android.net.Uri
import com.sergsave.purryourcat.helpers.FileUtils
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers
import io.reactivex.rxjava3.core.Single
import io.reactivex.rxjava3.schedulers.Schedulers
import java.io.File
import java.io.IOException
import java.util.UUID

class LocalFilesContentStorage(private val context: Context,
                               private val savingStrategy: SavingStrategy): ContentStorage {

    override fun store(sourceContent: Uri, fileName: String?): Single<Uri> {
        val sourceName = FileUtils.getContentFileName(context, sourceContent)
        val name = fileName ?: sourceName
        if(name == null)
            return Single.error(IOException("Invalid file name"))
        
        val dir = createUniqueDir()
        dir.mkdirs()
        val file = File(dir, name)

        return savingStrategy.save(sourceContent, file).map{ Uri.fromFile(file) }
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
    }

    override fun read(): Single<List<Uri>> {
        val files = dir().walk().filter{ it.isDirectory.not() }.toList()
        return Single.fromCallable{ files.map { Uri.fromFile(it) } }
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
    }

    override fun remove(uri: Uri): Single<Unit> {
        return Single.create<Unit> { emitter ->
            // Only file path uri contains in this storage type
            val path = uri.path

            var res = false
            if(path != null && path.startsWith(dir().path)) {
                val dir = File(path).parentFile
                res = dir?.deleteRecursively() ?: false
            }

            if(res)
                emitter.onSuccess(Unit)
            else
                emitter.onError(IOException("Removing error"))
        }
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())

    }

    private fun dir(): File {
        return File(context.filesDir, "content")
    }

    private fun createUniqueDir(): File {
        val uuid = UUID.randomUUID().toString()
        return File(dir(), uuid)
    }
}