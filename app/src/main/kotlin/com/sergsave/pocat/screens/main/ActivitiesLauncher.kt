package com.sergsave.pocat.screens.main

import android.app.Activity
import android.content.Intent
import android.view.View
import androidx.core.app.ActivityOptionsCompat
import com.sergsave.pocat.Constants
import com.sergsave.pocat.models.Card
import com.sergsave.pocat.screens.about.AboutActivity
import com.sergsave.pocat.screens.catcard.CatCardActivity
import com.sergsave.pocat.screens.donate.DonateActivity
import com.sergsave.pocat.screens.settings.SettingsActivity
import com.sergsave.pocat.screens.testing.TestingActivity

fun Activity.launchCatCard() {
    startActivity(Intent(this, CatCardActivity::class.java))
}

fun Activity.launchCatCard(forwardedIntent: Intent) {
    val intent = Intent(this, CatCardActivity::class.java)
    intent.putExtra(Constants.SHARING_INPUT_INTENT_KEY, forwardedIntent)

    startActivity(intent)
}

fun Activity.launchCatCard(card: Card, sharedElement: View, sharedElementTransitionName: String) {
    val intent = Intent(this, CatCardActivity::class.java)
    intent.putExtra(Constants.CARD_INTENT_KEY, card)
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

fun Activity.launchTesting() {
    startActivity(Intent(this, TestingActivity::class.java))
}
