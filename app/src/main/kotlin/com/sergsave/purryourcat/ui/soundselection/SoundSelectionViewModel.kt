package com.sergsave.purryourcat.ui.soundselection

import android.net.Uri
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.LiveData
import com.sergsave.purryourcat.content.ContentRepository
import com.sergsave.purryourcat.helpers.DisposableViewModel
import com.sergsave.purryourcat.helpers.Event
import com.sergsave.purryourcat.samples.SoundSampleProvider

class SoundSelectionViewModel(private val contentRepository: ContentRepository,
                              private val fileSizeCalculator: (Uri) -> Long,
                              provider: SoundSampleProvider,
                              initAudio: Uri?)
    : DisposableViewModel() {

    val audioSamples: Set<Uri> = provider.provide().toSet()

    private val _userAudio = MutableLiveData<Uri>(
        if(initAudio == null || audioSamples.contains(initAudio))
            null
        else
            initAudio
    )
    val userAudio: LiveData<Uri>
        get() = _userAudio

    private val _selectedAudio = MutableLiveData<Uri>(initAudio)
    val selectedAudio: LiveData<Uri>
        get() = _selectedAudio

    private val _playingAudio = MutableLiveData<Uri>()
    val playingAudio: LiveData<Uri>
        get() = _playingAudio

    private val _fileSizeExceededMessageEvent = MutableLiveData<Event<Long>>()
    val fileSizeExceededMessageEvent: LiveData<Event<Long>>
        get() = _fileSizeExceededMessageEvent

    fun onUserAudioAdded(uri: Uri) {
        if(checkFileSize(uri, contentRepository.maxAudioFileSize).not()) {
            _userAudio.value = _userAudio.value
            return
        }

        if(uri != _userAudio.value) {
            addDisposable(contentRepository.addAudio(uri).subscribe { newUri ->
                _userAudio.value = newUri
                _selectedAudio.value = newUri
            })
        }
    }

    fun onUserButtonClicked() {
        _playingAudio.value = null
    }

    fun onAudioSelected(uri: Uri?) {
        _selectedAudio.value = uri
    }

    fun onAudioPlayStarted(uri: Uri?) {
        _playingAudio.value = uri
    }

    private fun checkFileSize(uri: Uri, maxSize: Long): Boolean {
        val size = fileSizeCalculator(uri)

        if(size < maxSize)
            return true

        _fileSizeExceededMessageEvent.value = Event(maxSize)
        return false
    }
}
