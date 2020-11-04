package com.sergsave.purryourcat.samples

import android.content.Context
import android.net.Uri
import com.sergsave.purryourcat.R
import com.sergsave.purryourcat.helpers.FileUtils

class SoundSampleProvider(private val context: Context) {
    fun provide(): List<Pair<String, Uri>> {
        val ids = listOf<Pair<Int, Int>>(
            Pair(R.string.sample_audio_1_name, R.raw.sample_audio_1),
            Pair(R.string.sample_audio_2_name, R.raw.sample_audio_2),
            Pair(R.string.sample_audio_3_name, R.raw.sample_audio_3),
            Pair(R.string.sample_audio_4_name, R.raw.sample_audio_4),
            Pair(R.string.sample_audio_5_name, R.raw.sample_audio_5)
        )
        return ids.map { Pair(context.getString(it.first),
            FileUtils.uriOfResource(it.second,context)) }
    }
}