package com.sergsave.pocat.helpers

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar

fun AppCompatActivity.setToolbarAsActionBar(toolbar: Toolbar, showBackButton: Boolean) {
    setSupportActionBar(toolbar)

    supportActionBar?.apply {
        setDisplayHomeAsUpEnabled(showBackButton)
        setDisplayShowHomeEnabled(showBackButton)
    }

    if(showBackButton)
        toolbar.setNavigationOnClickListener { onBackPressed() }
}

// The first intent in list is main, the other are extra
fun createIntentChooser(intents: List<Intent>, title: String): Intent? {
    if (intents.isEmpty())
        return null

    return Intent.createChooser(intents.first(), title).apply {
        val extraIntents = intents.drop(1)
        if (extraIntents.isNotEmpty())
            putExtra(Intent.EXTRA_INITIAL_INTENTS, extraIntents.toTypedArray())
    }
}