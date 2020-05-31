package com.github.sergsave.purr_your_cat.extensions

import android.view.View
import android.view.ViewTreeObserver.OnGlobalLayoutListener
import android.os.Build.VERSION
import android.os.Build.VERSION_CODES

// onReady will call after the size will established
// https://stackoverflow.com/questions/3591784/views-getwidth-and-getheight-returns-0
fun View.setOnSizeReadyListener(onReady: (a: Int, b: Int)->Unit) {
    val view = this
    getViewTreeObserver().addOnGlobalLayoutListener(object : OnGlobalLayoutListener {
        override fun onGlobalLayout() {

            view.viewTreeObserver.removeOnGlobalLayoutListener(this)

            if(android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.JELLY_BEAN) {
                view.viewTreeObserver.removeOnGlobalLayoutListener(this)
            }
            else {
                view.viewTreeObserver.removeGlobalOnLayoutListener(this)
            }

            onReady(view.width, view.height)
        }
    })
}