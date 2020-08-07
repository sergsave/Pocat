package com.sergsave.purryourcat.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.ViewCompat
import androidx.recyclerview.selection.ItemDetailsLookup
import androidx.recyclerview.selection.SelectionTracker
import androidx.recyclerview.widget.RecyclerView
import kotlinx.android.extensions.LayoutContainer
import com.sergsave.purryourcat.R
import kotlinx.android.synthetic.main.view_cat_item.*
import kotlinx.android.synthetic.main.view_cat_item.view.*
import com.sergsave.purryourcat.models.CatData
import com.sergsave.purryourcat.helpers.ImageUtils

class CatsListAdapter(): RecyclerView.Adapter<CatsListAdapter.ViewHolder>() {

    var tracker: SelectionTracker<Long>? = null

    interface OnClickListener {
        fun onClick(catWithId: Pair<Long, CatData>,
                    sharedElement: View, sharedElementTransitionName: String)
    }

    var onClickListener: OnClickListener? = null

    private var cats = listOf<Pair<Long, CatData>>()

    init {
        // For the shared element transition to work correctly
        setHasStableIds(true)
    }

    override fun getItemId(position: Int): Long {
        return cats.get(position).first
    }

    class ViewHolder(override val containerView: View) :
        RecyclerView.ViewHolder(containerView), LayoutContainer {

        fun bind(cat: CatData, position: Int, isSelected: Boolean, onClickListener: OnClickListener?) {
            name_text.text = cat.name

            is_selected_indicator_image.visibility = if(isSelected) View.VISIBLE else View.GONE

            ImageUtils.loadInto(photo_image.context, cat.photoUri, photo_image)

            val view = photo_image
            val transitionName = "photo_image" + position
            ViewCompat.setTransitionName(view, transitionName)
            containerView.setOnClickListener{
                onClickListener?.onClick(Pair(itemId, cat), view, transitionName)
            }
        }

        fun getItemDetails(): ItemDetailsLookup.ItemDetails<Long> =
            object : ItemDetailsLookup.ItemDetails<Long>() {
                override fun getPosition(): Int = adapterPosition
                override fun getSelectionKey(): Long? = itemId
            }
    }

    fun setItems(catsWithId: List<Pair<Long, CatData>>) {
        this.cats = catsWithId
        notifyDataSetChanged()
    }

    fun getItems() : List<Pair<Long, CatData>> {
        return cats
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.view_cat_item, parent, false)

        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val isSelected = tracker?.isSelected(getItemId(position)) ?: false
        holder.bind(cats.get(position).second, position, isSelected, onClickListener)
    }

    override fun getItemCount() = cats.size
}