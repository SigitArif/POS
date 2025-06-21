package com.example.pos.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.example.pos.model.Activity
import com.example.pos.model.ActivityStatus
import com.example.pos.model.ActivityType
import java.util.Date

@Entity(tableName = "activities")
data class ActivityEntity(
    @PrimaryKey
    val activityId: String,
    val activityType: String,
    val status: String,
    val createdAt: Date,
    val updatedAt: Date
) {
    fun toActivity() = Activity(
        activityId = activityId,
        activityType = ActivityType.valueOf(activityType),
        status = ActivityStatus.valueOf(status),
        createdAt = createdAt,
        updatedAt = updatedAt
    )

    companion object {
        fun fromActivity(activity: Activity) = ActivityEntity(
            activityId = activity.activityId,
            activityType = activity.activityType.name,
            status = activity.status.name,
            createdAt = activity.createdAt,
            updatedAt = activity.updatedAt
        )
    }
} 