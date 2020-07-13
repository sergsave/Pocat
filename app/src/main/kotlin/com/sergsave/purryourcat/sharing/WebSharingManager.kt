package com.sergsave.purryourcat.sharing

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.AsyncTask
import androidx.core.content.FileProvider
import com.estmob.android.sendanywhere.sdk.ReceiveTask
import com.estmob.android.sendanywhere.sdk.SendTask
import com.estmob.android.sendanywhere.sdk.Task
import com.sergsave.purryourcat.BuildConfig
import com.sergsave.purryourcat.R
import com.sergsave.purryourcat.helpers.ImageUtils
import com.sergsave.purryourcat.helpers.NetworkUtils
import java.io.File
import java.net.URL
import java.util.*
import kotlin.concurrent.schedule


object MyTimeLogger {
    var time =  System.currentTimeMillis()
    var time2 =  System.currentTimeMillis()

    fun log(message: String) {
        val newTime = System.currentTimeMillis()
        println("$message: ${newTime - time}")
        time = newTime
    }

    fun reset() {
        time = System.currentTimeMillis()
        time2 = System.currentTimeMillis()
    }

    fun log2(message: String) {
        val newTime2 = System.currentTimeMillis()
        println("$message: ${newTime2 - time2}")
        time2 = newTime2
    }
}

private fun cacheDir(context: Context) =
    File(context.cacheDir, "sharing").also { if(it.exists().not()) it.mkdirs() }

private fun errorText(context: Context) = context.getString(R.string.connection_error)

private class WebPrepareTask(private val context: Context,
                             private val pack: Pack,
                             private val packer: IDataPacker): ISharingTask<Intent> {
    private var listener: ISharingListener<Intent>? = null
    private var sendTask: SendTask? = null
    private var link: Uri? = null

    override fun start() {
        MyTimeLogger.log("start")
        MyTimeLogger.reset()
        val files = arrayOf(packer.pack(pack))
        MyTimeLogger.log("packed")
//        sendTask = SendTask(context, files, true).also { init(it) }
//        sendTask?.start()
//        val task = SendspaceUploadTask()
//        task.execute(files.firstOrNull())
        val task = SendspaceDownloadTask(cacheDir(context))
        task.execute(URL("https://www.sendspace.com/file/jbksks"))
    }

    private fun init(task: SendTask) {
        task.setOnTaskListener{ state, detailedState, obj ->
            when(state) {
                Task.State.PREPARING -> {
                    val key = obj as? String
                    MyTimeLogger.log("preparing $key")
                    if(detailedState == SendTask.DetailedState.PREPARING_UPDATED_KEY && key != null)
                        (task.getValue(Task.Value.LINK_URL) as? String)?.let{ link = Uri.parse(it) }
                }
                Task.State.TRANSFERRING -> {
                    MyTimeLogger.log("transfering")
                    val fileState = obj as? Task.FileState
                    if(fileState != null) {
                        val progress = ( fileState.transferSize * 100 / fileState.totalSize).toInt()
                        listener?.onProgressChanged(progress)
                    }
                }
                Task.State.FINISHED -> {
                    MyTimeLogger.log("finished")

                    when (detailedState) {
                        SendTask.DetailedState.FINISHED_SUCCESS -> makeIntent{ intent ->
                            MyTimeLogger.log("done")
                            MyTimeLogger.log2("total")
                            if(intent != null) listener?.onSuccessed(intent)
                            else listener?.onFailed(errorText(context))
                        }
                        SendTask.DetailedState.FINISHED_CANCEL -> listener?.onCanceled()
                        SendTask.DetailedState.FINISHED_ERROR -> listener?.onFailed(errorText(context))
                    }
                }
            }
        }
    }

    private fun makeIntent(callback: (Intent?)->Unit) {
        makePreview { file ->
            if (file != null && file.exists()) {
                val previewUri = FileProvider.getUriForFile(context, BuildConfig.APPLICATION_ID, file)
                val intent = Intent(Intent.ACTION_SEND).apply {
                    putExtra(Intent.EXTRA_TEXT, link?.toString())
                    putExtra(Intent.EXTRA_STREAM, previewUri)
                    setType("image/*")
                    addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                }
                callback(intent)
            } else
                callback(null)
        }
    }

    private fun makePreview(callback: (File?)->Unit) {
        val photoUri = pack.cat.photoUri
        val previewFile = File(cacheDir(context), "preview.jpg")
        val width = 360
        val height = 640
        ImageUtils.loadInto(context, photoUri, previewFile, width, height, { res ->
            if(res) callback(previewFile) else callback(null)
        })
    }

    override fun cancel() {
        sendTask?.cancel()
    }

    override fun setListener(listener: ISharingListener<Intent>?) {
        this.listener = listener
    }
}

