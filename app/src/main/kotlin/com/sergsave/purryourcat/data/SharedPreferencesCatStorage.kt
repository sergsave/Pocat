package com.sergsave.purryourcat.data

import com.sergsave.purryourcat.models.CatData
import android.content.Context
import android.net.Uri
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.google.gson.TypeAdapter
import com.google.gson.reflect.TypeToken
import com.google.gson.stream.JsonReader
import com.google.gson.stream.JsonWriter

class SharedPreferencesCatStorage(private val context: Context): ICatStorage {

    private class UriAdapter : TypeAdapter<Uri?>() {

        override fun write(out: JsonWriter?, value: Uri?) {
            out?.value(value?.toString())
        }

        override fun read(`in`: JsonReader?): Uri? {
            return `in`?.nextString()?.let { Uri.parse(it) }
        }
    }

    override fun save(cats: List<CatData>?) {
        val json = GsonBuilder()
            .registerTypeAdapter(Uri::class.java, UriAdapter())
            .create()
            .toJson(cats)

        val preferences = context.getSharedPreferences(PREFERENCES_NAME, Context.MODE_PRIVATE)
        with (preferences.edit()) {
            putString(CATS_LIST_KEY, json)
            commit()
        }
    }

    override fun load(): List<CatData>? {
        val preferences = context.getSharedPreferences(PREFERENCES_NAME, Context.MODE_PRIVATE)

        val json = preferences.getString(CATS_LIST_KEY, null)
        if(json == null)
            return null

        val catsType = object : TypeToken<ArrayList<CatData>>() {}.type

        return GsonBuilder()
            .registerTypeAdapter(Uri::class.java, UriAdapter())
            .create()
            .fromJson<ArrayList<CatData>>(json, catsType)
    }

    companion object {
        private val PREFERENCES_NAME = "CatsList"
        private val CATS_LIST_KEY = "CatsListKey"
    }
}