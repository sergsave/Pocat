package com.sergsave.purryourcat.ui.catcard

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import com.sergsave.purryourcat.MyApplication
import com.sergsave.purryourcat.R
import com.sergsave.purryourcat.helpers.EventObserver
import com.sergsave.purryourcat.ui.catcard.SharingDataExtractViewModel.SharingState
import kotlinx.android.synthetic.main.fragment_sharing_data_extract.*

class SharingDataExtractFragment: Fragment() {
    private val navigation: NavigationViewModel by activityViewModels()
    private val viewModel: SharingDataExtractViewModel by viewModels {
        (requireActivity().application as MyApplication).appContainer
            .provideSharingDataExtractViewModelFactory()
    }

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
        return inflater.inflate(R.layout.fragment_sharing_data_extract, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        (activity as? AppCompatActivity)?.supportActionBar?.title =
            resources.getString(R.string.loading)

        val sharingIntent = arguments?.getParcelable<Intent>(ARG_INTENT)

        if(savedInstanceState == null)
            viewModel.startExtract(sharingIntent)

        retry_button.setOnClickListener { viewModel.startExtract(sharingIntent) }

        navigation.backPressedEvent.observe(viewLifecycleOwner, EventObserver {
            navigation.goToBackScreen()
        })

        viewModel.apply {
            sharingState.observe(viewLifecycleOwner, Observer {
                listOf(progress_bar, no_connection_layout, invalid_link_text, unknown_error_text)
                    .forEach { it.visibility = View.INVISIBLE }

                when (it) {
                    SharingState.INITIAL -> null
                    SharingState.LOADING -> progress_bar
                    SharingState.NO_CONNECTION_ERROR -> no_connection_layout
                    SharingState.INVALID_LINK_ERROR -> invalid_link_text
                    SharingState.UNKNOWN_ERROR -> unknown_error_text
                    else -> null
                }
                    ?.visibility = View.VISIBLE
            })

            extractSuccessEvent.observe(viewLifecycleOwner, EventObserver {
                navigation.openCat(it)
            })
        }
    }

    companion object {
        private const val ARG_INTENT = "ArgIntent"

        @JvmStatic
        fun newInstance(sharingIntent: Intent) =
            SharingDataExtractFragment().apply {
                arguments = Bundle().apply {
                    putParcelable(ARG_INTENT, sharingIntent)
                }
            }
    }
}