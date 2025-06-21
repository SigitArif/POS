package com.example.pos.data.local.dao

import androidx.room.*
import com.example.pos.data.local.ActivityEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface ActivityDao {
    @Query("SELECT * FROM activities ORDER BY createdAt DESC")
    fun getAllActivities(): Flow<List<ActivityEntity>>

    @Query("SELECT * FROM activities WHERE activityType = :activityType ORDER BY createdAt DESC")
    fun getActivitiesByType(activityType: String): Flow<List<ActivityEntity>>

    @Query("SELECT * FROM activities WHERE activityId = :activityId")
    suspend fun getActivityById(activityId: String): ActivityEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(activity: ActivityEntity)

    @Update
    suspend fun update(activity: ActivityEntity)

    @Delete
    suspend fun delete(activity: ActivityEntity)

    @Query("DELETE FROM activities WHERE activityId = :activityId")
    suspend fun deleteById(activityId: String)
} 