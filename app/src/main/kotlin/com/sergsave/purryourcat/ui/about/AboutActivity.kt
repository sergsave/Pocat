package com.sergsave.purryourcat.ui.about

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.sergsave.purryourcat.R
import com.sergsave.purryourcat.helpers.setToolbarAsActionBar
import kotlinx.android.synthetic.main.activity_about.*

class AboutActivity : AppCompatActivity() {

    override fun onDestroy() {
        super.onDestroy()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_about)

        setToolbarAsActionBar(toolbar, showBackButton = true)
    }
}