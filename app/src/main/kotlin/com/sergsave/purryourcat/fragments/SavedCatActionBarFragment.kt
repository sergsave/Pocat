package com.sergsave.purryourcat.fragments

import android.content.Context
import android.content.Intent
import androidx.fragment.app.Fragment
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.*
import com.sergsave.purryourcat.R
import com.sergsave.purryourcat.helpers.ActivityUtils
import com.sergsave.purryourcat.models.CatData
import com.sergsave.purryourcat.sharing.Pack

class SavedCatActionBarFragment: Fragment() {

    interface OnEditActionClikedListener {
        fun onEditClicked()
    }

    interface OnTakeSharingResultListener {
        fun onSuccess(intent: Intent)
        fun onError(error: String?)
    }

    var onTakeSharingResultListener: OnTakeSharingResultListener? = null
    var onEditActionClickedListener: OnEditActionClikedListener? = null

    private var sharingInProcess = false
        set(value) {
            field = value
            activity?.invalidateOptionsMenu()
        }

    override fun onDestroy() {
        super.onDestroy()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setHasOptionsMenu(true)

        val fragment = childFragmentManager.findFragmentByTag(SHARING_FRAGMENT_TAG) as?
                TakeSharingHeadlessFragment
        fragment?.let{ initFragment(it) }
        sharingInProcess = fragment != null
    }

    override fun onStart() {
        super.onStart()
        ActivityUtils.setupActionBar(activity as? AppCompatActivity,
            context?.getString(R.string.purring_title),
            true)
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.menu_show_saved_cat, menu)

        menu.findItem(R.id.action_share).apply {
            if(sharingInProcess)
                setActionView(R.layout.view_loader)
            else
                setActionView(null)
        }

        super.onCreateOptionsMenu(menu, inflater)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.action_edit -> onEditActionClickedListener?.onEditClicked()
            R.id.action_share -> startSharing()
            else -> return super.onOptionsItemSelected(item)
        }

        return true
    }

    private fun initFragment(fragment: TakeSharingHeadlessFragment) {
        val finish = {
            childFragmentManager.beginTransaction().remove(fragment).commit()
            sharingInProcess = false
        }

        fragment.onResultListener = object: BaseSharingHeadlessFragment.OnResultListener<Intent> {
            override fun onSuccess(data: Intent) {
                finish()
                onTakeSharingResultListener?.onSuccess(data)
            }

            override fun onError(error: BaseSharingHeadlessFragment.ErrorType, message: String?) {
                finish()
                if(error == BaseSharingHeadlessFragment.ErrorType.ERROR_IN_PROCESS)
                    onTakeSharingResultListener?.onError(message)
            }
        }
    }

    private fun startSharing() {
        if(sharingInProcess)
            return

        val catData = arguments?.let { it.getParcelable<CatData>(ARG_CAT_DATA) }
        if(catData == null)
            return

        val fragment = TakeSharingHeadlessFragment.newInstance(Pack(catData))
        initFragment(fragment)
        childFragmentManager.beginTransaction().add(fragment, SHARING_FRAGMENT_TAG).commit()

        sharingInProcess = true
    }

    companion object {
        private val SHARING_FRAGMENT_TAG = "SharingFragmentTag"
        private val ARG_CAT_DATA = "ArgCatData"

        @JvmStatic
        fun newInstance(catData: CatData) =
            SavedCatActionBarFragment().apply {
                arguments = Bundle().apply {
                    putParcelable(ARG_CAT_DATA, catData)
                }
            }
    }
}