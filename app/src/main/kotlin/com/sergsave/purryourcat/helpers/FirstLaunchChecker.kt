package com.sergsave.purryourcat.helpers

import android.content.SharedPreferences

class FirstLaunchChecker(private val preferences: SharedPreferences) {
    fun check(): Boolean {
        val key = "firstrun"
        val ret = preferences.getBoolean(key, true)
        preferences.edit().putBoolean(key, false).commit()
        return ret
    }
}