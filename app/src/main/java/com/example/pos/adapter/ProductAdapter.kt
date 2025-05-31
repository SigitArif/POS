package com.example.pos.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageButton
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.pos.R
import com.example.pos.model.Product
import java.text.NumberFormat
import java.util.Locale

class ProductAdapter(
    private var products: List<Product> = emptyList(),
    private val onEditClick: ((Product) -> Unit)? = null,
    private val onDeleteClick: ((Product) -> Unit)? = null,
    private val onQuantityChange: ((Product, Int) -> Unit)? = null
) : RecyclerView.Adapter<ProductAdapter.ViewHolder>() {

    private val numberFormat = NumberFormat.getCurrencyInstance(Locale("id", "ID"))

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val tvName: TextView = view.findViewById(R.id.tvName)
        val tvPrice: TextView = view.findViewById(R.id.tvPrice)
        val tvCategory: TextView = view.findViewById(R.id.tvCategory)
        val btnEdit: ImageButton = view.findViewById(R.id.btnEdit)
        val btnDelete: ImageButton = view.findViewById(R.id.btnDelete)
        val btnMinus: Button = view.findViewById(R.id.btnMinus)
        val btnPlus: Button = view.findViewById(R.id.btnPlus)
        val tvQuantity: TextView = view.findViewById(R.id.tvQuantity)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_product, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val product = products[position]
        
        holder.tvName.text = product.name
        holder.tvPrice.text = numberFormat.format(product.price)
        holder.tvCategory.text = product.category

        // Show/hide edit and delete buttons based on whether callbacks are provided
        holder.btnEdit.visibility = if (onEditClick != null) View.VISIBLE else View.GONE
        holder.btnDelete.visibility = if (onDeleteClick != null) View.VISIBLE else View.GONE

        // Show/hide quantity controls based on whether quantity change callback is provided
        val quantityControlsVisibility = if (onQuantityChange != null) View.VISIBLE else View.GONE
        holder.btnMinus.visibility = quantityControlsVisibility
        holder.btnPlus.visibility = quantityControlsVisibility
        holder.tvQuantity.visibility = quantityControlsVisibility

        // Set up click listeners
        holder.btnEdit.setOnClickListener { onEditClick?.invoke(product) }
        holder.btnDelete.setOnClickListener { onDeleteClick?.invoke(product) }

        // Set up quantity controls
        var quantity = 0
        holder.tvQuantity.text = quantity.toString()

        holder.btnMinus.setOnClickListener {
            if (quantity > 0) {
                quantity--
                holder.tvQuantity.text = quantity.toString()
                onQuantityChange?.invoke(product, quantity)
            }
        }

        holder.btnPlus.setOnClickListener {
            quantity++
            holder.tvQuantity.text = quantity.toString()
            onQuantityChange?.invoke(product, quantity)
        }
    }

    override fun getItemCount() = products.size

    fun updateProducts(newProducts: List<Product>) {
        products = newProducts
        notifyDataSetChanged()
    }
} 