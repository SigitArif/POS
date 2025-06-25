package com.example.pos.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.pos.data.repository.ActivityRepository
import com.example.pos.data.repository.ActivityRepositoryImpl
import com.example.pos.model.Activity
import com.example.pos.model.ActivityItem
import com.example.pos.model.ActivityStatus
import com.example.pos.model.ActivityType
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import java.util.Date
import java.util.UUID

class ActivityViewModel(private val activityRepository: ActivityRepository) : ViewModel() {
    
    private val _activities = MutableStateFlow<List<Activity>>(emptyList())
    val activities: StateFlow<List<Activity>> = _activities.asStateFlow()
    
    private val _activityItems = MutableStateFlow<List<ActivityItem>>(emptyList())
    val activityItems: StateFlow<List<ActivityItem>> = _activityItems.asStateFlow()
    
    private val _selectedActivity = MutableStateFlow<Activity?>(null)
    val selectedActivity: StateFlow<Activity?> = _selectedActivity.asStateFlow()
    
    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()
    
    init {
        loadRestockActivities()
    }
    
    fun loadRestockActivities() {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                // Collect the Flow directly - it will emit the initial value and then updates
                activityRepository.getActivitiesByType(ActivityType.RESTOCK.name).collect { activities ->
                    android.util.Log.d("ActivityViewModel", "Activities loaded/updated: ${activities.size}")
                    _activities.value = activities
                    _isLoading.value = false
                }
            } catch (e: Exception) {
                // Handle error
                android.util.Log.e("ActivityViewModel", "Error loading restock activities", e)
                _isLoading.value = false
            }
        }
    }
    
    fun loadActivityById(activityId: String) {
        viewModelScope.launch {
            try {
                val activity = activityRepository.getActivityById(activityId)
                activity?.let {
                    _selectedActivity.value = it
                    loadActivityItems(activityId)
                }
            } catch (e: Exception) {
                // Handle error
            }
        }
    }
    
    fun createRestockActivity(selectedProducts: List<Pair<Long, Int>>, productNames: Map<Long, String>, productCategories: Map<Long, String>) {
        viewModelScope.launch {
            try {
                android.util.Log.d("ActivityViewModel", "=== Starting createRestockActivity ===")
                val activityId = generateActivityId()
                val now = Date()
                
                android.util.Log.d("ActivityViewModel", "Creating restock activity with ID: $activityId")
                android.util.Log.d("ActivityViewModel", "Selected products: $selectedProducts")
                
                val activity = Activity(
                    activityId = activityId,
                    activityType = ActivityType.RESTOCK,
                    status = ActivityStatus.CREATED,
                    createdAt = now,
                    updatedAt = now
                )
                
                val activityItems = selectedProducts.map { (productId, quantity) ->
                    ActivityItem(
                        id = UUID.randomUUID().toString(),
                        activityId = activityId,
                        productId = productId,
                        productName = productNames[productId] ?: "",
                        productCategory = productCategories[productId] ?: "",
                        quantity = quantity,
                        isFulfill = false
                    )
                }
                
                android.util.Log.d("ActivityViewModel", "Created ${activityItems.size} activity items")
                android.util.Log.d("ActivityViewModel", "About to insert activity and items in transaction...")
                
                // Use transaction to isolate the database operations
                (activityRepository as ActivityRepositoryImpl).createActivityWithItems(activity, activityItems)
                
                android.util.Log.d("ActivityViewModel", "Activity and items inserted successfully in transaction")
                android.util.Log.d("ActivityViewModel", "=== createRestockActivity completed ===")
                
                // Don't call loadRestockActivities() here - the Flow will automatically update
                // when new data is inserted into the database
            } catch (e: Exception) {
                // Handle error
                android.util.Log.e("ActivityViewModel", "Error creating restock activity", e)
            }
        }
    }
    
    fun selectActivity(activity: Activity) {
        _selectedActivity.value = activity
        loadActivityItems(activity.activityId)
    }
    
    fun loadActivityItems(activityId: String) {
        viewModelScope.launch {
            try {
                android.util.Log.d("ActivityViewModel", "Loading activity items for activityId: $activityId")
                
                // Collect the Flow directly - it will emit the initial value and then updates
                activityRepository.getActivityItemsByActivityId(activityId).collect { items ->
                    android.util.Log.d("ActivityViewModel", "Items loaded/updated: ${items.size}")
                    _activityItems.value = items
                }
            } catch (e: Exception) {
                // Handle error
                android.util.Log.e("ActivityViewModel", "Error loading activity items", e)
                _activityItems.value = emptyList()
            }
        }
    }
    
    fun updateActivityItemFulfillment(activityItem: ActivityItem, isFulfill: Boolean) {
        viewModelScope.launch {
            try {
                val updatedItem = activityItem.copy(isFulfill = isFulfill)
                activityRepository.updateActivityItem(updatedItem)
                
                // Update the activity status based on fulfillment
                updateActivityStatus(activityItem.activityId)
            } catch (e: Exception) {
                // Handle error
            }
        }
    }
    
    private suspend fun updateActivityStatus(activityId: String) {
        val items = _activityItems.value
        val allFulfilled = items.all { it.isFulfill }
        val anyFulfilled = items.any { it.isFulfill }
        
        val currentActivity = _selectedActivity.value
        if (currentActivity != null) {
            val newStatus = when {
                allFulfilled -> ActivityStatus.COMPLETE
                anyFulfilled -> ActivityStatus.INPROGRESS
                else -> ActivityStatus.CREATED
            }
            
            val updatedActivity = currentActivity.copy(
                status = newStatus,
                updatedAt = Date()
            )
            
            activityRepository.updateActivity(updatedActivity)
            _selectedActivity.value = updatedActivity
        }
    }
    
    private fun generateActivityId(): String {
        val chars = "ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789"
        return (1..6).map { chars.random() }.joinToString("")
    }

    class Factory(
        private val activityRepository: ActivityRepository
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(ActivityViewModel::class.java)) {
                return ActivityViewModel(activityRepository) as T
            }
            throw IllegalArgumentException("Unknown ViewModel class")
        }
    }
} 