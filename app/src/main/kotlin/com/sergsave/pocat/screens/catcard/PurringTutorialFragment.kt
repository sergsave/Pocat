package com.sergsave.pocat.screens.catcard

import android.os.Bundle
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import com.sergsave.pocat.R
import com.sergsave.pocat.helpers.EventObserver
import kotlinx.android.synthetic.main.fragment_purring_tutorial.*

class PurringTutorialFragment: Fragment() {

    private val navigation: NavigationViewModel by activityViewModels()

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
        return inflater.inflate(R.layout.fragment_purring_tutorial, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        content_layout.setOnTouchListener { _, event ->
            if(event.action == MotionEvent.ACTION_DOWN)
                finish()
            false // Content should be transparent for touches
        }
        toolbar.setOnTouchListener { _, event ->
            if(event.action == MotionEvent.ACTION_DOWN)
                finish()
            true
        }
        navigation.backPressedEvent.observe(viewLifecycleOwner, EventObserver { finish() })
    }

    private fun finish() {
        requireActivity().supportFragmentManager.popBackStack()
        navigation.onTutorialFinished()
    }
}