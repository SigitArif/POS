package com.example.pos.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.example.pos.model.SalesOrderItem

@Entity(tableName = "sales_order_items")
data class SalesOrderItemEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val salesOrderId: Long,
    val productId: Long,
    val quantity: Int,
    val price: Double,
    val profit: Double
) {
    fun toSalesOrderItem() = SalesOrderItem(
        id = id,
        salesOrderId = salesOrderId,
        productId = productId,
        quantity = quantity,
        price = price,
        profit = profit
    )

    companion object {
        fun fromSalesOrderItem(item: SalesOrderItem) = SalesOrderItemEntity(
            id = item.id,
            salesOrderId = item.salesOrderId,
            productId = item.productId,
            quantity = item.quantity,
            price = item.price,
            profit = item.profit
        )
    }
} 