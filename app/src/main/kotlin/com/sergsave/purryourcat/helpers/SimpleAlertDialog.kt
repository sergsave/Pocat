package com.sergsave.purryourcat.helpers

import androidx.fragment.app.DialogFragment
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import android.app.AlertDialog
import android.app.Dialog
import android.content.DialogInterface
import android.os.Bundle
import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

class SimpleAlertDialog(): DialogFragment() {

    private var title: String? = null
    private var buttons: Map<Button, String>? = null

    enum class Button {
        POSITIVE, NEGATIVE
    }

    interface Listener {
        fun onDialogPositiveClick(dialog: DialogFragment?)
        fun onDialogNegativeClick(dialog: DialogFragment?)
    }

    var listener: Listener? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        arguments?.let {
            val cache = it.getParcelable<CachedData>(ARG_CACHED_DATA)
            title = cache?.title
            buttons = cache?.buttons?.mapKeys  { (k, _) -> Button.values().get(k) }
        }
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val builder = MaterialAlertDialogBuilder(context)
        builder.setTitle(title).apply {

            if(buttons?.contains(Button.POSITIVE) ?: false) {
                setPositiveButton(buttons?.get(Button.POSITIVE),
                    DialogInterface.OnClickListener { _, _ ->
                        listener?.onDialogPositiveClick(this@SimpleAlertDialog)
                    })
            }

            if(buttons?.contains(Button.NEGATIVE) ?: false) {
                setNegativeButton(buttons?.get(Button.NEGATIVE),
                    DialogInterface.OnClickListener { _, _ ->
                        listener?.onDialogNegativeClick(this@SimpleAlertDialog)
                    })
            }
        }
        return builder.create()
        }

    // It's not possible to put Map in Bundle directly. Map is not parcelable :(
    @Parcelize
    private class CachedData(val title: String?, val buttons: Map<Int, String>) : Parcelable

    companion object {
        private val ARG_CACHED_DATA = "ArgCached"

        @JvmStatic
        fun newInstance(title: String, buttons: Map<Button, String>) =
            SimpleAlertDialog().apply {
                arguments = Bundle().apply {
                    val cache = CachedData(title, buttons.mapKeys { (k, _) -> k.ordinal  })
                    putParcelable(ARG_CACHED_DATA, cache)
                }
            }
    }
}