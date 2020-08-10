package com.sergsave.purryourcat.helpers

import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar

fun AppCompatActivity.setToolbarAsActionBar(toolbar: Toolbar, showBackButton: Boolean) {
    setSupportActionBar(toolbar)

    getSupportActionBar()?.apply {
        setDisplayHomeAsUpEnabled(showBackButton)
        setDisplayShowHomeEnabled(showBackButton)
    }

    if(showBackButton)
        toolbar.setNavigationOnClickListener { onBackPressed() }
}