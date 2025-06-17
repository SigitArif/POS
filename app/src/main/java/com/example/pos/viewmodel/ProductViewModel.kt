package com.example.pos.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.pos.data.repository.CategoryRepository
import com.example.pos.data.repository.ProductRepository
import com.example.pos.model.Product
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.launch

class ProductViewModel(
    private val productRepository: ProductRepository,
    private val categoryRepository: CategoryRepository
) : ViewModel() {
    
    private val _products = MutableStateFlow<List<Product>>(emptyList())
    val products: StateFlow<List<Product>> = _products.asStateFlow()

    private val _categories = MutableStateFlow<List<String>>(emptyList())
    val categories: StateFlow<List<String>> = _categories.asStateFlow()

    private val _selectedQuantities = MutableStateFlow<Map<Product, Int>>(emptyMap())
    val selectedQuantities: StateFlow<Map<Product, Int>> = _selectedQuantities.asStateFlow()

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error

    init {
        loadProducts()
        loadCategories()
    }

    private fun loadProducts() {
        viewModelScope.launch {
            try {
                productRepository.getAllProducts()
                    .catch { e ->
                        _error.value = "Failed to load products: ${e.message}"
                        android.util.Log.e("ProductViewModel", "Error loading products", e)
                    }
                    .collect { products ->
                        _products.value = products
                        android.util.Log.d("ProductViewModel", "Loaded ${products.size} products")
                    }
            } catch (e: Exception) {
                _error.value = "Exception loading products: ${e.message}"
                android.util.Log.e("ProductViewModel", "Exception in loadProducts", e)
            }
        }
    }

    private fun loadCategories() {
        viewModelScope.launch {
            categoryRepository.getAllCategories()
                .catch { e ->
                    _error.value = e.message
                }
                .collect { categories ->
                    _categories.value = categories.map { it.name }
                }
        }
    }

    fun addProduct(name: String, price: Double, basePrice: Double = price, productCode: String? = null, category: String) {
        if (name.isBlank()) {
            _error.value = "Product name cannot be empty"
            return
        }
        if (price <= 0) {
            _error.value = "Price must be greater than 0"
            return
        }
        if (basePrice <= 0) {
            _error.value = "Base price must be greater than 0"
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
                val product = Product(
                    name = name,
                    price = price,
                    basePrice = basePrice,
                    productCode = productCode,
                    category = normalizedCategory
                )
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
        if (product.basePrice <= 0) {
            _error.value = "Base price must be greater than 0"
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

    fun updateQuantity(product: Product, quantity: Int) {
        val currentQuantities = _selectedQuantities.value.toMutableMap()
        if (quantity > 0) {
            currentQuantities[product] = quantity
        } else {
            currentQuantities.remove(product)
        }
        _selectedQuantities.value = currentQuantities
    }

    fun getQuantity(product: Product): Int {
        return _selectedQuantities.value[product] ?: 0
    }

    fun clearQuantities() {
        _selectedQuantities.value = emptyMap()
    }
    
    fun refreshProducts() {
        android.util.Log.d("ProductViewModel", "Manually refreshing products")
        loadProducts()
    }

    class Factory(
        private val productRepository: ProductRepository,
        private val categoryRepository: CategoryRepository
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(ProductViewModel::class.java)) {
                return ProductViewModel(productRepository, categoryRepository) as T
            }
            throw IllegalArgumentException("Unknown ViewModel class")
        }
    }
}