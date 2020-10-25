package com.sergsave.purryourcat.ui.main

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.sergsave.purryourcat.models.CatData
import com.sergsave.purryourcat.models.Card
import com.sergsave.purryourcat.samples.CatSampleProvider
import java.util.*

class SamplesViewModel(provider: CatSampleProvider): ViewModel() {
    private val samples = provider.provide().map { Pair(it.id, it.data) }

    private val _cats = MutableLiveData<List<Pair<String, CatData>>>(samples)
    val cats: LiveData<List<Pair<String, CatData>>>
        get() = _cats

    fun makeCard(data: CatData): Card {
        // Null persistent id because samples don't store in repo
        return Card(null, data, isSaveable = false, isShareable = false )
    }
}

