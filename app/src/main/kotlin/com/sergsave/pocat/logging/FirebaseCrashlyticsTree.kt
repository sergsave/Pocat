package com.sergsave.pocat.logging

import android.util.Log
import com.google.firebase.crashlytics.FirebaseCrashlytics
import timber.log.Timber

class FirebaseCrashlyticsTree: Timber.Tree() {
    override fun log(priority: Int, tag: String?, message: String, t: Throwable?) {
        if (priority != Log.ERROR)
            return

        t?.let { FirebaseCrashlytics.getInstance().recordException(it) }
    }
}
