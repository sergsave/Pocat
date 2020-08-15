package com.sergsave.purryourcat.fragments

import android.os.Bundle
import android.view.*
import com.sergsave.purryourcat.R

class UnsavedCatToolbarFragment: ToolbarFragment() {

    interface OnSaveActionClikedListener {
        fun onSaveClicked()
    }

    var onSaveActionClickedListener: OnSaveActionClikedListener? = null

    override val title: String?
        get() { return context?.getString(R.string.purring_title) }
    override val isBackEnabled = true

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.menu_show_not_saved_cat, menu)

        super.onCreateOptionsMenu(menu, inflater)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if(item.itemId != R.id.action_save)
            return super.onOptionsItemSelected(item)

        onSaveActionClickedListener?.onSaveClicked()
        return true
    }
}