package com.sergsave.purryourcat.screens.soundselection.analytics

import com.sergsave.purryourcat.analytics.AnalyticsEvent
import com.sergsave.purryourcat.AnalyticsConstants.Events.AudioSelected
import com.sergsave.purryourcat.AnalyticsConstants.Events.AudioSelectionStart
import com.sergsave.purryourcat.AnalyticsConstants.Events.RecorderNotFound

class AudioSelected(validationResult: Boolean):
    AnalyticsEvent(AudioSelected.NAME, mapOf(
        AudioSelected.Params.VALIDATION_RESULT to validationResult
    ))

class AudioSelectionStart(source: Source):
    AnalyticsEvent(AudioSelectionStart.NAME, mapOf(
        AudioSelectionStart.Params.SOURCE to source.toString()
    )) {
    enum class Source {
        SAMPLES, RECORDER, DEVICE;

        override fun toString() = when(this) {
            SAMPLES -> AudioSelectionStart.Source.SAMPLES
            RECORDER -> AudioSelectionStart.Source.RECORDER
            DEVICE -> AudioSelectionStart.Source.DEVICE
        }
    }
}

class RecorderNotFound: AnalyticsEvent(RecorderNotFound.NAME)
