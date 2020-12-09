package com.sergsave.pocat.screens.settings

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.sergsave.pocat.R
import com.sergsave.pocat.helpers.setToolbarAsActionBar
import kotlinx.android.synthetic.main.activity_settings.*

class SettingsActivity : AppCompatActivity() {

    override fun onDestroy() {
        super.onDestroy()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_settings)

        setToolbarAsActionBar(toolbar, showBackButton = true)

        if(savedInstanceState != null)
            return

        supportFragmentManager
            .beginTransaction()
            .add(R.id.container, SettingsFragment())
            .commit()
    }
}