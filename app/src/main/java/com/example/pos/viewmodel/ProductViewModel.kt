package com.example.pos.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.pos.data.repository.CategoryRepository
import com.example.pos.data.repository.ProductRepository
import com.example.pos.model.Product
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.launch

class ProductViewModel(
    private val productRepository: ProductRepository,
    private val categoryRepository: CategoryRepository
) : ViewModel() {
    
    private val _products = MutableStateFlow<List<Product>>(emptyList())
    val products: StateFlow<List<Product>> = _products

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error

    init {
        loadProducts()
    }

    private fun loadProducts() {
        viewModelScope.launch {
            productRepository.getAllProducts()
                .catch { e ->
                    _error.value = e.message
                }
                .collect { products ->
                    _products.value = products
                }
        }
    }

    fun addProduct(name: String, price: Double, category: String) {
        if (name.isBlank()) {
            _error.value = "Product name cannot be empty"
            return
        }
        if (price <= 0) {
            _error.value = "Price must be greater than 0"
            return
        }
        if (category.isBlank()) {
            _error.value = "Category cannot be empty"
            return
        }

        viewModelScope.launch {
            try {
                // First, ensure the category exists
                categoryRepository.addCategory(category)
                
                // Then add the product with normalized category
                val normalizedCategory = category.trim().lowercase()
                val product = Product(name = name, price = price, category = normalizedCategory)
                productRepository.insertProduct(product)
                _error.value = null
            } catch (e: Exception) {
                _error.value = e.message
            }
        }
    }

    fun updateProduct(product: Product) {
        if (product.name.isBlank()) {
            _error.value = "Product name cannot be empty"
            return
        }
        if (product.price <= 0) {
            _error.value = "Price must be greater than 0"
            return
        }
        if (product.category.isBlank()) {
            _error.value = "Category cannot be empty"
            return
        }

        viewModelScope.launch {
            try {
                // First, ensure the category exists
                categoryRepository.addCategory(product.category)
                
                // Then update the product with normalized category
                val normalizedCategory = product.category.trim().lowercase()
                val updatedProduct = product.copy(category = normalizedCategory)
                productRepository.updateProduct(updatedProduct)
                _error.value = null
            } catch (e: Exception) {
                _error.value = e.message
            }
        }
    }

    fun deleteProduct(product: Product) {
        viewModelScope.launch {
            try {
                productRepository.deleteProduct(product)
                _error.value = null
            } catch (e: Exception) {
                _error.value = e.message
            }
        }
    }

    fun clearError() {
        _error.value = null
    }

    class Factory(
        private val productRepository: ProductRepository,
        private val categoryRepository: CategoryRepository
    ) : ViewModelProvider.Factory {
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(ProductViewModel::class.java)) {
                @Suppress("UNCHECKED_CAST")
                return ProductViewModel(productRepository, categoryRepository) as T
            }
            throw IllegalArgumentException("Unknown ViewModel class")
        }
    }
} 