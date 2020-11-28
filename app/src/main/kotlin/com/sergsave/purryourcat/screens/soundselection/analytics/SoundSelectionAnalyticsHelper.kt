package com.sergsave.purryourcat.screens.soundselection.analytics

import com.sergsave.purryourcat.analytics.AnalyticsTracker
import com.sergsave.purryourcat.screens.soundselection.analytics.AddAudioRequested.Source


class SoundSelectionAnalyticsHelper(private val tracker: AnalyticsTracker) {
    fun onValidateResult(result: Boolean) = tracker.sendEvent(AudioValidated(result))

    fun onAddFromSamplesRequested() = tracker.sendEvent(AddAudioRequested(Source.SAMPLES))
    fun onAddFromRecorderRequested() = tracker.sendEvent(AddAudioRequested(Source.RECORDER))
    fun onAddFromDeviceRequested() = tracker.sendEvent(AddAudioRequested(Source.DEVICE))

    fun onRecorderNotFound() = tracker.sendEvent(RecorderNotFound())
}