package com.example.pos.data.repository

import com.example.pos.data.local.ProductDao
import com.example.pos.data.local.ProductEntity
import com.example.pos.model.Product
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class ProductRepositoryImpl(
    private val productDao: ProductDao
) : ProductRepository {

    override fun getAllProducts(): Flow<List<Product>> {
        return productDao.getAllProducts().map { entities ->
            entities.map { it.toProduct() }
        }
    }

    override suspend fun getProductById(id: Long): Product? {
        return productDao.getProductById(id)?.toProduct()
    }

    override suspend fun insertProduct(product: Product): Long {
        return productDao.insertProduct(ProductEntity.fromProduct(product))
    }

    override suspend fun insertProducts(products: List<Product>) {
        productDao.insertProducts(products.map { ProductEntity.fromProduct(it) })
    }

    override suspend fun updateProduct(product: Product) {
        productDao.updateProduct(ProductEntity.fromProduct(product))
    }

    override suspend fun deleteProduct(product: Product) {
        productDao.deleteProduct(ProductEntity.fromProduct(product))
    }

    override suspend fun deleteAllProducts() {
        productDao.deleteAllProducts()
    }
} 