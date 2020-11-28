package com.sergsave.purryourcat.screens.main

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.sergsave.purryourcat.persistent.CatDataRepository
import com.sergsave.purryourcat.helpers.DisposableViewModel
import com.sergsave.purryourcat.helpers.Event
import com.sergsave.purryourcat.models.CatData
import com.sergsave.purryourcat.models.Card
import com.sergsave.purryourcat.screens.main.analytics.MainAnalyticsHelper
import io.reactivex.Observable

class UserCatsViewModel(private val catDataRepository: CatDataRepository,
                        private val analytics: MainAnalyticsHelper): DisposableViewModel() {

    private val _cats = MutableLiveData<List<Pair<String, CatData>>>()
    val cats: LiveData<List<Pair<String, CatData>>>
        get() = _cats

    init {
        addDisposable(catDataRepository.read().subscribe(
            { catMap ->
                val sortedByTime = catMap.toList().sortedByDescending { it.second.timestamp.time }
                _cats.value = sortedByTime.map { (id, timed) -> Pair(id, timed.data) }
            },
            { Log.e(TAG, "Read failed", it) })
        )
    }

    fun makeCard(listItemId: String, data: CatData): Card {
        return Card(listItemId, data, isSaveable = true, isShareable = true)
    }

    fun onRemoveRequested(ids: List<String>) {
        remove(ids)
    }

    private fun remove(catIds: List<String>) {
        addDisposable(
            catDataRepository.remove(catIds).subscribe({}, { Log.e(TAG, "Remove failed", it) })
        )
    }

    fun onCardClicked() = analytics.onUserCardClicked()

    fun onAddClicked() = analytics.onAddClicked()

    companion object {
        private const val TAG = "UserCatsViewModel"
    }
}
