package com.sergsave.purryourcat.ui.catcard

import android.app.Dialog
import android.os.Bundle
import androidx.fragment.app.DialogFragment
import com.sergsave.purryourcat.helpers.SimpleAlertDialog

class UnsavedChangesDialog: DialogFragment() {
    private var lateinit simpleDialog: SimpleAlertDialog

    val onDiscardChangesListener: (() -> Unit)? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val positiveText = context.resources.getString(R.string.discard)
        val negativeText = context.resources.getString(R.string._continue)
        val message = context.resources.getString(R.string.changes_not_saved)

        val buttons = mapOf(
            SimpleAlertDialog.Button.POSITIVE to positiveText,
            SimpleAlertDialog.Button.NEGATIVE to negativeText)

        simpleDialog = SimpleAlertDialog.newInstance(message, buttons)
        simpleDialog.listener = object: SimpleAlertDialog.Listener {
            override fun onDialogNegativeClick(dialog: DialogFragment?) { }
            override fun onDialogPositiveClick(dialog: DialogFragment?) {
                onDiscardChangesListener?.invoke()
            }
        }
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        return simpleDialog.dialog
    }
}

class NotValidDataDialog: DialogFragment() {
    private var lateinit simpleDialog: SimpleAlertDialog

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        return SimpleAlertDialog.newInstance(
            context.resources.getString(R.string.fill_the_form),
            mapOf(SimpleAlertDialog.Button.POSITIVE to context.resources.getString(R.string.ok))
        )
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        return simpleDialog.dialog
    }
}
