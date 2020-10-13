package com.sergsave.purryourcat.ui.soundselection

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.text.format.Formatter
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import com.sergsave.purryourcat.helpers.Event
import com.sergsave.purryourcat.R
import com.sergsave.purryourcat.helpers.FileUtils

class SoundSelectionViewModel(private val applicationContext: Context, private val maxFileSize: Long)
    : ViewModel() {

    private val formattedMaxSize = Formatter.formatShortFileSize(applicationContext, maxFileSize)

    data class Message (val stringId: Int, val stringArgs: Array<Any>)

    val samplesSummary: Message
        get() {
            return Message(R.string.samples_summary, arrayOf<Any>())
        }

    val recorderSummary: Message
        get() {
            val averageSizeMBPerHour = 60
            val averageSizeBytesPerSec = averageSizeMBPerHour * 1024 * 1024 / 3600
            val approxDurationMin: Long = (maxFileSize / averageSizeBytesPerSec) / 60

            return Message(R.string.recorder_summary, arrayOf<Any>(formattedMaxSize, approxDurationMin))
        }

    val pickAudioSummary: Message
        get() {
            return Message(R.string.pick_audio_summary, arrayOf<Any>(formattedMaxSize))
        }

    private val _validationFailedEvent = MutableLiveData<Event<Message>>()
    val validationFailedEvent: LiveData<Event<Message>>
        get() = _validationFailedEvent

    private val _validationSuccessEvent = MutableLiveData<Event<Uri>>()
    val validationSuccessEvent: LiveData<Event<Uri>>
        get() = _validationSuccessEvent

    fun validateResult(uri: Uri) {
        val error = Message(R.string.file_size_exceeded_message_text, arrayOf(formattedMaxSize))
        val size = FileUtils.getContentFileSize(applicationContext, uri)

        if(size < maxFileSize)
            _validationSuccessEvent.value = Event(uri)
        else
            _validationFailedEvent.value = Event(error)
    }
}
