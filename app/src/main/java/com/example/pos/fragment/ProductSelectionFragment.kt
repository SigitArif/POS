package com.example.pos.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.appcompat.widget.SearchView
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
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
import com.google.android.material.button.MaterialButton
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.android.material.tabs.TabLayout
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import java.text.NumberFormat
import java.util.Locale

class ProductSelectionFragment : Fragment() {
    private lateinit var searchView: SearchView
    private lateinit var tabLayout: TabLayout
    private lateinit var recyclerView: RecyclerView
    private lateinit var fabSort: FloatingActionButton
    private lateinit var tvTotal: TextView
    private lateinit var btnCreateOrder: MaterialButton
    private var adapter: ProductAdapter? = null
    private var isAscending = true
    private var category: String = "ALL"
    private var searchQuery: String = ""
    private val numberFormat = NumberFormat.getCurrencyInstance(Locale("id", "ID"))

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

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_product_selection, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupViews(view)
        setupTabLayout()
        setupSearchView()
        setupRecyclerView()
        setupFab()
        observeProducts()
    }

    private fun setupViews(view: View) {
        searchView = view.findViewById(R.id.searchView)
        tabLayout = view.findViewById(R.id.tabLayout)
        recyclerView = view.findViewById(R.id.rvProducts)
        fabSort = view.findViewById(R.id.fabSort)
        tvTotal = view.findViewById(R.id.tvTotal)
        btnCreateOrder = view.findViewById(R.id.btnCreateOrder)

        recyclerView.layoutManager = LinearLayoutManager(context)

        btnCreateOrder.setOnClickListener {
            val selectedProducts = productViewModel.selectedQuantities.value
            if (selectedProducts.isNotEmpty()) {
                // Create sales order
                salesOrderViewModel.createSalesOrder(selectedProducts.toList())
                
                // Clear product quantities
                productViewModel.clearQuantities()
                
                // Navigate to sales order list
                findNavController().navigate(R.id.navigation_sales_order)
            }
        }
    }

    private fun setupTabLayout() {
        // Add "ALL" tab first
        tabLayout.addTab(tabLayout.newTab().setText("ALL"))
        
        // Observe categories and add them as tabs
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                productViewModel.categories.collect { categories ->
                    // Remove all tabs except "ALL"
                    while (tabLayout.tabCount > 1) {
                        tabLayout.removeTabAt(1)
                    }
                    
                    // Add category tabs
                    categories.forEach { categoryName ->
                        tabLayout.addTab(tabLayout.newTab().setText(categoryName))
                    }
                    
                    // Select the current category tab if it exists
                    for (i in 0 until tabLayout.tabCount) {
                        if (tabLayout.getTabAt(i)?.text.toString() == category) {
                            tabLayout.selectTab(tabLayout.getTabAt(i))
                            break
                        }
                    }
                }
            }
        }
        
        // Handle tab selection
        tabLayout.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
            override fun onTabSelected(tab: TabLayout.Tab?) {
                category = tab?.text.toString()
                productViewModel.products.value?.let { products ->
                    updateProductList(products)
                }
            }
            
            override fun onTabUnselected(tab: TabLayout.Tab?) {}
            override fun onTabReselected(tab: TabLayout.Tab?) {}
        })
    }

    private fun setupSearchView() {
        searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                return false
            }

            override fun onQueryTextChange(newText: String?): Boolean {
                searchQuery = newText ?: ""
                productViewModel.products.value?.let { products ->
                    updateProductList(products)
                }
                return true
            }
        })
    }

    private fun setupRecyclerView() {
        adapter = ProductAdapter(
            products = emptyList(),
            onQuantityChange = { product, quantity ->
                productViewModel.updateQuantity(product, quantity)
                updateTotal()
            },
            viewModel = productViewModel
        )
        recyclerView.adapter = adapter
    }

    private fun setupFab() {
        fabSort.setOnClickListener {
            isAscending = !isAscending
            updateSortButtonIcon()
            productViewModel.products.value?.let { products ->
                updateProductList(products)
            }
        }
        updateSortButtonIcon()
    }

    private fun updateSortButtonIcon() {
        fabSort.setImageResource(
            if (isAscending) R.drawable.ic_sort_ascending
            else R.drawable.ic_sort_descending
        )
    }

    private fun observeProducts() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                productViewModel.products.collectLatest { products ->
                    updateProductList(products)
                }
            }
        }

        // Observe selected quantities
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                productViewModel.selectedQuantities.collect { quantities ->
                    updateTotal()
                }
            }
        }
    }

    private fun updateProductList(products: List<Product>) {
        val filteredProducts = products.filter { product ->
            val matchesCategory = category == "ALL" || product.category.equals(category, ignoreCase = true)
            val matchesSearch = searchQuery.isEmpty() || product.name.contains(searchQuery, ignoreCase = true)
            matchesCategory && matchesSearch
        }.sortedWith(
            if (isAscending) {
                compareBy { it.name }
            } else {
                compareByDescending { it.name }
            }
        )

        adapter?.updateProducts(filteredProducts)
    }

    private fun updateTotal() {
        val total = productViewModel.selectedQuantities.value.entries.sumOf { (product, quantity) ->
            product.price * quantity
        }
        tvTotal.text = "Total: ${numberFormat.format(total)}"
    }

    override fun onDestroyView() {
        super.onDestroyView()
        // Clear quantities when leaving the fragment
        productViewModel.clearQuantities()
    }
} 