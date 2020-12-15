package com.sergsave.pocat.screens.about

import android.os.Bundle
import android.text.method.LinkMovementMethod
import androidx.appcompat.app.AppCompatActivity
import com.sergsave.pocat.R
import com.sergsave.pocat.helpers.setToolbarAsActionBar
import kotlinx.android.synthetic.main.activity_credits.*

// TODO: To fragment
class CreditsActivity: AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_credits)
        setToolbarAsActionBar(toolbar, showBackButton = true)

        credits_text.movementMethod = LinkMovementMethod.getInstance()
    }
}