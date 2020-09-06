package com.sergsave.purryourcat.ui.catslist

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
    private var viewModel: CatsListViewModel
    private lateinit var catsListAdapter: CatsListAdapter
    private val idMapper = Long2StringIdMapper()

    data class SharedElementTransitionData()
    fun getSharedElementTransitionData(): SharedElementTransitionData {}

    override fun onDestroy() {
        super.onDestroy()
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

        viewModel.cats.observe(viewLifecycleOwner, Observer<Map<String, CatData>> {
            catsListAdapter.cats = it.mapKeys { (k, _) -> idMapper.longIdFrom(k)}.toList()
        })

        viewModel.selection.observe(viewLifecycleOwner, Observer<List<String>> { selection ->
            idMapper // TODO Map
            catsListAdapter.tracker?.clearSelection()
            selection.forEach { catsListAdapter.tracker?.setItemsSelected(it, true) }

            // Workaround. Force update of recycler view. Without this not all items deselect.
            catsListAdapter.notifyDataSetChanged()
        })
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
                viewModel.onItemClicked(id)
//                if(id != null)
//                    onItemClickListener?.onItemClick(id, sharedElement, sharedElementTransitionName)
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
                        idMapper // TODO map
                        viewModel.changeSelection(selection)
                    }
                })
        }
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        catsListAdapter.tracker?.onSaveInstanceState(outState)
    }
}

// Note. getItemDetails return emptyItem when tap outside the recycler view items
// Id of emptyItem not be the same as id of any other items
private class CatsListItemDetailsLookup(
    private val recyclerView: RecyclerView,
    private val emptyItem: ItemDetails<Long>
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