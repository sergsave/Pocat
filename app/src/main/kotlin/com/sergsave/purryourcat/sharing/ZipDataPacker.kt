package com.sergsave.purryourcat.sharing

import android.content.Context
import android.net.Uri
import com.sergsave.purryourcat.helpers.FileUtils
import com.sergsave.purryourcat.models.extractContent
import com.sergsave.purryourcat.models.withUpdatedContent
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonConfiguration
import java.io.File
import java.lang.Exception

@Serializable
private data class Bundle(val version: Int, val pack: Pack) {
    companion object { const val ACTUAL_VERSION = 1 }
}

private const val BUNDLE_FILE_NAME = "bundle.json"

private fun savePackToBundleFile(pack: Pack, file: File) {
    if(file.exists())
        file.delete()

    val bundle = Bundle(Bundle.ACTUAL_VERSION, pack)
    file.writeText(Json(JsonConfiguration.Stable).stringify(Bundle.serializer(), bundle))
}

private fun readPackFromBundleFile(file: File): Pack? {
    if(file.exists().not())
        return null

    val text = file.readText()

    return try {
        val bundle = Json(JsonConfiguration.Stable).parse(Bundle.serializer(), text)
        if(bundle.version > Bundle.ACTUAL_VERSION)
            null
        else
            bundle.pack
    }
    catch (e: Exception) {
        null
    }
}

class ZipDataPacker(private val context: Context): DataPacker {

    override fun pack(pack: Pack, dir: File): File? {
        if(dir.exists().not()) dir.mkdirs()

        val name = pack.cat.name ?: "Cat"
        val zipPath = dir.path + "/$name.zip"

        val contentUris = pack.cat.extractContent().map { it }
        val withFixedUris = pack.cat.withUpdatedContent { uri ->
            val _name = uri?.let { FileUtils.getContentFileName(context, it) }
            _name?.let { Uri.parse(it) }
        }

        val bundleFile = File(dir, BUNDLE_FILE_NAME)
        savePackToBundleFile(Pack(withFixedUris), bundleFile)

        val zipped = contentUris + Uri.fromFile(bundleFile)

        FileUtils.zip(context, zipped.toTypedArray(), zipPath)
        return File(zipPath)
    }

    override fun unpack(file: File): Pack? {
        if(file.exists().not())
            return null

        val tempDir = file.parentFile
        if(tempDir == null)
            return null

        FileUtils.unzip(file.path, tempDir.path)

        val bundleFile = File(tempDir, BUNDLE_FILE_NAME)
        val pack = readPackFromBundleFile(bundleFile)
        if(pack == null)
            return null

        val withFixedUris = pack.cat.withUpdatedContent { uri ->
            uri?.let { Uri.fromFile(File(tempDir, it.toString())) }
        }
        return Pack(withFixedUris)
    }
}