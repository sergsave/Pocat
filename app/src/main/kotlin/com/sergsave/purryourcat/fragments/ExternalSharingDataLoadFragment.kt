package com.sergsave.purryourcat.fragments

import android.content.Intent
import android.opengl.Visibility
import android.view.View
import androidx.fragment.app.Fragment
import android.os.Bundle
import android.view.*
import com.sergsave.purryourcat.R
import com.sergsave.purryourcat.models.CatData
import com.sergsave.purryourcat.sharing.Pack
import kotlinx.android.synthetic.main.fragment_loader.*

class ExternalSharingDataLoadFragment: Fragment() {

    interface OnGiveSharingResultListener {
        fun onSuccess(catData: CatData)
        fun onError(error: String?)
    }

    var onGiveSharingResultListener: OnGiveSharingResultListener? = null

    private var sharingInProgress = false
        set(value) {
            field = value
            progressBar.visibility = if(value) View.VISIBLE else View.INVISIBLE
        }

    override fun onDestroy() {
        super.onDestroy()
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
        startSharing()
    }

    override fun onStop() {
        sharingFragment()?.onResultListener = null
        sharingInProgress = false
        super.onStop()
    }

    private fun sharingFragment() = childFragmentManager.findFragmentByTag(SHARING_FRAGMENT_TAG) as?
            GiveSharingHeadlessFragment

    private fun initFragment(fragment: GiveSharingHeadlessFragment) {
        val finish = {
            childFragmentManager.beginTransaction().remove(fragment).commit()
            sharingInProgress = false
        }

        fragment.onResultListener = object: SharingHeadlessFragment.OnResultListener<Pack> {
            override fun onSuccess(data: Pack) {
                finish()
                onGiveSharingResultListener?.onSuccess(data.cat)
            }

            override fun onError(error: SharingHeadlessFragment.ErrorType, message: String?) {
                finish()
                if(error == SharingHeadlessFragment.ErrorType.ERROR_IN_PROCESS)
                    onGiveSharingResultListener?.onError(message)
            }
        }
    }

    private fun startSharing() {
        val currentFragment = sharingFragment()

        if(currentFragment != null) {
            initFragment(currentFragment)
            sharingInProgress = true
            return
        }

        val intent = arguments?.let { it.getParcelable<Intent>(ARG_INTENT) }
        if(intent == null)
            return

        sharingInProgress = true
        val newFragment = GiveSharingHeadlessFragment.newInstance(intent)
        initFragment(newFragment)
        childFragmentManager.beginTransaction().add(newFragment, SHARING_FRAGMENT_TAG).commit()
    }

    companion object {
        private val SHARING_FRAGMENT_TAG = "SharingFragmentTag"
        private val ARG_INTENT = "ArgCatData"

        @JvmStatic
        fun newInstance(sharingIntent: Intent) =
            ExternalSharingDataLoadFragment().apply {
                arguments = Bundle().apply {
                    putParcelable(ARG_INTENT, sharingIntent)
                }
            }
    }
}