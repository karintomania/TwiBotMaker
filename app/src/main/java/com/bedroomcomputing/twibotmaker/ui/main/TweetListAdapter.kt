package com.bedroomcomputing.twibotmaker.ui.main

import android.text.Layout
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bedroomcomputing.twibotmaker.R
import com.bedroomcomputing.twibotmaker.databinding.MainFragmentBinding
import com.bedroomcomputing.twibotmaker.databinding.TweetItemBinding
import com.bedroomcomputing.twibotmaker.db.Tweet

class TweetListAdapter : ListAdapter<Tweet, TweetListAdapter.TweetViewHolder>(TweetsComparator()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TweetViewHolder {
        val layoutInflater = LayoutInflater.from(parent.context)
        val binding = TweetItemBinding.inflate(layoutInflater, parent, false)
        return TweetViewHolder(binding)
    }

    override fun onBindViewHolder(holder: TweetViewHolder, position: Int) {
        val current = getItem(position)

        holder.binding.tweet = current
    }

    class TweetViewHolder(val binding: TweetItemBinding) : RecyclerView.ViewHolder(binding.root) {


    }

    class TweetsComparator : DiffUtil.ItemCallback<Tweet>() {
        override fun areItemsTheSame(oldItem: Tweet, newItem: Tweet): Boolean {
            return oldItem === newItem
        }

        override fun areContentsTheSame(oldItem: Tweet, newItem: Tweet): Boolean {
            return oldItem.content == newItem.content
        }
    }
}