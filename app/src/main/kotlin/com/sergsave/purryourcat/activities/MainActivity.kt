package com.sergsave.purryourcat.activities

import android.content.ContentResolver
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityOptionsCompat
import androidx.lifecycle.Observer
import com.sergsave.purryourcat.R
import com.sergsave.purryourcat.adapters.CatsListAdapter
import com.sergsave.purryourcat.data.*
import com.sergsave.purryourcat.helpers.*
import com.sergsave.purryourcat.content.*
import com.sergsave.purryourcat.sharing.*
import com.sergsave.purryourcat.models.*
import kotlinx.android.synthetic.main.activity_main.*

// Global TODO
// TODO: Check sdk version of all function
// TODO: Check Leaks of fragment, activity, viewmodel. And local variable without reference (like visualizer)
// TODO: Names of constants (XX_BUNDLE_KEY or BUNDLE_KEY_XX)
// TODO: File size limits
// TODO: Code inspect

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
    private lateinit var sharingManager: ISharingManager
    private lateinit var catsListAdapter : CatsListAdapter
    private var recyclerItemIds2catIds = mapOf<Long, String>()

    override fun onDestroy() {
        super.onDestroy()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // TODO? init and cleanup in App
        catRepo = CatDataRepo.init(SharedPreferencesCatDataStorage(applicationContext))
        contentRepo = ContentRepo.init(InternalFilesDirContentStorage(applicationContext))
        cleanUpUnusedContent()

        addTestCats(applicationContext)

        sharingManager = ZipSharingManager(applicationContext,
            resources.getString(R.string.shared_file_extension))
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

        if(savedInstanceState == null)
            addCatFromIntent(intent)
    }

    private fun cleanUpUnusedContent() {
        val usedContent = ArrayList<Uri>()
        catRepo.read().value?.forEach{(_, cat) -> usedContent.addAll(cat.extractContent())}
        val allContent = contentRepo.read().value
        allContent?.let { all -> (all - usedContent).forEach{ contentRepo.remove(it) }}
    }

    private fun addCatFromIntent(intent: Intent?) {
        val extracted = sharingManager.extractFromSharingUri(intent?.data)
        if(extracted == null)
            return

        val updated = extracted.withUpdatedContent { uri -> contentRepo.add(uri) }

        Intent(this, CatCardActivity::class.java).apply {
            putExtra(Constants.CAT_DATA_INTENT_KEY, updated)
            startActivity(this)
        }
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
                val sharingUri = sharingManager.prepareSharingUri(catWithId.second)
                if(sharingUri == null)
                    return

                val sharingIntent = Intent(Intent.ACTION_SEND)
                sharingIntent.apply {
                    putExtra(Intent.EXTRA_STREAM, sharingUri)
                    setType(sharingManager.mimeType())
                    addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                    startActivity(this)
                }
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