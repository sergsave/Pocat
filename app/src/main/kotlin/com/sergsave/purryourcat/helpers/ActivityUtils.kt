package com.sergsave.purryourcat.helpers

import androidx.appcompat.app.AppCompatActivity

object ActivityUtils {
    fun setupActionBar(activity: AppCompatActivity?, title: String?, isBackEnabled: Boolean) {
        activity?.getSupportActionBar()?.apply {
            setDisplayHomeAsUpEnabled(isBackEnabled)
            setDisplayShowHomeEnabled(isBackEnabled)
            setTitle(title)
        }
    }
}