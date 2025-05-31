package com.example.pos.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.pos.R
import com.example.pos.model.SalesOrder
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.Locale

class SalesOrderAdapter : ListAdapter<SalesOrder, SalesOrderAdapter.SalesOrderViewHolder>(SalesOrderDiffCallback()) {

    private val numberFormat = NumberFormat.getCurrencyInstance(Locale("id", "ID"))

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SalesOrderViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_sales_order, parent, false)
        return SalesOrderViewHolder(view)
    }

    override fun onBindViewHolder(holder: SalesOrderViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    inner class SalesOrderViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val tvOrderId: TextView = itemView.findViewById(R.id.tvOrderId)
        private val tvDateTime: TextView = itemView.findViewById(R.id.tvDateTime)
        private val tvTotalRevenue: TextView = itemView.findViewById(R.id.tvTotalRevenue)
        private val tvTotalProfit: TextView = itemView.findViewById(R.id.tvTotalProfit)
        private val dateFormat = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault())

        fun bind(salesOrder: SalesOrder) {
            tvOrderId.text = "Order #${salesOrder.id}"
            tvDateTime.text = dateFormat.format(salesOrder.dateTime)
            tvTotalRevenue.text = "Revenue: ${numberFormat.format(salesOrder.totalRevenue)}"
            tvTotalProfit.text = "Profit: ${numberFormat.format(salesOrder.totalProfit)}"
        }
    }

    private class SalesOrderDiffCallback : DiffUtil.ItemCallback<SalesOrder>() {
        override fun areItemsTheSame(oldItem: SalesOrder, newItem: SalesOrder): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: SalesOrder, newItem: SalesOrder): Boolean {
            return oldItem == newItem
        }
    }
} 