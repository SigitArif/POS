package com.example.pos.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.pos.R
import com.example.pos.model.Activity
import java.text.SimpleDateFormat
import java.util.Locale

class RestockActivityAdapter(
    private val onItemClick: (Activity) -> Unit
) : ListAdapter<Activity, RestockActivityAdapter.ViewHolder>(ActivityDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_restock_activity, parent, false)
        return ViewHolder(view, onItemClick)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    class ViewHolder(
        itemView: View,
        private val onItemClick: (Activity) -> Unit
    ) : RecyclerView.ViewHolder(itemView) {
        
        private val tvActivityId: TextView = itemView.findViewById(R.id.tvActivityId)
        private val tvStatus: TextView = itemView.findViewById(R.id.tvStatus)
        private val tvCreatedDate: TextView = itemView.findViewById(R.id.tvCreatedDate)
        private val tvUpdatedDate: TextView = itemView.findViewById(R.id.tvUpdatedDate)
        
        private val dateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault())
        
        fun bind(activity: Activity) {
            tvActivityId.text = activity.activityId
            tvStatus.text = activity.status.name
            
            tvCreatedDate.text = "Created: ${dateFormat.format(activity.createdAt)}"
            tvUpdatedDate.text = "Updated: ${dateFormat.format(activity.updatedAt)}"
            
            // Set status background color
            val statusColor = when (activity.status) {
                com.example.pos.model.ActivityStatus.CREATED -> android.R.color.holo_blue_light
                com.example.pos.model.ActivityStatus.INPROGRESS -> android.R.color.holo_orange_light
                com.example.pos.model.ActivityStatus.COMPLETE -> android.R.color.holo_green_light
            }
            tvStatus.setBackgroundResource(statusColor)
            
            itemView.setOnClickListener {
                onItemClick(activity)
            }
        }
    }

    private class ActivityDiffCallback : DiffUtil.ItemCallback<Activity>() {
        override fun areItemsTheSame(oldItem: Activity, newItem: Activity): Boolean {
            return oldItem.activityId == newItem.activityId
        }

        override fun areContentsTheSame(oldItem: Activity, newItem: Activity): Boolean {
            return oldItem == newItem
        }
    }
} 