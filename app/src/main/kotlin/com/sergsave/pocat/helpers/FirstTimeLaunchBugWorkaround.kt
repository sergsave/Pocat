package com.sergsave.pocat.helpers

import android.app.Activity
import android.content.Intent
import android.util.Log

// On first launch the root activity relaunch after pressing the home button and returning to the application
// https://issuetracker.google.com/issues/36907463
// https://stackoverflow.com/questions/4341600/how-to-prevent-multiple-instances-of-an-activity-when-it-is-launched-with-differ

object FirstTimeLaunchBugWorkaround {
    fun needFinishOnCreate(mainActivity: Activity): Boolean {
        if (mainActivity.isTaskRoot)
            return false

        val intent = mainActivity.intent
        if (intent.hasCategory(Intent.CATEGORY_LAUNCHER) && Intent.ACTION_MAIN == intent.action) {
            Log.w("LaunchBugWorkaround",
                "Main Activity is not the root. Finishing Main Activity instead of launching.")
            return true
        }

        return false
    }
}
