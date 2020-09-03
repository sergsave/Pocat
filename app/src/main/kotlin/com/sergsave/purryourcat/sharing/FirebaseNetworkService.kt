package com.sergsave.purryourcat.sharing

import android.net.Uri
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.UploadTask
import com.google.firebase.storage.ktx.storage
import com.google.firebase.auth.ktx.auth
import com.google.firebase.dynamiclinks.ShortDynamicLink
import com.google.firebase.dynamiclinks.ktx.*
import com.google.firebase.storage.FileDownloadTask
import io.reactivex.rxjava3.core.Single
import java.io.File
import java.io.IOException
import java.net.URL
import java.util.*

class FirebaseNetworkService: NetworkService {
    private val storage = Firebase.storage
    private val auth = Firebase.auth

    private fun makeAuthSingle(): Single<Unit> {
        return Single.create<Unit> { emitter ->
            auth.signInAnonymously()
                .addOnCompleteListener{ task ->
                    if (task.isSuccessful) {
                        emitter.onSuccess(Unit)
                    } else {
                        emitter.onError(IOException("OMG!"))
                    }
                }
        }
    }

    private fun makeDynamicLinkSingle(downloadUri: Uri): Single<URL> {
        return Single.create<URL> { emitter ->
            Firebase.dynamicLinks.shortLinkAsync(ShortDynamicLink.Suffix.SHORT) {
                link = downloadUri
                domainUriPrefix = "https://purryourcat.page.link"
                // Open links with this app on Android
                androidParameters {  }
                socialMetaTagParameters {
                    title = "Checkout my cat in Purr My Cat App"
                    description = "Sima cat"
                    imageUrl = Uri.parse("https://resizeimage.net/mypic/BKF3uoiKsC0UCnFV/4G86B/img_20200504_211406_hdr.jpg")
                }
            }.addOnSuccessListener { result ->
                emitter.onSuccess(URL(result.shortLink.toString()))
            }.addOnFailureListener {
                emitter.onError(IOException("OMG!"))
            }
        }

    }

    override fun makeDownloadObservable(url: URL, destDir: File): Single<File> {
        var downloadTask: FileDownloadTask? = null

        val downloadSingle = Single.create<File> { emitter ->
            val reference = storage.getReferenceFromUrl(url.toString())
            val file = File(destDir, reference.name)

            downloadTask = reference.getFile(file).apply {
                addOnSuccessListener {
                    emitter.onSuccess(file)
                }.addOnFailureListener {
                    emitter.onError(IOException("OMG!"))
                }
            }
        }.doOnDispose {
            downloadTask?.cancel()
        }

        return makeAuthSingle().flatMap{ downloadSingle }
    }

