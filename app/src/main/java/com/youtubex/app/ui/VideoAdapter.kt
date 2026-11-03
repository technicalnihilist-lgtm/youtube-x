package com.youtubex.app.ui

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.recyclerview.widget.RecyclerView
import coil.load
import com.youtubex.app.databinding.ItemVideoBinding
import com.youtubex.app.model.VideoItem
import com.youtubex.app.util.Formats

class VideoAdapter(
    private val onClick: (VideoItem) -> Unit
) : RecyclerView.Adapter<VideoAdapter.VH>() {

    private val items = mutableListOf<VideoItem>()

    fun submit(list: List<VideoItem>) {
        items.clear()
        items.addAll(list)
        notifyDataSetChanged()
    }

    class VH(val binding: ItemVideoBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH =
        VH(ItemVideoBinding.inflate(LayoutInflater.from(parent.context), parent, false))

    override fun getItemCount(): Int = items.size

    override fun onBindViewHolder(holder: VH, position: Int) {
        val item = items[position]
        val b = holder.binding

        b.title.text = item.title
        b.channel.text = item.uploader
        b.meta.text = Formats.metaLine(item.durationSeconds, item.views)

        val duration = Formats.duration(item.durationSeconds)
        b.duration.text = duration
        b.duration.isVisible = duration.isNotBlank()

        if (item.thumbnailUrl.isNotBlank()) {
            b.thumb.load(item.thumbnailUrl)
        } else {
            b.thumb.setImageDrawable(null)
        }

        if (item.avatarUrl.isNotBlank()) {
            b.avatar.load(item.avatarUrl)
        } else {
            b.avatar.setImageDrawable(null)
        }

        b.root.setOnClickListener { onClick(item) }
    }
}
