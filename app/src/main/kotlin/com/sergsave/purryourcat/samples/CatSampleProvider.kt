package com.sergsave.purryourcat.samples

import android.content.Context
import com.sergsave.purryourcat.R
import com.sergsave.purryourcat.models.CatData
import com.sergsave.purryourcat.helpers.uriOfResource
import java.util.*

data class CatSample(val id: String, val data: CatData)

private data class Resource(val nameId: Int, val photoId: Int, val audioId: Int)

private val resources = listOf(
    Resource(R.string.sample_cat_1_name, R.raw.sample_cat_1_photo, R.raw.sample_cat_1_audio),
    Resource(R.string.sample_cat_1_name, R.raw.sample_cat_1_photo, R.raw.sample_cat_1_audio),
    Resource(R.string.sample_cat_1_name, R.raw.sample_cat_1_photo, R.raw.sample_cat_1_audio),
    Resource(R.string.sample_cat_1_name, R.raw.sample_cat_1_photo, R.raw.sample_cat_1_audio),
    Resource(R.string.sample_cat_1_name, R.raw.sample_cat_1_photo, R.raw.sample_cat_1_audio)
)

class CatSampleProvider(private val context: Context) {
    fun provide(): List<CatSample> {
        return resources.map { CatSample(UUID.randomUUID().toString(), dataFrom(it)) }
    }

    private fun dataFrom(resource: Resource) = CatData(
        context.getString(resource.nameId),
        uriOfResource(resource.photoId, context),
        uriOfResource(resource.audioId, context)
    )
}