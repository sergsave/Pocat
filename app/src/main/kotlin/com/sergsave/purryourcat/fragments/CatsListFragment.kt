package com.sergsave.purryourcat.fragments

import androidx.fragment.app.Fragment
import android.os.Bundle
import android.view.*
import androidx.recyclerview.selection.*
import androidx.recyclerview.widget.RecyclerView
import com.sergsave.purryourcat.R
import com.sergsave.purryourcat.adapters.CatsListAdapter
import com.sergsave.purryourcat.helpers.AutoFitGridLayoutManager
import com.sergsave.purryourcat.helpers.MarginItemDecoration
import com.sergsave.purryourcat.helpers.RecyclerViewSelectionUtils
import com.sergsave.purryourcat.models.CatData
import kotlinx.android.synthetic.main.fragment_cats_list.*

class CatsListFragment : Fragment() {

    interface OnItemClickListener {
        fun onItemClick(catId: String, sharedElement: View, transitionName: String)
    }

    interface OnSelectionChangedListener {
        fun onSelectionChanged(selected: List<String>)
    }

    var onItemClickListener: OnItemClickListener? = null
    var onSelectionChangedListener: OnSelectionChangedListener? = null

    var cats = mapOf<String, CatData>()
        set(value) {
            field = value
            catsListAdapter.cats = value.mapKeys { (k, _) -> idMapper.longIdFrom(k)}.toList()
        }

    var selection = listOf<String>()
        private set(value) {
            field = value
            onSelectionChangedListener?.onSelectionChanged(value)
        }

    fun clearSelection() {
        catsListAdapter.tracker?.clearSelection()
        // Workaround. Force update of recycler view. Without this not all items unselect.
        catsListAdapter.notifyDataSetChanged()
    }

    private lateinit var catsListAdapter: CatsListAdapter
    private val idMapper = Long2StringIdMapper()

    override fun onDestroy() {
        super.onDestroy()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_cats_list, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupCatsList(savedInstanceState)
    }

    private fun setupCatsList(savedInstanceState: Bundle?) {
        catsListAdapter = CatsListAdapter()

        catsListAdapter.onClickListener = object : CatsListAdapter.OnClickListener {
            override fun onClick(
                catWithId: Pair<Long, CatData>,
                sharedElement: View,
                sharedElementTransitionName: String
            ) {
                val id = idMapper.stringIdFrom(catWithId.first)
                if(id != null)
                    onItemClickListener?.onItemClick(id, sharedElement, sharedElementTransitionName)
            }
        }

        // TODO: remove hardcode
        val columnWidth = 180
        val itemMargin = 16

        val viewManager = AutoFitGridLayoutManager(requireContext(), columnWidth)
        val itemDecoration = MarginItemDecoration(itemMargin, { viewManager.spanCount })

        recycler_view.apply {
            setHasFixedSize(true)

            layoutManager = viewManager
            adapter = catsListAdapter
            addItemDecoration(itemDecoration)
        }

        catsListAdapter.tracker = createSelectionTracker().apply {
            onRestoreInstanceState(savedInstanceState)
        }
    }

    private fun createSelectionTracker(): SelectionTracker<Long> {
        val uniqueKey: Long = Long2StringIdMapper.INVALID_ID

        val emptyItem = object: ItemDetailsLookup.ItemDetails<Long>() {
            override fun getSelectionKey() = uniqueKey
            override fun getPosition() = Integer.MAX_VALUE
        }

        val tracker = SelectionTracker.Builder<Long>(
            "catsListSelection",
            recycler_view,
            RecyclerViewSelectionUtils.SafeStableIdItemKeyProvider(recycler_view),
            CatsListItemDetailsLookup(recycler_view, emptyItem),
            StorageStrategy.createLongStorage()
        ).withSelectionPredicate(
            RecyclerViewSelectionUtils.IgnoreItemSelectionPredicate(emptyItem)
        ).build()

        return tracker.apply {
            addObserver(
                object : SelectionTracker.SelectionObserver<Long>() {
                    override fun onSelectionChanged() {
                        super.onSelectionChanged()
                        this@CatsListFragment.selection =
                            tracker.selection.mapNotNull { idMapper.stringIdFrom(it) }
                    }
                })
        }
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        catsListAdapter.tracker?.onSaveInstanceState(outState)
    }
}

private class Long2StringIdMapper {
    companion object {
        val INVALID_ID = Long.MAX_VALUE
    }

    private val long2string = mutableMapOf<Long, String>()
    private val string2long = mutableMapOf<String, Long>()
    private var idCounter: Long = 0

    fun longIdFrom(stringId: String): Long {
        var id = string2long.get(stringId)

        if(id == null) {
            id = idCounter
            string2long.put(stringId, id)
            long2string.put(id, stringId)
            idCounter += 1
        }

        return id
    }

    fun stringIdFrom(longId: Long): String? {
        return long2string.get(longId)
    }
}

// Note. getItemDetails return emptyItem when tap outside the recycler view items
// Id of emptyItem not be the same as id of any other items
private class CatsListItemDetailsLookup(
    private val recyclerView: RecyclerView,
    private val emptyItem: ItemDetailsLookup.ItemDetails<Long>
) :
    ItemDetailsLookup<Long>() {
    override fun getItemDetails(event: MotionEvent): ItemDetails<Long>? {
        val view = recyclerView.findChildViewUnder(event.x, event.y)
        if (view != null) {
            return (recyclerView.getChildViewHolder(view) as CatsListAdapter.ViewHolder)
                .getItemDetails()
        }
        return emptyItem
    }
}