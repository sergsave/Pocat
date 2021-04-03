package com.sergsave.pocat.samples

import android.content.Context
import com.sergsave.pocat.R
import com.sergsave.pocat.helpers.FileUtils
import com.sergsave.pocat.models.CatData

data class CatSample(val id: String, val data: CatData)

private data class SampleData(val uuid: String,
                              val photoResId: Int,
                              val audioResId: Int)

// Warning! Images can be cached by the user. Update image version when update image.
private val samples = listOf(
    SampleData("29dd84ad-0611-4869-9389-b16661d74a4b",
        R.raw.sample_photo_1_v2, R.raw.sample_audio_1),
    SampleData("f4672441-0346-412d-9cdc-ea885a7edd74",
        R.raw.sample_photo_2_v2, R.raw.sample_audio_2),
    SampleData("45872693-0e82-4a42-872d-1cb1c83a7922",
        R.raw.sample_photo_3_v2, R.raw.sample_audio_3),
    SampleData("152654f0-1f3b-4fa1-9458-b04c7c992eed",
        R.raw.sample_photo_4_v2, R.raw.sample_audio_4),
    SampleData("bec14a1d-680e-4b29-9315-34043cb6600b",
        R.raw.sample_photo_5_v2, R.raw.sample_audio_5),
    SampleData("3fc3faa0-4c43-4586-9edb-c68aaa4cb386",
        R.raw.sample_photo_6_v2, R.raw.sample_audio_6)

//    SampleData("bc798471-e288-4bf7-8ea8-2645a52f5c1d",
//        R.raw.sample_photo_7_v1, R.raw.sample_audio_1),
//    SampleData("f2020e3f-90d9-4230-a566-ff1b5be12194",
//        R.raw.sample_photo_8_v1, R.raw.sample_audio_2),
//    SampleData("1bacf822-ec15-467d-893e-0b0a1fd3aa42",
//        R.raw.sample_photo_9_v1, R.raw.sample_audio_3),
//    SampleData("f016325f-c89f-4b53-aa66-58c832c8a56a",
//        R.raw.sample_photo_10_v1, R.raw.sample_audio_4),
//    SampleData("d35bea6a-7735-42e3-9ba4-f3c555ada902",
//        R.raw.sample_photo_11_v1, R.raw.sample_audio_5),
//    SampleData("f7ff3666-dd4d-4d44-8838-4bb0075c2433",
//        R.raw.sample_photo_12_v1, R.raw.sample_audio_6),
//
//    SampleData("d97d9c5b-1e85-4f09-8c5e-8c14c18ad9f3",
//        R.raw.sample_photo_13_v1, R.raw.sample_audio_1),
//    SampleData("88ab0507-8498-4616-98ba-2d737228db64",
//        R.raw.sample_photo_14_v1, R.raw.sample_audio_2),
//    SampleData("6d4ef522-4268-47cc-8095-3fe32e4d18d0",
//        R.raw.sample_photo_15_v1, R.raw.sample_audio_3),
//    SampleData("ffa54eb2-5aa0-475c-99ad-0c59f0fc345c",
//        R.raw.sample_photo_16_v1, R.raw.sample_audio_4),
//    SampleData("52f19281-d001-47e9-bdbb-ce0a4cf5f86b",
//        R.raw.sample_photo_17_v1, R.raw.sample_audio_5),
//    SampleData("9de685ed-1c47-4bab-9e4a-e6085ce561f7",
//        R.raw.sample_photo_18_v1, R.raw.sample_audio_6),
//
//    SampleData("b42d485c-db0f-4505-8d0c-b364066d6020",
//        R.raw.sample_photo_19_v1, R.raw.sample_audio_1),
//    SampleData("b7753d86-2749-4964-85ab-7938692713cd",
//        R.raw.sample_photo_20_v1, R.raw.sample_audio_2),
//    SampleData("4c67933a-4c3c-435c-ae6c-a7839aded016",
//        R.raw.sample_photo_21_v1, R.raw.sample_audio_3),
//    SampleData("49050005-798d-4783-b3ce-ff51fec3f09b",
//        R.raw.sample_photo_22_v1, R.raw.sample_audio_4),
//    SampleData("b31853d5-4393-4e8c-aa9d-4e8ac5d33f10",
//        R.raw.sample_photo_23_v1, R.raw.sample_audio_5),
//    SampleData("fc9a238d-9eda-4170-a4c4-b6900216a997",
//        R.raw.sample_photo_24_v1, R.raw.sample_audio_6),
//
//    SampleData("25bd87dd-c80e-4cef-9df2-7ed3ef18392d",
//        R.raw.sample_photo_25_v1, R.raw.sample_audio_1),
//    SampleData("1974e3a4-3987-494a-8438-616a997ff8b1",
//        R.raw.sample_photo_26_v1, R.raw.sample_audio_2),
//    SampleData("2b6e1b5f-fd6a-4cb9-b2bf-b33e943230c6",
//        R.raw.sample_photo_27_v1, R.raw.sample_audio_3),
//    SampleData("ad89d3fc-671b-44b5-81a4-6b4411f6041e",
//        R.raw.sample_photo_28_v1, R.raw.sample_audio_4),
//    SampleData("951a4c32-7639-4791-969c-bc1cb3db47c4",
//        R.raw.sample_photo_29_v1, R.raw.sample_audio_5),
//    SampleData("814af5ba-373e-4e51-a79d-eb186022d761",
//        R.raw.sample_photo_30_v1, R.raw.sample_audio_6),
//
//    SampleData("ab79f6dd-7ebb-4dae-99a9-275faacc7505",
//        R.raw.sample_photo_31_v1, R.raw.sample_audio_1),
//    SampleData("56541f7e-f86f-4e54-b4cf-8e82ca822ad4",
//        R.raw.sample_photo_32_v1, R.raw.sample_audio_2),
//    SampleData("6e150d9d-e6ad-4463-9e95-f82544784002",
//        R.raw.sample_photo_33_v1, R.raw.sample_audio_3),
//    SampleData("e6f790fd-42c7-497a-b28b-3e720ca30ab0",
//        R.raw.sample_photo_34_v1, R.raw.sample_audio_4),
//    SampleData("99e715c7-29de-41f5-bd56-23e3e4e19247",
//        R.raw.sample_photo_35_v1, R.raw.sample_audio_5),
//    SampleData("9fc71343-252d-44aa-b9e6-aa3e14260a6b",
//        R.raw.sample_photo_36_v1, R.raw.sample_audio_6),
//
//    SampleData("67aaf9ae-034e-4bd7-859b-eab984bd6ffd",
//        R.raw.sample_photo_37_v1, R.raw.sample_audio_1),
//    SampleData("25cab55b-93df-4bd7-a77d-39b3eb5bb0cc",
//        R.raw.sample_photo_38_v1, R.raw.sample_audio_2),
//    SampleData("5a83f23e-3bfd-4dfe-9133-8088e2e37d89",
//        R.raw.sample_photo_39_v1, R.raw.sample_audio_3),
//    SampleData("b1471639-e523-458a-aed6-01d79ab12873",
//        R.raw.sample_photo_40_v1, R.raw.sample_audio_4),
//    SampleData("51e184a5-6366-4c64-89dd-653d39341432",
//        R.raw.sample_photo_41_v1, R.raw.sample_audio_5),
//    SampleData("41f6523a-fa96-4b2b-885a-846ced52d41b",
//        R.raw.sample_photo_42_v1, R.raw.sample_audio_6),
//
//    SampleData("5abacfcb-ddb3-463f-af4b-abbf7fbb5cb1",
//        R.raw.sample_photo_43_v1, R.raw.sample_audio_1),
//    SampleData("45bea93e-6a86-4240-a2fe-92017c5cccff",
//        R.raw.sample_photo_44_v1, R.raw.sample_audio_2),
//    SampleData("afd1a14d-ff2a-4644-acf1-ee41b654abad",
//        R.raw.sample_photo_45_v1, R.raw.sample_audio_3),
//    SampleData("121816c6-9506-4d1a-9ee5-8a6f3348c172",
//        R.raw.sample_photo_46_v1, R.raw.sample_audio_4),
//    SampleData("feb7eaa2-537a-4238-8a13-2ca268e86a96",
//        R.raw.sample_photo_47_v1, R.raw.sample_audio_5),
//    SampleData("d974aee5-d31c-4d16-9149-e96920bb76b2",
//        R.raw.sample_photo_48_v1, R.raw.sample_audio_6),
//
//    SampleData("714c8a25-28f2-46dd-99e4-44263db8a8df",
//        R.raw.sample_photo_49_v1, R.raw.sample_audio_1),
//    SampleData("94a4c0c2-c47d-4767-b830-eba814c7c849",
//        R.raw.sample_photo_50_v1, R.raw.sample_audio_2)
)

class CatSampleProvider(private val context: Context) {
    fun provide(): List<CatSample> {
        return emptyList()
//        return samples.mapIndexed { i, source -> CatSample(source.uuid, dataFrom(i, source)) }
    }

    private fun dataFrom(number: Int, sample: SampleData) = CatData(
        context.resources.getStringArray(R.array.samples_cats_names)[number],
        FileUtils.uriOfResource(context, sample.photoResId),
        FileUtils.uriOfResource(context, sample.audioResId)
    )
}