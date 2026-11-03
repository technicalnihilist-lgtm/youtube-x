package com.youtubex.app.ui

import android.view.LayoutInflater
import android.view.ViewGroup
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
        holder.binding.title.text = item.title
        holder.binding.channel.text = item.uploader
        holder.binding.meta.text = Formats.metaLine(item.durationSeconds, item.views)
        if (item.thumbnailUrl.isNotBlank()) {
            holder.binding.thumb.load(item.thumbnailUrl)
        } else {
            holder.binding.thumb.setImageDrawable(null)
        }
        holder.binding.root.setOnClickListener { onClick(item) }
    }
}
