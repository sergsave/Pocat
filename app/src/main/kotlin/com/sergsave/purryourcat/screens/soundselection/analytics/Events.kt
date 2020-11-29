package com.sergsave.purryourcat.screens.soundselection.analytics

import com.sergsave.purryourcat.analytics.AnalyticsEvent
import com.sergsave.purryourcat.AnalyticsConstants.Events.AudioSelected
import com.sergsave.purryourcat.AnalyticsConstants.Events.AddAudioRequested
import com.sergsave.purryourcat.AnalyticsConstants.Events.RecorderNotFound

class AudioValidated(validationResult: Boolean):
    AnalyticsEvent(AudioSelected.NAME, mapOf(
        AudioSelected.Params.VALIDATION_RESULT to validationResult
    ))

class AddAudioRequested(source: Source):
    AnalyticsEvent(AddAudioRequested.NAME, mapOf(
        AddAudioRequested.Params.SOURCE to source.toString()
    )) {
    enum class Source {
        SAMPLES, RECORDER, DEVICE;

        override fun toString() = when(this) {
            SAMPLES -> AddAudioRequested.Source.SAMPLES
            RECORDER -> AddAudioRequested.Source.RECORDER
            DEVICE -> AddAudioRequested.Source.DEVICE
        }
    }
}

class RecorderNotFound: AnalyticsEvent(RecorderNotFound.NAME)
