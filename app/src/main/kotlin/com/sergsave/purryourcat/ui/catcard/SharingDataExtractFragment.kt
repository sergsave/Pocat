package com.sergsave.purryourcat.ui.catcard

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.google.android.material.snackbar.Snackbar
import com.sergsave.purryourcat.R
import com.sergsave.purryourcat.models.CatData
import kotlinx.android.synthetic.main.fragment_loader.*

class SharingDataExtractFragment: Fragment() {

    private var navigator: NavigationViewModel
    private var viewModel: SharingDataExtractViewModel

    override fun onDestroy() {
        super.onDestroy()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        initViewModel()
    }

    private fun initViewModel() {
        arguments?.let {
            transitionName = it.getString(ARG_TRANSITION_NAME)
        }
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

        if(savedInstanceState == null)
            arguments?.getParcelable<Intent>(ARG_INTENT)?.let { startSharing(it) }

        viewModel.sharingState.observe(viewLifecycleOwner, Observer<Boolean> {
            progressBar.visibility = if(it) View.VISIBLE else View.INVISIBLE
        })

        viewModel.extractSuccessEvent.observe(viewLifecycleOwner, Observer<CatData> {
            navigation.openCat(it)
        })

        viewModel.extractFailedEvent.observe(viewLifecycleOwner, Observer<String> {
            Snackbar.make(container, it, Snackbar.LENGTH_LONG).show()
        })
    }

    override fun onStop() {
        super.onStop()
    }

    companion object {
        private const val ARG_INTENT = "ArgCatData"

        @JvmStatic
        fun newInstance(sharingIntent: Intent) =
            ExternalSharingDataLoadFragment().apply {
                arguments = Bundle().apply {
                    putParcelable(ARG_INTENT, sharingIntent)
                }
            }
    }
}