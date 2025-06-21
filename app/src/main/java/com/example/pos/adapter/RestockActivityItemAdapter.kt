package com.example.pos.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CheckBox
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.pos.R
import com.example.pos.model.ActivityItem

class RestockActivityItemAdapter(
    private val onItemCheckedChange: (ActivityItem, Boolean) -> Unit
) : ListAdapter<ActivityItem, RestockActivityItemAdapter.ViewHolder>(ActivityItemDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_restock_activity_item, parent, false)
        return ViewHolder(view, onItemCheckedChange)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    class ViewHolder(
        itemView: View,
        private val onItemCheckedChange: (ActivityItem, Boolean) -> Unit
    ) : RecyclerView.ViewHolder(itemView) {
        
        private val checkBoxFulfill: CheckBox = itemView.findViewById(R.id.checkBoxFulfill)
        private val tvProductName: TextView = itemView.findViewById(R.id.tvProductName)
        private val tvProductCategory: TextView = itemView.findViewById(R.id.tvProductCategory)
        private val tvQuantity: TextView = itemView.findViewById(R.id.tvQuantity)
        
        fun bind(activityItem: ActivityItem) {
            tvProductName.text = activityItem.productName
            tvProductCategory.text = activityItem.productCategory
            tvQuantity.text = "Quantity: ${activityItem.quantity}"
            
            // Set checkbox state without triggering listener
            checkBoxFulfill.setOnCheckedChangeListener(null)
            checkBoxFulfill.isChecked = activityItem.isFulfill
            checkBoxFulfill.setOnCheckedChangeListener { _, isChecked ->
                onItemCheckedChange(activityItem, isChecked)
            }
        }
    }

    private class ActivityItemDiffCallback : DiffUtil.ItemCallback<ActivityItem>() {
        override fun areItemsTheSame(oldItem: ActivityItem, newItem: ActivityItem): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: ActivityItem, newItem: ActivityItem): Boolean {
            return oldItem == newItem
        }
    }
} 