package com.example.pos.model

import java.util.Date

enum class ActivityType {
    RESTOCK
}

enum class ActivityStatus {
    CREATED,
    INPROGRESS,
    COMPLETE
}

data class Activity(
    val activityId: String,
    val activityType: ActivityType,
    val status: ActivityStatus,
    val createdAt: Date,
    val updatedAt: Date
)

data class ActivityItem(
    val id: String,
    val activityId: String,
    val productId: Long,
    val productName: String,
    val productCategory: String,
    val quantity: Int,
    val isFulfill: Boolean
) 