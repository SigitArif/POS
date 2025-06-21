package com.example.pos.adapter

import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CheckBox
import android.widget.EditText
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.pos.R
import com.example.pos.model.Product

class ProductSelectionAdapter(
    private val onProductSelectionChanged: (Long, Boolean, Int) -> Unit
) : ListAdapter<Product, ProductSelectionAdapter.ViewHolder>(ProductDiffCallback()) {

    private val selectedProducts = mutableMapOf<Long, Pair<Boolean, Int>>()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_product_selection, parent, false)
        return ViewHolder(view, onProductSelectionChanged, selectedProducts)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    fun getSelectedProducts(): List<Pair<Long, Int>> {
        return selectedProducts.entries
            .filter { it.value.first }
            .map { it.key to it.value.second }
    }

    class ViewHolder(
        itemView: View,
        private val onProductSelectionChanged: (Long, Boolean, Int) -> Unit,
        private val selectedProducts: MutableMap<Long, Pair<Boolean, Int>>
    ) : RecyclerView.ViewHolder(itemView) {
        
        private val checkBoxSelect: CheckBox = itemView.findViewById(R.id.checkBoxSelect)
        private val tvProductName: TextView = itemView.findViewById(R.id.tvProductName)
        private val tvProductCategory: TextView = itemView.findViewById(R.id.tvProductCategory)
        private val etQuantity: EditText = itemView.findViewById(R.id.etQuantity)
        
        fun bind(product: Product) {
            tvProductName.text = product.name
            tvProductCategory.text = product.category
            
            // Get current selection state
            val currentSelection = selectedProducts[product.id] ?: Pair(false, 0)
            
            // Set checkbox state without triggering listener
            checkBoxSelect.setOnCheckedChangeListener(null)
            checkBoxSelect.isChecked = currentSelection.first
            checkBoxSelect.setOnCheckedChangeListener { _, isChecked ->
                selectedProducts[product.id] = Pair(isChecked, currentSelection.second)
                onProductSelectionChanged(product.id, isChecked, currentSelection.second)
                etQuantity.isEnabled = isChecked
            }
            
            // Set quantity without triggering listener
            etQuantity.removeTextChangedListener(quantityTextWatcher)
            etQuantity.setText(currentSelection.second.toString())
            etQuantity.isEnabled = currentSelection.first
            etQuantity.addTextChangedListener(quantityTextWatcher)
            
            // Store product ID for text watcher
            etQuantity.tag = product.id
        }
        
        private val quantityTextWatcher = object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: Editable?) {
                val productId = etQuantity.tag as? Long ?: return
                val quantity = s.toString().toIntOrNull() ?: 0
                val currentSelection = selectedProducts[productId] ?: Pair(false, 0)
                selectedProducts[productId] = Pair(currentSelection.first, quantity)
                onProductSelectionChanged(productId, currentSelection.first, quantity)
            }
        }
    }

    private class ProductDiffCallback : DiffUtil.ItemCallback<Product>() {
        override fun areItemsTheSame(oldItem: Product, newItem: Product): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: Product, newItem: Product): Boolean {
            return oldItem == newItem
        }
    }
} 