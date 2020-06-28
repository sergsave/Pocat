package com.sergsave.purryourcat.activities

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.sergsave.purryourcat.activities.MainActivity

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
        val newIntent = Intent(this, MainActivity::class.java)
        newIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        newIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK)
        newIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
        newIntent.setData(intent?.data)
        startActivity(newIntent)
        finish()
    }
}