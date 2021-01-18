package com.sergsave.pocat.screens.main.analytics

import com.sergsave.pocat.AnalyticsConstants.Events.AppStart
import com.sergsave.pocat.AnalyticsConstants.Events.TabOpen
import com.sergsave.pocat.AnalyticsConstants.Events.UsersCatCardClick
import com.sergsave.pocat.AnalyticsConstants.Events.SampleCardClick
import com.sergsave.pocat.AnalyticsConstants.Events.AddButtonClick
import com.sergsave.pocat.AnalyticsConstants.Events.SettingsActionClick
import com.sergsave.pocat.AnalyticsConstants.Events.DonateActionClick
import com.sergsave.pocat.AnalyticsConstants.Events.AboutActionClick
import com.sergsave.pocat.AnalyticsConstants.Events.CatsRemove
import com.sergsave.pocat.AnalyticsConstants.Events.UsersCatsListUpdate
import com.sergsave.pocat.analytics.AnalyticsEvent
import com.sergsave.pocat.screens.main.TabInfo

class AppStarted : AnalyticsEvent(AppStart.NAME)

private fun TabInfo.toConstant() = when(this) {
    TabInfo.SAMPLES -> TabOpen.TabType.SAMPLES
    TabInfo.USER_CATS -> TabOpen.TabType.USERS
}

class TabOpen(tab: TabInfo):
    AnalyticsEvent(TabOpen.NAME, mapOf(TabOpen.Params.TAB to tab.toConstant()))

class UsersCatCardClick : AnalyticsEvent(UsersCatCardClick.NAME)

class SampleCardClick(id: String):
    AnalyticsEvent(SampleCardClick.NAME, mapOf(SampleCardClick.Params.ID to id))

class AddButtonClick : AnalyticsEvent(AddButtonClick.NAME)
class CatsRemove(count: Int):
    AnalyticsEvent(CatsRemove.NAME, mapOf(CatsRemove.Params.COUNT to count))

class UsersCatsListUpdate(count: Int):
    AnalyticsEvent(UsersCatsListUpdate.NAME, mapOf(UsersCatsListUpdate.Params.COUNT to count))

class SettingsActionClick : AnalyticsEvent(SettingsActionClick.NAME)
class AboutActionClick : AnalyticsEvent(AboutActionClick.NAME)
class DonateActionClick : AnalyticsEvent(DonateActionClick.NAME)
