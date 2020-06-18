package com.sergsave.purryourcat.activities

import android.content.ContentResolver
import android.content.Context
import android.content.Intent
import androidx.lifecycle.Observer
import android.net.Uri
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityOptionsCompat
import com.sergsave.purryourcat.R
import com.sergsave.purryourcat.adapters.CatsListAdapter
import com.sergsave.purryourcat.helpers.AutoFitGridLayoutManager
import com.sergsave.purryourcat.helpers.Constants
import com.sergsave.purryourcat.helpers.MarginItemDecoration
import com.sergsave.purryourcat.models.CatData
import com.sergsave.purryourcat.data.CatDataRepo
import com.sergsave.purryourcat.data.ICatStorage
import com.sergsave.purryourcat.data.SharedPreferencesCatStorage
import kotlinx.android.synthetic.main.activity_main.*

// Global TODO
// TODO: Check sdk version of all function
// TODO: Check Leaks of fragment, activity, viewmodel. And local variable without reference (like visualizer)
// TODO: Names of constants (XX_BUNDLE_KEY or BUNDLE_KEY_XX)

class MainActivity : AppCompatActivity() {

    private lateinit var catsListAdapter : CatsListAdapter
    private var recyclerItemIds2catIds = mapOf<Long, String>()

    override fun onDestroy() {
        super.onDestroy()
    }

    class TestCatStorage(val context: Context) : ICatStorage {
        val storage = SharedPreferencesCatStorage(context)
        override fun save(cats: List<CatData>?) {
            storage.save(cats)
        }

        override fun load(): List<CatData>? {

            val testUri = Uri.parse(
                ContentResolver.SCHEME_ANDROID_RESOURCE +
                        "://" + context.getResources().getResourcePackageName(R.drawable.cat)
                        + '/' + context.getResources().getResourceTypeName(R.drawable.cat)
                        + '/' + context.getResources().getResourceEntryName(R.drawable.cat))

            val testCats = arrayListOf(
                CatData("Simka", testUri),
                CatData("Masik", testUri),
                CatData("Uta", testUri),
                CatData("Sherya", testUri),
                CatData("Sema", testUri),
                CatData("Philya", testUri),
                CatData("Ganya", testUri)
            )
            return storage.load() ?: testCats
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // TODO? init in App
        CatDataRepo.init(TestCatStorage(applicationContext))
//        CatDataRepo.init(SharedPreferencesCatStorage(applicationContext))
        setupCatsList()

        val observer = Observer<Map<String, CatData>> { cats ->
            updateCatsList(cats)
        }
        CatDataRepo.instance?.read()?.observe(this, observer)

        fab.setOnClickListener {
            val intent = Intent(this, CatCardActivity::class.java)
            startActivity(intent)
        }

        fab_clickable_layout.setOnClickListener { fab.performClick() }
    }

    private fun setupCatsList() {
        // TODO: remove hardcode
        val columnWidth = 180
        val itemMargin = 16

        val listener = object : CatsListAdapter.OnClickListener {
            override fun onClick(
                catWithId: Pair<Long, CatData>,
                sharedElement: View,
                sharedElementTransitionName: String
            ) {
                val id = recyclerItemIds2catIds.get(catWithId.first)
                if(id != null)
                    goToPurringAnimated(id, sharedElement, sharedElementTransitionName)
            }

        }

        catsListAdapter = CatsListAdapter(listener)

        val viewManager = AutoFitGridLayoutManager(this, columnWidth)
        val itemDecoration = MarginItemDecoration(itemMargin, { viewManager.spanCount })

        recycler_view.apply {
            setHasFixedSize(true)

            layoutManager = viewManager
            adapter = catsListAdapter
            addItemDecoration(itemDecoration)
        }
    }

    private fun updateCatsList(cats: Map<String, CatData>) {
        val itemIdFromCatId = { catId: String -> catId.hashCode().toLong() }
        val catsList = cats.mapKeys { (k, _) -> itemIdFromCatId(k) }.toList()

        recyclerItemIds2catIds = cats.map { (k, _) -> Pair(itemIdFromCatId(k), k) }.toMap()

        catsListAdapter.setItems(catsList)
    }

    private fun goToPurringAnimated(catId: String, sharedElement: View, transitionName: String) {
        val intent = Intent(this, CatCardActivity::class.java)
        intent.putExtra(Constants.CAT_ID_INTENT_KEY, catId)
        intent.putExtra(Constants.SHARED_TRANSITION_NAME_INTENT_KEY, transitionName)

        val transitionOption = ActivityOptionsCompat.makeSceneTransitionAnimation(
            this, sharedElement, transitionName)

        startActivity(intent, transitionOption.toBundle())
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
    }

    companion object {
        private val CATS_LIST_KEY = "CatsList"
    }
}