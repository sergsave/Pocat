package com.sergsave.pocat.screens.soundselection.analytics

import com.sergsave.pocat.analytics.AnalyticsTracker
import com.sergsave.pocat.screens.soundselection.analytics.AudioSelectionStart.Source

class SoundSelectionAnalyticsHelper(private val tracker: AnalyticsTracker) {
    fun onValidateResult(result: Boolean) = tracker.sendEvent(AudioSelected(result))

    fun onAddFromSamplesRequested() = tracker.sendEvent(AudioSelectionStart(Source.SAMPLES))
    fun onAddFromRecorderRequested() = tracker.sendEvent(AudioSelectionStart(Source.RECORDER))
    fun onAddFromDeviceRequested() = tracker.sendEvent(AudioSelectionStart(Source.DEVICE))

    fun onRecorderNotFound() = tracker.sendEvent(RecorderNotFound())
}