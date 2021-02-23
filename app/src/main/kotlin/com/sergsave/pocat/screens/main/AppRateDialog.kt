package com.sergsave.pocat.screens.main

import android.app.Dialog
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.DialogFragment
import com.codemybrainsout.ratingdialog.RatingDialog
import com.sergsave.pocat.R
import com.sergsave.pocat.helpers.openRateAppLink
import kotlin.math.roundToInt

class AppRateDialog: DialogFragment() {
    // rating from 1 to 5
    var onAppRatedListener: ((rating: Int) -> Unit)? = null

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {

        val title = getString(R.string.apprate_dialog_title)
        val positiveText = getString(R.string.apprate_dialog_ask_later)
        val negativeText = getString(R.string.apprate_dialog_ask_never)

        return RatingDialog.Builder(requireActivity())
            .threshold(3f)
            .session(3)
            .title(title)
            .positiveButtonText(positiveText)
            .negativeButtonText(negativeText)
            .onThresholdCleared { ratingDialog, _, _ ->
                ratingDialog.dismiss()
                (activity as? AppCompatActivity)?.openRateAppLink()
            }
            .onThresholdFailed { ratingDialog, _, _ ->
                ratingDialog.dismiss()
                activity?.let { FeedbackEmailDialog().show(it.supportFragmentManager, null) }
            }
            .onRatingChanged { rating, _ ->  onAppRatedListener?.invoke(rating.roundToInt())}
            .build()
    }
}