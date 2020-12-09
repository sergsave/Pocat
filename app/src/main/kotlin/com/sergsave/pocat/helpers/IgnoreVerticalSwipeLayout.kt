package com.sergsave.pocat.helpers

import android.content.Context
import android.util.AttributeSet
import android.view.MotionEvent
import android.widget.FrameLayout
import kotlin.math.absoluteValue

// A layout that only supports horizontal swipe. Usually used for a horizontal ViewPager child
// to avoid unwanted swipes instead of vertical child scrolling
class IgnoreVerticalSwipeLayout : FrameLayout {
    constructor(context: Context) : super(context)
    constructor(context: Context, attrs: AttributeSet?) : super(context, attrs)

    private var initialX = 0f
    private var initialY = 0f

    override fun onInterceptTouchEvent(e: MotionEvent): Boolean {
        when(e.action) {
            MotionEvent.ACTION_DOWN -> {
                initialX = e.x
                initialY = e.y
                parent.requestDisallowInterceptTouchEvent(true)
            }
            MotionEvent.ACTION_MOVE -> {
                val dx = e.x - initialX
                val dy = e.y - initialY
                parent.requestDisallowInterceptTouchEvent(dx.absoluteValue < dy.absoluteValue)
            }
        }
        return super.onInterceptTouchEvent(e)
    }
}