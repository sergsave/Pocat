package com.sergsave.purryourcat.fragments

import android.os.Bundle
import androidx.preference.PreferenceFragmentCompat
import com.sergsave.purryourcat.R

class PreferenceFragment: PreferenceFragmentCompat() {
    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        setPreferencesFromResource(R.xml.preferences, rootKey)
    }
}