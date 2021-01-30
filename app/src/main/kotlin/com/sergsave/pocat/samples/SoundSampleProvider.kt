package com.sergsave.pocat.samples

import android.content.Context
import android.net.Uri
import com.sergsave.pocat.R
import com.sergsave.pocat.helpers.FileUtils

class SoundSampleProvider(private val context: Context) {
    fun provide(): List<Pair<String, Uri>> {
        val ids = listOf<Pair<Int, Int>>(
            Pair(0, R.raw.sample_audio_1),
            Pair(1, R.raw.sample_audio_2),
            Pair(2, R.raw.sample_audio_3),
            Pair(3, R.raw.sample_audio_4),
            Pair(4, R.raw.sample_audio_5),
            Pair(5, R.raw.sample_audio_6)
        )
        val getName = { stringArrayIndex: Int ->
            context.resources.getStringArray(R.array.samples_sounds_names)[stringArrayIndex]
        }
        return ids.map { Pair(getName(it.first), FileUtils.uriOfResource(context, it.second)) }
    }
}