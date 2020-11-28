package com.sergsave.purryourcat.screens.settings

import android.os.Bundle
import androidx.fragment.app.viewModels
import androidx.preference.Preference
import androidx.preference.PreferenceFragmentCompat
import com.sergsave.purryourcat.R
import com.sergsave.purryourcat.MyApplication

class SettingsFragment: PreferenceFragmentCompat() {
    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        setPreferencesFromResource(R.xml.settings_preferences, rootKey)

        val viewModel: SettingsViewModel by viewModels {
            (requireActivity().application as MyApplication).appContainer
                .provideSettingsViewModelFactory()
        }

        findPreference<Preference>(getString(R.string.vibration_preference_key))?.apply {
            onPreferenceChangeListener = Preference.OnPreferenceChangeListener { _, value ->
                viewModel.onVibrationSwitched(value as Boolean)
                true
            }
        }
    }
}