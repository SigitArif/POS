package com.example.pos.data.repository

import com.example.pos.model.SalesOrder
import com.example.pos.model.SalesOrderItem
import kotlinx.coroutines.flow.Flow

interface SalesOrderRepository {
    fun getAllSalesOrders(): Flow<List<SalesOrder>>
    suspend fun getSalesOrderItems(salesOrderId: Long): Flow<List<SalesOrderItem>>
    suspend fun createSalesOrder(salesOrder: SalesOrder): Long
    suspend fun addSalesOrderItems(items: List<SalesOrderItem>)
} 