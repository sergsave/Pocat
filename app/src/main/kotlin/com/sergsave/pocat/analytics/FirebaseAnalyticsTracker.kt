package com.sergsave.pocat.analytics

import android.os.Bundle
import com.google.firebase.analytics.ktx.analytics
import com.google.firebase.ktx.Firebase

class FirebaseAnalyticsTracker: AnalyticsTracker {
    override fun sendEvent(event: AnalyticsEvent) {
        val bundle = Bundle().apply {
            event.params.forEach { (key, value) -> value?.let { putString(key, it.toString()) } }
        }
        Firebase.analytics.logEvent(event.name, bundle)
    }

    override fun setProperty(property: AnalyticsProperty) {
        Firebase.analytics.setUserProperty(property.name, property.param.toString())
    }
}