package com.github.sergsave.purr_your_cat.activities

import android.content.ContentResolver
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.os.Parcelable
import android.transition.Transition
import android.transition.Transition.TransitionListener
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
import kotlinx.android.synthetic.main.activity_main.*


class MainActivity : AppCompatActivity() {

    private lateinit var catsListAdapter : CatsListAdapter
    private var catId : Int? = null

    override fun onDestroy() {
        super.onDestroy()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val testUri = Uri.parse(
            ContentResolver.SCHEME_ANDROID_RESOURCE +
                "://" + getResources().getResourcePackageName(R.drawable.cat)
                + '/' + getResources().getResourceTypeName(R.drawable.cat)
                + '/' + getResources().getResourceEntryName(R.drawable.cat) );

        val testCats = arrayListOf(
            CatData("Simka", testUri),
            CatData("Masik", testUri),
            CatData("Uta", testUri),
            CatData("Sherya", testUri),
            CatData("Sema", testUri),
            CatData("Philya", testUri),
            CatData("Ganya", testUri)
        )

        val initState =
            if(savedInstanceState != null)
                restoreInstanceState(savedInstanceState)
            else
                InstanceState(testCats, null)

        setupCatsList(initState)

        fab.setOnClickListener {
            val intent = Intent(this, CatCardActivity::class.java)
            startActivity(intent)
        }

        fab_clickable_layout.setOnClickListener { fab.performClick() }
    }

    private fun setupCatsList(initState: InstanceState) {
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
        if(initState.catsList != null)
            catsListAdapter.addItems(initState.catsList)

        val viewManager = AutoFitGridLayoutManager(this, columnWidth)
        if(initState.layoutManagerState != null)
            viewManager.onRestoreInstanceState(initState.layoutManagerState)

        val itemDecoration = MarginItemDecoration(itemMargin, { viewManager.spanCount })

        // For the shared element transition to work correctly when returning to this screen
        catsListAdapter?.setHasStableIds(true)

        recycler_view.apply {
            setHasFixedSize(true)

            layoutManager = viewManager
            adapter = catsListAdapter
            addItemDecoration(itemDecoration)
        }
    }

    private fun goToPurringAnimated(cat: CatData, sharedElement: View, transitionName: String) {
        val intent = Intent(this, CatCardActivity::class.java)
        intent.putExtra(Constants.CAT_DATA_INTENT_KEY, cat)
        intent.putExtra(Constants.SHARED_TRANSITION_NAME_INTENT_KEY, transitionName)

        val transitionOption = ActivityOptionsCompat.makeSceneTransitionAnimation(
            this, sharedElement, transitionName)

        startActivity(intent, transitionOption.toBundle())
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)

        outState.putParcelableArrayList(CATS_LIST_KEY, catsListAdapter.getItems())
        outState.putParcelable(LAYOUT_MANAGER_STATE_KEY, recycler_view.layoutManager?.onSaveInstanceState())
    }

    data class InstanceState(val catsList: ArrayList<CatData>?, val layoutManagerState: Parcelable? )

    private fun restoreInstanceState(savedInstanceState: Bundle?) : InstanceState {

        val catsList: ArrayList<CatData>? = savedInstanceState?.getParcelableArrayList(CATS_LIST_KEY)
        val layoutManagerState : Parcelable? = savedInstanceState?.getParcelable(LAYOUT_MANAGER_STATE_KEY)

        return InstanceState(catsList, layoutManagerState)
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
        private val LAYOUT_MANAGER_STATE_KEY = "LayoutManager"
    }
}