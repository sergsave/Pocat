package com.sergsave.purryourcat.screens.soundselection

import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import com.sergsave.purryourcat.helpers.Event
import com.sergsave.purryourcat.R
import com.sergsave.purryourcat.helpers.FileUtils
import com.sergsave.purryourcat.screens.soundselection.analytics.SoundSelectionAnalyticsHelper
import kotlin.math.roundToLong

class SoundSelectionViewModel(private val fileSizeByteCalculator: (Uri) -> Long,
                              private val maxFileSizeMB: Long,
                              private val analytics: SoundSelectionAnalyticsHelper)
    : ViewModel() {
    data class Message (val stringId: Int, val stringArgs: Array<Any>)

    val samplesSummary: Message
        get() {
            return Message(R.string.samples_summary, arrayOf<Any>())
        }

    val recorderSummary: Message
        get() {
            val averageSizeMBPerMin = 1.25
            val approxMaxDurationMin = (maxFileSizeMB.toFloat() / averageSizeMBPerMin).roundToLong()

            return Message(R.string.recorder_summary,
                arrayOf<Any>(maxFileSizeMB, approxMaxDurationMin))
        }

    val pickAudioSummary: Message
        get() {
            return Message(R.string.pick_audio_summary, arrayOf<Any>(maxFileSizeMB))
        }

    private val _validationFailedEvent = MutableLiveData<Event<Message>>()
    val validationFailedEvent: LiveData<Event<Message>>
        get() = _validationFailedEvent

    private val _validationSuccessEvent = MutableLiveData<Event<Uri>>()
    val validationSuccessEvent: LiveData<Event<Uri>>
        get() = _validationSuccessEvent

    fun validateResult(uri: Uri) {
        val error = Message(R.string.file_size_exceeded_message_text, arrayOf(maxFileSizeMB))
        val size = fileSizeByteCalculator(uri)

        val result = size < maxFileSizeMB * 1000000

        if(result)
            _validationSuccessEvent.value = Event(uri)
        else
            _validationFailedEvent.value = Event(error)

        analytics.onValidateResult(result)
    }

    fun onAddFromSamplesRequested() = analytics.onAddFromSamplesRequested()
    fun onAddFromRecorderRequested() = analytics.onAddFromRecorderRequested()
    fun onAddFromDeviceRequested() = analytics.onAddFromDeviceRequested()

    fun onRecorderNotFound() = analytics.onRecorderNotFound()
}
