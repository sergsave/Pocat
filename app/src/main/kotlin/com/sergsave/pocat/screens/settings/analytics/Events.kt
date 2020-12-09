package com.sergsave.pocat.screens.settings.analytics

import com.sergsave.pocat.analytics.AnalyticsEvent
import com.sergsave.pocat.AnalyticsConstants.Events.VibrationSwitch

class VibrationSwitched(state: Boolean):
    AnalyticsEvent(VibrationSwitch.NAME, mapOf(VibrationSwitch.Params.STATE to state))
