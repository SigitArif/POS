package com.example.pos.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ProgressBar
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.pos.R
import com.example.pos.adapter.ProductSelectionAdapter
import com.example.pos.data.local.AppDatabase
import com.example.pos.data.repository.ActivityRepositoryImpl
import com.example.pos.data.repository.CategoryRepositoryImpl
import com.example.pos.data.repository.ProductRepositoryImpl
import com.example.pos.viewmodel.ActivityViewModel
import com.example.pos.viewmodel.ProductViewModel
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

class CreateRestockActivityFragment : Fragment() {

    private val activityViewModel: ActivityViewModel by viewModels {
        val database = AppDatabase.getDatabase(requireContext())
        val activityRepository = ActivityRepositoryImpl(database)
        ActivityViewModel.Factory(activityRepository)
    }
    private val productViewModel: ProductViewModel by viewModels(ownerProducer = { this }) {
        val database = AppDatabase.getDatabase(requireContext())
        val productRepository = ProductRepositoryImpl(database.productDao())
        val categoryRepository = CategoryRepositoryImpl(database.categoryDao())
        ProductViewModel.Factory(productRepository, categoryRepository)
    }
    private lateinit var adapter: ProductSelectionAdapter
    private lateinit var recyclerView: RecyclerView
    private lateinit var emptyView: TextView
    private lateinit var progressBar: ProgressBar
    private lateinit var btnCancel: Button
    private lateinit var btnCreate: Button

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_create_restock_activity, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        setupViews(view)
        setupRecyclerView()
        setupObservers()
        setupClickListeners()
    }

    private fun setupViews(view: View) {
        recyclerView = view.findViewById(R.id.recyclerView)
        emptyView = view.findViewById(R.id.emptyView)
        progressBar = view.findViewById(R.id.progressBar)
        btnCancel = view.findViewById(R.id.btnCancel)
        btnCreate = view.findViewById(R.id.btnCreate)
    }

    private fun setupRecyclerView() {
        adapter = ProductSelectionAdapter { productId, isSelected, quantity ->
            updateCreateButtonState()
        }
        
        recyclerView.apply {
            layoutManager = LinearLayoutManager(context)
            adapter = this@CreateRestockActivityFragment.adapter
        }
    }

    private fun setupObservers() {
        viewLifecycleOwner.lifecycleScope.launch {
            productViewModel.products.collectLatest { products ->
                adapter.submitList(products)
                updateEmptyView(products.isEmpty())
            }
        }
    }

    private fun setupClickListeners() {
        btnCancel.setOnClickListener {
            findNavController().navigateUp()
        }

        btnCreate.setOnClickListener {
            createRestockActivity()
        }
    }

    private fun updateCreateButtonState() {
        val selectedProducts = adapter.getSelectedProducts()
        val hasValidSelections = selectedProducts.any { it.second > 0 }
        btnCreate.isEnabled = hasValidSelections
    }

    private fun updateEmptyView(isEmpty: Boolean) {
        emptyView.visibility = if (isEmpty) View.VISIBLE else View.GONE
        recyclerView.visibility = if (isEmpty) View.GONE else View.VISIBLE
    }

    private fun createRestockActivity() {
        android.util.Log.d("CreateRestockActivityFragment", "Starting createRestockActivity")
        
        val selectedProducts = adapter.getSelectedProducts()
        if (selectedProducts.isEmpty()) {
            android.util.Log.d("CreateRestockActivityFragment", "No products selected")
            return
        }

        android.util.Log.d("CreateRestockActivityFragment", "Selected products: $selectedProducts")

        // Get product names and categories
        val productNames = mutableMapOf<Long, String>()
        val productCategories = mutableMapOf<Long, String>()
        
        productViewModel.products.value.forEach { product ->
            if (selectedProducts.any { it.first == product.id }) {
                productNames[product.id] = product.name
                productCategories[product.id] = product.category
            }
        }

        android.util.Log.d("CreateRestockActivityFragment", "Calling activityViewModel.createRestockActivity")
        activityViewModel.createRestockActivity(selectedProducts, productNames, productCategories)
        
        android.util.Log.d("CreateRestockActivityFragment", "Navigating back to activity list")
        // Navigate back to activity list
        findNavController().navigateUp()
    }
} 