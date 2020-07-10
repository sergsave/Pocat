package com.sergsave.purryourcat.activities

import android.content.ContentResolver
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityOptionsCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import com.google.android.material.snackbar.Snackbar
import com.sergsave.purryourcat.R
import com.sergsave.purryourcat.adapters.CatsListAdapter
import com.sergsave.purryourcat.data.*
import com.sergsave.purryourcat.helpers.*
import com.sergsave.purryourcat.content.*
import com.sergsave.purryourcat.fragments.BaseSharingFragment
import com.sergsave.purryourcat.fragments.GiveSharingFragment
import com.sergsave.purryourcat.fragments.TakeSharingFragment
import com.sergsave.purryourcat.sharing.*
import com.sergsave.purryourcat.models.*
import kotlinx.android.synthetic.main.activity_main.*

// Global TODO
// TODO: Check sdk version of all function
// TODO: Check Leaks of fragment, activity, viewmodel. And local variable without reference (like visualizer)
// TODO: Names of constants (XX_BUNDLE_KEY or BUNDLE_KEY_XX)
// TODO: File size limits
// TODO: Code inspect
// TODO: Подвисание на Светином телефоне

//TODO: watsapp text send!

private fun addTestCats(context: Context) {
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

    if(CatDataRepo.instance?.read()?.value?.isEmpty() ?: false)
        testCats.forEach { cat ->
            val updatedContent = cat.withUpdatedContent{ uri -> ContentRepo.instance?.add(uri) }
            CatDataRepo.instance?.add(updatedContent)
        }
}

class MainActivity : AppCompatActivity() {

    private lateinit var catRepo: CatDataRepo
    private lateinit var contentRepo: ContentRepo
    private lateinit var catsListAdapter: CatsListAdapter
    private var recyclerItemIds2catIds = mapOf<Long, String>()

    private var takeSharingFragment: TakeSharingFragment? = null
    private var giveSharingFragment: GiveSharingFragment? = null

    override fun onDestroy() {
        // To avoid memory leakage (sharing fragments work async)
        takeSharingFragment?.onFinishedListener = null
        giveSharingFragment?.onFinishedListener = null
        super.onDestroy()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        assert(CatDataRepo.instance == null || ContentRepo.instance == null) { "Must be inited!" }
        catRepo = CatDataRepo.instance!!
        contentRepo = ContentRepo.instance!!

        addTestCats(this)
        setupCatsList()

        val observer = Observer<Map<String, CatData>> { cats ->
            updateCatsList(cats)
        }
        catRepo.read().observe(this, observer)

        fab.setOnClickListener {
            val intent = Intent(this, CatCardActivity::class.java)
            startActivity(intent)
        }

        fab_clickable_layout.setOnClickListener { fab.performClick() }

        if(savedInstanceState == null) {
            startExtractSharingData(intent)
            return
        }

        val currentFragment = supportFragmentManager.findFragmentById(R.id.recycler_layout)
        takeSharingFragment = (currentFragment as? TakeSharingFragment)?.also { init(it) }
        giveSharingFragment = (currentFragment as? GiveSharingFragment)?.also { init(it) }
    }

    private fun init(fragment: TakeSharingFragment) {
        fragment.onFinishedListener =
            object: BaseSharingFragment.OnFinishedListener<Intent> {
                override fun onFinished(data: Intent?) {
                    supportFragmentManager.popBackStack()
                    data?.let{ startActivity(it) }
                }

                override fun onFailed(error: String?) {
                    supportFragmentManager.popBackStack()
                    showFailedSnackBar(error)
                }
            }
    }

    private fun init(fragment: GiveSharingFragment) {
        fragment.onFinishedListener =
            object: BaseSharingFragment.OnFinishedListener<Pack> {
                override fun onFinished(data: Pack?) {
                    supportFragmentManager.popBackStack()
                    if(data == null)
                        return

                    val updated = data.cat.withUpdatedContent { uri -> contentRepo.add(uri) }

                    val intent = Intent(this@MainActivity, CatCardActivity::class.java)
                    intent.putExtra(Constants.CAT_DATA_INTENT_KEY, updated)
                    startActivity(intent)
                }

                override fun onFailed(error: String?) {
                    supportFragmentManager.popBackStack()
                    showFailedSnackBar(error)
                }
            }
    }

    private fun startExtractSharingData(intent: Intent) {
        giveSharingFragment = GiveSharingFragment.newInstance(intent).also { fragment ->
            init(fragment)
            showSharingFragment(fragment, null)
        }
    }

    private fun startPrepareSharingData(pack: Pack) {
        takeSharingFragment = TakeSharingFragment.newInstance(pack).also { fragment ->
            init(fragment)
            showSharingFragment(fragment, null)
        }
    }

    private fun showSharingFragment(fragment: Fragment, tag: String?) {
        supportFragmentManager
            .beginTransaction()
            .add(R.id.recycler_layout, fragment, tag)
            .addToBackStack(null)
            .commit()
    }

    private fun showFailedSnackBar(errorText: String?) {
        if(errorText == null)
            return

        Snackbar.make(
                recycler_layout,
                errorText,
                Snackbar.LENGTH_LONG
            )
            .setAction(R.string.close) { }
            .show()
    }

    private fun setupCatsList() {
        catsListAdapter = CatsListAdapter()

        catsListAdapter.onClickListener = object : CatsListAdapter.OnClickListener {
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

        catsListAdapter.onLongClickListener = object : CatsListAdapter.OnLongClickListener {
            override fun onLongClick(
                catWithId: Pair<Long, CatData>,
                sharedElement: View,
                sharedElementTransitionName: String
            ) {
                startPrepareSharingData(Pack(catWithId.second))
            }
        }

        // TODO: remove hardcode
        val columnWidth = 180
        val itemMargin = 16

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
}