private class WebExtractTask(private val context: Context,
                             private val intent: Intent,
                             private val packer: IDataPacker): ISharingTask<Pack> {
    private var listener: ISharingListener<Pack>? = null
    private var receiveTask: ReceiveTask? = null
    private var fileName: String? = null

    override fun start() {
        MyTimeLogger.log("start")
        MyTimeLogger.reset()
        val key = extractKey(intent)
        receiveTask = ReceiveTask(context, key, cacheDir(context)).also { init(it) }
        receiveTask?.start()
    }

    private fun init(task: ReceiveTask) {
        task.setOnTaskListener { state, detailedState, obj ->
            when (state) {
                Task.State.TRANSFERRING -> {
                    MyTimeLogger.log("transferring")
                    val fileState = obj as? Task.FileState
                    if (fileState != null) {
                        fileName = fileState.pathName
                        val progress = (fileState.transferSize * 100 / fileState.totalSize).toInt()
                        listener?.onProgressChanged(progress)
                    }
                }
                Task.State.FINISHED -> {
                    MyTimeLogger.log("finished")
                    when (detailedState) {
                        SendTask.DetailedState.FINISHED_SUCCESS -> {
                            val pack = makePackage(fileName)
                            MyTimeLogger.log("done")
                            MyTimeLogger.log2("total")
                            if(pack != null) listener?.onSuccessed(pack)
                            else listener?.onFailed(errorText(context))
                        }
                        SendTask.DetailedState.FINISHED_CANCEL -> listener?.onCanceled()
                        SendTask.DetailedState.FINISHED_ERROR -> listener?.onFailed(errorText(context))
                    }
                }
            }
        }
    }

    private fun makePackage(fileName: String?): Pack? {
        val file = fileName?.let { File(cacheDir(context), it) }
        if(file != null && file.exists())
            return packer.unpack(file)
        return null
    }

    private fun extractKey(intent: Intent): String? {
        return intent.data?.toString()
    }

    override fun cancel() {
        receiveTask?.cancel()
    }

    override fun setListener(listener: ISharingListener<Pack>?) {
        this.listener = listener
    }
}

class WebSharingManager(private val context: Context, cleanCacheOnCreate: Boolean): ISharingManager {

    private val packer: IDataPacker = ZipDataPacker(cacheDir(context))
    init {
        if(cleanCacheOnCreate) {
            cacheDir(context).apply {
                deleteRecursively()
                mkdirs()
            }
        }
        val apiKey = "dfe91eb770456c0a269625ce8e0128ad2b4a5fb0"
        Task.init(apiKey)
    }

    override fun makePrepareTask(pack: Pack): ISharingTask<Intent>? {
//        return ConnectionCheckerDecorator<Intent>(context, WebPrepareTask(context, pack, packer))
        return WebPrepareTask(context, pack, packer)
    }

    override fun makeExtractTask(intent: Intent): ISharingTask<Pack>? {
        if(intent.data == null)
            return null

        return ConnectionCheckerDecorator<Pack>(context, WebExtractTask(context, intent, packer))
    }
}

private class ConnectionChecker(private val context: Context,
                                private val onDisconnected: ()->Unit) {
    private var timer: Timer? = null

    fun start(timeout: Long) {
        stop()
        timer = Timer("Connection checker", false)
        timer?.schedule(timeout) {
            if(NetworkUtils.isNetworkAvailable(context).not()) onDisconnected()
        }
    }

    fun stop() {
        timer?.cancel()
        timer?.purge()
        timer = null
    }
}

private val CONNECTION_TIMEOUT: Long = 5000

private class ConnectionCheckerDecorator<T>(private val context: Context,
                                            private val task: ISharingTask<T>): ISharingTask<T> {
    private var connectionChecker = ConnectionChecker(context, {
        listener?.onFailed(errorText(context))
        ignoreCanceledEvent = true
        task.cancel()
    })
    private var listener: ISharingListener<T>? = null
    private var ignoreCanceledEvent = false

    override fun start() {
        ignoreCanceledEvent = false
        task.start()
        connectionChecker.start(CONNECTION_TIMEOUT)
    }

    override fun cancel() {
        connectionChecker.stop()
        task.cancel()
    }

    override fun setListener(listener: ISharingListener<T>?) {
        val wrappedlistener = object: ISharingListener<T> {
            override fun onSuccessed(data: T) {
                connectionChecker.stop()
                listener?.onSuccessed(data)
            }

            override fun onFailed(error: String) {
                connectionChecker.stop()
                listener?.onFailed(error)
            }

            override fun onCanceled() {
                connectionChecker.stop()
                if(ignoreCanceledEvent.not())
                    listener?.onCanceled()
            }

            override fun onProgressChanged(progress: Int) {
                connectionChecker.start(CONNECTION_TIMEOUT)
                listener?.onProgressChanged(progress)
            }
        }
        task.setListener(wrappedlistener)
        this.listener = wrappedlistener
    }
}