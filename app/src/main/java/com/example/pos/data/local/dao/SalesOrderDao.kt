package com.example.pos.data.local.dao

import androidx.room.*
import com.example.pos.data.local.SalesOrderEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface SalesOrderDao {
    @Query("SELECT * FROM sales_orders ORDER BY dateTime DESC")
    fun getAllSalesOrders(): Flow<List<SalesOrderEntity>>

    @Query("SELECT * FROM sales_orders WHERE id = :id")
    suspend fun getSalesOrderById(id: Long): SalesOrderEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(salesOrder: SalesOrderEntity): Long

    @Update
    suspend fun update(salesOrder: SalesOrderEntity)

    @Delete
    suspend fun delete(salesOrder: SalesOrderEntity)

    @Query("DELETE FROM sales_orders WHERE id = :id")
    suspend fun deleteById(id: Long)
} 