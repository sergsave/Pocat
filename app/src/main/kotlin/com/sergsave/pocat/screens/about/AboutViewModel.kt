package com.sergsave.pocat.screens.about

import android.app.Activity
import androidx.lifecycle.ViewModel
import com.sergsave.pocat.apprate.AppRateManager

class AboutViewModel(private val appRateManager: AppRateManager): ViewModel() {
    fun askToRate(activity: Activity) = appRateManager.askToRate(activity)
}