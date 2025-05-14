package com.example.pos.data.repository

import com.example.pos.model.Product
import kotlinx.coroutines.flow.Flow

interface ProductRepository {
    fun getAllProducts(): Flow<List<Product>>
    suspend fun getProductById(id: Long): Product?
    suspend fun insertProduct(product: Product): Long
    suspend fun insertProducts(products: List<Product>)
    suspend fun updateProduct(product: Product)
    suspend fun deleteProduct(product: Product)
    suspend fun deleteAllProducts()
} 