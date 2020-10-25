package com.sergsave.purryourcat.ui.main

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.sergsave.purryourcat.content.ContentRepository
import com.sergsave.purryourcat.data.CatDataRepository
import com.sergsave.purryourcat.helpers.DisposableViewModel
import com.sergsave.purryourcat.helpers.Event
import com.sergsave.purryourcat.models.extractContent
import com.sergsave.purryourcat.sharing.SharingManager
import io.reactivex.Observable
import io.reactivex.rxkotlin.Flowables

class MainViewModel(
    private val catDataRepository: CatDataRepository,
    private val contentRepository: ContentRepository,
    sharingManager: SharingManager
): DisposableViewModel() {

    private var pagePosition: Int? = null

    init {
        // Cleanup not in Application, because Application is created only after device reload
        addDisposable(sharingManager.cleanup().subscribe())
        cleanUpUnusedContent()
    }

    private val _clearSelectionEvent = MutableLiveData<Event<Unit>>()
    val clearSelectionEvent: LiveData<Event<Unit>>
        get() = _clearSelectionEvent

    fun onPageChanged(position: Int) {
        if(pagePosition != position)
            _clearSelectionEvent.value = Event(Unit)
        pagePosition = position
    }

    private fun cleanUpUnusedContent() {
        val disposable = Flowables.zip(catDataRepository.read(), contentRepository.read())
            .take(1)
            .subscribe { (data, content) ->
                val usedContent = data.flatMap { (_, cat) -> cat.data.extractContent() }
                val unusedContent = content - usedContent

                addDisposable(
                    Observable.fromIterable(unusedContent)
                    .concatMapCompletable { contentRepository.remove(it) }
                    .subscribe{})
            }
        addDisposable(disposable)
    }
}

