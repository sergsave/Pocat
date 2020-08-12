package com.sergsave.purryourcat.preference

import android.content.Context
import androidx.preference.PreferenceManager
import com.sergsave.purryourcat.R

class PreferenceReader(private val context: Context) {

    init {
        // With "readAgain = false" it's safe to create this class any times
        PreferenceManager.setDefaultValues(context, R.xml.preferences, false)
    }

    val isVibrationEnabled: Boolean
        get() = PreferenceManager
            .getDefaultSharedPreferences(context)
            .getBoolean(context.getString(R.string.vibration_preference_key), false)
}