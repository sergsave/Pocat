package com.sergsave.purryourcat.ui.catcard

import android.content.Intent
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import io.reactivex.rxjava3.kotlin.Singles
import com.sergsave.purryourcat.content.ContentRepository
import com.sergsave.purryourcat.models.CatData
import com.sergsave.purryourcat.helpers.Event
import com.sergsave.purryourcat.helpers.DisposableViewModel
import com.sergsave.purryourcat.sharing.SharingManager

// TODO: Cancel on stop??
class SharingDataExtractViewModel(
    private val sharingManager: SharingManager,
    private val contentRepo: ContentRepository
) : DisposableViewModel() {

    private val _sharingState = MutableLiveData<Boolean>()
    val sharingState: LiveData<Boolean>
        get() = _sharingState

    private val _extractFailedEvent = MutableLiveData<Event<String>>()
    val extractFailedEvent: LiveData<Event<String>>
        get() = _extractFailedEvent

    private val _extractSuccessEvent = MutableLiveData<Event<CatData>>()
    val extractSuccessEvent: LiveData<Event<CatData>>
        get() = _extractSuccessEvent

    // Use intent is safe here because we don't save reference to any context.
    fun startExtract(intent: Intent) {
        val single = sharingManager.makeGiveObservable(intent)
        if(single == null)
            return

        val disposable = single.subscribe(
            { data -> updateContent(data.cat) },
            { throwable -> throwable.message?.let { _extractFailedEvent.value = Event(it) } }
        )

        addDisposable(disposable)
    }

    private fun updateContent(data: CatData) {
        // TODO: if one is null
        addDisposable(
            Singles.zip(contentRepo.addImage(data.photoUri), contentRepo.addAudio(data.purrAudioUri))
                .subscribe { (photo, audio) ->
                    val updated = data.copy(photoUri = photo, purrAudioUri = audio)
                    _extractSuccessEvent.value = Event(updated)
                }
        )
    }
}
