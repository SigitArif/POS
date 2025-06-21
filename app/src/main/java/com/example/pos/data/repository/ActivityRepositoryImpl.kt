package com.example.pos.data.repository

import com.example.pos.data.local.AppDatabase
import com.example.pos.data.local.ActivityEntity
import com.example.pos.data.local.ActivityItemEntity
import com.example.pos.model.Activity
import com.example.pos.model.ActivityItem
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class ActivityRepositoryImpl(private val database: AppDatabase) : ActivityRepository {
    
    override fun getAllActivities(): Flow<List<Activity>> {
        return database.activityDao().getAllActivities().map { entities ->
            entities.map { it.toActivity() }
        }
    }
    
    override fun getActivitiesByType(activityType: String): Flow<List<Activity>> {
        return database.activityDao().getActivitiesByType(activityType).map { entities ->
            entities.map { it.toActivity() }
        }
    }
    
    override suspend fun getActivityById(activityId: String): Activity? {
        return database.activityDao().getActivityById(activityId)?.toActivity()
    }
    
    override suspend fun insertActivity(activity: Activity) {
        database.activityDao().insert(ActivityEntity.fromActivity(activity))
    }
    
    override suspend fun updateActivity(activity: Activity) {
        database.activityDao().update(ActivityEntity.fromActivity(activity))
    }
    
    override suspend fun deleteActivity(activity: Activity) {
        database.activityDao().delete(ActivityEntity.fromActivity(activity))
    }
    
    override fun getActivityItemsByActivityId(activityId: String): Flow<List<ActivityItem>> {
        return database.activityItemDao().getActivityItemsByActivityId(activityId).map { entities ->
            entities.map { it.toActivityItem() }
        }
    }
    
    override suspend fun getActivityItemById(id: String): ActivityItem? {
        return database.activityItemDao().getActivityItemById(id)?.toActivityItem()
    }
    
    override suspend fun insertActivityItem(activityItem: ActivityItem) {
        database.activityItemDao().insert(ActivityItemEntity.fromActivityItem(activityItem))
    }
    
    override suspend fun insertActivityItems(activityItems: List<ActivityItem>) {
        database.activityItemDao().insertAll(activityItems.map { ActivityItemEntity.fromActivityItem(it) })
    }
    
    override suspend fun updateActivityItem(activityItem: ActivityItem) {
        database.activityItemDao().update(ActivityItemEntity.fromActivityItem(activityItem))
    }
    
    override suspend fun updateActivityItems(activityItems: List<ActivityItem>) {
        database.activityItemDao().updateAll(activityItems.map { ActivityItemEntity.fromActivityItem(it) })
    }
    
    override suspend fun deleteActivityItem(activityItem: ActivityItem) {
        database.activityItemDao().delete(ActivityItemEntity.fromActivityItem(activityItem))
    }
    
    override suspend fun deleteActivityItemsByActivityId(activityId: String) {
        database.activityItemDao().deleteByActivityId(activityId)
    }
} 