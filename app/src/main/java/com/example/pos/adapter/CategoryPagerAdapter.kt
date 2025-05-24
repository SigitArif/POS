package com.example.pos.adapter

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.example.pos.data.local.AppDatabase
import com.example.pos.data.local.CategoryEntity
import com.example.pos.data.repository.CategoryRepositoryImpl
import com.example.pos.fragment.ProductListFragment
import com.example.pos.viewmodel.CategoryViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class CategoryPagerAdapter(
    private val activity: FragmentActivity,
    private val lifecycleOwner: LifecycleOwner
) : FragmentStateAdapter(activity) {
    
    private val _categories = MutableStateFlow<List<CategoryEntity>>(emptyList())
    val categories: StateFlow<List<CategoryEntity>> = _categories

    private val viewModel: CategoryViewModel by lazy {
        val database = AppDatabase.getDatabase(activity)
        val repository = CategoryRepositoryImpl(database.categoryDao())
        CategoryViewModel.Factory(repository).create(CategoryViewModel::class.java)
    }

    init {
        observeCategories()
    }

    private fun observeCategories() {
        lifecycleOwner.lifecycleScope.launch {
            lifecycleOwner.repeatOnLifecycle(androidx.lifecycle.Lifecycle.State.STARTED) {
                viewModel.categories.collect { categories ->
                    _categories.value = categories
                    notifyDataSetChanged()
                }
            }
        }
    }

    override fun getItemCount(): Int = categories.value.size

    override fun createFragment(position: Int): Fragment {
        val category = categories.value[position].name
        return ProductListFragment.newInstance(category)
    }
} 