package com.sergsave.purryourcat.ui.catcard

import android.app.Dialog
import android.os.Bundle
import androidx.fragment.app.DialogFragment
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.sergsave.purryourcat.R

class UnsavedChangesDialog: DialogFragment() {
    var onDiscardChangesListener: (() -> Unit)? = null

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        super.onCreateDialog(savedInstanceState)
        // TODO: simplify dialogs
        val resources = requireContext().resources
        val positiveText = resources.getString(R.string.discard)
        val negativeText = resources.getString(R.string._continue)
        val message = resources.getString(R.string.changes_not_saved)

        val builder = MaterialAlertDialogBuilder(context)
        builder.setTitle(message).apply {
            setPositiveButton(positiveText, { _, _ -> onDiscardChangesListener?.invoke() })
            setNegativeButton(negativeText, { _, _ -> })
        }
        return builder.create()
    }
}
