package com.sergsave.purryourcat.fragments

import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import com.sergsave.purryourcat.R

class UnsavedCatActionBarFragment: Fragment() {

    interface OnSaveActionClikedListener {
        fun onSaveClicked()
    }

    var onSaveActionClickedListener: OnSaveActionClikedListener? = null

    override fun onDestroy() {
        super.onDestroy()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)
    }

    override fun onStart() {
        super.onStart()
        (activity as? AppCompatActivity)?.supportActionBar?.title = context?.getString(R.string.purring_title)
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