    override fun makeUploadObservable(file: File): Single<URL> {
        var uploadTask: UploadTask? = null

        val uploadSingle = Single.create<Uri> { emitter ->
            var uri = Uri.fromFile(file)
            val uuid = UUID.randomUUID().toString()
            val reference = storage.reference.child("archives/$uuid/${uri.lastPathSegment}")

            uploadTask = reference.putFile(uri).apply {
                addOnSuccessListener {
                    reference.downloadUrl.addOnSuccessListener {
                        emitter.onSuccess(it)
                    }
                }.addOnFailureListener {
                    emitter.onError(IOException("OMG!"))
                }
            }
        }.doOnDispose {
            uploadTask?.cancel()
        }

        return makeAuthSingle().flatMap{ uploadSingle }.flatMap{ makeDynamicLinkSingle(it) }
    }
}
//package com.sergsave.purryourcat.sharing
//
//import android.content.Context
//import android.util.Log
//import com.estmob.android.sendanywhere.sdk.ReceiveTask
//import com.estmob.android.sendanywhere.sdk.SendTask
//import com.estmob.android.sendanywhere.sdk.Task
//import com.sergsave.purryourcat.helpers.NetworkUtils
//import io.reactivex.rxjava3.core.Single
//import io.reactivex.rxjava3.core.SingleEmitter
//import java.io.File
//import java.io.IOException
//import java.net.URL
//
//private const val DEBUG_TAG = "SendAnywhereService"
//private fun logProgress(fileState: Task.FileState?) =
//    fileState?.let{ Log.d(DEBUG_TAG, "progress: " + it.transferSize * 100 / it.totalSize) }
//
//class SendAnywhereNetworkService(private val context: Context): NetworkService {
//
//    init {
//        val apiKey = "dfe91eb770456c0a269625ce8e0128ad2b4a5fb0"
//        Task.init(apiKey)
//    }
//
//    private val connectionError = IOException("No connection")
//
//    override fun makeUploadObservable(file: File): Single<URL> {
//        var task: SendTask? = null
//
//        return Single.create<URL> { emitter ->
//            if(NetworkUtils.isNetworkAvailable(context)) {
//                task = SendTask(context, arrayOf(file), true).also {
//                    initSendTask(it, emitter)
//                    it.start()
//                }
//            } else
//                emitter.onError(connectionError)
//        }.doOnDispose {
//            task?.cancel()
//            task = null
//        }
//    }
//
//    private fun initSendTask(task: SendTask, emitter: SingleEmitter<URL>) {
//        var link: URL? = null
//
//        val handleSuccess = { _link: URL -> emitter.onSuccess(_link) }
//        val handleError = { emitter.onError(IOException("Upload failed")) }
//
//        task.setOnTaskListener { state, detailedState, obj ->
//            when (state) {
//                Task.State.PREPARING -> {
//                    Log.d(DEBUG_TAG, "preparing")
//                    val key = obj as? String
//                    if (detailedState == SendTask.DetailedState.PREPARING_UPDATED_KEY && key != null)
//                        (task.getValue(Task.Value.LINK_URL) as? String)?.let { link = URL(it) }
//                }
//                Task.State.TRANSFERRING -> {
//                    logProgress(obj as? Task.FileState)
//                    // Progress not supported
//                }
//                Task.State.FINISHED -> {
//                    when (detailedState) {
//                        SendTask.DetailedState.FINISHED_SUCCESS ->
//                            link?.let { handleSuccess(it) } ?: run { handleError() }
//                        SendTask.DetailedState.FINISHED_ERROR -> handleError()
//                    }
//                }
//            }
//        }
//    }
//
//    override fun makeDownloadObservable(url: URL, destDir: File): Single<File> {
//        var task: ReceiveTask? = null
//
//        return Single.create<File> { emitter ->
//            if (NetworkUtils.isNetworkAvailable(context)) {
//                task = ReceiveTask(context, url.toString(), destDir).also {
//                    initReceiveTask(it, emitter)
//                    it.start()
//                }
//            } else
//                emitter.onError(connectionError)
//        }.doOnDispose {
//            task?.cancel()
//            task = null
//        }
//    }
//
//    private fun initReceiveTask(task: ReceiveTask, emitter: SingleEmitter<File>) {
//        var file: File? = null
//
//        val handleSuccess = { _file: File -> emitter.onSuccess(_file) }
//        val handleError = { emitter.onError(IOException("Download failed")) }
//
//        task.setOnTaskListener { state, detailedState, obj ->
//            when (state) {
//                Task.State.TRANSFERRING -> {
//                    val fileState = obj as? Task.FileState
//                    if (fileState != null)
//                        file = fileState.file.path?.let { File(it) }
//
//                    logProgress(fileState)
//                }
//                Task.State.FINISHED -> {
//                    when (detailedState) {
//                        SendTask.DetailedState.FINISHED_SUCCESS ->
//                            file?.let { handleSuccess(it) } ?: run { handleError() }
//                        SendTask.DetailedState.FINISHED_ERROR -> handleError()
//                    }
//                }
//            }
//        }
//    }
//}