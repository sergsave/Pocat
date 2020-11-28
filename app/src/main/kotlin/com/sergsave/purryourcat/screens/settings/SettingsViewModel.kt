package com.sergsave.purryourcat.screens.settings

import androidx.lifecycle.ViewModel
import com.sergsave.purryourcat.R
import com.sergsave.purryourcat.screens.main.analytics.MainAnalyticsHelper
import com.sergsave.purryourcat.screens.settings.analytics.SettingsAnalyticsHelper

class SettingsViewModel(private val analytics: SettingsAnalyticsHelper) : ViewModel() {
    fun onVibrationSwitched(state: Boolean) = analytics.onVibrationSwitched(state)
}

