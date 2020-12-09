package com.sergsave.pocat.screens.main.analytics

import com.sergsave.pocat.analytics.AnalyticsTracker
import com.sergsave.pocat.screens.main.TabInfo
import com.sergsave.pocat.R

class MainAnalyticsHelper(private val tracker: AnalyticsTracker) {
    fun onAppStarted() = tracker.sendEvent(AppStarted())

    fun onTabOpened(tab: TabInfo) = tracker.sendEvent(TabOpen(tab))

    fun onUserCardClicked() = tracker.sendEvent(UserCardClick())
    fun onSampleCardClicked(id: String) = tracker.sendEvent(SampleCardClick(id))

    fun onAddClicked() = tracker.sendEvent(AddButtonClick())

    fun onCatsRemoved(count: Int) = tracker.sendEvent(CatsRemove(count))

    fun onOptionsItemSelected(menuId: Int) {
        when(menuId) {
            R.id.action_settings -> tracker.sendEvent(SettingsActionClick())
            R.id.action_donate -> tracker.sendEvent(DonateActionClick())
            R.id.action_about -> tracker.sendEvent(AboutActionClick())
        }
    }
}