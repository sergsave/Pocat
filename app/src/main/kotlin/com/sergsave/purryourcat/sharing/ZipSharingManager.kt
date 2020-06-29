package com.sergsave.purryourcat.sharing

import android.content.Context
import android.net.Uri
import androidx.core.content.FileProvider
import com.sergsave.purryourcat.BuildConfig
import com.sergsave.purryourcat.helpers.FileUtils
import com.sergsave.purryourcat.models.*
import kotlinx.serialization.json.*
import kotlinx.serialization.*
import java.io.File

@Serializable
private data class Bundle(val version: Int, val data: CatData)

class ZipSharingManager(private val context: Context,
                        private val extensionWithoutDot: String): ISharingManager {

    private val jsonObj = Json(JsonConfiguration.Stable)

    init {
        // clear useless cache
        if(dir().exists()) dir().deleteRecursively()
    }

    override fun prepareSharingUri(catData: CatData?): Uri? {
        if(catData == null)
            return null

        dir().mkdirs()

        val name = catData.name ?: "Cat"
        val zipPath = dir().path + "/$name.$extensionWithoutDot"

        val contentPaths = catData.extractContent().mapNotNull { it.path }
        val paths = contentPaths + createDescFile(catData).path

        FileUtils.zip(paths.toTypedArray(), zipPath)
        return FileProvider.getUriForFile(context, BuildConfig.APPLICATION_ID, File(zipPath))
    }

    private fun createDescFile(catData: CatData): File {
        val updated = catData.withUpdatedContent { uri ->
            uri?.lastPathSegment?.let { Uri.parse(it) }
        }

        val descFile = File(dir(), DESC_FILE_NAME)
        if(descFile.exists())
            descFile.delete()

        val json = jsonObj.stringify(Bundle.serializer(), Bundle(BUNDLE_ACTUAL_VERSION, updated))
        descFile.writeText(json)

        return descFile
    }

    override fun extractFromSharingUri(uri: Uri?): CatData? {
        if(uri == null)
            return null

        val inputStream = context.contentResolver.openInputStream(uri)
        if(inputStream == null)
            return null

        FileUtils.unzip(inputStream, dir().path)
        inputStream.close()

        return parseDescFile(File(dir(), DESC_FILE_NAME))
    }

    private fun parseDescFile(file: File): CatData? {
        if(file.exists().not())
            return null

        try {
            val bundle = jsonObj.parse(Bundle.serializer(), file.readText())

            if (bundle.version > BUNDLE_ACTUAL_VERSION)
                return null

            return bundle.data.withUpdatedContent { uri ->
                uri?.let { Uri.fromFile(File(dir(), it.toString())) }
            }
        }
        catch(e: Exception) {
            return null
        }
    }

    override fun mimeType() = "application/zip"

    private fun dir() = File(context.cacheDir, "sharing")

    companion object {
        private val DESC_FILE_NAME = "desc.json"
        private val BUNDLE_ACTUAL_VERSION = 1
    }
}