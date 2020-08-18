package com.sergsave.purryourcat.helpers

import android.app.Activity
import androidx.fragment.app.Fragment
import android.content.Context
import android.content.pm.PackageManager
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import android.os.Build.VERSION
import android.os.Build.VERSION_CODES

class PermissionUtils {
    companion object {

        fun checkPermission(context: Context, name: String) : Boolean
        {
            return VERSION.SDK_INT < VERSION_CODES.M ||
                    ContextCompat.checkSelfPermission(context, name) == PackageManager.PERMISSION_GRANTED
        }

        fun requestPermissions(activity: Activity, names: Array<String>, code: Int) {
            ActivityCompat.requestPermissions(activity, names, code)
        }

        fun requestPermissions(fragment: Fragment, names: Array<String>, code: Int) {
            fragment.requestPermissions(names, code)
        }

        fun checkRequestResult(grantResults: IntArray) : Boolean
        {
            return grantResults.isNotEmpty() && grantResults.all { it == PackageManager.PERMISSION_GRANTED }
        }
    }
}