package com.sergsave.purryourcat.samples

import android.content.Context
import com.sergsave.purryourcat.R
import com.sergsave.purryourcat.helpers.FileUtils
import com.sergsave.purryourcat.models.CatData
import java.util.*

data class CatSample(val id: String, val data: CatData)

private data class ResourcePack(val nameId: Int, val photoId: Int, val audioId: Int)

// Warning! Images can be cached by the user. Be careful when changing the image without changing the file name
private val resources = listOf(
    ResourcePack(R.string.sample_cat_1_name, R.raw.sample_photo_1, R.raw.sample_audio_1),
    ResourcePack(R.string.sample_cat_2_name, R.raw.sample_photo_2, R.raw.sample_audio_2),
    ResourcePack(R.string.sample_cat_3_name, R.raw.sample_photo_3, R.raw.sample_audio_3),
    ResourcePack(R.string.sample_cat_4_name, R.raw.sample_photo_4, R.raw.sample_audio_4),
    ResourcePack(R.string.sample_cat_5_name, R.raw.sample_photo_5, R.raw.sample_audio_5)
)

class CatSampleProvider(private val context: Context) {
    fun provide(): List<CatSample> {
        return resources.map { CatSample(UUID.randomUUID().toString(), dataFrom(it)) }
    }

    private fun dataFrom(resource: ResourcePack) = CatData(
        context.getString(resource.nameId),
        FileUtils.uriOfResource(resource.photoId, context),
        FileUtils.uriOfResource(resource.audioId, context)
    )
}