package com.sergsave.purryourcat.ui.main

import androidx.fragment.app.Fragment
import android.os.Bundle
import android.view.*
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import com.sergsave.purryourcat.R
import com.sergsave.purryourcat.models.Cat
import com.sergsave.purryourcat.models.Card
import com.sergsave.purryourcat.MyApplication
import kotlinx.android.synthetic.main.fragment_samples.*

class SamplesFragment : Fragment() {
    private val viewModel: SamplesViewModel by viewModels {
        (requireActivity().application as MyApplication)
            .appContainer.provideSamplesViewModelFactory()
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_samples, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val fragment = childFragmentManager.findFragmentById(R.id.list_container)
                as? CatsListFragment ?: CatsListFragment.newInstance(isSelectionEnabled = false)

        if(savedInstanceState == null) {
            childFragmentManager
                .beginTransaction()
                .add(R.id.list_container, fragment)
                .commit()
        }

        viewModel.cats.observe(viewLifecycleOwner, Observer { fragment.cats = it })

        fragment.onItemClickListener = object : CatsListFragment.OnItemClickListener {
            override fun onItemClick(
                cat: Cat,
                sharedElement: View,
                sharedElementTransitionName: String
            ) {
                val card = Card(cat, isSaveable = false, isShareable = false)
                activity?.launchCatCard(card, sharedElement, sharedElementTransitionName)
            }
        }

    }
}