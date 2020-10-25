package com.sergsave.purryourcat.ui.catcard

import android.app.Dialog
import android.os.Bundle
import androidx.fragment.app.DialogFragment
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.sergsave.purryourcat.R

class RemoveConfirmationDialog: DialogFragment() {
    var onRemoveConfirmListener: (() -> Unit)? = null

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        super.onCreateDialog(savedInstanceState)

        val resources = requireContext().resources
        val positiveText = resources.getString(R.string.yes)
        val negativeText = resources.getString(R.string.no)
        val message = resources.getString(R.string.remove_confirmation)

        val builder = MaterialAlertDialogBuilder(context)
        builder.setTitle(message).apply {
            setPositiveButton(positiveText, { _, _ -> onRemoveConfirmListener?.invoke() })
            setNegativeButton(negativeText, { _, _ -> })
        }
        return builder.create()
    }
}
