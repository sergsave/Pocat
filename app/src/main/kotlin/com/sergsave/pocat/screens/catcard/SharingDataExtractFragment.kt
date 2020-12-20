package com.sergsave.pocat.screens.catcard

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
import com.sergsave.pocat.MyApplication
import com.sergsave.pocat.R
import com.sergsave.pocat.helpers.EventObserver
import com.sergsave.pocat.screens.catcard.SharingDataExtractViewModel.ExtractState
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
            extractState.observe(viewLifecycleOwner, Observer {
                listOf(progress_bar, no_connection_layout, error_text)
                    .forEach { it.visibility = View.INVISIBLE }

                val errorTexts = mapOf(
                    ExtractState.INVALID_LINK_ERROR to R.string.invalid_link_error,
                    ExtractState.INVALID_DATA_ERROR to R.string.invalid_data_error,
                    ExtractState.UNKNOWN_ERROR to R.string.general_sharing_error
                )

                when (it) {
                    ExtractState.LOADING ->
                        progress_bar.visibility = View.VISIBLE
                    ExtractState.NO_CONNECTION_ERROR ->
                        no_connection_layout.visibility = View.VISIBLE
                    ExtractState.INVALID_LINK_ERROR,
                    ExtractState.INVALID_DATA_ERROR,
                    ExtractState.UNKNOWN_ERROR -> {
                        error_text.visibility = View.VISIBLE
                        error_text.text = getString(errorTexts.getValue(it))
                    }
                    else -> { }
                }
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