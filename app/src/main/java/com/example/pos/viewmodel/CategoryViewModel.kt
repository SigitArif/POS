package com.example.pos.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.pos.data.local.CategoryEntity
import com.example.pos.data.repository.CategoryRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.launch

class CategoryViewModel(private val repository: CategoryRepository) : ViewModel() {
    
    private val _categories = MutableStateFlow<List<CategoryEntity>>(emptyList())
    val categories: StateFlow<List<CategoryEntity>> = _categories

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error

    init {
        loadCategories()
    }

    private fun loadCategories() {
        viewModelScope.launch {
            repository.getAllCategories()
                .catch { e ->
                    _error.value = e.message
                }
                .collect { categories ->
                    _categories.value = categories
                }
        }
    }

    fun addCategory(category: String) {
        if (category.isBlank()) {
            _error.value = "Category name cannot be empty"
            return
        }

        viewModelScope.launch {
            try {
                repository.addCategory(category)
                _error.value = null
            } catch (e: Exception) {
                _error.value = e.message
            }
        }
    }

    fun clearError() {
        _error.value = null
    }

    class Factory(private val repository: CategoryRepository) : ViewModelProvider.Factory {
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(CategoryViewModel::class.java)) {
                @Suppress("UNCHECKED_CAST")
                return CategoryViewModel(repository) as T
            }
            throw IllegalArgumentException("Unknown ViewModel class")
        }
    }
} 