package com.sergsave.purryourcat.helpers

import android.content.Context
import android.util.DisplayMetrics
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.RecyclerView.Recycler

class AutoFitGridLayoutManager(context: Context, private var columnWidthDp : Int) :
    GridLayoutManager(context, 1) {

    private val displayMetrics = context.getResources().getDisplayMetrics()
    private var columnWidthChanged = true

    init {
        setColumnWidth(columnWidthDp)
    }

    fun setColumnWidth(newColumnWidthDp: Int) {
        if (newColumnWidthDp > 0 && newColumnWidthDp != columnWidthDp) {
            columnWidthDp = newColumnWidthDp
            columnWidthChanged = true
        }
    }

    override fun onLayoutChildren(recycler: Recycler, state: RecyclerView.State) {
        if (columnWidthChanged && columnWidthDp > 0) {
            val columnWidthPx = (columnWidthDp * displayMetrics.density).toInt()

            val totalSpace : Int = if (orientation == LinearLayoutManager.VERTICAL) {
                width - paddingRight - paddingLeft
            } else {
                height - paddingTop - paddingBottom
            }
            val spanCount = Math.max(1, totalSpace / columnWidthPx)
            setSpanCount(spanCount)
            columnWidthChanged = false
        }
        super.onLayoutChildren(recycler, state)
    }
}