package com.sergsave.purryourcat.screens.settings.analytics

import com.sergsave.purryourcat.analytics.AnalyticsEvent
import com.sergsave.purryourcat.AnalyticsConstants.Events.VibrationSwitch

class VibrationSwitched(state: Boolean):
    AnalyticsEvent(VibrationSwitch.NAME, mapOf(VibrationSwitch.Params.STATE to state))
