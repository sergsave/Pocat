package com.sergsave.pocat.helpers

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import androidx.appcompat.app.AppCompatActivity

fun AppCompatActivity.openRateAppLink() {
    val marketIntent = makeRateIntent(this, "market://details")
    val storeIntent = makeRateIntent(this, "https://play.google.com/store/apps/details")

    if (marketIntent.resolveActivity(packageManager) != null)
        startActivity(marketIntent)
    else if (storeIntent.resolveActivity(packageManager) != null)
        startActivity(storeIntent)
}

private fun makeRateIntent(context: Context, baseUrl: String): Intent {
    val url = Uri.parse("$baseUrl?id=${context.packageName}")

    val extraFlag = if (Build.VERSION.SDK_INT >= 21)
        Intent.FLAG_ACTIVITY_NEW_DOCUMENT
    else {
        @Suppress("DEPRECATION")
        Intent.FLAG_ACTIVITY_CLEAR_WHEN_TASK_RESET
    }

    return Intent(Intent.ACTION_VIEW, url).apply {
        addFlags(Intent.FLAG_ACTIVITY_NO_HISTORY or Intent.FLAG_ACTIVITY_MULTIPLE_TASK or extraFlag)
    }
}