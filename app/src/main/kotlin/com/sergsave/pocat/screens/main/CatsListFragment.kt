package com.sergsave.pocat.screens.main

import android.os.Bundle
import android.view.*
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.recyclerview.selection.ItemDetailsLookup
import androidx.recyclerview.selection.SelectionTracker
import androidx.recyclerview.selection.StorageStrategy
import androidx.recyclerview.widget.RecyclerView
import com.sergsave.pocat.R
import com.sergsave.pocat.helpers.*
import com.sergsave.pocat.models.CatData
import kotlinx.android.synthetic.main.fragment_cats_list.*

class CatsListFragment : Fragment() {

    interface OnRemoveSelectionRequestedListener {
        fun onRemoveRequested(selectedIds: List<String>)
    }

    interface OnItemClickListener {
        fun onItemClick(id: String, data: CatData,
            sharedElement: View, sharedElementTransitionName: String)
    }

    var onRemoveSelectionRequestedListener: OnRemoveSelectionRequestedListener? = null
    var onItemClickListener: OnItemClickListener? = null

    var cats: List<Pair<String, CatData>>
        get() = viewModel.catsWithStringId
        set(value) { viewModel.catsWithStringId = value }

    fun clearSelection() = viewModel.clearSelection()

    private val viewModel: CatsListViewModel by viewModels()
    private var actionModeController = ActionModeController()
    private lateinit var catsListAdapter: CatsListAdapter

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

        viewModel.apply {
            cats.observe(viewLifecycleOwner, Observer {
                catsListAdapter.cats = it
            })

            actionModeState.observe(viewLifecycleOwner, Observer {
                if(it) startActionMode() else actionModeController.finishActionMode()
            })

            actionModeTitle.observe(viewLifecycleOwner, Observer {
                actionModeController.title = it
            })

            clearSelectionEvent.observe(viewLifecycleOwner, EventObserver {
                setAllItemSelectionState(false)
            })

            removeRequestedEvent.observe(viewLifecycleOwner, EventObserver {
                onRemoveSelectionRequestedListener?.onRemoveRequested(it)
            })
        }

        savedInstanceState?.let { restoreDialogState() }
    }

    private fun startActionMode() {
        val onActionItemClickListener = object: ActionModeController.OnActionItemClickListener {
            override fun onItemClick(item: MenuItem) {
                when(item.itemId) {
                    R.id.action_remove -> {
                        RemoveConfirmationDialog().also { initDialog(it) }
                            .show(childFragmentManager, REMOVE_DIALOG_TAG)
                    }
                    R.id.action_select_all -> setAllItemSelectionState(true)
                }
            }
        }

        val onFinishedListener = object: ActionModeController.OnFinishedListener {
            override fun onFinished() = viewModel.onActionModeFinished()
        }

        actionModeController.apply {
            this.onActionItemClickListener = onActionItemClickListener
            this.onFinishedListener = onFinishedListener
            startActionMode(requireActivity(), R.menu.menu_selection_context)
            viewModel.onActionModeStarted()
        }
    }

    private fun setupCatsList(savedInstanceState: Bundle?) {
        catsListAdapter = CatsListAdapter()

        catsListAdapter.onClickListener = object : CatsListAdapter.OnClickListener {
            override fun onClick(
                catWithId: Pair<Long, CatData>,
                sharedElement: View,
                sharedElementTransitionName: String
            ) {
                val id = viewModel.stringCatIdFrom(catWithId.first)
                val data = catWithId.second
                if(id == null || !viewModel.handleOnItemClick())
                    return

                onItemClickListener?.onItemClick(id, data, sharedElement,
                    sharedElementTransitionName)
            }
        }

        val columnWidth = resources.getDimensionPixelSize(R.dimen.list_column_width)
        val itemMargin = resources.getDimensionPixelSize(R.dimen.list_item_margin)

        val viewManager = AutoFitGridLayoutManager(requireContext(), columnWidth)
        val itemDecoration = MarginItemDecoration(itemMargin) { viewManager.spanCount }

        recycler_view.apply {
            setHasFixedSize(true)

            layoutManager = viewManager
            adapter = catsListAdapter
            addItemDecoration(itemDecoration)
            setScrollBarVisibleOnlyOnTouch()
        }

        if(!isSelectionEnabled)
            return

        catsListAdapter.tracker = createSelectionTracker().apply {
            onRestoreInstanceState(savedInstanceState)
        }
    }

    private fun setAllItemSelectionState(isSelected: Boolean) {
        catsListAdapter.tracker?.let {
            it.setItemsSelected(viewModel.allSelectionIds, isSelected)
            // Workaround. Force update of recycler view. Without this not all items deselect.
            catsListAdapter.notifyDataSetChanged()
        }
    }

    private fun RecyclerView.setScrollBarVisibleOnlyOnTouch() {
        viewTreeObserver.addOnGlobalLayoutListener {
            isVerticalScrollBarEnabled = false
        }

        setOnTouchListener { _, _ ->
            isVerticalScrollBarEnabled = true
            false
        }
    }

    private fun createSelectionTracker(): SelectionTracker<Long> {
        val uniqueKey: Long = viewModel.invalidId

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
                        viewModel.onSelectionChanged(tracker.selection.toList())
                    }
                })
        }
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        catsListAdapter.tracker?.onSaveInstanceState(outState)
    }

    private val isSelectionEnabled: Boolean
        get() = arguments?.getBoolean(ARG_IS_SELECTION_ENABLED) == true

    private fun initDialog(dialog: RemoveConfirmationDialog) {
        dialog.onRemoveConfirmListener = { viewModel.onRemoveConfirmed() }
    }

    private fun restoreDialogState() {
        val dialog = childFragmentManager.findFragmentByTag(REMOVE_DIALOG_TAG)
                as? RemoveConfirmationDialog
        dialog?.let{ initDialog(it) }
    }

    companion object {
        private const val ARG_IS_SELECTION_ENABLED = "Selection"
        private const val REMOVE_DIALOG_TAG = "RemoveDialog"

        @JvmStatic
        fun newInstance(isSelectionEnabled: Boolean) =
            CatsListFragment().apply {
                arguments = Bundle().apply {
                    putBoolean(ARG_IS_SELECTION_ENABLED, isSelectionEnabled)
                }
            }
    }
}

// Note. This class fixes clear selection on tap outside recycler view
// getItemDetails return emptyItem when tap outside
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