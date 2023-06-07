package com.example.storyapp.adapters

import android.content.Intent
import android.icu.text.SimpleDateFormat
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.paging.PagingDataAdapter
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.storyapp.activities.DetailStoryActivity
import com.example.storyapp.activities.DetailStoryActivity.Companion.EXTRA_NAME
import com.example.storyapp.databinding.StoryItemBinding
import com.example.storyapp.responses.ListStoryItem
import java.util.*
import kotlin.collections.ArrayList

class MainAdapter : PagingDataAdapter<ListStoryItem, MainAdapter.MyViewHolder>(DIFF_CALLBACK) {

    private val listStories = ArrayList<ListStoryItem>()
    private var onItemClickCallback: OnItemClickCallback? = null

    fun setOnItemClickCallback(onItemClick: OnItemClickCallback) {
        onItemClickCallback = onItemClick
    }

    class MyViewHolder(var binding: StoryItemBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder {
        val binding = StoryItemBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return MyViewHolder(binding)
    }

    override fun getItemCount(): Int {
        return listStories.size
    }

    override fun onBindViewHolder(holder: MyViewHolder, position: Int) {
        val item = getItem(position)
        item.let { storyItem ->
            holder.binding.tvName.text = storyItem?.name
            holder.binding.tvStory.text = storyItem?.description
            val inputFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.getDefault())
            val outputFormat = SimpleDateFormat("MMM dd, yyyy - hh:mm a", Locale.getDefault())
            val date = inputFormat.parse(storyItem?.createdAt)
            val formattedDateTimeString = outputFormat.format(date)

            holder.binding.tvDate.text = formattedDateTimeString
            Glide.with(holder.itemView.context)
                .load(storyItem?.photoUrl)
                .into(holder.binding.imageView2)

            holder.itemView.setOnClickListener {
                val intent = Intent(holder.itemView.context, DetailStoryActivity::class.java)
                intent.putExtra(EXTRA_NAME, storyItem)
                holder.itemView.context.startActivity(intent)
                onItemClickCallback?.onItemClicked(listStories[position])
            }
        }
    }

    interface OnItemClickCallback {
        fun onItemClicked(data: ListStoryItem)
    }

    companion object {
        val DIFF_CALLBACK = object : DiffUtil.ItemCallback<ListStoryItem>() {
            override fun areItemsTheSame(oldItem: ListStoryItem, newItem: ListStoryItem): Boolean {
                return oldItem == newItem
            }

            override fun areContentsTheSame(
                oldItem: ListStoryItem,
                newItem: ListStoryItem
            ): Boolean {
                return oldItem.id == newItem.id
            }
        }
    }
}