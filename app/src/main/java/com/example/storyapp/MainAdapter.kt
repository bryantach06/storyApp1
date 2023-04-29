package com.example.storyapp

import android.annotation.SuppressLint
import android.icu.text.SimpleDateFormat
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.storyapp.databinding.StoryItemBinding
import java.util.*
import kotlin.collections.ArrayList

class MainAdapter: RecyclerView.Adapter<MainAdapter.MyViewHolder>() {

    private val listStories = ArrayList<ListStoryItem>()
    private lateinit var onItemClickCallback: OnItemClickCallback

    class MyViewHolder(var binding: StoryItemBinding) : RecyclerView.ViewHolder(binding.root) {

    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder {
        val binding = StoryItemBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return MyViewHolder(binding)
    }

    override fun getItemCount() = listStories.size

    override fun onBindViewHolder(holder: MyViewHolder, position: Int) {
        val (photoUrl, createdAt, name, description, _) = listStories[position]
        holder.binding.tvName.text = name
        holder.binding.tvStory.text = description
        val inputFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.getDefault())
        val outputFormat = SimpleDateFormat("MMM dd, yyyy - hh:mm a", Locale.getDefault())
        val date = inputFormat.parse(createdAt)
        val formattedDateTimeString = outputFormat.format(date)

        holder.binding.tvDate.text = formattedDateTimeString
        Glide.with(holder.itemView.context)
            .load(photoUrl)
            .into(holder.binding.imageView2)
        holder.binding.imageView2.setOnClickListener{
            onItemClickCallback.onItemClicked(listStories[holder.adapterPosition])
        }
    }

    @SuppressLint("NotifyDataSetChanged")
    fun setListStories(stories: List<ListStoryItem>) {
        listStories.clear()
        listStories.addAll(stories)
        notifyDataSetChanged()
    }

    fun setOnItemClickCallback(onItemClickCallback: OnItemClickCallback) {
        this.onItemClickCallback = onItemClickCallback
    }

    interface OnItemClickCallback {
        fun onItemClicked(data: ListStoryItem)
    }
}