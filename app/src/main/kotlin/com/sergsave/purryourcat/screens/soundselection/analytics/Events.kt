package com.sergsave.purryourcat.screens.soundselection.analytics

import com.sergsave.purryourcat.analytics.AnalyticsEvent
import com.sergsave.purryourcat.AnalyticsConstants.Events.AudioValidated
import com.sergsave.purryourcat.AnalyticsConstants.Events.AddAudioRequested
import com.sergsave.purryourcat.AnalyticsConstants.Events.RecorderNotFound

class AudioValidated(result: Boolean):
    AnalyticsEvent(AudioValidated.NAME, mapOf(
        AudioValidated.Params.RESULT to result
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
