package com.sergsave.pocat.screens.soundselection

import androidx.lifecycle.ViewModel
import com.sergsave.pocat.samples.SoundSampleProvider

class SamplesListViewModel(provider: SoundSampleProvider) : ViewModel() {
    val samples = provider.provide()
}
