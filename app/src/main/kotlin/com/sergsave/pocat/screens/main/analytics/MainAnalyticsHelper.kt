package com.sergsave.pocat.screens.main.analytics

import com.sergsave.pocat.R
import com.sergsave.pocat.analytics.AnalyticsTracker
import com.sergsave.pocat.screens.main.TabInfo

class MainAnalyticsHelper(private val tracker: AnalyticsTracker) {
    fun onAppStarted() = tracker.sendEvent(AppStarted())

    fun onCatsReadedFromRepo(catsCount: Int) = tracker.sendEvent(UsersCatsListUpdate(catsCount))

    fun onTabOpened(tab: TabInfo) = tracker.sendEvent(TabOpen(tab))

    fun onUserCardClicked() = tracker.sendEvent(UsersCatCardClick())
    fun onSampleCardClicked(id: String) = tracker.sendEvent(SampleCardClick(id))

    fun onAddClicked() = tracker.sendEvent(AddButtonClick())

    fun onCatsRemoved(count: Int) = tracker.sendEvent(CatsRemove(count))

    fun onAppRateShowed() = tracker.sendEvent(AppRateShowed())
    fun onAppRateAccepted() = tracker.sendEvent(AppRateAccepted())
    fun onAppRateDeclined() = tracker.sendEvent(AppRateDeclined())
    fun onAppRatePostponed() = tracker.sendEvent(AppRatePostponed())

    fun onOptionsItemSelected(menuId: Int) {
        when(menuId) {
            R.id.action_settings -> tracker.sendEvent(SettingsActionClick())
            R.id.action_donate -> tracker.sendEvent(DonateActionClick())
            R.id.action_about -> tracker.sendEvent(AboutActionClick())
        }
    }
}