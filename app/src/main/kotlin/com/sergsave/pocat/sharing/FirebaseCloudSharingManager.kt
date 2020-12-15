package com.sergsave.pocat.sharing

import android.content.Context
import android.content.Intent
import android.net.Uri
import com.google.firebase.auth.ktx.auth
import com.google.firebase.dynamiclinks.ShortDynamicLink
import com.google.firebase.dynamiclinks.ktx.androidParameters
import com.google.firebase.dynamiclinks.ktx.dynamicLinks
import com.google.firebase.dynamiclinks.ktx.shortLinkAsync
import com.google.firebase.dynamiclinks.ktx.socialMetaTagParameters
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.FileDownloadTask
import com.google.firebase.storage.StorageException
import com.google.firebase.storage.UploadTask
import com.google.firebase.storage.ktx.storage
import com.sergsave.pocat.BuildConfig
import com.sergsave.pocat.R
import com.sergsave.pocat.helpers.ImageUtils
import com.sergsave.pocat.helpers.NetworkUtils
import com.sergsave.pocat.helpers.createIntentChooser
import io.reactivex.Completable
import io.reactivex.Single
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.rxkotlin.zipWith
import io.reactivex.schedulers.Schedulers
import io.reactivex.subjects.BehaviorSubject
import java.io.File
import java.io.IOException
import java.util.*

class FirebaseCloudSharingManager(
    private val context: Context,
    private val packer: DataPacker
): WebSharingManager {
    private val cacheDir = File(context.cacheDir, "sharing")
    private val cleanupInProcess = BehaviorSubject.createDefault(false)

    private fun createTempDir(): File {
        cacheDir.mkdirs()
        return createTempDir(suffix = null, directory = cacheDir)
    }

    override fun cleanup(): Completable {
        return Completable.fromCallable {
            cacheDir.deleteRecursively()
            cacheDir.mkdirs()
        }
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .doOnSubscribe { cleanupInProcess.onNext(true) }
            .doOnDispose { cleanupInProcess.onNext(false) }
            .doOnComplete { cleanupInProcess.onNext(false) }
    }

    private fun waitCleanupFinish(): Completable {
        return Completable.fromSingle(
            cleanupInProcess.filter { !it }.first(false)
        )
    }

    override fun upload(pack: Pack): Single<Intent> {
        val uploadData = { _pack: Pack, tempDir: File ->
            packer.pack(_pack, File(tempDir, "pack")).flatMap { uploadFile(it, "data") }
        }

        val uploadPreview = { photo: Uri?, tempDir: File ->
            if (photo == null)
                Single.error(IllegalArgumentException("Null photo uri"))
            else
                resizePreview(photo, tempDir, context).flatMap { uploadFile(it, "preview") }
        }

        return waitCleanupFinish()
            .andThen(Single.fromCallable { createTempDir() })
            .flatMap { temp ->
                checkConnection(context)
                    .andThen(uploadData(pack, temp))
                    .zipWith(uploadPreview(pack.cat.photoUri, temp).onErrorReturn { Uri.EMPTY })
            }
            .flatMap {
                val dataLink = it.first
                val previewLink = if (it.second == Uri.EMPTY) null else it.second
                val appName = context.getString(R.string.app_name)
                val header = context.getString(R.string.sharing_text, appName)
                createDynamicLink(dataLink, header, previewLink, pack.cat.name)
            }
            .map { makeIntent(context, it) }
    }

    override fun download(intent: Intent): Single<Pack> {
        return waitCleanupFinish()
            .andThen(extractDownloadLink(intent))
            .flatMap { link ->
                val temp = createTempDir()
                checkConnection(context)
                    .andThen(downloadFile(link, temp))
                    .flatMap { packer.unpack(it, File(temp, "unpack")) }
            }
    }
}

private fun checkConnection(context: Context): Completable {
    return Completable.create { emitter ->
        if(NetworkUtils.isNetworkAvailable(context))
            emitter.onComplete()
        else
            emitter.onError(WebSharingManager.NoConnectionException("Network not available"))
    }
}

