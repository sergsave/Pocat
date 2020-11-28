package com.sergsave.purryourcat.analytics

abstract class AnalyticsEvent(val name: String, val params: Map<String, Any?> = emptyMap())