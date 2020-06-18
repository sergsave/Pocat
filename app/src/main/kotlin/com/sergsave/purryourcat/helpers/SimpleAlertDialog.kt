package com.sergsave.purryourcat.helpers

import androidx.fragment.app.DialogFragment
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import android.app.AlertDialog
import android.app.Dialog
import android.content.Context
import android.content.DialogInterface
import android.os.Bundle

class SimpleAlertDialog(
    private val parentContext: Context,
    private val title: String,
    private val buttons: Map<Button, String>): DialogFragment() {

    enum class Button {
        POSITIVE, NEGATIVE
    }

    interface Listener {
        fun onDialogPositiveClick(dialog: DialogFragment?)
        fun onDialogNegativeClick(dialog: DialogFragment?)
    }

    var listener: Listener? = null

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val builder = MaterialAlertDialogBuilder(parentContext)
        builder.setTitle(title).apply {

            if(buttons.contains(Button.POSITIVE)) {
                setPositiveButton(buttons.get(Button.POSITIVE),
                    DialogInterface.OnClickListener { _, _ ->
                        listener?.onDialogPositiveClick(this@SimpleAlertDialog)
                    })
            }

            if(buttons.contains(Button.NEGATIVE)) {
                setNegativeButton(buttons.get(Button.NEGATIVE),
                    DialogInterface.OnClickListener { _, _ ->
                        listener?.onDialogNegativeClick(this@SimpleAlertDialog)
                    })
            }
        }
        return builder.create()
    }
}