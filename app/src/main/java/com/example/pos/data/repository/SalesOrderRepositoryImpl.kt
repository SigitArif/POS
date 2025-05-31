package com.example.pos.data.repository

import com.example.pos.data.local.SalesOrderEntity
import com.example.pos.data.local.SalesOrderItemEntity
import com.example.pos.data.local.dao.SalesOrderDao
import com.example.pos.data.local.dao.SalesOrderItemDao
import com.example.pos.model.SalesOrder
import com.example.pos.model.SalesOrderItem
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class SalesOrderRepositoryImpl(
    private val salesOrderDao: SalesOrderDao,
    private val salesOrderItemDao: SalesOrderItemDao
) : SalesOrderRepository {

    override fun getAllSalesOrders(): Flow<List<SalesOrder>> = salesOrderDao.getAllSalesOrders().map { it.map { entity -> entity.toSalesOrder() } }

    override suspend fun getSalesOrderItems(salesOrderId: Long): Flow<List<SalesOrderItem>> =
        salesOrderItemDao.getItemsBySalesOrderId(salesOrderId).map { it.map { entity -> entity.toSalesOrderItem() } }

    override suspend fun createSalesOrder(salesOrder: SalesOrder): Long {
        val salesOrderEntity = SalesOrderEntity.fromSalesOrder(salesOrder)
        return salesOrderDao.insert(salesOrderEntity)
    }

    override suspend fun addSalesOrderItems(items: List<SalesOrderItem>) {
        val salesOrderItems = items.map { SalesOrderItemEntity.fromSalesOrderItem(it) }
        salesOrderItemDao.insertAll(salesOrderItems)
    }
}