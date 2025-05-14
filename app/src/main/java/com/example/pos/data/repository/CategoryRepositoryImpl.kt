package com.example.pos.data.repository

import com.example.pos.data.local.CategoryDao
import com.example.pos.data.local.CategoryEntity
import kotlinx.coroutines.flow.Flow

class CategoryRepositoryImpl(
    private val categoryDao: CategoryDao
) : CategoryRepository {

    override fun getAllCategories(): Flow<List<CategoryEntity>> {
        return categoryDao.getAllCategories()
    }

    override suspend fun addCategory(category: String) {
        categoryDao.insertCategory(CategoryEntity(category))
    }

    override suspend fun addCategories(categories: List<String>) {
        categoryDao.insertCategories(categories.map { CategoryEntity(it) })
    }

    override suspend fun deleteCategory(category: String) {
        categoryDao.deleteCategory(CategoryEntity(category))
    }

    override suspend fun deleteAllCategories() {
        categoryDao.deleteAllCategories()
    }
} 