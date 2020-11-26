package com.sergsave.purryourcat.screens.main.analytics

import com.sergsave.purryourcat.analytics.AnalyticsTracker
import com.sergsave.purryourcat.screens.main.TabInfo

class MainAnalyticsHelper(private val tracker: AnalyticsTracker) {
    fun onTabSwitched(tab: TabInfo) = tracker.sendEvent(TabSwitchedEvent(tab))
}