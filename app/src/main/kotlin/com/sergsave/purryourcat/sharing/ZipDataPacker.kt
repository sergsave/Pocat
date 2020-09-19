package com.sergsave.purryourcat.sharing

import android.content.Context
import android.net.Uri
import com.sergsave.purryourcat.helpers.FileUtils
import com.sergsave.purryourcat.models.extractContent
import com.sergsave.purryourcat.models.withUpdatedContent
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers
import io.reactivex.rxjava3.core.Single
import io.reactivex.rxjava3.schedulers.Schedulers
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

private class ZipDataPacker(tempDir: File, private val context: Context): DataPacker(tempDir) {

    private fun packSync(pack: Pack): File? {
        if(tempDir.exists().not()) tempDir.mkdirs()

        val name = pack.cat.name ?: "Cat"
        val zipPath = tempDir.path + "/$name.zip"

        val contentUris = pack.cat.extractContent().map { it }
        val withFixedUris = pack.cat.withUpdatedContent { uri ->
            val _name = uri?.let { FileUtils.getContentFileName(context, it) }
            _name?.let { Uri.parse(it) }
        }

        val bundleFile = File(tempDir, BUNDLE_FILE_NAME)
        savePackToBundleFile(Pack(withFixedUris), bundleFile)

        val zipped = contentUris + Uri.fromFile(bundleFile)

        FileUtils.zip(context, zipped.toTypedArray(), zipPath)
        return File(zipPath)
    }

    private fun unpackSync(file: File): Pack? {
        if(tempDir.exists().not()) tempDir.mkdirs()

        if(file.exists().not())
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

    private val error = Exception("Packing error")

    override fun pack(pack: Pack): Single<File> {
        return Single.create<File> { emitter ->
            val file = packSync(pack)
            if(file != null) emitter.onSuccess(file) else emitter.onError(error)
        }
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
    }

    override fun unpack(file: File): Single<Pack> {
        return Single.create<Pack> { emitter ->
            val pack = unpackSync(file)
            if(pack != null) emitter.onSuccess(pack) else emitter.onError(error)
        }
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
    }
}

class ZipDataPackerFactory(private val context: Context): DataPackerFactory {
    override fun make(tempDir: File): DataPacker {
        return ZipDataPacker(tempDir, context)
    }
}