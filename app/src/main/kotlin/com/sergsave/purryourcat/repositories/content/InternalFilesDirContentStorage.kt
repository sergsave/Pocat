package com.sergsave.purryourcat.repositories.content

import android.content.Context
import android.net.Uri
import com.sergsave.purryourcat.helpers.FileUtils
import java.io.File
import java.util.UUID

class InternalFilesDirContentStorage(private val context: Context): ContentStorage {

    override fun store(sourceContent: Uri, fileName: String): Uri? {
        val inputStream = context.contentResolver.openInputStream(sourceContent)
        if(inputStream == null)
            return null

        val dir = createUniqueDir()
        dir.mkdirs()

        val file = File(dir, fileName)
        FileUtils.copyStreamToFile(inputStream, file)
        inputStream.close()
        return Uri.fromFile(file)
    }

    override fun store(sourceContent: Uri): Uri? {
        val sourceName = FileUtils.getContentFileName(context, sourceContent)
        return sourceName?.let { store(sourceContent, it) }
    }

    override fun read(): List<Uri>? {
        val files = dir().walk().filter{ it.isDirectory.not() }.toList()
        return files.map { Uri.fromFile(it) }
    }

    override fun remove(uri: Uri): Boolean {
        // Only file path uri contains in this storage type
        val path = uri.path
        if(path == null || path.startsWith(dir().path).not())
            return false

        val dir = File(path).parentFile
        return dir?.deleteRecursively() ?: false
    }

    private fun dir(): File {
        return File(context.filesDir, "content")
    }

    private fun createUniqueDir(): File {
        val uuid = UUID.randomUUID().toString()
        return File(dir(), uuid)
    }
}