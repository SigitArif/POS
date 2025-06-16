package com.example.pos.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.view.isVisible
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.example.pos.R
import com.example.pos.data.local.AppDatabase
import com.example.pos.data.repository.CategoryRepositoryImpl
import com.example.pos.data.repository.ProductRepositoryImpl
import com.example.pos.databinding.FragmentAddProductBinding
import com.example.pos.viewmodel.CategoryViewModel
import com.example.pos.viewmodel.ProductViewModel
import kotlinx.coroutines.launch

class AddProductFragment : DialogFragment() {
    private var _binding: FragmentAddProductBinding? = null
    private val binding get() = _binding!!

    private val productViewModel: ProductViewModel by viewModels {
        val database = AppDatabase.getDatabase(requireContext())
        val productRepository = ProductRepositoryImpl(database.productDao())
        val categoryRepository = CategoryRepositoryImpl(database.categoryDao())
        ProductViewModel.Factory(productRepository, categoryRepository)
    }

    private val categoryViewModel: CategoryViewModel by viewModels {
        val database = AppDatabase.getDatabase(requireContext())
        val repository = CategoryRepositoryImpl(database.categoryDao())
        CategoryViewModel.Factory(repository)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setStyle(STYLE_NORMAL, R.style.Theme_MaterialComponents_Dialog)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentAddProductBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupUI()
        observeViewModels()
    }

    private fun setupUI() {
        // Setup category dropdown
        setupCategoryDropdown()
        
        binding.addButton.setOnClickListener {
            val name = binding.nameEditText.text.toString()
            val priceStr = binding.priceEditText.text.toString()
            val basePriceStr = binding.basePriceEditText.text.toString()
            val productCode = binding.productCodeEditText.text.toString().takeIf { it.isNotBlank() }
            val category = binding.categoryAutoComplete.text.toString()

            if (name.isBlank()) {
                Toast.makeText(context, "Please enter a product name", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            if (category.isBlank()) {
                Toast.makeText(context, "Please enter a category", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            try {
                val price = priceStr.toDoubleOrNull()
                if (price == null) {
                    Toast.makeText(context, "Please enter a valid selling price", Toast.LENGTH_SHORT).show()
                    return@setOnClickListener
                }

                val basePrice = basePriceStr.toDoubleOrNull() ?: price
                
                // If it's a new category, add it to the database
                if (!categoryViewModel.categories.value.map { it.name }.contains(category)) {
                    categoryViewModel.addCategory(category)
                }
                
                productViewModel.addProduct(name, price, basePrice, productCode, category)
                dismiss()
            } catch (e: NumberFormatException) {
                Toast.makeText(context, "Please enter valid prices", Toast.LENGTH_SHORT).show()
            }
        }
    }
    
    private fun setupCategoryDropdown() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                categoryViewModel.categories.collect { categories ->
                    // Filter out the "ALL" category as it's not a real category
                    val categoryNames = categories
                        .filter { it.name != "ALL" }
                        .map { it.name }
                    
                    val adapter = android.widget.ArrayAdapter(
                        requireContext(),
                        android.R.layout.simple_dropdown_item_1line,
                        categoryNames
                    )
                    
                    binding.categoryAutoComplete.setAdapter(adapter)
                }
            }
        }
        
        // Allow user to enter custom text for new categories
        binding.categoryAutoComplete.setOnItemClickListener { _, _, _, _ ->
            // Item selected, nothing special to do
        }
    }

    private fun observeViewModels() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                productViewModel.error.collect { error ->
                    binding.errorTextView.apply {
                        text = error
                        isVisible = error != null
                    }
                }
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}