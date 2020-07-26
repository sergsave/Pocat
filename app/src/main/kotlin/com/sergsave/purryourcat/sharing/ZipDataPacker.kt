package com.sergsave.purryourcat.sharing

import android.content.Context
import android.net.Uri
import com.sergsave.purryourcat.BuildConfig
import com.sergsave.purryourcat.helpers.BundleUtils
import com.sergsave.purryourcat.helpers.FileUtils
import com.sergsave.purryourcat.models.CatData
import com.sergsave.purryourcat.models.extractContent
import com.sergsave.purryourcat.models.withUpdatedContent
import java.io.File

class ZipDataPacker(private val context: Context, private val workDir: File): IDataPacker {

    override fun pack(pack: Pack): File? {
        ensureWorkDir()

        val name = pack.cat.name ?: "Cat"
        val zipPath = workDir.path + "/$name.zip"

        val contentUris = pack.cat.extractContent().mapNotNull { it }
        val withFixedUris = pack.cat.withUpdatedContent { uri ->
            val _name = uri?.let { FileUtils.getContentFileName(context, it) }
            _name?.let { Uri.parse(it) }
        }

        val bundle = Bundle(Bundle.ACTUAL_VERSION, Pack(withFixedUris))
        val bundleFile = BundleUtils.toJsonFile(bundle, workDir)
        val zipped = contentUris + Uri.fromFile(bundleFile)

        FileUtils.zip(context, zipped.toTypedArray(), zipPath)
        return File(zipPath)
    }

    override fun unpack(file: File): Pack? {
        ensureWorkDir()
        FileUtils.unzip(file.path, workDir.path)

        val bundleFile = File(workDir, BundleUtils.JSON_FILE_NAME)
        val bundle = BundleUtils.fromJsonFile<Bundle>(bundleFile)

        if (bundle == null || bundle.version > Bundle.ACTUAL_VERSION)
            return null

        val withFixedUris = bundle.pack.cat.withUpdatedContent { uri ->
            uri?.let { Uri.fromFile(File(workDir, it.toString())) }
        }
        return Pack(withFixedUris)
    }

    private fun ensureWorkDir() { if(workDir.exists().not()) workDir.mkdirs() }
}