package com.sergsave.purryourcat.helpers

import android.provider.MediaStore
import android.content.ContentResolver
import android.content.Context
import android.database.Cursor
import android.net.Uri
import android.provider.OpenableColumns
import android.util.TypedValue
import java.io.*
import java.util.zip.ZipEntry
import java.util.zip.ZipInputStream
import java.util.zip.ZipOutputStream

object FileUtils {
    // https://stackoverflow.com/questions/10854211/android-store-inputstream-in-file
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

    fun uriOfResource(resId: Int, context: Context): Uri {
        return Uri.Builder()
            .scheme(ContentResolver.SCHEME_ANDROID_RESOURCE)
            .authority(context.resources.getResourcePackageName(resId))
            .appendPath(resId.toString())
            .build()
    }

    fun resourceIdFromUri(uri: Uri): Int? {
        return if (uri.scheme.equals(ContentResolver.SCHEME_ANDROID_RESOURCE))
            uri.lastPathSegment?.toIntOrNull()
        else
            null
    }

    private fun getContentResolverQuery(context: Context, contentUri: Uri): Cursor? {
        return if (contentUri.scheme.equals(ContentResolver.SCHEME_CONTENT))
            context.contentResolver.query(contentUri, null, null, null, null)
        else
            null
    }

    private fun getResourceName(resId: Int, context: Context): String? {
        val typedValue = TypedValue()

        try {
            context.resources.getValue(resId, typedValue, true)
        } catch(e: Exception) {
            return null
        }

        return cutLastSegment(typedValue.string.toString())
    }

    private fun getResourceSize(resId: Int, context: Context): Long {
        val size = try {
            context.resources.openRawResource(resId).use { it.available().toLong() }
        } catch(e: Exception) {
            0L
        }
        return size
    }

    private fun cutLastSegment(path: String): String {
        val cut = path.lastIndexOf('/')
        if (cut != -1)
            return path.substring(cut + 1)
        return path
    }

    // Support schemes: content, file and android.resource (should be obtained from "uriOfResource")
    fun getContentFileName(context: Context, contentUri: Uri): String? {
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

        val resourceId = resourceIdFromUri(contentUri)
        resourceId?.let { id ->
            getResourceName(id, context)?.let { return it }
        }

        return contentUri.lastPathSegment
    }

    // Support schemes: content, file and android.resource (should be obtained from "uriOfResource")
    fun getContentFileSize(context: Context, contentUri: Uri): Long {
        val cursor = getContentResolverQuery(context, contentUri)
        cursor?.use {
            if (it.moveToFirst())
                return it.getLong(it.getColumnIndex(OpenableColumns.SIZE))
        }

        val resourceId = resourceIdFromUri(contentUri)
        resourceId?.let { id ->
            getResourceSize(id, context).let { if (it != 0L) return it }
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
                val entry = ZipEntry(getContentFileName(context, uri))
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
            if (f.isDirectory.not()) {
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
}