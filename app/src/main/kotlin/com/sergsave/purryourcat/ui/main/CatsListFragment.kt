package com.sergsave.purryourcat.ui.main

import android.os.Bundle
import android.view.*
import android.view.ViewTreeObserver.OnGlobalLayoutListener
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.recyclerview.selection.ItemDetailsLookup
import androidx.recyclerview.selection.SelectionTracker
import androidx.recyclerview.selection.StorageStrategy
import androidx.recyclerview.widget.RecyclerView
import com.sergsave.purryourcat.R
import com.sergsave.purryourcat.helpers.*
import com.sergsave.purryourcat.models.CatData
import com.sergsave.purryourcat.models.Cat
import com.sergsave.purryourcat.ui.catcard.RemoveConfirmationDialog
import kotlinx.android.synthetic.main.fragment_cats_list.*
import java.util.*

class CatsListFragment : Fragment() {

    interface OnRemoveSelectionRequestedListener {
        fun onRemoveRequested(selectedIds: List<UUID>)
    }

    interface OnItemClickListener {
        fun onItemClick(cat: Cat, sharedElement: View, sharedElementTransitionName: String)
    }

    var onRemoveSelectionRequestedListener: OnRemoveSelectionRequestedListener? = null
    var onItemClickListener: OnItemClickListener? = null

    var cats: List<Cat>
        get() = viewModel.cats
        set(value) { viewModel.cats = value }

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
            catsWithLongId.observe(viewLifecycleOwner, Observer {
                catsListAdapter.cats = it
            })

            actionModeState.observe(viewLifecycleOwner, Observer {
                if(it) startActionMode() else actionModeController.finishActionMode()
            })

            actionModeTitle.observe(viewLifecycleOwner, Observer {
                actionModeController.title = it
            })

            clearSelectionEvent.observe(viewLifecycleOwner, EventObserver {
                catsListAdapter.tracker?.let {
                    it.clearSelection()
                    // Workaround. Force update of recycler view. Without this not all items deselect.
                    catsListAdapter.notifyDataSetChanged()
                }
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
                if(item.itemId != R.id.action_remove)
                    return

                RemoveConfirmationDialog().also { initDialog(it) }
                    .show(childFragmentManager, REMOVE_DIALOG_TAG)
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
                cat: Pair<Long, CatData>,
                sharedElement: View,
                sharedElementTransitionName: String
            ) {
                val uuid = viewModel.uuidFrom(cat.first)
                val data = cat.second
                if(uuid == null || viewModel.handleOnItemClick().not())
                    return

                onItemClickListener?.onItemClick(Cat(uuid, data), sharedElement,
                    sharedElementTransitionName)
            }
        }

        // TODO: remove hardcode
        val columnWidth = 180
        val itemMargin = 16

        val viewManager = AutoFitGridLayoutManager(requireContext(), columnWidth)
        val itemDecoration = MarginItemDecoration(itemMargin) { viewManager.spanCount }

        recycler_view.apply {
            setHasFixedSize(true)

            layoutManager = viewManager
            adapter = catsListAdapter
            addItemDecoration(itemDecoration)
            setScrollBarVisibleOnlyOnTouch()
        }

        if(isSelectionEnabled.not())
            return

        catsListAdapter.tracker = createSelectionTracker().apply {
            onRestoreInstanceState(savedInstanceState)
        }
    }

    private fun RecyclerView.setScrollBarVisibleOnlyOnTouch() {
        getViewTreeObserver().addOnGlobalLayoutListener {
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