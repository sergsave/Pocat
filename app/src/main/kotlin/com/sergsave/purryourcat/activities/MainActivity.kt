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
import androidx.fragment.app.FragmentActivity
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
// TODO: Check Leaks of fragment, activity, viewmodel. And local variable without reference (like visualizer). USE Profiler!!
// TODO: Names of constants (XX_BUNDLE_KEY or BUNDLE_KEY_XX)
// TODO: File size limits
// TODO: Code inspect
// TODO: Подвисание на Светином телефоне

//TODO: ЭТО ВАЩЕ норма, что при закрытии приложения с телефона дебаггер не прекращается??

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
    private lateinit var sharingHelper: SharingHelper
    private var recyclerItemIds2catIds = mapOf<Long, String>()

    override fun onDestroy() {
        // To avoid memory leakage (sharing fragments work async)
        sharingHelper.release()
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

        initSharingHelper()

        if(savedInstanceState == null) {
            sharingHelper.extractSharingData(intent)
            return
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
                sharingHelper.prepareSharingData(Pack(catWithId.second))
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

    private fun initSharingHelper() {
        val onFailedListener = { error: String? -> showFailedSnackBar(error) }
        val onPreparedListener = { intent: Intent -> startActivity(intent) }
        val onExtractedListener = { pack: Pack ->
            val updated = pack.cat.withUpdatedContent { uri -> contentRepo.add(uri) }

            val intent = Intent(this@MainActivity, CatCardActivity::class.java)
            intent.putExtra(Constants.CAT_DATA_INTENT_KEY, updated)
            startActivity(intent)
        }

        sharingHelper = SharingHelper(this, R.id.recycler_layout,
            onExtractedListener,
            onPreparedListener,
            onFailedListener)
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

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
    }
}

private class SharingHelper(private val activity: FragmentActivity,
                    private val fragmentContainerViewId: Int,
                    private val onExtractResultListener: (Pack) -> Unit,
                    private val onPrepareResultListener: (Intent) -> Unit,
                    private val onFailedListener: (String?) -> Unit) {

    private var takeSharingFragment: TakeSharingFragment? = null
    private var giveSharingFragment: GiveSharingFragment? = null

    init {
        val currentFragment = activity.supportFragmentManager
            .findFragmentById(fragmentContainerViewId)
        takeSharingFragment = (currentFragment as? TakeSharingFragment)?.also { init(it) }
        giveSharingFragment = (currentFragment as? GiveSharingFragment)?.also { init(it) }
    }

    fun extractSharingData(intent: Intent) {
        giveSharingFragment = GiveSharingFragment.newInstance(intent).also { fragment ->
            init(fragment)
            showFragment(fragment, null)
        }
    }

    fun prepareSharingData(pack: Pack) {
        takeSharingFragment = TakeSharingFragment.newInstance(pack).also { fragment ->
            init(fragment)
            showFragment(fragment, null)
        }
    }

    fun release() {
        deinit(takeSharingFragment)
        deinit(giveSharingFragment)
    }

    private fun init(fragment: TakeSharingFragment) {
        init(fragment, onPrepareResultListener, onFailedListener)
    }

    private fun init(fragment: GiveSharingFragment) {
        init(fragment, onExtractResultListener, onFailedListener)
    }

    private fun <T> init(fragment: BaseSharingFragment<T>,
                         onSuccess: (T) -> Unit,
                         onFailed: (String?) -> Unit) {
        val close = { activity.supportFragmentManager.popBackStack() }

        fragment.onSuccessListener = object: BaseSharingFragment.OnSuccessListener<T> {
            override fun onSuccess(data: T) {
                close()
                onSuccess(data)
            }
        }

        fragment.onErrorListener = object: BaseSharingFragment.OnErrorListener {
            override fun onError(error: String?) {
                close()
                onFailed(error)
            }
        }

        fragment.onStartFailedListener = object: BaseSharingFragment.OnStartFailedListener {
            override fun onStartFailed() = close()
        }
    }

    private fun <T> deinit(fragment: BaseSharingFragment<T>?) {
        fragment?.onStartFailedListener = null
        fragment?.onSuccessListener = null
        fragment?.onErrorListener = null
    }

    private fun showFragment(fragment: Fragment, tag: String?) {
        activity
            .supportFragmentManager
            .beginTransaction()
            .add(fragmentContainerViewId, fragment, tag)
            .addToBackStack(null)
            .commit()
    }
}