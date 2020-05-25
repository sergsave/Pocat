package com.github.sergsave.purr_your_cat

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import kotlinx.android.extensions.LayoutContainer
import kotlinx.android.synthetic.main.view_cat_item.*

class CatsListAdapter(private val onClickListener: (CatData) -> Unit) :
    RecyclerView.Adapter<CatsListAdapter.ViewHolder>() {

    private var cats = arrayListOf<CatData>()

    class ViewHolder(override val containerView: View) :
        RecyclerView.ViewHolder(containerView), LayoutContainer {

        fun bind(cat: CatData, onClickListener: (CatData) -> Unit) {
            name_text.text = cat.name

            containerView.setOnClickListener({ onClickListener(cat) })
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
        holder.bind(cats.get(position), onClickListener)
    }

    override fun getItemCount() = cats.size
}
