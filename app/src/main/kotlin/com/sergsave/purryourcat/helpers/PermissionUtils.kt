package com.sergsave.purryourcat.helpers

import androidx.fragment.app.Fragment
import android.content.pm.PackageManager.PERMISSION_GRANTED
import android.os.Bundle
import android.os.Parcelable
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import kotlinx.android.parcel.Parcelize

fun Fragment.isPermissionGranted(permission: String) =
    ContextCompat.checkSelfPermission(requireContext(), permission) == PERMISSION_GRANTED

fun checkPermissionRequestResult(grantResults: IntArray) =
    grantResults.isNotEmpty() && grantResults.all { it == PERMISSION_GRANTED }

// Based on https://stackoverflow.com/a/41304699
class PermissionDenyTypeQualifier(private val activity: AppCompatActivity,
                                  private val bundleTag: String) {

    @Parcelize
    private data class State(val permission2shouldShow: MutableMap<String, Boolean>): Parcelable

    private var state = State(mutableMapOf<String, Boolean>())

    enum class Type {
        DENIED,
        DENIED_FIRST_TIME,
        DENIED_PERMANENTLY,
        DENIED_PERMANENTLY_FIRST_TIME
    }

    fun onRestoreInstanceState(bundle: Bundle) {
        bundle.getParcelable<State>(bundleTag)?.let { state = it }
    }

    fun onSaveInstanceState(bundle: Bundle) {
        bundle.putParcelable(bundleTag, state)
    }

    fun onRequestPermission(permission: String) {
        state.permission2shouldShow[permission] =
            activity.shouldShowRequestPermissionRationale(permission)
    }

    // Return null if permission is granted or there was no call onRequestPermission before
    fun handleRequestPermissionResult(permission: String): Type? {
        val cached = state.permission2shouldShow[permission]
        if (cached == null || activity.checkSelfPermission(permission) == PERMISSION_GRANTED)
            return null

        val current = activity.shouldShowRequestPermissionRationale(permission)
        return when {
            cached && current -> Type.DENIED
            cached.not() && current -> Type.DENIED_FIRST_TIME
            cached && current.not() -> Type.DENIED_PERMANENTLY_FIRST_TIME
            cached.not() && current.not() -> Type.DENIED_PERMANENTLY
            else -> null
        }
    }
}
