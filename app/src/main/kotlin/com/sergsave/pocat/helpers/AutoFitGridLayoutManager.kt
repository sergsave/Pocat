package com.sergsave.pocat.helpers

import android.content.Context
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.RecyclerView.Recycler
import kotlin.math.max

class AutoFitGridLayoutManager(context: Context, private var columnWidthPx : Int) :
    GridLayoutManager(context, 1) {

    private val displayMetrics = context.resources.displayMetrics
    private var columnWidthChanged = true

    init {
        setColumnWidth(columnWidthPx)
    }

    private fun setColumnWidth(newColumnWidthPx: Int) {
        if (newColumnWidthPx > 0 && newColumnWidthPx != columnWidthPx) {
            columnWidthPx = newColumnWidthPx
            columnWidthChanged = true
        }
    }

    override fun onLayoutChildren(recycler: Recycler, state: RecyclerView.State) {
        if (columnWidthChanged && columnWidthPx > 0) {
            val totalSpace : Int = if (orientation == LinearLayoutManager.VERTICAL) {
                width - paddingRight - paddingLeft
            } else {
                height - paddingTop - paddingBottom
            }
            val spanCount = max(1, totalSpace / columnWidthPx)
            setSpanCount(spanCount)
            columnWidthChanged = false
        }
        super.onLayoutChildren(recycler, state)
    }
}