package com.sergsave.pocat.screens.settings

import androidx.lifecycle.ViewModel
import com.sergsave.pocat.screens.settings.analytics.SettingsAnalyticsHelper

class SettingsViewModel(private val analytics: SettingsAnalyticsHelper) : ViewModel() {
    fun onVibrationSwitched(state: Boolean) = analytics.onVibrationSwitched(state)
}

