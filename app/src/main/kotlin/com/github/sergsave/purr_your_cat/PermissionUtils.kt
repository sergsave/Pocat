package com.github.sergsave.purr_your_cat

import android.app.Activity
import android.content.pm.PackageManager
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import android.os.Build.VERSION
import android.os.Build.VERSION_CODES

class PermissionUtils {
    companion object {

        fun checkPermission(activity: Activity, name: String) : Boolean
        {
            return VERSION.SDK_INT < VERSION_CODES.M ||
                    ContextCompat.checkSelfPermission(activity, name) == PackageManager.PERMISSION_GRANTED
        }

        fun requestPermissions(activity: Activity, names: Array<String>, code: Int) {
            ActivityCompat.requestPermissions(activity, names, code)
        }

        fun checkRequestResult(grantResults: IntArray) : Boolean
        {
            return grantResults.size > 0 && grantResults.all { it == PackageManager.PERMISSION_GRANTED }
        }
    }
}