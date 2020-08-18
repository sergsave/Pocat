package com.sergsave.purryourcat.helpers

import android.graphics.Rect
import android.view.View
import androidx.recyclerview.widget.RecyclerView

class MarginItemDecoration(private val margin: Int, private val columnCount : () -> Int)
    : RecyclerView.ItemDecoration() {

    override fun getItemOffsets(outRect: Rect, view: View,
                                parent: RecyclerView, state: RecyclerView.State) {

        val position = parent.getChildAdapterPosition(view)
        val columnCount = columnCount()
        val column = position % columnCount

        with(outRect) {
            left = margin - column * margin / columnCount
            right = (column + 1) * margin / columnCount

            if (position < columnCount) {
                top = margin
            }
            bottom = margin
        }
    }
}