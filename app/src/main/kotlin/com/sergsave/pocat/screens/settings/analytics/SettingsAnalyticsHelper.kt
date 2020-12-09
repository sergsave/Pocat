package com.sergsave.pocat.screens.settings.analytics

import com.sergsave.pocat.analytics.AnalyticsTracker

class SettingsAnalyticsHelper(private val tracker: AnalyticsTracker) {
    fun onVibrationSwitched(state: Boolean) = tracker.sendEvent(VibrationSwitched(state))
}