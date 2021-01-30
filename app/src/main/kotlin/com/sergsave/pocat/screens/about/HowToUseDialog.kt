package com.sergsave.pocat.screens.about

import android.app.Dialog
import android.os.Bundle
import androidx.fragment.app.DialogFragment
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.sergsave.pocat.R

class HowToUseDialog: DialogFragment() {
    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        super.onCreateDialog(savedInstanceState)

        return MaterialAlertDialogBuilder(requireContext(),
            R.style.MyThemeOverlay_MaterialComponents_MaterialAlertDialog_MessageOnly)
            .setMessage(R.string.about_how_to_use_dialog_text)
            .setPositiveButton(R.string.ok, { _, _ -> })
            .create()
    }
}
