package com.example.pos.data.local.dao

import androidx.room.*
import com.example.pos.data.local.SalesOrderItemEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface SalesOrderItemDao {
    @Query("SELECT * FROM sales_order_items WHERE salesOrderId = :salesOrderId")
    fun getItemsBySalesOrderId(salesOrderId: String): Flow<List<SalesOrderItemEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(item: SalesOrderItemEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(items: List<SalesOrderItemEntity>)

    @Update
    suspend fun update(item: SalesOrderItemEntity)

    @Delete
    suspend fun delete(item: SalesOrderItemEntity)

    @Query("DELETE FROM sales_order_items WHERE salesOrderId = :salesOrderId")
    suspend fun deleteBySalesOrderId(salesOrderId: String)
} 