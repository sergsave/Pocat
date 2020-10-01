package com.sergsave.purryourcat.ui.soundselection

import android.net.Uri
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import kotlinx.android.extensions.LayoutContainer
import com.sergsave.purryourcat.R
import com.sergsave.purryourcat.helpers.FileUtils
import kotlinx.android.synthetic.main.view_sound_item.*
import kotlinx.android.synthetic.main.view_sound_header_item.*

class SoundSelectionAdapter : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    private sealed class Item(val viewType: Int) {
        data class Sample(val uri: Uri) : Item(ViewType.SAMPLE.ordinal)
        data class Header(val text: String) : Item(ViewType.HEADER.ordinal)
    }

    private enum class ViewType {
        SAMPLE, HEADER
    }

    interface OnCurrentUriChanged {
        fun onCurrentUriChanged(from: Uri?, to: Uri?)
    }

    private val items: List<Item>
        get() {
            return sections.flatMap { section ->
                val header = section.header?.let { Item.Header(it) }
                val samples = section.samples.map { Item.Sample(it) }.toList()
                (listOf(header) + samples).filterNotNull()
            }
        }

    private val uri2adapterPosition = mutableMapOf<Uri, Int>()

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

    var onSelectedChangedListener: OnCurrentUriChanged? = null
    var onPlayingChangedListener: OnCurrentUriChanged? = null

    data class Section(val header: String?, val samples: Set<Uri>)

    var sections = listOf<Section>()
        private set(value) {
            field = value
            val uris = extractUris(value)

            if(uris.contains(playing).not())
                playing = null

            if(uris.contains(selected).not())
                selected = null

            uri2adapterPosition.clear()
            notifyDataSetChanged()
        }

    // All uris in Adapter must be unique
    fun updateSections(sections: List<Section>): Boolean {
        val uris = extractUris(sections)
        if(uris.size != uris.distinct().size)
            return false

        this.sections = sections
        return true
    }

    private fun extractUris(sections: List<Section>): List<Uri> {
        return sections.flatMap { it.samples }
    }

    private fun notifyUriItemChanged(from: Uri?, to: Uri?, callback: OnCurrentUriChanged?) {
        val position = { uri: Uri? ->
            uri?.let { uri2adapterPosition.get(it) }
        }

        position(from)?.let { notifyItemChanged(it) }
        position(to)?.let { notifyItemChanged(it) }
        if (from != to)
            callback?.onCurrentUriChanged(from, to)
    }

    inner class SampleViewHolder(override val containerView: View) :
        RecyclerView.ViewHolder(containerView), LayoutContainer {

        private fun updateUi(uri: Uri, isSelected: Boolean, isPlaying: Boolean) {
            name_text.text = FileUtils.getContentFileName(containerView.context, uri)

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

        fun bind(current: Uri) {
            uri2adapterPosition.put(current, adapterPosition)

            val update = { updateUi(current, current == selected, current == playing) }
            update()

            containerView.setOnClickListener {
                selected = if (current != selected) current else selected
                playing = if (current != playing) current else null
                update()
            }
        }
    }

    class HeaderViewHolder(override val containerView: View) :
        RecyclerView.ViewHolder(containerView), LayoutContainer {

        fun bind(header: String) {
            header_text.text = header
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val inflate = { layoutId: Int ->
            LayoutInflater.from(parent.context).inflate(layoutId, parent, false)
        }

        return when (viewType) {
            ViewType.HEADER.ordinal -> HeaderViewHolder(inflate(R.layout.view_sound_header_item))
            ViewType.SAMPLE.ordinal -> SampleViewHolder(inflate(R.layout.view_sound_item))
            else -> object : RecyclerView.ViewHolder(View(parent.context)) {}
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val item = items.get(position)
        when (item) {
            is Item.Header -> (holder as? HeaderViewHolder)?.bind(item.text)
            is Item.Sample -> (holder as? SampleViewHolder)?.bind(item.uri)
        }
    }

    override fun getItemCount() = items.size

    override fun getItemViewType(position: Int): Int {
        return items.get(position).viewType
    }
}
