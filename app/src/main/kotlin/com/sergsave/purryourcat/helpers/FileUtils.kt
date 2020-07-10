package com.sergsave.purryourcat.helpers

import android.content.Context
import android.net.Uri
import android.provider.OpenableColumns
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

    // https://stackoverflow.com/questions/5568874/how-to-extract-the-file-name-from-uri-returned-from-intent-action-get-content
    fun getContentFileName(context: Context, contentUri: Uri): String? {
        var result: String? = null
        if (contentUri.getScheme().equals("content")) {
            val cursor = context.getContentResolver().query(contentUri, null, null, null, null)
            try {
                if (cursor != null && cursor.moveToFirst()) {
                    result = cursor.getString(cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME))
                }
            } finally {
                cursor?.close()
            }
        }
        if (result == null) {
            result = contentUri.getPath()
            val cut = result?.lastIndexOf('/')
            if (cut != null && cut != -1) {
                result = result?.substring(cut + 1)
            }
        }
        return result
    }

    // https://stackoverflow.com/questions/25562262/how-to-compress-files-into-zip-folder-in-android
    fun zip(files: Array<String>, zipFileName: String) {
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
            for (i in files.indices) {
                val fi = FileInputStream(files[i])
                origin = BufferedInputStream(fi, bufferSize)
                val entry =
                    ZipEntry(files[i].substring(files[i].lastIndexOf("/") + 1))
                out.putNextEntry(entry)
                var count = 0
                while (origin.read(data, 0, bufferSize).also({ count = it }) != -1) {
                    out.write(data, 0, count)
                }
                origin.close()
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
            if (f.isDirectory().not()) {
                f.mkdirs();
            }
        }

        //create target location folder if not exist
        dirChecker(targetLocation)
        try {
            val fin = FileInputStream(zipPath)
            val zin = ZipInputStream(fin)
            var ze: ZipEntry? = null
            while (zin.getNextEntry().also({ ze = it }) != null) {

                //create dir if required while unzipping
                if (ze!!.isDirectory) {
                    dirChecker(ze!!.name)
                } else {
                    val fout = FileOutputStream(targetLocation + "/" + ze!!.name)
                    val bufout = BufferedOutputStream(fout)
                    val buffer = ByteArray(1024)
                    var read = 0
                    while (zin.read(buffer).also({ read = it }) != -1) {
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