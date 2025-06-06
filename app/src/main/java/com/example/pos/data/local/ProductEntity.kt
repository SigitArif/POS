package com.example.pos.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.example.pos.model.Product

@Entity(tableName = "products")
data class ProductEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val name: String,
    val price: Double,
    val category: String
) {
    fun toProduct() = Product(
        id = id,
        name = name,
        price = price,
        category = category
    )

    companion object {
        fun fromProduct(product: Product) = ProductEntity(
            id = product.id,
            name = product.name,
            price = product.price,
            category = product.category
        )
    }
} 