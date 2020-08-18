package com.sergsave.purryourcat.helpers

import android.app.Dialog
import android.os.Bundle
import android.os.Parcelable
import androidx.fragment.app.DialogFragment
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import kotlinx.android.parcel.Parcelize

class SimpleAlertDialog : DialogFragment() {

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

            if(buttons?.contains(Button.POSITIVE) == true) {
                setPositiveButton(buttons?.get(Button.POSITIVE), { _, _ ->
                        listener?.onDialogPositiveClick(this@SimpleAlertDialog)
                    })
            }

            if(buttons?.contains(Button.NEGATIVE) == true) {
                setNegativeButton(buttons?.get(Button.NEGATIVE), { _, _ ->
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
        private const val ARG_CACHED_DATA = "ArgCached"

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