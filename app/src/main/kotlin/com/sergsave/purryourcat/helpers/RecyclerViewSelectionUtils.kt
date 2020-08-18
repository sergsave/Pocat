package com.sergsave.purryourcat.helpers

import androidx.recyclerview.selection.ItemDetailsLookup
import androidx.recyclerview.selection.ItemKeyProvider
import androidx.recyclerview.selection.SelectionTracker
import androidx.recyclerview.widget.RecyclerView

object RecyclerViewSelectionUtils {

    class IgnoreItemSelectionPredicate(private val item: ItemDetailsLookup.ItemDetails<Long>)
        : SelectionTracker.SelectionPredicate<Long>() {
        override fun canSelectMultiple() = true
        override fun canSetStateForKey(key: Long, nextState: Boolean) =
            key != item.selectionKey
        override fun canSetStateAtPosition(position: Int, nextState: Boolean) =
            position != item.position
    }

    // standard StableIdKeyProvider throw exception when orientation changed
    class SafeStableIdItemKeyProvider(private val recyclerView: RecyclerView) :
        ItemKeyProvider<Long>(SCOPE_MAPPED) {
        override fun getKey(position: Int): Long? {
            return recyclerView.adapter?.getItemId(position)
        }

        override fun getPosition(key: Long): Int {
            val viewHolder = recyclerView.findViewHolderForItemId(key)
            return viewHolder?.layoutPosition ?: RecyclerView.NO_POSITION
        }
    }
}