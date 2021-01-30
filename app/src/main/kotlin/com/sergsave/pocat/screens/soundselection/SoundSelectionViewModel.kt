package com.sergsave.pocat.screens.soundselection

import android.net.Uri
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.sergsave.pocat.R
import com.sergsave.pocat.helpers.Event
import com.sergsave.pocat.screens.soundselection.analytics.SoundSelectionAnalyticsHelper
import kotlin.math.roundToLong

class SoundSelectionViewModel(private val fileSizeByteCalculator: (Uri) -> Long,
                              private val maxFileSizeMB: Long,
                              private val analytics: SoundSelectionAnalyticsHelper)
    : ViewModel() {
    data class Message (val stringId: Int, val stringArgs: List<Any>)

    val samplesSummary: Message
        get() {
            return Message(R.string.sound_selection_samples_summary, listOf<Any>())
        }

    val recorderSummary: Message
        get() {
            val averageSizeMBPerMin = 1.25
            val approxMaxDurationMin = (maxFileSizeMB.toFloat() / averageSizeMBPerMin).roundToLong()

            return Message(R.string.sound_selection_recorder_summary,
                listOf<Any>(maxFileSizeMB, approxMaxDurationMin))
        }

    val pickAudioSummary: Message
        get() {
            return Message(R.string.sound_selection_pick_audio_summary, listOf<Any>(maxFileSizeMB))
        }

    private val _validationFailedEvent = MutableLiveData<Event<Message>>()
    val validationFailedEvent: LiveData<Event<Message>>
        get() = _validationFailedEvent

    private val _validationSuccessEvent = MutableLiveData<Event<Uri>>()
    val validationSuccessEvent: LiveData<Event<Uri>>
        get() = _validationSuccessEvent

    fun validateResult(uri: Uri) {
        val error = Message(R.string.sound_selection_popup_file_size_exceeded, listOf(maxFileSizeMB))
        val size = fileSizeByteCalculator(uri)

        val result = size < maxFileSizeMB * 1000000

        if(result)
            _validationSuccessEvent.value = Event(uri)
        else {
            analytics.onSizeExceededError()
            _validationFailedEvent.value = Event(error)
        }
    }

    fun onAddFromSamplesRequested() = analytics.onAddFromSamplesRequested()
    fun onAddFromRecorderRequested() = analytics.onAddFromRecorderRequested()
    fun onAddFromDeviceRequested() = analytics.onAddFromDeviceRequested()

    fun onRecorderNotFound() = analytics.onRecorderNotFound()
}