private fun uploadFile(file: File, folderName: String): Single<Uri> {
    var uploadTask: UploadTask? = null

    val uploadSingle = Single.create<Uri> { emitter ->
        Firebase.storage.maxUploadRetryTimeMillis = 5000

        val uri = Uri.fromFile(file)
        val uuid = UUID.randomUUID().toString()
        val reference = Firebase.storage.reference.child("$folderName/$uuid/${uri.lastPathSegment}")

        uploadTask = reference.putFile(uri).apply {
            addOnSuccessListener {
                reference.downloadUrl.addOnSuccessListener {
                    emitter.onSuccess(it)
                }
            }.addOnFailureListener {
                val errorCode = (it as? StorageException)?.errorCode
                if (errorCode == StorageException.ERROR_RETRY_LIMIT_EXCEEDED)
                    emitter.onError(WebSharingManager.NoConnectionException("Retry limit"))
                else
                    emitter.onError(IOException("Uploading error"))
            }
        }
    }.doOnDispose {
        uploadTask?.cancel()
    }

    return auth().andThen(uploadSingle)
}

private fun resizePreview(previewUri: Uri, tempDir: File, context: Context): Single<File> {
    val resizedFile = File(tempDir, "preview.jpg")
    // Size for firebase deeplink preview should be greater than 300 x 200
    val width = 640
    val height = 360

    return Single.create { emitter ->
        if(tempDir.exists().not()) tempDir.mkdirs()

        ImageUtils.loadInto(context, previewUri, resizedFile, width, height) { res ->
            if(res)
                emitter.onSuccess(resizedFile)
            else
                emitter.onError(IOException("Image load error"))
        }
    }
}

private fun createDynamicLink(downloadLink: Uri,
                              header: String?,
                              previewLink: Uri?,
                              catName: String?): Single<Uri> {
    val error = IOException("Error in deeplink create")
    return Single.create<Uri> { emitter ->
        Firebase.dynamicLinks.shortLinkAsync(ShortDynamicLink.Suffix.SHORT) {
            link = downloadLink
            domainUriPrefix = BuildConfig.DYNAMIC_LINK_DOMAIN
            androidParameters {  }
            socialMetaTagParameters {
                header?.let { title = it }
                catName?.let { description = it }
                previewLink?.let { imageUrl = it }
            }
        }.addOnSuccessListener { result ->
            result.shortLink?.let { emitter.onSuccess(it) } ?: emitter.onError(error)
        }.addOnFailureListener {
            emitter.onError(error)
        }
    }
}

private fun makeIntent(context: Context, link: Uri): Intent {
    val intent = Intent(Intent.ACTION_SEND).apply {
        val text = link.toString()
        putExtra(Intent.EXTRA_TEXT, text)
        type = "text/plain"
    }
    return createIntentChooser(listOf(intent), context.getString(R.string.send_data)) ?: intent
}

private fun extractDownloadLink(intent: Intent): Single<Uri> {
    val error = WebSharingManager.InvalidLinkException("Extract link error")
    return Single.create<Uri> { emitter ->
        Firebase.dynamicLinks
            .getDynamicLink(intent)
            .addOnSuccessListener { pendingLinkData ->
                // Hack. Firebase allow exract uri with "getDynamicLink" only once.
                // Follow-up call of "getDynamicLink" return null.
                // In this case extract uri form intent directly
                val link = if (pendingLinkData == null)
                    intent.data
                else
                    pendingLinkData.link

                link?.let { emitter.onSuccess(it) } ?: emitter.onError(error)
            }
            .addOnFailureListener { emitter.onError(error) }
    }
}

private fun downloadFile(uri: Uri, dir: File): Single<File> {
    var downloadTask: FileDownloadTask? = null

    val downloadSingle = Single.create<File> { emitter ->
        if(dir.exists().not()) dir.mkdirs()

        Firebase.storage.maxDownloadRetryTimeMillis = 5000

        val reference = Firebase.storage.getReferenceFromUrl(uri.toString())
        val file = File(dir, reference.name)

        downloadTask = reference.getFile(file).apply {
            addOnSuccessListener {
                emitter.onSuccess(file)
            }.addOnFailureListener {
                val error = when ((it as? StorageException)?.errorCode) {
                    StorageException.ERROR_RETRY_LIMIT_EXCEEDED ->
                        WebSharingManager.NoConnectionException("Retry limit")
                    StorageException.ERROR_OBJECT_NOT_FOUND,
                    StorageException.ERROR_BUCKET_NOT_FOUND ->
                        WebSharingManager.InvalidLinkException("Object not found")
                    else -> IOException("Downloading error")
                }
                emitter.onError(error)
            }
        }
    }.doOnDispose {
        downloadTask?.cancel()
    }

    return auth().andThen(downloadSingle)
}

private fun auth(): Completable {
    return Completable.create { emitter ->
        Firebase.auth.signInAnonymously()
            .addOnCompleteListener{ task ->
                if (task.isSuccessful) {
                    emitter.onComplete()
                } else {
                    emitter.onError(IOException("Auth error"))
                }
            }
    }
}
