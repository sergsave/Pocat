package com.sergsave.purryourcat.screens.main.analytics

import com.sergsave.purryourcat.AnalyticsConstants.Events.TabSwitched
import com.sergsave.purryourcat.analytics.AnalyticsEvent
import com.sergsave.purryourcat.screens.main.TabInfo

fun TabInfo.toConstant(): String {
    return when(this) {
        TabInfo.SAMPLES -> TabSwitched.TabType.SAMPLES
        TabInfo.USER_CATS -> TabSwitched.TabType.USERS
    }
}

class TabSwitchedEvent(val tab: TabInfo):
    AnalyticsEvent(TabSwitched.NAME, mapOf(TabSwitched.Params.TAB_TYPE to tab.toConstant()))