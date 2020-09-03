package com.sergsave.purryourcat.fragments

import android.content.Intent
import android.view.*
import android.os.Bundle
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Observer
import com.sergsave.purryourcat.R
import com.sergsave.purryourcat.models.CatData
import com.sergsave.purryourcat.sharing.Pack
import com.sergsave.purryourcat.viewmodels.CatDataViewModel

class SavedCatToolbarFragment: ToolbarFragment() {

    interface OnEditActionClickedListener {
        fun onEditClicked()
    }

    interface OnTakeSharingResultListener {
        fun onSuccess(intent: Intent)
        fun onError(error: String?)
    }

    var onTakeSharingResultListener: OnTakeSharingResultListener? = null
    var onEditActionClickedListener: OnEditActionClickedListener? = null

    private var sharingInProgress = false
        set(value) {
            field = value
            activity?.invalidateOptionsMenu()
        }
    private var catData: CatData? = null

    //https://medium.com/@BladeCoder/architecture-components-pitfalls-part-1-9300dd969808
    private val observer = Observer<CatData> { catData = it }

    override val title: String?
        get() { return context?.getString(R.string.purring_title) }
    override val isBackEnabled = true

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setHasOptionsMenu(true)

        val fragment = sharingFragment()
        fragment?.let{ initFragment(it) }
        sharingInProgress = fragment != null

        val viewModel: CatDataViewModel by activityViewModels()
        viewModel.data.removeObserver(observer)
        viewModel.data.observe(this, observer)
    }

    private fun sharingFragment() = childFragmentManager.findFragmentByTag(SHARING_FRAGMENT_TAG) as?
            TakeSharingHeadlessFragment

    override fun onStop() {
        sharingFragment()?.onResultListener = null
        sharingInProgress = false
        super.onStop()
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.menu_show_saved_cat, menu)

        menu.findItem(R.id.action_share).apply {
            if(sharingInProgress)
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
            sharingInProgress = false
        }

        fragment.onResultListener = object: SharingHeadlessFragment.OnResultListener<Intent> {
            override fun onSuccess(data: Intent) {
                finish()
                onTakeSharingResultListener?.onSuccess(data)
            }

            override fun onError(error: SharingHeadlessFragment.ErrorType, message: String?) {
                finish()
                if(error == SharingHeadlessFragment.ErrorType.ERROR_IN_PROCESS)
                    onTakeSharingResultListener?.onError(message)
            }
        }
    }

    private fun startSharing() {
        if(sharingInProgress)
            return

        if(catData == null)
            return

        val fragment = TakeSharingHeadlessFragment.newInstance(Pack(catData!!))
        initFragment(fragment)
        childFragmentManager.beginTransaction().add(fragment, SHARING_FRAGMENT_TAG).commit()

        sharingInProgress = true
    }

    companion object {
        private const val SHARING_FRAGMENT_TAG = "SharingFragmentTag"
    }
}