package com.sergsave.purryourcat.ui.main

import android.app.Activity
import android.content.Intent
import android.view.View
import androidx.core.app.ActivityOptionsCompat
import com.sergsave.purryourcat.Constants
import com.sergsave.purryourcat.models.CatCard
import com.sergsave.purryourcat.ui.about.AboutActivity
import com.sergsave.purryourcat.ui.catcard.CatCardActivity
import com.sergsave.purryourcat.ui.donate.DonateActivity
import com.sergsave.purryourcat.ui.settings.SettingsActivity

fun Activity.launchCatCard() {
    startActivity(Intent(this, CatCardActivity::class.java))
}

fun Activity.launchCatCard(forwardedIntent: Intent) {
    val intent = Intent(this, CatCardActivity::class.java)
    intent.putExtra(Constants.SHARING_INPUT_INTENT_KEY, forwardedIntent)

    startActivity(intent)
}

fun Activity.launchCatCard(card: CatCard, sharedElement: View,
                                   sharedElementTransitionName: String) {
    val intent = Intent(this, CatCardActivity::class.java)
    intent.putExtra(Constants.CAT_ID_INTENT_KEY, card.dataRepoId)
    intent.putExtra(Constants.SHARED_TRANSITION_NAME_INTENT_KEY, sharedElementTransitionName)

    val transitionOption = ActivityOptionsCompat.makeSceneTransitionAnimation(
        this, sharedElement, sharedElementTransitionName)

    startActivity(intent, transitionOption.toBundle())
}

fun Activity.launchSettings() {
    startActivity(Intent(this, SettingsActivity::class.java))
}

fun Activity.launchAbout() {
    startActivity(Intent(this, AboutActivity::class.java))
}

fun Activity.launchDonate() {
    startActivity(Intent(this, DonateActivity::class.java))
}
