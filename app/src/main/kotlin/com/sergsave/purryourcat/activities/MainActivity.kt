package com.sergsave.purryourcat.activities

import android.content.ContentResolver
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.*
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.view.menu.MenuBuilder
import androidx.core.app.ActivityOptionsCompat
import androidx.lifecycle.Observer
import androidx.recyclerview.selection.ItemDetailsLookup
import androidx.recyclerview.selection.ItemKeyProvider
import androidx.recyclerview.selection.SelectionTracker
import androidx.recyclerview.selection.StorageStrategy
import androidx.recyclerview.widget.RecyclerView
import com.sergsave.purryourcat.R
import com.sergsave.purryourcat.adapters.CatsListAdapter
import com.sergsave.purryourcat.content.ContentRepo
import com.sergsave.purryourcat.data.CatDataRepo
import com.sergsave.purryourcat.helpers.AutoFitGridLayoutManager
import com.sergsave.purryourcat.helpers.Constants
import com.sergsave.purryourcat.helpers.MarginItemDecoration
import com.sergsave.purryourcat.models.CatData
import com.sergsave.purryourcat.models.withUpdatedContent
import kotlinx.android.synthetic.main.activity_main.*

// Global TODO
// TODO: Check sdk version of all function
// TODO: Check Leaks of fragment, activity, viewmodel. And local variable without reference (like visualizer). USE Profiler!!
// TODO: Names of constants (XX_BUNDLE_KEY or BUNDLE_KEY_XX)
// TODO: File size limits
// TODO: Code inspect
// TODO: Подвисание на Светином телефоне

// TODO: ЭТО ВАЩЕ норма, что при закрытии приложения с телефона дебаггер не прекращается??

// TODO: Main activity refactoring after settings and about pages

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
    private var actionMode: ActionMode? = null

    override fun onDestroy() {
        super.onDestroy()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        assert(CatDataRepo.instance == null || ContentRepo.instance == null) { "Must be inited!" }
        catRepo = CatDataRepo.instance!!
        contentRepo = ContentRepo.instance!!

        setSupportActionBar(toolbar)

        addTestCats(this)
        setupCatsList(savedInstanceState)

        val observer = Observer<Map<String, CatData>> { cats ->
            updateCatsList(cats)
        }
        catRepo.read().observe(this, observer)

        fab.setOnClickListener {
            actionMode?.finish()
            val intent = Intent(this, CatCardActivity::class.java)
            startActivity(intent)
        }

        fab_clickable_layout.setOnClickListener { fab.performClick() }

        if(savedInstanceState == null)
            checkInputSharingIntent()
    }

    private fun setupCatsList(bundle: Bundle?) {
        catsListAdapter = CatsListAdapter()

        catsListAdapter.onClickListener = object : CatsListAdapter.OnClickListener {
            override fun onClick(
                catWithId: Pair<Long, CatData>,
                sharedElement: View,
                sharedElementTransitionName: String
            ) {
                if(actionMode != null)
                    return

                val id = recyclerItemIds2catIds.get(catWithId.first)
                if(id != null)
                    goToPurringAnimated(id, sharedElement, sharedElementTransitionName)
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

        val tracker = SelectionTracker.Builder<Long>(
            "catsListSelection",
            recycler_view,
            MyItemKeyProvider(recycler_view),
            CatsListItemDetailsLookup(recycler_view),
            StorageStrategy.createLongStorage()
        ).withSelectionPredicate(
            NotSelectEmptySelectionPredicate()
        ).build()

        tracker?.addObserver(
            object : SelectionTracker.SelectionObserver<Long>() {
                override fun onSelectionChanged() {
                    super.onSelectionChanged()

                    val isSelected = tracker.selection?.isEmpty?.not() ?: false

                    if(isSelected && actionMode == null) {
                        actionMode = startActionMode(ActionModeCallback({
                            tracker.selection?.forEach { sel ->
                                recyclerItemIds2catIds.get(sel.toLong())?.let { catRepo.remove(it) }
                            }
                            actionMode?.finish()
                        }))
                    }

                    if(isSelected)
                        actionMode?.title = tracker.selection?.size().toString()

                    if(isSelected.not() && actionMode != null)
                        actionMode?.finish()
                }
            })

        catsListAdapter.tracker = tracker
        tracker.onRestoreInstanceState(bundle)
    }

    override fun onActionModeFinished(mode: ActionMode?) {
        super.onActionModeFinished(mode)
        actionMode = null
        catsListAdapter.tracker?.clearSelection()
        // Workaround. Force update of recycler view. Without this not all items unselect.
        catsListAdapter.notifyDataSetChanged()
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

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menu_preferences, menu)
        if (menu is MenuBuilder)
            menu.setOptionalIconsVisible(true)

        return true
    }

    private fun checkInputSharingIntent() {
        val isForwarded = intent?.getBooleanExtra(Constants.IS_FORWARDED_INTENT_KEY, false) ?: false
        if(isForwarded.not())
            return

        // Forward further
        val intent = Intent(this, CatCardActivity::class.java)
        intent.putExtra(Constants.SHARING_INPUT_INTENT_KEY, this.intent)

        startActivity(intent)
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        catsListAdapter.tracker?.onSaveInstanceState(outState)
    }
}

private object EMPTY_ITEM : ItemDetailsLookup.ItemDetails<Long>() {
    override fun getSelectionKey() = 666.toLong()
    override fun getPosition() = Integer.MAX_VALUE
}

private class NotSelectEmptySelectionPredicate: SelectionTracker.SelectionPredicate<Long>() {
    override fun canSelectMultiple() = true
    override fun canSetStateForKey(key: Long, nextState: Boolean) =
        key != EMPTY_ITEM.selectionKey
    override fun canSetStateAtPosition(position: Int, nextState: Boolean) =
        position != EMPTY_ITEM.position
}

private class MyItemKeyProvider(private val recyclerView: RecyclerView) :
    ItemKeyProvider<Long>(ItemKeyProvider.SCOPE_MAPPED) {

    override fun getKey(position: Int): Long? {
        return recyclerView.adapter?.getItemId(position)
    }

    override fun getPosition(key: Long): Int {
        val viewHolder = recyclerView.findViewHolderForItemId(key)
        return viewHolder?.layoutPosition ?: RecyclerView.NO_POSITION
    }
}

private class CatsListItemDetailsLookup(private val recyclerView: RecyclerView) :
    ItemDetailsLookup<Long>() {
    override fun getItemDetails(event: MotionEvent): ItemDetails<Long>? {
        val view = recyclerView.findChildViewUnder(event.x, event.y)
        if (view != null) {
            return (recyclerView.getChildViewHolder(view) as CatsListAdapter.ViewHolder)
                .getItemDetails()
        }
        return EMPTY_ITEM
    }
}

private class ActionModeCallback(private var listener: () -> Unit): ActionMode.Callback {

//    var mode: ActionMode? = null

    override fun onCreateActionMode(mode: ActionMode, menu: Menu): Boolean {
//        this.mode = mode
        val inflater = mode.menuInflater
        inflater.inflate(R.menu.menu_selection_context, menu)
        return true
    }

    override fun onPrepareActionMode(mode: ActionMode, menu: Menu): Boolean {
        return false
    }

    override fun onActionItemClicked(mode: ActionMode, item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.action_remove -> {
                listener?.invoke()
                // REMOVE CATS
//                mode.finish()
                true
            }
            else -> false
        }
    }

    override fun onDestroyActionMode(mode: ActionMode) {
//        this.mode = null
    }
}