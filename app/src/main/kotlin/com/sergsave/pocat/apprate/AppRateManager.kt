package com.sergsave.pocat.apprate

import android.app.Activity
import android.content.Context
import com.google.android.play.core.review.ReviewManagerFactory

class AppRateManager(context: Context) {

    private val manager = ReviewManagerFactory.create(context)

    fun askToRate(activity: Activity) {
        manager
            .requestReviewFlow()
            .addOnCompleteListener { request ->
                if (request.isSuccessful) {
                    val reviewInfo = request.result
                    val flow = manager.launchReviewFlow(activity, reviewInfo)
                    flow.addOnCompleteListener { _ ->
                        // The flow has finished. The API does not indicate whether the user
                        // reviewed or not, or even whether the review dialog was shown. Thus, no
                        // matter the result, we continue our app flow.
                    }
                }
        }
    }
}
