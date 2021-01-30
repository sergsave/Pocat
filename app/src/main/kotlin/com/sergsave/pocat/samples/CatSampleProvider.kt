package com.sergsave.pocat.samples

import android.content.Context
import com.sergsave.pocat.R
import com.sergsave.pocat.helpers.FileUtils
import com.sergsave.pocat.models.CatData

data class CatSample(val id: String, val data: CatData)

private data class SampleData(val uuid: String,
                              val nameStringArrayIndex: Int,
                              val photoResId: Int,
                              val audioResId: Int)

// Warning! Images can be cached by the user. Update image version when update image.
private val samples = listOf(
    SampleData("29dd84ad-0611-4869-9389-b16661d74a4b",
        0, R.raw.sample_photo_1_v1, R.raw.sample_audio_1),
    SampleData("f4672441-0346-412d-9cdc-ea885a7edd74",
        1, R.raw.sample_photo_2_v1, R.raw.sample_audio_2),
    SampleData("45872693-0e82-4a42-872d-1cb1c83a7922",
        2, R.raw.sample_photo_3_v1, R.raw.sample_audio_3),
    SampleData("152654f0-1f3b-4fa1-9458-b04c7c992eed",
        3, R.raw.sample_photo_4_v1, R.raw.sample_audio_4),
    SampleData("bec14a1d-680e-4b29-9315-34043cb6600b",
        4, R.raw.sample_photo_5_v1, R.raw.sample_audio_5),
    SampleData("3fc3faa0-4c43-4586-9edb-c68aaa4cb386",
        5, R.raw.sample_photo_6_v1, R.raw.sample_audio_6)
)

class CatSampleProvider(private val context: Context) {
    fun provide(): List<CatSample> {
        return samples.map { CatSample(it.uuid, dataFrom(it)) }
    }

    private fun dataFrom(sample: SampleData) = CatData(
        context.resources.getStringArray(R.array.samples_cats_names)[sample.nameStringArrayIndex],
        FileUtils.uriOfResource(context, sample.photoResId),
        FileUtils.uriOfResource(context, sample.audioResId)
    )
}