package com.sergsave.purryourcat.samples

import android.content.Context
import com.sergsave.purryourcat.R
import com.sergsave.purryourcat.helpers.FileUtils
import com.sergsave.purryourcat.models.CatData

data class CatSample(val id: String, val data: CatData)

private data class SampleData(val uuid: String,
                              val nameStringId: Int,
                              val photoResId: Int,
                              val audioResId: Int)

// Warning! Images can be cached by the user. Be careful when changing the image without changing the file name
private val samples = listOf(
    SampleData("29dd84ad-0611-4869-9389-b16661d74a4b",
        R.string.sample_cat_1_name, R.raw.sample_photo_1, R.raw.sample_audio_1),
    SampleData("f4672441-0346-412d-9cdc-ea885a7edd74",
        R.string.sample_cat_2_name, R.raw.sample_photo_2, R.raw.sample_audio_2),
    SampleData("45872693-0e82-4a42-872d-1cb1c83a7922",
        R.string.sample_cat_3_name, R.raw.sample_photo_3, R.raw.sample_audio_3),
    SampleData("152654f0-1f3b-4fa1-9458-b04c7c992eed",
        R.string.sample_cat_4_name, R.raw.sample_photo_4, R.raw.sample_audio_4),
    SampleData("bec14a1d-680e-4b29-9315-34043cb6600b",
        R.string.sample_cat_5_name, R.raw.sample_photo_5, R.raw.sample_audio_5)
)

class CatSampleProvider(private val context: Context) {
    fun provide(): List<CatSample> {
        return samples.map { CatSample(it.uuid, dataFrom(it)) }
    }

    private fun dataFrom(sample: SampleData) = CatData(
        context.getString(sample.nameStringId),
        FileUtils.uriOfResource(sample.photoResId, context),
        FileUtils.uriOfResource(sample.audioResId, context)
    )
}