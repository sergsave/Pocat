package com.github.sergsave.purr_your_cat

import android.content.Intent
import android.os.Bundle
import android.os.Parcelable
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity() {

    private lateinit var catsListAdapter : CatsListAdapter

    override fun onDestroy() {
        super.onDestroy()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val testCats = arrayListOf(
            CatData("Simka"),
            CatData("Masik"),
            CatData("Uta"),
            CatData("Sherya"),
            CatData("Sema"),
            CatData("Philya"),
            CatData("Ganya")
        )

        val initState =
            if(savedInstanceState != null)
                restoreInstanceState(savedInstanceState)
            else
                InstanceState(testCats, null)

        setupCatsList(initState)

        fab.setOnClickListener {
            val intent = Intent(this, CatProfileActivity::class.java)
            startActivity(intent)
//            catsListAdapter.addItems(arrayListOf(CatData("Pusik")))
        }

        fab_clickable_layout.setOnClickListener { fab.performClick() }
    }

    private fun setupCatsList(initState: InstanceState) {
        // TODO: remove hardcode
        val columnWidth = 180
        val itemMargin = 16

        val onItemClick: (CatData) -> Unit = { cat ->
            val intent = Intent(this, PurringActivity::class.java)
            intent.putExtra(Constants.CAT_DATA_INTENT_KEY, cat)
            startActivity(intent)
        }
        catsListAdapter = CatsListAdapter(onItemClick)
        if(initState.catsList != null)
            catsListAdapter.addItems(initState.catsList)

        val viewManager = AutoFitGridLayoutManager(this, columnWidth)
        if(initState.layoutManagerState != null)
            viewManager.onRestoreInstanceState(initState.layoutManagerState)

        val itemDecoration = MarginItemDecoration(itemMargin, { viewManager.spanCount })

        recycler_view.apply {
            setHasFixedSize(true)

            layoutManager = viewManager
            adapter = catsListAdapter
            addItemDecoration(itemDecoration)
        }
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)

        outState.putParcelableArrayList(CATS_LIST_KEY, catsListAdapter.getItemsCopy())
        outState.putParcelable(LAYOUT_MANAGER_STATE_KEY, recycler_view.layoutManager?.onSaveInstanceState())
    }

    data class InstanceState(val catsList: ArrayList<CatData>?, val layoutManagerState: Parcelable? )

    private fun restoreInstanceState(savedInstanceState: Bundle?) : InstanceState {

        val catsList: ArrayList<CatData>? = savedInstanceState?.getParcelableArrayList(CATS_LIST_KEY)
        val layoutManagerState : Parcelable? = savedInstanceState?.getParcelable(LAYOUT_MANAGER_STATE_KEY)

        return InstanceState(catsList, layoutManagerState)
    }

    companion object {
        private val CATS_LIST_KEY = "CatsList"
        private val LAYOUT_MANAGER_STATE_KEY = "LayoutManager"
    }
}