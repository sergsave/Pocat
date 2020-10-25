package com.sergsave.purryourcat.ui.main

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.sergsave.purryourcat.models.Cat
import com.sergsave.purryourcat.samples.CatSampleProvider
import java.util.*

class SamplesViewModel(provider: CatSampleProvider): ViewModel() {
    private val _cats = MutableLiveData<List<Cat>>(provider.provide())
    val cats: LiveData<List<Cat>>
        get() = _cats
}

