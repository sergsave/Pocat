package com.sergsave.purryourcat.ui.settings

import android.os.Bundle
import androidx.preference.PreferenceFragmentCompat
import com.sergsave.purryourcat.R

class PreferenceFragment: PreferenceFragmentCompat() {
    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        setPreferencesFromResource(R.xml.settings_preferences, rootKey)
    }
}