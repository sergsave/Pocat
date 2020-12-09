package com.sergsave.pocat.screens.donate

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.sergsave.pocat.R
import com.sergsave.pocat.helpers.setToolbarAsActionBar
import kotlinx.android.synthetic.main.activity_donate.*

class DonateActivity : AppCompatActivity() {

    override fun onDestroy() {
        super.onDestroy()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_donate)

        setToolbarAsActionBar(toolbar, showBackButton = true)
    }
}