package com.sergsave.pocat.screens.main

import android.app.Dialog
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.DialogFragment
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.sergsave.pocat.R
import com.sergsave.pocat.helpers.sendEmail

class FeedbackEmailDialog: DialogFragment() {
    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        return MaterialAlertDialogBuilder(context,
            R.style.MyThemeOverlay_MaterialComponents_MaterialAlertDialog_MessageOnly)
            .setMessage(R.string.feedback_dialog_title)
            .setPositiveButton(R.string.feedback_dialog_positive_button, { _, _ ->
                val address = getString(R.string.feedback_email)
                val subject = getString(
                    R.string.feedback_message_subject,
                    getString(R.string.app_name))

                (activity as? AppCompatActivity?)?.sendEmail(address, subject)
            })
            .setNegativeButton(R.string.feedback_dialog_negative_button, { _, _ -> })
            .create()
    }
}