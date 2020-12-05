package com.sergsave.purryourcat.screens.about

import android.os.Bundle
import android.text.method.LinkMovementMethod
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import com.sergsave.purryourcat.R
import com.sergsave.purryourcat.helpers.setToolbarAsActionBar
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