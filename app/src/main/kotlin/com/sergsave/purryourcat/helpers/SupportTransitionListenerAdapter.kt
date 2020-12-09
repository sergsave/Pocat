package com.sergsave.pocat.helpers

import android.transition.Transition

open class SupportTransitionListenerAdapter: Transition.TransitionListener {
    override fun onTransitionCancel(transition: Transition?) {}
    override fun onTransitionEnd(transition: Transition?) {}
    override fun onTransitionPause(transition: Transition?) {}
    override fun onTransitionResume(transition: Transition?) {}
    override fun onTransitionStart(transition: Transition?) {}
}