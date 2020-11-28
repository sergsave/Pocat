package com.sergsave.purryourcat.screens.main.analytics

import com.sergsave.purryourcat.AnalyticsConstants.Events.TabSwitched
import com.sergsave.purryourcat.AnalyticsConstants.Events.UserCardClicked
import com.sergsave.purryourcat.AnalyticsConstants.Events.SampleCardClicked
import com.sergsave.purryourcat.AnalyticsConstants.Events.AddButtonClicked
import com.sergsave.purryourcat.AnalyticsConstants.Events.SettingsActionClicked
import com.sergsave.purryourcat.AnalyticsConstants.Events.DonateActionClicked
import com.sergsave.purryourcat.AnalyticsConstants.Events.AboutActionClicked
import com.sergsave.purryourcat.analytics.AnalyticsEvent
import com.sergsave.purryourcat.screens.main.TabInfo

private fun TabInfo.toConstant() = when(this) {
    TabInfo.SAMPLES -> TabSwitched.TabType.SAMPLES
    TabInfo.USER_CATS -> TabSwitched.TabType.USERS
}

class TabSwitched(tab: TabInfo):
    AnalyticsEvent(TabSwitched.NAME, mapOf(TabSwitched.Params.TAB to tab.toConstant()))

class UserCardClicked(): AnalyticsEvent(UserCardClicked.NAME)

class SampleCardClicked(id: String):
    AnalyticsEvent(SampleCardClicked.NAME, mapOf(SampleCardClicked.Params.ID to id))

class AddButtonClicked(): AnalyticsEvent(AddButtonClicked.NAME)

class SettingsActionClicked(): AnalyticsEvent(SettingsActionClicked.NAME)
class AboutActionClicked(): AnalyticsEvent(AboutActionClicked.NAME)
class DonateActionClicked(): AnalyticsEvent(DonateActionClicked.NAME)