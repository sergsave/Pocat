package com.sergsave.purryourcat.fragments

import androidx.fragment.app.Fragment
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity

class ActionBarFragment : Fragment() {

    override fun onDestroy() {
        super.onDestroy()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onStart() {
        super.onStart()

        val title = arguments?.let{ it.getString(ARG_TITLE) }
        val isBackEnabled = arguments?.let{ it.getBoolean(ARG_IS_BACK_ENABLED) } ?: false
        val actionBar = (activity as? AppCompatActivity)?.supportActionBar
        actionBar?.apply {
            setDisplayHomeAsUpEnabled(isBackEnabled)
            setDisplayShowHomeEnabled(isBackEnabled)
            this.title = title
        }
    }

    companion object {
        private val ARG_TITLE = "ArgTitle"
        private val ARG_IS_BACK_ENABLED = "ArgIsBackEnabled"

        @JvmStatic
        fun newInstance(title: String?, isBackEnabled: Boolean = true) =
            ActionBarFragment().apply {
                arguments = Bundle().apply {
                    putString(ARG_TITLE, title)
                    putBoolean(ARG_IS_BACK_ENABLED, isBackEnabled)
                }
            }
    }
}