package com.sergsave.purryourcat.dialogs

import android.app.Dialog
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.Settings
import androidx.fragment.app.DialogFragment
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.sergsave.purryourcat.R

class StoragePermissionPermanentlyDeniedDialog: DialogFragment() {
    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        super.onCreateDialog(savedInstanceState)

        return MaterialAlertDialogBuilder(requireContext())
            .setTitle(R.string.need_permission)
            .setMessage(R.string.need_storage_permission)
            .setPositiveButton(R.string.show_settings, { _, _ -> goToSettings() })
            .create()
    }

    private fun goToSettings() =
        with(Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)) {
            data = Uri.fromParts("package", requireContext().packageName, null)
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            startActivity(this)
        }
}
