package com.example.pos.data.repository

import com.example.pos.model.SalesOrder
import com.example.pos.model.SalesOrderItem
import kotlinx.coroutines.flow.Flow

interface SalesOrderRepository {
    fun getAllSalesOrders(): Flow<List<SalesOrder>>
    suspend fun getSalesOrderItems(salesOrderId: String): Flow<List<SalesOrderItem>>
    suspend fun createSalesOrder(salesOrder: SalesOrder): String
    suspend fun addSalesOrderItems(items: List<SalesOrderItem>)
} 