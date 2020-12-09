package com.sergsave.pocat.content

import android.content.Context
import android.net.Uri
import com.sergsave.pocat.helpers.FileUtils
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.BackpressureStrategy
import io.reactivex.Single
import io.reactivex.Flowable
import io.reactivex.Completable
import io.reactivex.subjects.BehaviorSubject
import io.reactivex.schedulers.Schedulers
import java.io.File
import java.io.IOException
import java.util.UUID

class LocalFilesContentStorage(private val context: Context,
                               private val savingStrategy: SavingStrategy): ContentStorage {

    private val contentListSubject = BehaviorSubject.create<List<Uri>>()

    private fun sendNotification() {
        // http://arturogutierrez.github.io/2016/05/07/rxjava-data-store-hot-observables/
        contentListSubject.onNext(emptyList())
    }

    override fun read(): Flowable<List<Uri>> {
        val readFileUris = {
            dir().walk().filter{ it.isDirectory.not() }.map{ Uri.fromFile(it) }.toList()
        }
        return contentListSubject
            .toFlowable(BackpressureStrategy.LATEST)
            .map{ readFileUris() }
            .startWith(Flowable.fromCallable{ readFileUris() })
    }

    override fun add(sourceContent: Uri, keepFileName: Boolean): Single<Uri> {
        // This implementation always keep file name
        val name = FileUtils.resolveContentFileName(context, sourceContent)

        if(name == null)
            return Single.error(IOException("Invalid file name"))
        
        val dir = createUniqueDir()
        dir.mkdirs()
        val file = File(dir, name)

        return savingStrategy.save(sourceContent, file).toSingle { Uri.fromFile(file) }
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .doOnSuccess { sendNotification() }
    }

    override fun remove(uri: Uri): Completable {
        return Completable.create { emitter ->
            // Only file path uri contains in this storage type
            val path = uri.path

            var res = false
            if(path != null && path.startsWith(dir().path)) {
                val dir = File(path).parentFile
                res = dir?.deleteRecursively() ?: false
            }

            if(res)
                emitter.onComplete()
            else
                emitter.onError(IOException("Removing error"))
        }
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .doOnComplete { sendNotification() }
    }

    private fun dir(): File {
        return File(context.filesDir, "content")
    }

    private fun createUniqueDir(): File {
        val uuid = UUID.randomUUID().toString()
        return File(dir(), uuid)
    }
}