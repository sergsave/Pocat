package com.sergsave.purryourcat.screens.settings.analytics

import com.sergsave.purryourcat.analytics.AnalyticsEvent
import com.sergsave.purryourcat.AnalyticsConstants.Events.VibrationSwitched

class VibrationSwitched(state: Boolean):
    AnalyticsEvent(VibrationSwitched.NAME, mapOf(VibrationSwitched.Params.STATE to state))
