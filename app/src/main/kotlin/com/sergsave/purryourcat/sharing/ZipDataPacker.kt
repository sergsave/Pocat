package com.sergsave.purryourcat.sharing

import android.net.Uri
import com.sergsave.purryourcat.BuildConfig
import com.sergsave.purryourcat.helpers.BundleUtils
import com.sergsave.purryourcat.helpers.FileUtils
import com.sergsave.purryourcat.models.CatData
import com.sergsave.purryourcat.models.extractContent
import com.sergsave.purryourcat.models.withUpdatedContent
import java.io.File

class ZipDataPacker(private val workDir: File): IDataPacker {

    override fun pack(pack: Pack): File? {
        ensureWorkDir()

        val name = pack.cat.name ?: "Cat"
        val zipPath = workDir.path + "/$name.zip"

        val contentPaths = pack.cat.extractContent().mapNotNull { it.path }
        val withFixedPaths = pack.cat.withUpdatedContent { uri ->
            uri?.lastPathSegment?.let { Uri.parse(it) }
        }

        val bundle = Bundle(BUNDLE_ACTUAL_VERSION, Pack(withFixedPaths))
        val bundleFilePath = BundleUtils.toJsonFile(bundle, workDir).path
        val paths = contentPaths + bundleFilePath

        FileUtils.zip(paths.toTypedArray(), zipPath)
        return File(zipPath)
    }

    override fun unpack(file: File): Pack? {
        ensureWorkDir()
        FileUtils.unzip(file.path, workDir.path)

        val bundleFile = File(workDir, BundleUtils.JSON_FILE_NAME)
        val bundle = BundleUtils.fromJsonFile<Bundle>(bundleFile)

        if (bundle == null || bundle.version > BUNDLE_ACTUAL_VERSION)
            return null

        val withFixedPath = bundle.pack.cat.withUpdatedContent { uri ->
            uri?.let { Uri.fromFile(File(workDir, it.toString())) }
        }
        return Pack(withFixedPath)
    }

    private fun ensureWorkDir() { if(workDir.exists().not()) workDir.mkdirs() }

    companion object {
        private val BUNDLE_ACTUAL_VERSION = 1
    }
}