package com.sergsave.purryourcat.samples

import android.content.Context
import com.sergsave.purryourcat.R
import com.sergsave.purryourcat.models.CatData
import com.sergsave.purryourcat.helpers.uriOfResource

private data class Sample(val nameResource: Int, val photoResource: Int, val audioResource: Int)

private val samples = listOf(
    Sample(R.string.sample_cat_1_name, R.raw.sample_cat_1_photo, R.raw.sample_cat_1_audio),
    Sample(R.string.sample_cat_1_name, R.raw.sample_cat_1_photo, R.raw.sample_cat_1_audio),
    Sample(R.string.sample_cat_1_name, R.raw.sample_cat_1_photo, R.raw.sample_cat_1_audio),
    Sample(R.string.sample_cat_1_name, R.raw.sample_cat_1_photo, R.raw.sample_cat_1_audio),
    Sample(R.string.sample_cat_1_name, R.raw.sample_cat_1_photo, R.raw.sample_cat_1_audio)
)

class CatSampleProvider(private val context: Context) {
    fun provide(): List<CatData> {
        return samples.map {
            CatData(
                context.getString(it.nameResource),
                uriOfResource(it.photoResource, context),
                uriOfResource(it.audioResource, context)
            )
        }
    }
}