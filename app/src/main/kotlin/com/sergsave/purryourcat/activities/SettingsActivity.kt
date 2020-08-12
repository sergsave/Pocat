package com.sergsave.purryourcat.activities

import android.content.Context
import android.net.Uri
import android.os.Bundle
import android.view.*
import androidx.appcompat.app.AppCompatActivity
import com.sergsave.purryourcat.R
import com.sergsave.purryourcat.fragments.PreferenceFragment
import com.sergsave.purryourcat.helpers.setToolbarAsActionBar
import kotlinx.android.synthetic.main.activity_settings.*

class SettingsActivity : AppCompatActivity() {

    override fun onDestroy() {
        super.onDestroy()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_settings)

        setToolbarAsActionBar(toolbar, showBackButton = true)

        supportFragmentManager
            .beginTransaction()
            .replace(R.id.container, PreferenceFragment())
            .commit()
    }
}