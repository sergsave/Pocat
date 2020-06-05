package com.github.sergsave.purr_your_cat.extensions

import android.view.View
import android.os.Build.VERSION
import android.os.Build.VERSION_CODES

// onReady will call after the size will established
// https://stackoverflow.com/questions/3591784/views-getwidth-and-getheight-returns-0
fun View.setOnSizeReadyListener(onReady: (a: Int, b: Int)->Unit) {
    val view = this
    view.addOnLayoutChangeListener(object: View.OnLayoutChangeListener {
        override fun onLayoutChange(
            v: View?,
            left: Int,
            top: Int,
            right: Int,
            bottom: Int,
            oldLeft: Int,
            oldTop: Int,
            oldRight: Int,
            oldBottom: Int
        ) {
            if(v?.width != 0 && v?.height != 0) {
                onReady(view.width, view.height)
                view.removeOnLayoutChangeListener(this)
            }
        }
    })
}