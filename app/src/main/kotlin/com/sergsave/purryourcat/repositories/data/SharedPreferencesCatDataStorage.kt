package com.sergsave.purryourcat.repositories.data

import android.content.Context
import com.sergsave.purryourcat.models.CatData
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonConfiguration

// TODO: USE bundle utils

@Serializable
private data class Bundle(val version: Int, val map: Map<String, CatData>)

class SharedPreferencesCatDataStorage(private val context: Context): CatDataStorage {

    private val jsonObj = Json(JsonConfiguration.Stable)

    override fun save(cats: Map<String, CatData>?) {
        if(cats == null)
            return

        val json = jsonObj.stringify(Bundle.serializer(), Bundle(BUNDLE_ACTUAL_VERSION, cats))

        val preferences = context.getSharedPreferences(PREFERENCES_NAME, Context.MODE_PRIVATE)
        with (preferences.edit()) {
            putString(CATS_LIST_KEY, json)
            commit()
        }
    }

    override fun load(): Map<String, CatData>? {
        val preferences = context.getSharedPreferences(PREFERENCES_NAME, Context.MODE_PRIVATE)

        val json = preferences.getString(CATS_LIST_KEY, null)
        if(json == null)
            return null

        try {
            val bundle = jsonObj.parse(Bundle.serializer(), json)
            if(bundle.version > BUNDLE_ACTUAL_VERSION)
                return null

            return bundle.map
        }
        catch(e: Exception) {
            return null
        }
    }

    companion object {
        private const val BUNDLE_ACTUAL_VERSION = 1
        private const val PREFERENCES_NAME = "cats_database"
        private const val CATS_LIST_KEY = "cats_map"
    }
}