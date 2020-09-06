package com.sergsave.purryourcat.ui.catcard

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider.Factory
import com.sergsave.purryourcat.models.CatData

// TODO: Cancel on stop??
class SharingDataExtractViewModel(sharingManager: SharingManager) : ViewModel() {

    private var disposable = CompositeDisposable()

    private val _sharingState = MutableLiveData<Boolean>()
    val sharingState: LiveData<Boolean>
        get() = _sharingState

    private val _extractFailedEvent = MutableLiveData<Event<String>>()
    val extractFailedEvent: LiveData<Event<String>>
        get() = _extractFailedEvent

    private val _extractSuccessEvent = MutableLiveData<Event<CatData>>()
    val extractSuccessEvent: LiveData<Event<CatData>>
        get() = _extractSuccessEvent

    override fun onCleared() {
        disposable.clear()
        super.onCleared()
    }

    // Use intent is safe here because we don't save reference to any context.
    fun startExtract(intent: Intent) {
        val single = sharingManager.makeGiveObservable(intent)
        if(single == null)
            return

        val disposable = single.subscribe {
            { data -> _extractSuccessEvent.value = data.cat },
            { throwable -> _extractFailedEvent.value = throwable.message }
        }

        this.disposable.add(disposable)
    }

}

class SharingDataExtractViewModelFactory(private val data: LiveData<CatData>): Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return if (modelClass.isAssignableFrom(SharingDataLoadViewModel::class.java)) {
            SharingDataLoadViewModel(data) as T
        } else {
            throw IllegalArgumentException("ViewModel Not Found")
        }
    }
}