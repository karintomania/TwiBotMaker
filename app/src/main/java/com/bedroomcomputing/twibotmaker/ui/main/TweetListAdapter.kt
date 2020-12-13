package com.bedroomcomputing.twibotmaker.ui.main

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bedroomcomputing.twibotmaker.R
import com.bedroomcomputing.twibotmaker.db.Tweet

class TweetListAdapter : ListAdapter<Tweet, TweetListAdapter.TweetViewHolder>(TweetsComparator()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TweetViewHolder {
        return TweetViewHolder.create(parent)
    }

    override fun onBindViewHolder(holder: TweetViewHolder, position: Int) {
        val current = getItem(position)
        holder.bind(current.content)
    }

    class TweetViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val tweetItemView: TextView = itemView.findViewById(R.id.text_item)

        fun bind(text: String?) {
            tweetItemView.text = text
        }

        companion object {
            fun create(parent: ViewGroup): TweetViewHolder {
                val view: View = LayoutInflater.from(parent.context)
                    .inflate(R.layout.main_fragment, parent, false)
                return TweetViewHolder(view)
            }
        }
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