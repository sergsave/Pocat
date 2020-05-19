package com.github.sergsave.purr_your_cat

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import kotlinx.android.extensions.LayoutContainer
import kotlinx.android.synthetic.main.view_cat_item.*

class CatsListAdapter(private var catItems: ArrayList<CatItem>,
                      private val onClickListener: (CatItem) -> Unit) :
    RecyclerView.Adapter<CatsListAdapter.ViewHolder>() {

    class ViewHolder(override val containerView: View) :
        RecyclerView.ViewHolder(containerView), LayoutContainer {

        fun bind(item: CatItem, onClickListener: (CatItem) -> Unit) {
            name_text.text = item.name

            containerView.setOnClickListener({ onClickListener(item) })
        }
    }

    fun addItems(items: ArrayList<CatItem>) {
        catItems.addAll(items)
        notifyDataSetChanged()
    }

    fun clearItems() {
        catItems.clear()
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.view_cat_item, parent, false)

        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(catItems.get(position), onClickListener)
    }

    override fun getItemCount() = catItems.size
}
