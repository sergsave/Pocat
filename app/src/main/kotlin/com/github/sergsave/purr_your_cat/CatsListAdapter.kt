package com.github.sergsave.purr_your_cat

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.ViewCompat
import androidx.recyclerview.widget.RecyclerView
import kotlinx.android.extensions.LayoutContainer
import kotlinx.android.synthetic.main.view_cat_item.*
import kotlinx.android.synthetic.main.view_cat_item.view.*

class CatsListAdapter(private val onClickListener: OnClickListener):
    RecyclerView.Adapter<CatsListAdapter.ViewHolder>() {

    interface OnClickListener {
        fun onClick(catData: CatData, sharedElement: View, sharedElementTransitionName: String)
    }

    private var cats = arrayListOf<CatData>()

    class ViewHolder(override val containerView: View) :
        RecyclerView.ViewHolder(containerView), LayoutContainer {

        fun bind(cat: CatData, onClickListener: OnClickListener, position: Int) {
            name_text.text = cat.name
            photo_image.setImageURI(cat.photoUri)

            val view = containerView.photo_image
            val transitionName = "photo_image" + position
            ViewCompat.setTransitionName(view, transitionName)
            containerView.setOnClickListener{ onClickListener.onClick(cat, view, transitionName) }
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

    fun getItemsCopy() : ArrayList<CatData> {
        val catsCopy = arrayListOf<CatData>()
        catsCopy.addAll(cats)
        return catsCopy
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
