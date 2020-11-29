package com.sergsave.purryourcat.screens.main.analytics

import com.sergsave.purryourcat.AnalyticsConstants.Events.AppStarted
import com.sergsave.purryourcat.AnalyticsConstants.Events.TabOpened
import com.sergsave.purryourcat.AnalyticsConstants.Events.UserCardClicked
import com.sergsave.purryourcat.AnalyticsConstants.Events.SampleCardClicked
import com.sergsave.purryourcat.AnalyticsConstants.Events.AddButtonClicked
import com.sergsave.purryourcat.AnalyticsConstants.Events.SettingsActionClicked
import com.sergsave.purryourcat.AnalyticsConstants.Events.DonateActionClicked
import com.sergsave.purryourcat.AnalyticsConstants.Events.AboutActionClicked
import com.sergsave.purryourcat.AnalyticsConstants.Events.CatsRemoved
import com.sergsave.purryourcat.analytics.AnalyticsEvent
import com.sergsave.purryourcat.screens.main.TabInfo

class AppStarted(): AnalyticsEvent(AppStarted.NAME)

private fun TabInfo.toConstant() = when(this) {
    TabInfo.SAMPLES -> TabOpened.TabType.SAMPLES
    TabInfo.USER_CATS -> TabOpened.TabType.USERS
}

class TabOpened(tab: TabInfo):
    AnalyticsEvent(TabOpened.NAME, mapOf(TabOpened.Params.TAB to tab.toConstant()))

class UserCardClicked(): AnalyticsEvent(UserCardClicked.NAME)

class SampleCardClicked(id: String):
    AnalyticsEvent(SampleCardClicked.NAME, mapOf(SampleCardClicked.Params.ID to id))

class AddButtonClicked(): AnalyticsEvent(AddButtonClicked.NAME)
class CatsRemoved(count: Int):
    AnalyticsEvent(CatsRemoved.NAME, mapOf(CatsRemoved.Params.COUNT to count))

class SettingsActionClicked(): AnalyticsEvent(SettingsActionClicked.NAME)
class AboutActionClicked(): AnalyticsEvent(AboutActionClicked.NAME)
class DonateActionClicked(): AnalyticsEvent(DonateActionClicked.NAME)