package com.example.pos.model

import java.util.Date

data class SalesOrder(
    val id: String,
    val dateTime: Date,
    val totalRevenue: Double,
    val totalProfit: Double
)

data class SalesOrderItem(
    val id: String,
    val salesOrderId: String,
    val productId: Long,
    val quantity: Int,
    val price: Double,
    val profit: Double
) 