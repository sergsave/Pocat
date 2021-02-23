package com.sergsave.pocat.screens.main

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import com.sergsave.pocat.MyApplication
import com.sergsave.pocat.R
import com.sergsave.pocat.helpers.EventObserver
import com.sergsave.pocat.models.CatData
import kotlinx.android.synthetic.main.fragment_user_cats.*

class UserCatsFragment : Fragment() {
    private val viewModel: UserCatsViewModel by viewModels {
        (requireActivity().application as MyApplication).appContainer.provideUserCatsViewModelFactory()
    }

    private val navigation by activityViewModels<NavigationViewModel>()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_user_cats, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val fragment = childFragmentManager.findFragmentById(R.id.list_container)
                as? CatsListFragment ?: CatsListFragment.newInstance(isSelectionEnabled = true)

        if(savedInstanceState == null) {
            childFragmentManager
                .beginTransaction()
                .add(R.id.list_container, fragment)
                .commit()
        }

        navigation.pageChangedEvent.observe(viewLifecycleOwner, EventObserver {
            fragment.clearSelection()
        })

        viewModel.cats.observe(viewLifecycleOwner, Observer {
            empty_list_stub.visibility = if(it.isEmpty()) View.VISIBLE else View.GONE
            fragment.cats = it
        })

        val removeListener = object : CatsListFragment.OnRemoveSelectionRequestedListener {
            override fun onRemoveRequested(selectedIds: List<String>) {
                viewModel.onRemoveRequested(selectedIds)
            }
        }

        val clickListener = object : CatsListFragment.OnItemClickListener {
            override fun onItemClick(
                id: String,
                data: CatData,
                transition: SharedElementTransitionData
            ) {
                viewModel.onCardClicked()
                val card = viewModel.makeCard(id, data)
                navigation.openCat(card, transition)
            }
        }

        fragment.apply {
            onItemClickListener = clickListener
            onRemoveSelectionRequestedListener = removeListener
        }

        fab.setOnClickListener {
            viewModel.onAddClicked()
            fragment.clearSelection()
            navigation.addNewCat()
        }

        fab_clickable_layout.setOnClickListener { fab.performClick() }
    }
}