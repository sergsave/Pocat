package com.sergsave.purryourcat.screens.main.analytics

import com.sergsave.purryourcat.AnalyticsConstants.Events.AppStart
import com.sergsave.purryourcat.AnalyticsConstants.Events.TabOpen
import com.sergsave.purryourcat.AnalyticsConstants.Events.UserCardClick
import com.sergsave.purryourcat.AnalyticsConstants.Events.SampleCardClick
import com.sergsave.purryourcat.AnalyticsConstants.Events.AddButtonClick
import com.sergsave.purryourcat.AnalyticsConstants.Events.SettingsActionClick
import com.sergsave.purryourcat.AnalyticsConstants.Events.DonateActionClick
import com.sergsave.purryourcat.AnalyticsConstants.Events.AboutActionClick
import com.sergsave.purryourcat.AnalyticsConstants.Events.CatsRemove
import com.sergsave.purryourcat.analytics.AnalyticsEvent
import com.sergsave.purryourcat.screens.main.TabInfo

class AppStarted(): AnalyticsEvent(AppStart.NAME)

private fun TabInfo.toConstant() = when(this) {
    TabInfo.SAMPLES -> TabOpen.TabType.SAMPLES
    TabInfo.USER_CATS -> TabOpen.TabType.USERS
}

class TabOpen(tab: TabInfo):
    AnalyticsEvent(TabOpen.NAME, mapOf(TabOpen.Params.TAB to tab.toConstant()))

class UserCardClick(): AnalyticsEvent(UserCardClick.NAME)

class SampleCardClick(id: String):
    AnalyticsEvent(SampleCardClick.NAME, mapOf(SampleCardClick.Params.ID to id))

class AddButtonClick(): AnalyticsEvent(AddButtonClick.NAME)
class CatsRemove(count: Int):
    AnalyticsEvent(CatsRemove.NAME, mapOf(CatsRemove.Params.COUNT to count))

class SettingsActionClick(): AnalyticsEvent(SettingsActionClick.NAME)
class AboutActionClick(): AnalyticsEvent(AboutActionClick.NAME)
class DonateActionClick(): AnalyticsEvent(DonateActionClick.NAME)