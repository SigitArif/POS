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
    private lateinit var adapter: ProductAdapter
    private var isAscending = true

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
            viewModel.setCategory(it.getString(ARG_CATEGORY) ?: "ALL")
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
        observeViewModel()
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
            adapter = ProductAdapter(
                onEditClick = { product ->
                    showEditProductDialog(product)
                },
                onDeleteClick = { product ->
                    viewModel.deleteProduct(product)
                }
            )
            recyclerView.adapter = adapter
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
                        // Logic to select the correct tab is handled by observing the ViewModel state
                    }
                }
            }

            tabLayout.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
                override fun onTabSelected(tab: TabLayout.Tab?) {
                    viewModel.setCategory(tab?.text.toString() ?: "ALL")
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
                    viewModel.setSearchQuery(newText ?: "")
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
                viewModel.toggleSortOrder()
            }
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

    private fun observeViewModel() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                launch {
                    viewModel.products.collectLatest { products ->
                        adapter.submitList(products)
                    }
                }
                launch {
                    viewModel.isSortAscending().collectLatest { isAscending ->
                        this@ProductListFragment.isAscending = isAscending
                        updateSortButtonIcon()
                    }
                }
            }
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