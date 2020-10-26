package com.sergsave.purryourcat.ui.main

import androidx.fragment.app.Fragment
import android.os.Bundle
import android.view.*
import androidx.fragment.app.viewModels
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Observer
import com.sergsave.purryourcat.R
import com.sergsave.purryourcat.models.CatData
import com.sergsave.purryourcat.MyApplication
import com.sergsave.purryourcat.helpers.EventObserver
import kotlinx.android.synthetic.main.fragment_user_cats.*

class UserCatsFragment : Fragment() {
    private val viewModel: UserCatsViewModel by viewModels {
        (requireActivity().application as MyApplication).appContainer.provideUserCatsViewModelFactory()
    }

    private val activityViewModel: MainViewModel by activityViewModels()

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

        activityViewModel.clearSelectionEvent.observe(viewLifecycleOwner, EventObserver {
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
                sharedElement: View,
                sharedElementTransitionName: String
            ) {
                val card = viewModel.makeCard(id, data)
                activity?.launchCatCard(card, sharedElement, sharedElementTransitionName)
            }
        }

        fragment.apply {
            onItemClickListener = clickListener
            onRemoveSelectionRequestedListener = removeListener
        }

        fab.setOnClickListener {
            fragment.clearSelection()
            activity?.launchCatCard()
        }

        fab_clickable_layout.setOnClickListener { fab.performClick() }
    }
}