package com.example.pos.data.local

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey
import com.example.pos.model.ActivityItem

@Entity(
    tableName = "activity_items",
    foreignKeys = [
        ForeignKey(
            entity = ActivityEntity::class,
            parentColumns = ["activityId"],
            childColumns = ["activityId"],
            onDelete = ForeignKey.CASCADE
        )
    ]
)
data class ActivityItemEntity(
    @PrimaryKey
    val id: String,
    val activityId: String,
    val productId: Long,
    val productName: String,
    val productCategory: String,
    val quantity: Int,
    val isFulfill: Boolean
) {
    fun toActivityItem() = ActivityItem(
        id = id,
        activityId = activityId,
        productId = productId,
        productName = productName,
        productCategory = productCategory,
        quantity = quantity,
        isFulfill = isFulfill
    )

    companion object {
        fun fromActivityItem(item: ActivityItem) = ActivityItemEntity(
            id = item.id,
            activityId = item.activityId,
            productId = item.productId,
            productName = item.productName,
            productCategory = item.productCategory,
            quantity = item.quantity,
            isFulfill = item.isFulfill
        )
    }
} 