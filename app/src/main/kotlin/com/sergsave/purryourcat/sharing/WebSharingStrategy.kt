package com.sergsave.purryourcat.sharing

import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.core.content.FileProvider
import com.sergsave.purryourcat.BuildConfig
import com.sergsave.purryourcat.R
import com.sergsave.purryourcat.helpers.ImageUtils
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers
import io.reactivex.rxjava3.core.Observable
import io.reactivex.rxjava3.core.Single
import io.reactivex.rxjava3.disposables.Disposable
import io.reactivex.rxjava3.kotlin.Singles
import io.reactivex.rxjava3.schedulers.Schedulers
import java.io.File
import java.net.URL
import java.util.*
import java.util.concurrent.TimeUnit
import java.util.function.Function

private fun cacheDir(context: Context) =
    File(context.cacheDir, "sharing").also { if(it.exists().not()) it.mkdirs() }

class WebSharingStrategy(private val context: Context,
                        private val service: INetworkService,
                        cleanCacheOnCreate: Boolean): ISharingStrategy {

    private val packer: IDataPacker = ZipDataPacker(context, cacheDir(context))
    init {
        if(cleanCacheOnCreate) {
            cacheDir(context).apply {
                deleteRecursively()
                mkdirs()
            }
        }
    }

    override fun makePrepareObservable(pack: Pack): Single<Intent>? {
        val previewSingle = makePreview(pack)

        val uploadSingle = Single.fromCallable { packer.pack(pack)!! } // rxJava doesn't support null
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .flatMap{ file -> service.makeUploadObservable(file) }

        return Singles.zip(previewSingle, uploadSingle, { preview, link ->
            makeIntent(link, preview)
        })
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

        return Single.create<Uri> { emitter ->
            ImageUtils.loadInto(context, photoUri, file, width, height, { res ->
                val uri = FileProvider.getUriForFile(context, BuildConfig.APPLICATION_ID, file)
                if(res)
                    emitter.onSuccess(uri)
                else
                    emitter.onError(IllegalStateException("Image load error"))
            })
        }
    }

    override fun makeExtractObservable(intent: Intent): Single<Pack>? {
        val uri = intent.data
        if(uri == null)
            return null

        return service
            .makeDownloadObservable(URL(uri.toString()), cacheDir(context))
            .map { file -> packer.unpack(file)!! }  // rxJava doesn't support null
    }
}