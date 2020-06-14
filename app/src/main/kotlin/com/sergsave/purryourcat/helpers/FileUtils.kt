package com.sergsave.purryourcat.helpers

import android.content.Context
import android.net.Uri
import android.provider.OpenableColumns
import java.io.File
import java.io.FileOutputStream
import java.io.InputStream

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
}