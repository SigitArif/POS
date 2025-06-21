package com.example.pos.data.local.dao

import androidx.room.*
import com.example.pos.data.local.ActivityItemEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface ActivityItemDao {
    @Query("SELECT * FROM activity_items WHERE activityId = :activityId")
    fun getActivityItemsByActivityId(activityId: String): Flow<List<ActivityItemEntity>>

    @Query("SELECT * FROM activity_items WHERE id = :id")
    suspend fun getActivityItemById(id: String): ActivityItemEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(activityItem: ActivityItemEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(activityItems: List<ActivityItemEntity>)

    @Update
    suspend fun update(activityItem: ActivityItemEntity)

    @Update
    suspend fun updateAll(activityItems: List<ActivityItemEntity>)

    @Delete
    suspend fun delete(activityItem: ActivityItemEntity)

    @Query("DELETE FROM activity_items WHERE activityId = :activityId")
    suspend fun deleteByActivityId(activityId: String)

    @Query("DELETE FROM activity_items WHERE id = :id")
    suspend fun deleteById(id: String)
} 