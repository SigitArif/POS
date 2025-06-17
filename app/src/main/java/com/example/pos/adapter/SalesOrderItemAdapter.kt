package com.example.pos.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.pos.R
import com.example.pos.model.SalesOrderItem
import java.text.NumberFormat
import java.util.Locale

class SalesOrderItemAdapter : ListAdapter<SalesOrderItem, SalesOrderItemAdapter.SalesOrderItemViewHolder>(SalesOrderItemDiffCallback()) {

    private val numberFormat = NumberFormat.getCurrencyInstance(Locale("id", "ID"))

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SalesOrderItemViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_sales_order_detail, parent, false)
        return SalesOrderItemViewHolder(view)
    }

    override fun onBindViewHolder(holder: SalesOrderItemViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    inner class SalesOrderItemViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val tvProductName: TextView = itemView.findViewById(R.id.tvProductName)
        private val tvProductCategory: TextView = itemView.findViewById(R.id.tvProductCategory)
        private val tvQuantity: TextView = itemView.findViewById(R.id.tvQuantity)
        private val tvUnitPrice: TextView = itemView.findViewById(R.id.tvUnitPrice)
        private val tvTotalPrice: TextView = itemView.findViewById(R.id.tvTotalPrice)

        fun bind(item: SalesOrderItem) {
            tvProductName.text = item.productName
            tvProductCategory.text = item.productCategory
            tvQuantity.text = item.quantity.toString()
            tvUnitPrice.text = numberFormat.format(item.price)
            tvTotalPrice.text = numberFormat.format(item.price * item.quantity)
        }
    }

    class SalesOrderItemDiffCallback : DiffUtil.ItemCallback<SalesOrderItem>() {
        override fun areItemsTheSame(oldItem: SalesOrderItem, newItem: SalesOrderItem): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: SalesOrderItem, newItem: SalesOrderItem): Boolean {
            return oldItem == newItem
        }
    }
}