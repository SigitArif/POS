package com.example.pos.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
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
import com.example.pos.viewmodel.ProductViewModel
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

class ProductListFragment : Fragment() {
    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: ProductAdapter
    private var category: String? = null
    private var searchQuery: String = ""

    private val viewModel: ProductViewModel by viewModels {
        val database = AppDatabase.getDatabase(requireContext())
        val productRepository = ProductRepositoryImpl(database.productDao())
        val categoryRepository = CategoryRepositoryImpl(database.categoryDao())
        ProductViewModel.Factory(productRepository, categoryRepository)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            category = it.getString(ARG_CATEGORY)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_product_list, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        recyclerView = view.findViewById(R.id.rvProducts)
        recyclerView.layoutManager = LinearLayoutManager(context)
        
        observeProducts()
    }

    private fun observeProducts() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.products.collectLatest { products ->
                    updateProductList(products)
                }
            }
        }
    }

    fun updateSearchQuery(query: String) {
        searchQuery = query
        viewModel.products.value?.let { updateProductList(it) }
    }

    private fun updateProductList(products: List<com.example.pos.model.Product>) {
        val filteredProducts = products.filter { product ->
            val matchesCategory = category == "ALL" || product.category == category
            val matchesSearch = searchQuery.isEmpty() || product.name.contains(searchQuery, ignoreCase = true)
            matchesCategory && matchesSearch
        }
        adapter = ProductAdapter(
            products = filteredProducts,
            onEditClick = { product ->
                showEditProductDialog(product)
            },
            onDeleteClick = { product ->
                viewModel.deleteProduct(product)
            }
        )
        recyclerView.adapter = adapter
    }

    private fun showEditProductDialog(product: com.example.pos.model.Product) {
        val editProductFragment = EditProductFragment.newInstance(product)
        editProductFragment.show(parentFragmentManager, "EditProductFragment")
    }

    companion object {
        private const val ARG_CATEGORY = "category"

        fun newInstance(category: String) = ProductListFragment().apply {
            arguments = Bundle().apply {
                putString(ARG_CATEGORY, category)
            }
        }
    }
} 