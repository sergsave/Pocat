package com.sergsave.purryourcat.ui.catslist

import android.content.Intent
import android.annotation.SuppressLint
import androidx.fragment.app.Fragment
import androidx.appcompat.view.menu.MenuBuilder
import android.os.Bundle
import android.view.*
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityOptionsCompat
import androidx.fragment.app.viewModels
import androidx.recyclerview.selection.*
import androidx.recyclerview.widget.RecyclerView
import androidx.lifecycle.Observer
import com.sergsave.purryourcat.R
import com.sergsave.purryourcat.helpers.AutoFitGridLayoutManager
import com.sergsave.purryourcat.helpers.MarginItemDecoration
import com.sergsave.purryourcat.helpers.RecyclerViewSelectionUtils
import com.sergsave.purryourcat.helpers.ActionModeController
import com.sergsave.purryourcat.helpers.EventObserver
import com.sergsave.purryourcat.helpers.setToolbarAsActionBar
import com.sergsave.purryourcat.models.CatData
import com.sergsave.purryourcat.ui.settings.SettingsActivity
import com.sergsave.purryourcat.ui.about.AboutActivity
import com.sergsave.purryourcat.ui.donate.DonateActivity
import com.sergsave.purryourcat.ui.catcard.CatCardActivity
import com.sergsave.purryourcat.MyApplication
import com.sergsave.purryourcat.Constants
import kotlinx.android.synthetic.main.fragment_cats_list.*

class CatsListFragment : Fragment() {

    private val viewModel: CatsListViewModel by viewModels {
        (requireActivity().application as MyApplication).appContainer.provideCatsListViewModelFactory()
    }
    private var actionModeController = ActionModeController()
    private lateinit var catsListAdapter: CatsListAdapter

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

        setHasOptionsMenu(true)

        (activity as? AppCompatActivity)?.setToolbarAsActionBar(toolbar, showBackButton = false)

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
                catsListAdapter.tracker?.clearSelection()
                // Workaround. Force update of recycler view. Without this not all items deselect.
                catsListAdapter.notifyDataSetChanged()
            })
        }

        fab.setOnClickListener {
            actionModeController.finishActionMode()
            startActivity(Intent(requireContext(), CatCardActivity::class.java))
        }

        fab_clickable_layout.setOnClickListener { fab.performClick() }
    }

    private fun startActionMode() {
        val onActionItemClickListener = object: ActionModeController.OnActionItemClickListener {
            override fun onItemClick(item: MenuItem) {
                if(item.itemId == R.id.action_remove)
                    viewModel.onRemovePressed()
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
                if(id != null && viewModel.handleOnItemClick())
                    openCardAnimated(id, sharedElement, sharedElementTransitionName)
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
        }

        catsListAdapter.tracker = createSelectionTracker().apply {
            onRestoreInstanceState(savedInstanceState)
        }
    }

    private fun openCardAnimated(catId: String, sharedElement: View, transitionName: String) {
        val intent = Intent(requireContext(), CatCardActivity::class.java)
        intent.putExtra(Constants.CAT_ID_INTENT_KEY, catId)
        intent.putExtra(Constants.SHARED_TRANSITION_NAME_INTENT_KEY, transitionName)

        val transitionOption = ActivityOptionsCompat.makeSceneTransitionAnimation(
            requireActivity(), sharedElement, transitionName)

        startActivity(intent, transitionOption.toBundle())
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

    @SuppressLint("RestrictedApi")
    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        super.onCreateOptionsMenu(menu, inflater)
        inflater.inflate(R.menu.menu_main, menu)
        if (menu is MenuBuilder)
            menu.setOptionalIconsVisible(true)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        val intent = when (item.itemId) {
            R.id.action_settings -> Intent(requireContext(), SettingsActivity::class.java)
            R.id.action_about -> Intent(requireContext(), AboutActivity::class.java)
            R.id.action_donate ->  Intent(requireContext(), DonateActivity::class.java)
            else -> null
        }

        if(intent == null)
            return super.onOptionsItemSelected(item)

        startActivity(intent)
        return true
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        catsListAdapter.tracker?.onSaveInstanceState(outState)
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