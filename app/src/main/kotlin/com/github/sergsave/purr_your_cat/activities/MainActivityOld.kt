package com.github.sergsave.purr_your_cat.activities

import android.content.ContentResolver
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityOptionsCompat
import com.github.sergsave.purr_your_cat.R
import com.github.sergsave.purr_your_cat.Singleton
import com.github.sergsave.purr_your_cat.adapters.CatsListAdapter
import com.github.sergsave.purr_your_cat.helpers.AutoFitGridLayoutManager
import com.github.sergsave.purr_your_cat.helpers.Constants
import com.github.sergsave.purr_your_cat.helpers.MarginItemDecoration
import com.github.sergsave.purr_your_cat.models.CatData
import com.google.gson.GsonBuilder
import com.google.gson.TypeAdapter
import com.google.gson.reflect.TypeToken
import com.google.gson.stream.JsonReader
import com.google.gson.stream.JsonWriter
import kotlinx.android.synthetic.main.activity_main_old.*


class MainActivityOld : AppCompatActivity() {

    private lateinit var catsListAdapter : CatsListAdapter
    private var catId : Int? = null

    override fun onDestroy() {
        super.onDestroy()
    }

    // TODO: DATABASE storage
    class UriAdapter : TypeAdapter<Uri?>() {

        override fun write(out: JsonWriter?, value: Uri?) {
            out?.value(value?.toString())
        }

        override fun read(`in`: JsonReader?): Uri? {
            return `in`?.nextString()?.let { Uri.parse(it) }
        }
    }

    override fun onPause() {
        super.onPause()

        val json = GsonBuilder()
            .registerTypeAdapter(Uri::class.java, UriAdapter())
            .create()
            .toJson(catsListAdapter.getItems())

        val preferences = getPreferences(Context.MODE_PRIVATE)
        with (preferences.edit()) {
            putString(CATS_LIST_KEY, json)
            commit()
        }
    }

    private fun loadCatsFromSettings(): ArrayList<CatData>? {
        val preferences = getPreferences(Context.MODE_PRIVATE)

        val json = preferences.getString(CATS_LIST_KEY, null)
        if(json == null)
            return null

        val catsType = object : TypeToken<ArrayList<CatData>>() {}.type

        return GsonBuilder()
            .registerTypeAdapter(Uri::class.java, UriAdapter())
            .create()
            .fromJson<ArrayList<CatData>>(json, catsType)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val testUri = Uri.parse(
            ContentResolver.SCHEME_ANDROID_RESOURCE +
                "://" + getResources().getResourcePackageName(R.drawable.cat)
                + '/' + getResources().getResourceTypeName(R.drawable.cat)
                + '/' + getResources().getResourceEntryName(R.drawable.cat))

        val testCats = arrayListOf(
            CatData("Simka", testUri),
            CatData("Masik", testUri),
            CatData("Uta", testUri),
            CatData("Sherya", testUri),
            CatData("Sema", testUri),
            CatData("Philya", testUri),
            CatData("Ganya", testUri)
        )

        setupCatsList(loadCatsFromSettings() ?: testCats)

        fab.setOnClickListener {
            val intent = Intent(this, MainActivity::class.java)
            startActivity(intent)
        }

        fab_clickable_layout.setOnClickListener { fab.performClick() }
    }

    private fun setupCatsList(cats: ArrayList<CatData>?) {
        // TODO: remove hardcode
        val columnWidth = 180
        val itemMargin = 16

        val listener = object : CatsListAdapter.OnClickListener {
            override fun onClick(position: Int, sharedElement: View, sharedElementTransitionName: String) {
                val data = catsListAdapter.getItems().get(position)
                catId = position
                goToPurringAnimated(data, sharedElement, sharedElementTransitionName)
            }
        }

        catsListAdapter = CatsListAdapter(listener)
        if(cats != null)
            catsListAdapter.addItems(cats)

        val viewManager = AutoFitGridLayoutManager(this, columnWidth)
        val itemDecoration = MarginItemDecoration(itemMargin, { viewManager.spanCount })

        // For the shared element transition to work correctly when returning to this screen
        catsListAdapter.setHasStableIds(true)

        recycler_view.apply {
            setHasFixedSize(true)

            layoutManager = viewManager
            adapter = catsListAdapter
            addItemDecoration(itemDecoration)
        }
    }

    private fun goToPurringAnimated(cat: CatData, sharedElement: View, transitionName: String) {
        val intent = Intent(this, MainActivity::class.java)
        intent.putExtra(Constants.CAT_DATA_INTENT_KEY, cat)
        intent.putExtra(Constants.SHARED_TRANSITION_NAME_INTENT_KEY, transitionName)

        val transitionOption = ActivityOptionsCompat.makeSceneTransitionAnimation(
            this, sharedElement, transitionName)

        startActivity(intent, transitionOption.toBundle())
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
    }

    override fun onActivityReenter(resultCode: Int, data: Intent?) {
        super.onActivityReenter(resultCode, data)

        if(resultCode != RESULT_OK || data == null)
            return

        val catData = Singleton.catData
//        val catData = data?.getParcelableExtra(Constants.CAT_DATA_INTENT_KEY) as CatData?

        if(catData == null)
            return

        val catsCopy = ArrayList<CatData>()
        catsCopy.addAll(catsListAdapter.getItems())

        catId?.let { catsCopy.set(it, catData) }

        catsListAdapter.clearItems()
        catsListAdapter.addItems(catsCopy)
    }

    companion object {
        private val CATS_LIST_KEY = "CatsList"
    }
}