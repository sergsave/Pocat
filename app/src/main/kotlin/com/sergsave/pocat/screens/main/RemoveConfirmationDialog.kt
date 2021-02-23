package com.sergsave.pocat.screens.main

import android.app.Dialog
import android.os.Bundle
import androidx.fragment.app.DialogFragment
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.sergsave.pocat.R

class RemoveConfirmationDialog: DialogFragment() {
    var onRemoveConfirmListener: (() -> Unit)? = null

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        return MaterialAlertDialogBuilder(context,
            R.style.MyThemeOverlay_MaterialComponents_MaterialAlertDialog_MessageOnly)
            .setMessage(R.string.main_remove_dialog_text)
            .setPositiveButton(R.string.yes, { _, _ -> onRemoveConfirmListener?.invoke() })
            .setNegativeButton(R.string.no, { _, _ -> })
            .create()
    }
}
