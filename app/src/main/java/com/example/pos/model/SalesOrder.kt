package com.example.pos.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.Date

data class SalesOrder(
    val id: Long = 0,
    val dateTime: Date,
    val totalRevenue: Double,
    val totalProfit: Double
)

data class SalesOrderItem(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val salesOrderId: Long,
    val productId: Long,
    val quantity: Int,
    val price: Double,
    val profit: Double
) 