package com.example.pos.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.pos.R
import com.example.pos.adapter.ProductAdapter
import com.example.pos.data.local.AppDatabase
import com.example.pos.data.repository.CategoryRepositoryImpl
import com.example.pos.data.repository.ProductRepositoryImpl
import com.example.pos.data.repository.SalesOrderRepositoryImpl
import com.example.pos.model.Product
import com.example.pos.viewmodel.ProductViewModel
import com.example.pos.viewmodel.SalesOrderViewModel
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import java.text.NumberFormat
import java.util.Locale

class CreateSalesOrderFragment : DialogFragment() {
    private lateinit var recyclerView: RecyclerView
    private lateinit var btnCreate: Button
    private lateinit var tvTotal: TextView
    private lateinit var adapter: ProductAdapter

    private val productViewModel: ProductViewModel by viewModels {
        val database = AppDatabase.getDatabase(requireContext())
        val productRepository = ProductRepositoryImpl(database.productDao())
        val categoryRepository = CategoryRepositoryImpl(database.categoryDao())
        ProductViewModel.Factory(productRepository, categoryRepository)
    }

    private val salesOrderViewModel: SalesOrderViewModel by viewModels {
        val database = AppDatabase.getDatabase(requireContext())
        val salesOrderRepository = SalesOrderRepositoryImpl(database.salesOrderDao(), database.salesOrderItemDao())
        SalesOrderViewModel.Factory(salesOrderRepository)
    }

    private val numberFormat = NumberFormat.getCurrencyInstance(Locale("id", "ID"))

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_create_sales_order, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupViews(view)
        setupRecyclerView()
        observeProducts()
    }

    private fun setupViews(view: View) {
        recyclerView = view.findViewById(R.id.rvProducts)
        btnCreate = view.findViewById(R.id.btnCreate)
        tvTotal = view.findViewById(R.id.tvTotal)

        btnCreate.setOnClickListener {
            createSalesOrder()
        }
    }

    private fun setupRecyclerView() {
        adapter = ProductAdapter(
            onQuantityChange = { product, quantity ->
                productViewModel.updateQuantity(product, quantity)
            },
            viewModel = productViewModel
        )
        recyclerView.layoutManager = LinearLayoutManager(context)
        recyclerView.adapter = adapter
    }

    private fun observeProducts() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                launch {
                    productViewModel.products.collectLatest { products ->
                        adapter.submitList(products)
                    }
                }
                launch {
                    productViewModel.selectedQuantities.collectLatest {
                        updateTotal()
                    }
                }
            }
        }
    }

    private fun updateTotal() {
        val total = productViewModel.selectedQuantities.value.entries.sumOf { (product, quantity) ->
            product.price * quantity
        }
        tvTotal.text = "Total: ${numberFormat.format(total)}"
    }

    private fun createSalesOrder() {
        val selectedProducts = productViewModel.selectedQuantities.value
        if (selectedProducts.isEmpty()) {
            // Show error message
            return
        }

        val products = selectedProducts.map { (product, quantity) -> product to quantity }
        salesOrderViewModel.createSalesOrder(products)
        productViewModel.clearQuantities()
        dismiss()
    }
} 