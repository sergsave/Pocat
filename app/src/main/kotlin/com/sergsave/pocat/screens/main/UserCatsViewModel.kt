package com.sergsave.pocat.screens.main

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.sergsave.pocat.helpers.DisposableViewModel
import com.sergsave.pocat.models.Card
import com.sergsave.pocat.models.CatData
import com.sergsave.pocat.persistent.CatDataRepository
import com.sergsave.pocat.screens.main.analytics.MainAnalyticsHelper

class UserCatsViewModel(private val catDataRepository: CatDataRepository,
                        private val analytics: MainAnalyticsHelper): DisposableViewModel() {

    private val _cats = MutableLiveData<List<Pair<String, CatData>>>()
    val cats: LiveData<List<Pair<String, CatData>>>
        get() = _cats

    init {
        addDisposable(catDataRepository.read().subscribe(
            { catMap ->
                analytics.onCatsReadedFromRepo(catMap.size)
                val sortedByTime = catMap.toList().sortedByDescending { it.second.timestamp.time }
                _cats.value = sortedByTime.map { (id, timed) -> Pair(id, timed.data) }
            }
            // Don't handle error, because it's unexpected
        ))
    }

    fun makeCard(listItemId: String, data: CatData): Card {
        return Card(listItemId, data, isSaveable = true, isShareable = true)
    }

    fun onRemoveRequested(ids: List<String>) {
        remove(ids)
    }

    private fun remove(catIds: List<String>) {
        addDisposable(
            catDataRepository.remove(catIds)
                .doOnComplete { analytics.onCatsRemoved(catIds.size) }
                .subscribe({}, {
                    analytics.onCatsRemoveError()
                    Log.e(TAG, "Remove failed", it)
                })
        )
    }

    fun onCardClicked() = analytics.onUserCardClicked()

    fun onAddClicked() = analytics.onAddClicked()

    companion object {
        private const val TAG = "UserCatsViewModel"
    }
}
