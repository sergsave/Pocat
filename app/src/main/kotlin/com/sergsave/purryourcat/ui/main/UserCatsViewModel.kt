package com.sergsave.purryourcat.ui.main

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.sergsave.purryourcat.persistent.CatRepository
import com.sergsave.purryourcat.helpers.DisposableViewModel
import com.sergsave.purryourcat.helpers.Event
import com.sergsave.purryourcat.models.Cat
import io.reactivex.Observable
import java.util.*

class UserCatsViewModel(private val catRepository: CatRepository): DisposableViewModel() {

    private val _cats = MutableLiveData<List<Cat>>()
    val cats: LiveData<List<Cat>>
        get() = _cats

    init {
        addDisposable(catRepository.read().subscribe { timedCats ->
            val sortedByTime = timedCats.sortedByDescending { it.timestamp }
            _cats.value = sortedByTime.map { it.cat }
        })
    }

    fun onRemoveRequested(ids: List<UUID>) {
        remove(ids)
    }

    private fun remove(catIds: List<UUID>) {
        addDisposable(
            Observable.fromIterable(catIds)
                .concatMapCompletable { catRepository.remove(it) }
                .subscribe{}
        )
    }
}
