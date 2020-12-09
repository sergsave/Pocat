package com.sergsave.pocat.screens.main

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.sergsave.pocat.models.CatData
import com.sergsave.pocat.models.Card
import com.sergsave.pocat.samples.CatSampleProvider
import com.sergsave.pocat.screens.main.analytics.MainAnalyticsHelper
import java.util.*

class SamplesViewModel(provider: CatSampleProvider,
                       private val analytics: MainAnalyticsHelper): ViewModel() {
    private val samples = provider.provide().map { Pair(it.id, it.data) }

    private val _cats = MutableLiveData<List<Pair<String, CatData>>>(samples)
    val cats: LiveData<List<Pair<String, CatData>>>
        get() = _cats

    fun makeCard(data: CatData): Card {
        // Null persistent id because samples don't store in repo
        return Card(null, data, isSaveable = false, isShareable = false )
    }

    fun onCardClicked(id: String) = analytics.onSampleCardClicked(id)
}

