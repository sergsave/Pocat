package com.sergsave.pocat.analytics

import timber.log.Timber

class AnalyticsLoggingDecorator(private val tracker: AnalyticsTracker): AnalyticsTracker {
    override fun sendEvent(event: AnalyticsEvent) {
        tracker.sendEvent(event)
        Timber.i("Event. Name: ${event.name}. Params: ${event.params}")
    }

    override fun setProperty(property: AnalyticsProperty) {
        tracker.setProperty(property)
        Timber.i("Property. Name: ${property.name}. Param: ${property.param}")
    }
}