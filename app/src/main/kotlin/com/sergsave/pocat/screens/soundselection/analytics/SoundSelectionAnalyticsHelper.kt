package com.sergsave.pocat.screens.soundselection.analytics

import com.sergsave.pocat.analytics.AnalyticsTracker

class SoundSelectionAnalyticsHelper(private val tracker: AnalyticsTracker) {
    fun onSizeExceededError() = tracker.sendEvent(AudioSizeError())

    fun onAddFromSamplesRequested() = tracker.sendEvent(AudioFromSamplesClick())
    fun onAddFromRecorderRequested() = tracker.sendEvent(AudioFromRecorderClick())
    fun onAddFromDeviceRequested() = tracker.sendEvent(AudioFromMemoryClick())

    fun onRecorderNotFound() = tracker.sendEvent(RecorderNotFound())
}