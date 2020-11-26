package com.sergsave.purryourcat.screens.soundselection

import android.net.Uri
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import kotlinx.android.extensions.LayoutContainer
import com.sergsave.purryourcat.R
import kotlinx.android.synthetic.main.view_sound_item.*

class SamplesListAdapter : RecyclerView.Adapter<SamplesListAdapter.ViewHolder>() {

    // Uri to displayed name
    var samples = linkedMapOf<Uri, String>()
        set(value) {
            field = value

            if(field.contains(playing).not())
                playing = null

            if(field.contains(selected).not())
                selected = null

            uri2adapterPosition.clear()
            notifyDataSetChanged()
        }

    var selected: Uri? = null
        set(value) {
            val prev = field
            field = value
            notifyUriItemChanged(prev, value, onSelectedChangedListener)
        }

    var playing: Uri? = null
        set(value) {
            val prev = field
            field = value
            notifyUriItemChanged(prev, value, onPlayingChangedListener)
        }

    var onSelectedChangedListener: ((Uri?) -> Unit)? = null
    var onPlayingChangedListener: ((Uri?) -> Unit)? = null

    private val uri2adapterPosition = mutableMapOf<Uri, Int>()

    private fun notifyUriItemChanged(from: Uri?, to: Uri?, callback: ((Uri?) -> Unit)?) {
        val position = { uri: Uri? ->
            uri?.let { uri2adapterPosition.get(it) }
        }

        position(from)?.let { notifyItemChanged(it) }
        position(to)?.let { notifyItemChanged(it) }
        if(from != to)
            callback?.invoke(to)
    }

    inner class ViewHolder(override val containerView: View) :
        RecyclerView.ViewHolder(containerView), LayoutContainer {

        private fun updateUi(name: String, isSelected: Boolean, isPlaying: Boolean) {
            name_text.text = name

            containerView.isSelected = isSelected
            // Setup alpha from xml supported only for 23 api
            containerView.background.alpha = 127

            val resId =
                if (isPlaying)
                    R.drawable.ic_pause_circle_outline_black_24dp
                else
                    R.drawable.ic_play_circle_outline_black_24dp

            icon_image.setImageResource(resId)
        }

        fun bind(uri: Uri, name: String) {
            uri2adapterPosition.put(uri, adapterPosition)

            val update = { updateUi(name, uri == selected, uri == playing) }
            update()

            containerView.setOnClickListener {
                selected = if (uri != selected) uri else selected
                playing = if (uri != playing) uri else null
                update()
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.view_sound_item, parent, false)

        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        samples.toList().elementAtOrNull(position)?.let { holder.bind(it.first, it.second) }
    }

    override fun getItemCount() = samples.size
}
