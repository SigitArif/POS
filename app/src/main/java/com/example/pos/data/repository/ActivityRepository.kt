package com.example.pos.data.repository

import com.example.pos.model.Activity
import com.example.pos.model.ActivityItem
import kotlinx.coroutines.flow.Flow

interface ActivityRepository {
    fun getAllActivities(): Flow<List<Activity>>
    fun getActivitiesByType(activityType: String): Flow<List<Activity>>
    suspend fun getActivityById(activityId: String): Activity?
    suspend fun insertActivity(activity: Activity)
    suspend fun updateActivity(activity: Activity)
    suspend fun deleteActivity(activity: Activity)
    
    fun getActivityItemsByActivityId(activityId: String): Flow<List<ActivityItem>>
    suspend fun getActivityItemById(id: String): ActivityItem?
    suspend fun insertActivityItem(activityItem: ActivityItem)
    suspend fun insertActivityItems(activityItems: List<ActivityItem>)
    suspend fun updateActivityItem(activityItem: ActivityItem)
    suspend fun updateActivityItems(activityItems: List<ActivityItem>)
    suspend fun deleteActivityItem(activityItem: ActivityItem)
    suspend fun deleteActivityItemsByActivityId(activityId: String)
} 