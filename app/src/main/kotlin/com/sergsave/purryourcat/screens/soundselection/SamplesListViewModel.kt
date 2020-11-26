package com.sergsave.purryourcat.screens.soundselection

import androidx.lifecycle.ViewModel
import com.sergsave.purryourcat.samples.SoundSampleProvider

class SamplesListViewModel(private val provider: SoundSampleProvider) : ViewModel() {
    val samples = provider.provide()
}
