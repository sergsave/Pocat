package com.sergsave.pocat.screens.soundselection.analytics

import com.sergsave.pocat.analytics.AnalyticsEvent
import com.sergsave.pocat.AnalyticsConstants.Events.AudioFromMemoryClick
import com.sergsave.pocat.AnalyticsConstants.Events.AudioFromRecorderClick
import com.sergsave.pocat.AnalyticsConstants.Events.AudioFromSamplesClick
import com.sergsave.pocat.AnalyticsConstants.Events.AudioSizeError
import com.sergsave.pocat.AnalyticsConstants.Events.RecorderNotFound

class AudioSizeError: AnalyticsEvent(AudioSizeError.NAME)

class AudioFromMemoryClick: AnalyticsEvent(AudioFromMemoryClick.NAME)
class AudioFromRecorderClick: AnalyticsEvent(AudioFromRecorderClick.NAME)
class AudioFromSamplesClick: AnalyticsEvent(AudioFromSamplesClick.NAME)

class RecorderNotFound: AnalyticsEvent(RecorderNotFound.NAME)
