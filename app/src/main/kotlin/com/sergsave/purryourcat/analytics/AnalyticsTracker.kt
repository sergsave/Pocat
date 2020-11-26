package com.sergsave.purryourcat.analytics

interface AnalyticsTracker {
    fun sendEvent(event: AnalyticsEvent)
    fun setProperty(property: AnalyticsProperty)
}