package com.example.pos.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.example.pos.model.SalesOrder
import java.util.Date

@Entity(tableName = "sales_orders")
data class SalesOrderEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val dateTime: Date,
    val totalRevenue: Double,
    val totalProfit: Double
) {
    fun toSalesOrder() = SalesOrder(
        id = id,
        dateTime = dateTime,
        totalRevenue = totalRevenue,
        totalProfit = totalProfit
    )

    companion object {
        fun fromSalesOrder(salesOrder: SalesOrder) = SalesOrderEntity(
            id = salesOrder.id,
            dateTime = salesOrder.dateTime,
            totalRevenue = salesOrder.totalRevenue,
            totalProfit = salesOrder.totalProfit
        )
    }
} 