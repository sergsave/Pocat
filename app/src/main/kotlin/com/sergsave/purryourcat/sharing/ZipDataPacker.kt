package com.sergsave.purryourcat.sharing

import android.content.Context
import android.net.Uri
import com.sergsave.purryourcat.helpers.FileUtils
import com.sergsave.purryourcat.models.extractContent
import com.sergsave.purryourcat.models.withUpdatedContent
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonConfiguration
import kotlinx.serialization.json.JsonLiteral
import kotlinx.serialization.json.JsonObject
import java.io.File

private const val BUNDLE_FILE_NAME = "bundle.json"
private const val BUNDLE_ACTUAL_VERSION = 1
private const val VERSION_KEY = "version"
private const val PACK_KEY = "pack"

private fun savePackToBundleFile(pack: Pack, file: File) {
    if(file.exists())
        file.delete()

    val jsonMap = mapOf(
        VERSION_KEY to JsonLiteral(BUNDLE_ACTUAL_VERSION),
        PACK_KEY to Json(JsonConfiguration.Stable).toJson(pack)
    )

    file.writeText(JsonObject(jsonMap).toString())
}

private fun readPackFromBundleFile(file: File): Pack? {
    if(file.exists().not())
        return null

    val text = file.readText()

    try {
        val jsoner = Json(JsonConfiguration.Stable)
        val jsonObject = jsoner.parseJson(text).jsonObject

        val version = jsonObject.getPrimitive(VERSION_KEY).int
        if(version > BUNDLE_ACTUAL_VERSION)
            return null

        val packJsonObj = jsonObject.getObject(PACK_KEY)
        if(version < BUNDLE_ACTUAL_VERSION) {
            // adapt pack here
        }

        return jsoner.fromJson(packJsonObj)
    }
    catch (e: Exception) {
        return null
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