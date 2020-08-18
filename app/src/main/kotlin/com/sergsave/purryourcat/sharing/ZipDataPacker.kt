package com.sergsave.purryourcat.sharing

import android.content.Context
import android.net.Uri
import com.sergsave.purryourcat.helpers.BundleUtils
import com.sergsave.purryourcat.helpers.FileUtils
import com.sergsave.purryourcat.models.extractContent
import com.sergsave.purryourcat.models.withUpdatedContent
import java.io.File

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

        val bundle = Bundle(Bundle.ACTUAL_VERSION, Pack(withFixedUris))
        val bundleFile = BundleUtils.toJsonFile(bundle, dir)
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

        val bundleFile = File(tempDir, BundleUtils.JSON_FILE_NAME)
        val bundle = BundleUtils.fromJsonFile<Bundle>(bundleFile)

        if (bundle == null || bundle.version > Bundle.ACTUAL_VERSION)
            return null

        val withFixedUris = bundle.pack.cat.withUpdatedContent { uri ->
            uri?.let { Uri.fromFile(File(tempDir, it.toString())) }
        }
        return Pack(withFixedUris)
    }
}