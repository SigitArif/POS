package com.example.pos.fragment

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.widget.SearchView
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
import com.example.pos.model.Product
import com.example.pos.viewmodel.ProductViewModel
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.android.material.tabs.TabLayout
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

class ProductListFragment : Fragment() {
    private lateinit var tabLayout: TabLayout
    private lateinit var searchView: SearchView
    private lateinit var fabAddProduct: FloatingActionButton
    private lateinit var fabSort: FloatingActionButton
    private lateinit var recyclerView: RecyclerView
    private var adapter: ProductAdapter? = null
    private var isAscending = true
    private var category: String = "ALL"
    private var searchQuery: String = ""

    private val viewModel: ProductViewModel by viewModels {
        val database = AppDatabase.getDatabase(requireContext())
        val productRepository = ProductRepositoryImpl(database.productDao())
        val categoryRepository = CategoryRepositoryImpl(database.categoryDao())
        ProductViewModel.Factory(productRepository, categoryRepository)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Log.d("ProductListFragment", "onCreate called")
        arguments?.let {
            category = it.getString(ARG_CATEGORY) ?: "ALL"
            Log.d("ProductListFragment", "Category set to: $category")
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        Log.d("ProductListFragment", "onCreateView called")
        return inflater.inflate(R.layout.fragment_product_list, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        Log.d("ProductListFragment", "onViewCreated called")
        setupViews(view)
        setupTabLayout()
        setupSearchView()
        setupFab()
        observeProducts()
    }

    private fun setupViews(view: View) {
        try {
            Log.d("ProductListFragment", "Setting up views")
            tabLayout = view.findViewById(R.id.tabLayout)
            searchView = view.findViewById(R.id.searchView)
            fabAddProduct = view.findViewById(R.id.fabAddProduct)
            fabSort = view.findViewById(R.id.fabSort)
            recyclerView = view.findViewById(R.id.rvProducts)
            recyclerView.layoutManager = LinearLayoutManager(context)
            Log.d("ProductListFragment", "Views setup completed")
        } catch (e: Exception) {
            Log.e("ProductListFragment", "Error setting up views", e)
            throw e
        }
    }

    private fun setupTabLayout() {
        try {
            Log.d("ProductListFragment", "Setting up tab layout")
            // Add "ALL" tab first
            tabLayout.addTab(tabLayout.newTab().setText("ALL"))
            
            // Observe categories and add them as tabs
            viewLifecycleOwner.lifecycleScope.launch {
                viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                    viewModel.categories.collect { categories ->
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
                    viewModel.products.value?.let { products ->
                        updateProductList(products)
                    }
                }
                
                override fun onTabUnselected(tab: TabLayout.Tab?) {}
                override fun onTabReselected(tab: TabLayout.Tab?) {}
            })
            
            Log.d("ProductListFragment", "Tab layout setup completed")
        } catch (e: Exception) {
            Log.e("ProductListFragment", "Error setting up tab layout", e)
            throw e
        }
    }

    private fun setupSearchView() {
        try {
            Log.d("ProductListFragment", "Setting up search view")
            searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
                override fun onQueryTextSubmit(query: String?): Boolean {
                    return false
                }

                override fun onQueryTextChange(newText: String?): Boolean {
                    searchQuery = newText ?: ""
                    viewModel.products.value?.let { products ->
                        updateProductList(products)
                    }
                    return true
                }
            })
            Log.d("ProductListFragment", "Search view setup completed")
        } catch (e: Exception) {
            Log.e("ProductListFragment", "Error setting up search view", e)
            throw e
        }
    }

    private fun setupFab() {
        try {
            Log.d("ProductListFragment", "Setting up FABs")
            fabAddProduct.setOnClickListener {
                showAddProductFragment()
            }

            fabSort.setOnClickListener {
                isAscending = !isAscending
                updateSortButtonIcon()
                viewModel.products.value?.let { products ->
                    updateProductList(products)
                }
            }
            updateSortButtonIcon()
            Log.d("ProductListFragment", "FABs setup completed")
        } catch (e: Exception) {
            Log.e("ProductListFragment", "Error setting up FABs", e)
            throw e
        }
    }

    private fun updateSortButtonIcon() {
        fabSort.setImageResource(
            if (isAscending) R.drawable.ic_sort_ascending
            else R.drawable.ic_sort_descending
        )
    }

    private fun showAddProductFragment() {
        AddProductFragment().show(parentFragmentManager, "AddProductFragment")
    }

    private fun observeProducts() {
        try {
            Log.d("ProductListFragment", "Starting to observe products")
            viewLifecycleOwner.lifecycleScope.launch {
                viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                    viewModel.products.collectLatest { products ->
                        Log.d("ProductListFragment", "Received ${products.size} products")
                        updateProductList(products)
                    }
                }
            }
            Log.d("ProductListFragment", "Product observation setup completed")
        } catch (e: Exception) {
            Log.e("ProductListFragment", "Error observing products", e)
            throw e
        }
    }

    private fun updateProductList(products: List<Product>) {
        try {
            Log.d("ProductListFragment", "Updating product list with ${products.size} products")
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
            Log.d("ProductListFragment", "Filtered to ${filteredProducts.size} products")

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
            Log.d("ProductListFragment", "Product list update completed")
        } catch (e: Exception) {
            Log.e("ProductListFragment", "Error updating product list", e)
            throw e
        }
    }

    private fun showEditProductDialog(product: Product) {
        try {
            Log.d("ProductListFragment", "Showing edit dialog for product: ${product.name}")
            val editProductFragment = EditProductFragment.newInstance(product)
            editProductFragment.show(parentFragmentManager, "EditProductFragment")
        } catch (e: Exception) {
            Log.e("ProductListFragment", "Error showing edit dialog", e)
            throw e
        }
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