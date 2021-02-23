package com.sergsave.pocat.screens.main

import android.app.Activity
import android.content.Intent
import com.sergsave.pocat.Constants
import com.sergsave.pocat.models.Card
import com.sergsave.pocat.screens.about.AboutActivity
import com.sergsave.pocat.screens.catcard.CatCardActivity
import com.sergsave.pocat.screens.donate.DonateActivity
import com.sergsave.pocat.screens.settings.SettingsActivity
import com.sergsave.pocat.screens.testing.TestingActivity

fun Activity.launchCatCard(activityRequestCode: Int) {
    startActivityForResult(Intent(this, CatCardActivity::class.java),
        activityRequestCode)
}

fun Activity.launchCatCard(activityRequestCode: Int, forwardedIntent: Intent) {
    val intent = Intent(this, CatCardActivity::class.java)
    intent.putExtra(Constants.SHARING_INPUT_INTENT_KEY, forwardedIntent)

    startActivityForResult(intent, activityRequestCode)
}

fun Activity.launchCatCard(activityRequestCode: Int, card: Card,
                           transition: SharedElementTransitionData) {
    val intent = Intent(this, CatCardActivity::class.java)
    intent.putExtra(Constants.CARD_INTENT_KEY, card)
    intent.putExtra(Constants.SHARED_TRANSITION_NAME_INTENT_KEY, transition.name)

    startActivityForResult(intent, activityRequestCode, transition.sceneTransitionAnimation)
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
