package com.sergsave.purryourcat.ui.catslist

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.sergsave.purryourcat.sharing.SharingManager
import com.sergsave.purryourcat.helpers.DisposableViewModel
import com.sergsave.purryourcat.helpers.Event

class CatsListActivityViewModel(sharingManager: SharingManager): DisposableViewModel() {

    private val _readyForHandleSharingDataEvent = MutableLiveData<Event<Unit>>()
    val readyForHandleSharingDataEvent: LiveData<Event<Unit>>
        get() = _readyForHandleSharingDataEvent

    init {
        // Cleanup not in Application, because Application is created only after device reload
        addDisposable(sharingManager.cleanup().subscribe {
            _readyForHandleSharingDataEvent.value = Event(Unit)
        })
    }
}

