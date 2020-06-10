package com.github.sergsave.purr_your_cat.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.ViewCompat
import androidx.recyclerview.widget.RecyclerView
import kotlinx.android.extensions.LayoutContainer
import com.github.sergsave.purr_your_cat.R
import kotlinx.android.synthetic.main.view_cat_item.*
import kotlinx.android.synthetic.main.view_cat_item.view.*
import com.github.sergsave.purr_your_cat.models.CatData
import com.github.sergsave.purr_your_cat.helpers.ImageUtils

class CatsListAdapter(private val onClickListener: OnClickListener):
    RecyclerView.Adapter<CatsListAdapter.ViewHolder>() {

    interface OnClickListener {
        fun onClick(position: Int, sharedElement: View, sharedElementTransitionName: String)
    }

    private var cats = arrayListOf<CatData>()

    class ViewHolder(override val containerView: View) :
        RecyclerView.ViewHolder(containerView), LayoutContainer {

        fun bind(cat: CatData, onClickListener: OnClickListener, position: Int) {
            name_text.text = cat.name

            ImageUtils.loadInto(photo_image.context, cat.photoUri, photo_image)

            val view = photo_image
            val transitionName = "photo_image" + position
            ViewCompat.setTransitionName(view, transitionName)
            containerView.setOnClickListener{ onClickListener.onClick(position, view, transitionName) }
        }
    }

    fun addItems(cats: ArrayList<CatData>) {
        this.cats.addAll(cats)
        notifyDataSetChanged()
    }

    fun clearItems() {
        cats.clear()
        notifyDataSetChanged()
    }

    fun getItems() : ArrayList<CatData> {
        return cats
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.view_cat_item, parent, false)

        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(cats.get(position), onClickListener, position)
    }

    override fun getItemCount() = cats.size
}
