package com.sergsave.pocat.helpers

import android.Manifest
import android.provider.MediaStore
import android.content.ContentResolver
import android.content.ContentValues
import android.content.Context
import android.content.res.Resources
import android.database.Cursor
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.OpenableColumns
import android.util.TypedValue
import androidx.annotation.RequiresApi
import androidx.annotation.RequiresPermission
import androidx.core.content.FileProvider
import timber.log.Timber
import java.io.IOException
import java.io.*
import java.text.SimpleDateFormat
import java.util.*
import java.util.zip.ZipEntry
import java.util.zip.ZipInputStream
import java.util.zip.ZipOutputStream

object FileUtils {
    // https://stackoverflow.com/questions/10854211/android-store-inputstream-in-file
    @Throws(IOException::class)
    fun copyStreamToFile(inputStream: InputStream, outputFile: File) {
        inputStream.use { input ->
            val outputStream = FileOutputStream(outputFile)
            outputStream.use { output ->
                val buffer = ByteArray(4 * 1024) // buffer size
                while (true) {
                    val byteCount = input.read(buffer)
                    if (byteCount < 0) break
                    output.write(buffer, 0, byteCount)
                }
                output.flush()
            }
        }
    }

    fun uriOfResource(context: Context, resId: Int): Uri {
        return Uri.Builder()
            .scheme(ContentResolver.SCHEME_ANDROID_RESOURCE)
            .authority(context.resources.getResourcePackageName(resId))
            .appendPath(context.resources.getResourceTypeName(resId))
            .appendPath(context.resources.getResourceEntryName(resId))
            .build()
    }

    private fun resourceIdFromUri(context: Context, uri: Uri): Int? {
        if (!uri.scheme.equals(ContentResolver.SCHEME_ANDROID_RESOURCE))
            return null

        // Call of "getIdentifier" can be long.
        // But when using the id of the resource when composing the uri,
        // there may be problems with caching images.
        val type = uri.pathSegments[0]
        val name = uri.pathSegments[1]
        val packageName = uri.authority
        return context.resources.getIdentifier(name, type, packageName).let {
            if (it == 0) null else it
        }
    }

    private fun getContentResolverQuery(context: Context, contentUri: Uri): Cursor? {
        return if (contentUri.scheme.equals(ContentResolver.SCHEME_CONTENT))
            context.contentResolver.query(contentUri, null, null, null, null)
        else
            null
    }

    private fun getResourceFileNameWithExtension(context: Context, resId: Int): String? {
        val typedValue = TypedValue()

        try {
            context.resources.getValue(resId, typedValue, true)
        } catch(e: Resources.NotFoundException) {
            return null
        }

        return cutLastSegment(typedValue.string.toString())
    }

    private fun getResourceSize(context: Context, resId: Int): Long {
        return try {
            context.resources.openRawResource(resId).use { it.available().toLong() }
        } catch(e: Exception) {
            when(e) {
                is Resources.NotFoundException, is IOException -> return 0L
                else -> throw e
            }
        }
    }

    private fun cutLastSegment(path: String): String {
        val cut = path.lastIndexOf('/')
        if (cut != -1)
            return path.substring(cut + 1)
        return path
    }

    // Support schemes: content, file and android.resource (should be obtained from "uriOfResource")
    fun resolveContentFileName(context: Context, contentUri: Uri): String? {
        val cursor = getContentResolverQuery(context, contentUri)
        cursor?.use {
            if (it.moveToFirst()) {
                val indexName = it.getColumnIndex(OpenableColumns.DISPLAY_NAME)
                it.getString(indexName)?.let { name -> return name }

                // Using a deprecated constant. On files from Redmi voice recorders, DISPLAY_NAME returns null
                @Suppress("DEPRECATION")
                val indexData = it.getColumnIndex(MediaStore.MediaColumns.DATA)
                it.getString(indexData)?.let { path -> return cutLastSegment(path) }
            }
        }

        val resourceId = resourceIdFromUri(context, contentUri)
        resourceId?.let { id ->
            getResourceFileNameWithExtension(context, id)?.let { return it }
        }

        return contentUri.lastPathSegment
    }

    // Support schemes: content, file and android.resource (should be obtained from "uriOfResource")
    fun resolveContentFileSize(context: Context, contentUri: Uri): Long {
        val cursor = getContentResolverQuery(context, contentUri)
        cursor?.use {
            if (it.moveToFirst())
                return it.getLong(it.getColumnIndex(OpenableColumns.SIZE))
        }

        val resourceId = resourceIdFromUri(context, contentUri)
        resourceId?.let { id ->
            getResourceSize(context, id).let { if (it != 0L) return it }
        }

        return contentUri.path?.let { File(it).length() } ?: 0
    }

