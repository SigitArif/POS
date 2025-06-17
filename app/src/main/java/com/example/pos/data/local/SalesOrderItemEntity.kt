package com.example.pos.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.example.pos.model.SalesOrderItem

@Entity(tableName = "sales_order_items")
data class SalesOrderItemEntity(
    @PrimaryKey
    val id: String,
    val salesOrderId: String,
    val productId: Long,
    val quantity: Int,
    val price: Double,
    val profit: Double,
    val productCode: String?,
    val productName: String,
    val productCategory: String
) {
    fun toSalesOrderItem() = SalesOrderItem(
        id = id,
        salesOrderId = salesOrderId,
        productId = productId,
        quantity = quantity,
        price = price,
        profit = profit,
        productCode = productCode,
        productName = productName,
        productCategory = productCategory
    )

    companion object {
        fun fromSalesOrderItem(item: SalesOrderItem) = SalesOrderItemEntity(
            id = item.id,
            salesOrderId = item.salesOrderId,
            productId = item.productId,
            quantity = item.quantity,
            price = item.price,
            profit = item.profit,
            productCode = item.productCode,
            productName = item.productName.ifEmpty { "" },
            productCategory = item.productCategory.ifEmpty { "" }
        )
    }
}