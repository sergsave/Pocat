package com.sergsave.purryourcat.sharing

import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.core.content.FileProvider
import com.sergsave.purryourcat.BuildConfig
import com.sergsave.purryourcat.R
import com.sergsave.purryourcat.helpers.ImageUtils
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers
import io.reactivex.rxjava3.core.Single
import io.reactivex.rxjava3.kotlin.Singles
import io.reactivex.rxjava3.schedulers.Schedulers
import java.io.File
import java.io.IOException
import java.net.URL

private fun cacheDir(context: Context) =
    File(context.cacheDir, "sharing").also { if(it.exists().not()) it.mkdirs() }

private fun errorThrowable(context: Context)
        = IOException(context.getString(R.string.connection_error))

class WebSharingManager(private val context: Context,
                        private val service: NetworkService,
                        private val packer: DataPacker
): SharingManager {

    override fun cleanup() {
        Single.fromCallable {
            val cache = cacheDir(context)
            cache.deleteRecursively()
            cache.mkdirs()
        }
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe()
    }

    override fun makeTakeObservable(pack: Pack): Single<Intent>? {
        val previewSingle = makePreview(pack)
        val throwable = errorThrowable(context)
        val cache = cacheDir(context)

        val uploadSingle = Single.fromCallable { packer.pack(pack, cache)!! } // rxJava doesn't support null
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .flatMap{ file -> service.makeUploadObservable(file) }

        return Singles.zip(previewSingle, uploadSingle, { preview, link ->
            makeIntent(link, preview)
        })
            .onErrorResumeNext { Single.error(throwable) }
    }

    private fun makeIntent(url: URL, previewUri: Uri): Intent {
        return Intent(Intent.ACTION_SEND).apply {
            val text = context.getString(R.string.sharing_text, url.toString())
            putExtra(Intent.EXTRA_TEXT, text)
            putExtra(Intent.EXTRA_STREAM, previewUri)
            setType("image/*")
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
    }

    private fun makePreview(pack: Pack): Single<Uri> {
        val photoUri = pack.cat.photoUri
        val file = File(cacheDir(context), "preview.jpg")
        val width = 360
        val height = 640

        return Single.create { emitter ->
            ImageUtils.loadInto(context, photoUri, file, width, height) { res ->
                val uri = FileProvider.getUriForFile(context, BuildConfig.APPLICATION_ID, file)
                if(res)
                    emitter.onSuccess(uri)
                else
                    emitter.onError(IllegalStateException("Image load error"))
            }
        }
    }

    override fun makeGiveObservable(intent: Intent): Single<Pack>? {
        val uri = intent.data
        if(uri == null)
            return null

        val throwable = errorThrowable(context)
        return service
            .makeDownloadObservable(URL(uri.toString()), cacheDir(context))
            .map { file -> packer.unpack(file)!! }  // rxJava doesn't support null
            .onErrorResumeNext { Single.error(throwable) }
    }
}