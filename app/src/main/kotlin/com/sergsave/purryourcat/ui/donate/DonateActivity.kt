package com.sergsave.purryourcat.ui.donate

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.sergsave.purryourcat.R
import com.sergsave.purryourcat.helpers.setToolbarAsActionBar
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