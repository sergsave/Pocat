package com.sergsave.purryourcat.screens.settings.analytics

import com.sergsave.purryourcat.analytics.AnalyticsTracker

class SettingsAnalyticsHelper(private val tracker: AnalyticsTracker) {
    fun onVibrationSwitched(state: Boolean) = tracker.sendEvent(VibrationSwitched(state))
}