package com.sergsave.pocat.dialogs

import android.app.Dialog
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.Settings
import androidx.annotation.StringRes
import androidx.fragment.app.DialogFragment
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.sergsave.pocat.R

class PermissionPermanentlyDeniedDialog(): DialogFragment() {
    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        super.onCreateDialog(savedInstanceState)

        val appName = getString(R.string.app_name)
        val permissionName = getString(arguments?.getInt(ARG_PERMISSION) ?: 0)
        return MaterialAlertDialogBuilder(requireContext())
            .setTitle(R.string.need_permission)
            .setMessage(getString(R.string.need_storage_permission, permissionName, appName))
            .setPositiveButton(R.string.show_settings, { _, _ -> goToSettings() })
            .create()
    }

    private fun goToSettings() =
        with(Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)) {
            data = Uri.fromParts("package", requireContext().packageName, null)
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            startActivity(this)
        }

    companion object {
        private const val ARG_PERMISSION = "ArgPermission"

        @JvmStatic
        fun newInstance(@StringRes permission: Int) =
            PermissionPermanentlyDeniedDialog().apply {
                arguments = Bundle().apply {
                    putInt(ARG_PERMISSION, permission)
                }
            }
    }
}
