package com.sergsave.purryourcat.ui

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.sergsave.purryourcat.Constants
import com.sergsave.purryourcat.ui.catslist.CatsListActivity

// Telegram doesn't want open app in new window when MainActivity has "singleTask" launch mode.
// Therefore use additional activity.
class IntentForwardingActivity : AppCompatActivity() {

    override fun onDestroy() {
        super.onDestroy()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        forwardIntent(intent)
    }

    override fun onNewIntent(intent: Intent?) {
        super.onNewIntent(intent)
        forwardIntent(intent)
    }

    private fun forwardIntent(intent: Intent?) {
        val newIntent = Intent(this, CatsListActivity::class.java).apply {
            putExtra(Constants.IS_FORWARDED_INTENT_KEY, true)
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK)
            addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
            data = intent?.data
        }

        startActivity(newIntent)
        finish()
    }
}