    // https://stackoverflow.com/questions/25562262/how-to-compress-files-into-zip-folder-in-android
    fun zip(context: Context, content: Array<Uri>, zipFileName: String) {
        val bufferSize = 80000
        try {
            var origin: BufferedInputStream?
            val dest = FileOutputStream(zipFileName)
            val out = ZipOutputStream(
                BufferedOutputStream(
                    dest
                )
            )
            val data = ByteArray(bufferSize)
            content.forEach { uri ->
                val input = context.contentResolver.openInputStream(uri)
                origin = BufferedInputStream(input, bufferSize)
                val entry = ZipEntry(resolveContentFileName(context, uri))
                out.putNextEntry(entry)
                var count = 0
                while (origin?.read(data, 0, bufferSize)?.also({ count = it }) != -1) {
                    out.write(data, 0, count)
                }
                origin?.close()
            }
            out.close()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    // https://stackoverflow.com/questions/4504291/how-to-speed-up-unzipping-time-in-java-android
    fun unzip(zipPath: String, targetLocation: String) {

        val dirChecker = { dir: String ->
            val f = File(dir)
            if (!f.isDirectory) {
                f.mkdirs()
            }
        }

        //create target location folder if not exist
        dirChecker(targetLocation)
        try {
            val fin = FileInputStream(zipPath)
            val zin = ZipInputStream(fin)
            var ze: ZipEntry?
            while (zin.nextEntry.also { ze = it } != null) {

                //create dir if required while unzipping
                if (ze!!.isDirectory) {
                    dirChecker(ze!!.name)
                } else {
                    val fout = FileOutputStream(targetLocation + "/" + ze!!.name)
                    val bufout = BufferedOutputStream(fout)
                    val buffer = ByteArray(1024)
                    var read: Int
                    while (zin.read(buffer).also { read = it } != -1) {
                        bufout.write(buffer, 0, read)
                    }

                    zin.closeEntry()
                    bufout.close()
                    fout.close()
                }
            }
            zin.close()
        } catch (e: java.lang.Exception) {
            println(e)
        }
    }

    private fun generateImageName(): String {
        val timestamp = SimpleDateFormat("yyyyMMdd_HHmmss_SSS", Locale.US).format(Date())
        return "IMG_$timestamp.jpg"
    }

    private fun prepareExternalStorageImageUri(context: Context,
                                               standardDir: String,
                                               subDir: String,
                                               providerAuthority: String): Uri? {
        // This function is used only for old android versions
        @Suppress("DEPRECATION")
        val storageDir = Environment.getExternalStoragePublicDirectory(standardDir)

        val targetDir = File(storageDir, subDir)
        // TODO: To background?
        targetDir.mkdirs()
        val file = File(targetDir, generateImageName())

        return FileProvider.getUriForFile(context, providerAuthority, file)
    }

    @RequiresApi(Build.VERSION_CODES.Q)
    private fun prepareMediaStoreImageUri(context: Context,
                                          standardDir: String,
                                          subDir: String): Uri? {
        val values = ContentValues().apply {
            put(MediaStore.MediaColumns.DISPLAY_NAME, generateImageName())
            put(MediaStore.MediaColumns.MIME_TYPE, "image/jpeg")
            put(MediaStore.MediaColumns.RELATIVE_PATH, "$standardDir/$subDir")
        }
        return context.contentResolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values)
    }

    // @param standardDir - see Environment.STANDARD_DIRECTORIES
    // To avoid FileUriExposedException this function use FileProvider.
    // FileProvider should provide path "$standardDir/$subPath" with "external-path" type.
    @RequiresPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE)
    fun provideContentUriInPublicStorage(context: Context,
                                         standardDir: String,
                                         subDir: String,
                                         fileProviderAuthority: String): Uri? {
        return if (Build.VERSION.SDK_INT < Build.VERSION_CODES.Q)
            prepareExternalStorageImageUri(context, standardDir, subDir, fileProviderAuthority)
        else
            prepareMediaStoreImageUri(context, standardDir, subDir)
    }

    // Clear content on uri returned from "provideContentUriInPublicStorage"
    @RequiresPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE)
    fun releaseContentUri(context: Context, uri: Uri): Boolean {
        // Nothing to release for lower versions
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.Q)
            return true

        return try {
            context.contentResolver.delete(uri, null, null) != 0
        } catch(e: SecurityException) {
            Timber.e(e)
            false
        }
    }
}