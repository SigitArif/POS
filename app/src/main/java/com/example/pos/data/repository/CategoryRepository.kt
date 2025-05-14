package com.example.pos.data.repository

import com.example.pos.data.local.CategoryEntity
import kotlinx.coroutines.flow.Flow

interface CategoryRepository {
    fun getAllCategories(): Flow<List<CategoryEntity>>
    suspend fun addCategory(category: String)
    suspend fun addCategories(categories: List<String>)
    suspend fun deleteCategory(category: String)
    suspend fun deleteAllCategories()
} 