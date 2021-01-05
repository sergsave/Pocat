package com.sergsave.pocat.screens.catcard

import android.app.Dialog
import android.os.Bundle
import androidx.fragment.app.DialogFragment
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.sergsave.pocat.R

class UnsavedChangesDialog: DialogFragment() {
    var onDiscardChangesListener: (() -> Unit)? = null

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        super.onCreateDialog(savedInstanceState)

        return MaterialAlertDialogBuilder(context,
            R.style.MyThemeOverlay_MaterialComponents_MaterialAlertDialog_MessageOnly)
            .setMessage(R.string.changes_not_saved)
            .setPositiveButton(R.string.discard, { _, _ -> onDiscardChangesListener?.invoke() })
            .setNegativeButton(R.string._continue, { _, _ -> })
            .create()
    }
}
