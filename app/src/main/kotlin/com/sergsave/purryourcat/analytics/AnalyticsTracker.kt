package com.sergsave.pocat.analytics

interface AnalyticsTracker {
    fun sendEvent(event: AnalyticsEvent)
    fun setProperty(property: AnalyticsProperty)
}