package com.sergsave.purryourcat.samples

import android.content.Context
import android.net.Uri
import com.sergsave.purryourcat.R
import com.sergsave.purryourcat.helpers.uriOfResource

class SoundSampleProvider(private val context: Context) {
    fun provide(): List<Uri> {
        val ids = listOf(
            R.raw.sample_1_audio,
            R.raw.sample_2_audio,
            R.raw.sample_3_audio,
            R.raw.sample_4_audio,
            R.raw.sample_5_audio
        )
        return ids.map { uriOfResource(it, context) }
    }
}