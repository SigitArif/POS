package com.example.pos.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.activity.OnBackPressedCallback
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
    private lateinit var adapter: ProductAdapter
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
        observeViewModel()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // Set up back press handling
        requireActivity().onBackPressedDispatcher.addCallback(
            this,
            object : OnBackPressedCallback(true) {
                override fun handleOnBackPressed() {
                    // Clear quantities when going back
                    productViewModel.clearQuantities()
                    // Pop back to sales order list
                    findNavController().popBackStack(R.id.navigation_sales_order, false)
                }
            }
        )
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
                
                // Navigate to sales order list using the action
                findNavController().navigate(R.id.action_product_selection_to_sales_order)
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
                }
            }
        }
        
        // Handle tab selection
        tabLayout.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
            override fun onTabSelected(tab: TabLayout.Tab?) {
                productViewModel.setCategory(tab?.text.toString() ?: "ALL")
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
                productViewModel.setSearchQuery(newText ?: "")
                return true
            }
        })
    }

    private fun setupRecyclerView() {
        adapter = ProductAdapter(
            onQuantityChange = { product, quantity ->
                productViewModel.updateQuantity(product, quantity)
            },
            viewModel = productViewModel
        )
        recyclerView.adapter = adapter
    }

    private fun setupFab() {
        fabSort.setOnClickListener {
            productViewModel.toggleSortOrder()
        }
    }

    private fun updateSortButtonIcon(isAscending: Boolean) {
        fabSort.setImageResource(
            if (isAscending) R.drawable.ic_sort_ascending
            else R.drawable.ic_sort_descending
        )
    }

    private fun observeViewModel() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                launch {
                    productViewModel.products.collectLatest { products ->
                        adapter.submitList(products)
                    }
                }
                launch {
                    productViewModel.selectedQuantities.collect { quantities ->
                        updateTotal()
                    }
                }
                launch {
                    productViewModel.isSortAscending().collectLatest { isAscending ->
                        updateSortButtonIcon(isAscending)
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

    override fun onDestroyView() {
        super.onDestroyView()
        // Clear quantities when leaving the fragment
        productViewModel.clearQuantities()
    }
} 