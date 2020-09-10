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
import com.google.android.material.snackbar.Snackbar
import com.sergsave.purryourcat.R
import com.sergsave.purryourcat.MyApplication
import com.sergsave.purryourcat.models.CatData
import com.sergsave.purryourcat.helpers.EventObserver
import kotlinx.android.synthetic.main.fragment_loader.*

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
        return inflater.inflate(R.layout.fragment_loader, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        (activity as? AppCompatActivity)?.supportActionBar?.title =
            resources.getString(R.string.wait)

        if(savedInstanceState == null)
            arguments?.getParcelable<Intent>(ARG_INTENT)?.let { viewModel.startExtract(it) }

        viewModel.apply {
            sharingState.observe(viewLifecycleOwner, Observer {
                progressBar.visibility = if(it) View.VISIBLE else View.INVISIBLE
            })

            extractSuccessEvent.observe(viewLifecycleOwner, EventObserver {
                navigation.openCat(it)
            })

            extractFailedEvent.observe(viewLifecycleOwner, EventObserver {
                Snackbar.make(main_layout, it, Snackbar.LENGTH_LONG).show()
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