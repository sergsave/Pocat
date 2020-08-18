package com.sergsave.purryourcat.helpers

import kotlinx.serialization.*
import kotlinx.serialization.json.*
import java.io.File

object BundleUtils {

    const val JSON_FILE_NAME = "bundle.json"

    @ImplicitReflectionSerializer
    inline fun <reified T: Any> toJsonFile(bundle: T, fileRootDir: File): File {
        val file = File(fileRootDir, JSON_FILE_NAME)
        if(file.exists())
            file.delete()

        val json = Json(JsonConfiguration.Stable).stringify(T::class.serializer(), bundle)
        file.writeText(json)

        return file
    }

    @ImplicitReflectionSerializer
    inline fun <reified T: Any> fromJsonFile(file: File): T? {
        if(file.exists().not())
            return null

        return try {
            Json(JsonConfiguration.Stable).parse(T::class.serializer(), file.readText())
        } catch(e: Exception) {
            null
        }
    }
}