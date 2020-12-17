package com.sergsave.pocat.screens.main

import android.app.Dialog
import android.os.Bundle
import androidx.fragment.app.DialogFragment
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.sergsave.pocat.R

class RemoveConfirmationDialog: DialogFragment() {
    var onRemoveConfirmListener: (() -> Unit)? = null

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        super.onCreateDialog(savedInstanceState)

        return MaterialAlertDialogBuilder(context)
            .setTitle(R.string.remove_confirmation)
            .setPositiveButton(R.string.yes, { _, _ -> onRemoveConfirmListener?.invoke() })
            .setNegativeButton(R.string.no, { _, _ -> })
            .create()
    }
}
