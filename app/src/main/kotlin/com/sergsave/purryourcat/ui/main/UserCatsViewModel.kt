package com.sergsave.purryourcat.ui.main

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.sergsave.purryourcat.persistent.CatDataRepository
import com.sergsave.purryourcat.helpers.DisposableViewModel
import com.sergsave.purryourcat.helpers.Event
import com.sergsave.purryourcat.models.CatData
import com.sergsave.purryourcat.models.Card
import io.reactivex.Observable

class UserCatsViewModel(private val catDataRepository: CatDataRepository): DisposableViewModel() {

    private val _cats = MutableLiveData<List<Pair<String, CatData>>>()
    val cats: LiveData<List<Pair<String, CatData>>>
        get() = _cats

    init {
        addDisposable(catDataRepository.read().subscribe { catMap ->
            val sortedByTime = catMap.toList().sortedByDescending { it.second.timestamp.time }
            _cats.value = sortedByTime.map { (id, timed) -> Pair(id, timed.data) }
        })
    }

    fun makeCard(listItemId: String, data: CatData): Card {
        return Card(listItemId, data, isSaveable = true, isShareable = true)
    }

    fun onRemoveRequested(ids: List<String>) {
        remove(ids)
    }

    private fun remove(catIds: List<String>) {
        addDisposable(
            Observable.fromIterable(catIds)
                .concatMapCompletable { catDataRepository.remove(it) }
                .subscribe{}
        )
    }
}
