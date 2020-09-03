package com.sergsave.purryourcat.sampleprovider

import android.content.ContentResolver
import android.content.Context
import android.net.Uri
import com.sergsave.purryourcat.R
import com.sergsave.purryourcat.models.CatData

private data class Sample(val nameResource: Int, val photoResource: Int, val audioResource: Int)

private val samples = listOf(
    Sample(R.string.sample_1_name, R.raw.sample_1_photo, R.raw.sample_1_audio),
    Sample(R.string.sample_1_name, R.raw.sample_1_photo, R.raw.sample_1_audio),
    Sample(R.string.sample_1_name, R.raw.sample_1_photo, R.raw.sample_1_audio),
    Sample(R.string.sample_1_name, R.raw.sample_1_photo, R.raw.sample_1_audio),
    Sample(R.string.sample_1_name, R.raw.sample_1_photo, R.raw.sample_1_audio),
    Sample(R.string.sample_1_name, R.raw.sample_1_photo, R.raw.sample_1_audio),
    Sample(R.string.sample_1_name, R.raw.sample_1_photo, R.raw.sample_1_audio),
    Sample(R.string.sample_1_name, R.raw.sample_1_photo, R.raw.sample_1_audio),
    Sample(R.string.sample_1_name, R.raw.sample_1_photo, R.raw.sample_1_audio),
    Sample(R.string.sample_1_name, R.raw.sample_1_photo, R.raw.sample_1_audio),
    Sample(R.string.sample_1_name, R.raw.sample_1_photo, R.raw.sample_1_audio),
    Sample(R.string.sample_1_name, R.raw.sample_1_photo, R.raw.sample_1_audio),
    Sample(R.string.sample_1_name, R.raw.sample_1_photo, R.raw.sample_1_audio),
    Sample(R.string.sample_1_name, R.raw.sample_1_photo, R.raw.sample_1_audio),
    Sample(R.string.sample_1_name, R.raw.sample_1_photo, R.raw.sample_1_audio),
    Sample(R.string.sample_1_name, R.raw.sample_1_photo, R.raw.sample_1_audio)
)

class SampleProvider(private val context: Context) {
    fun provide(): List<CatData> {
        return samples.map {
            CatData(
                context.getString(it.nameResource),
                uriOfResource(it.photoResource),
                uriOfResource(it.audioResource)
            )
        }
    }

    private fun uriOfResource(id: Int): Uri {
        return Uri.Builder()
            .scheme(ContentResolver.SCHEME_ANDROID_RESOURCE)
            .authority(context.resources.getResourcePackageName(id))
            .appendPath(context.resources.getResourceTypeName(id))
            .appendPath(context.resources.getResourceEntryName(id))
            .build()
    }
}