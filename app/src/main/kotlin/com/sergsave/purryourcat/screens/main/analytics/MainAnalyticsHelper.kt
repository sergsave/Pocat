package com.sergsave.purryourcat.screens.main.analytics

import com.sergsave.purryourcat.analytics.AnalyticsTracker
import com.sergsave.purryourcat.screens.main.TabInfo
import com.sergsave.purryourcat.R

class MainAnalyticsHelper(private val tracker: AnalyticsTracker) {
    fun onTabSwitched(tab: TabInfo) = tracker.sendEvent(TabSwitched(tab))

    fun onUserCardClicked() = tracker.sendEvent(UserCardClicked())
    fun onSampleCardClicked(id: String) = tracker.sendEvent(SampleCardClicked(id))

    fun onAddClicked() = tracker.sendEvent(AddButtonClicked())

    fun onOptionsItemSelected(menuId: Int) {
        when(menuId) {
            R.id.action_settings -> tracker.sendEvent(SettingsActionClicked())
            R.id.action_donate -> tracker.sendEvent(DonateActionClicked())
            R.id.action_about -> tracker.sendEvent(AboutActionClicked())
        }
    }
}