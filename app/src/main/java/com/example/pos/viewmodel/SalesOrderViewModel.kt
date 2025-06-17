package com.example.pos.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.pos.data.repository.SalesOrderRepository
import com.example.pos.model.Product
import com.example.pos.model.SalesOrder
import com.example.pos.model.SalesOrderItem
import com.example.pos.util.StringUtils
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.util.Calendar
import java.util.Date

class SalesOrderViewModel(
    private val salesOrderRepository: SalesOrderRepository
) : ViewModel() {

    private val _salesOrders = MutableStateFlow<List<SalesOrder>>(emptyList())
    val salesOrders: StateFlow<List<SalesOrder>> = _salesOrders.asStateFlow()

    private val _todayRevenue = MutableStateFlow(0.0)
    val todayRevenue: StateFlow<Double> = _todayRevenue.asStateFlow()

    private val _todayProfit = MutableStateFlow(0.0)
    val todayProfit: StateFlow<Double> = _todayProfit.asStateFlow()

    private val _dateRangeRevenue = MutableStateFlow(0.0)
    val dateRangeRevenue: StateFlow<Double> = _dateRangeRevenue.asStateFlow()

    private val _dateRangeProfit = MutableStateFlow(0.0)
    val dateRangeProfit: StateFlow<Double> = _dateRangeProfit.asStateFlow()

    private var selectedStartDate: Date? = null
    private var selectedEndDate: Date? = null

    init {
        viewModelScope.launch {
            try {
                salesOrderRepository.getAllSalesOrders().collect { orders ->
                    _salesOrders.value = orders
                    updateTodaySummary(orders)
                    updateDateRangeSummary(orders)
                    android.util.Log.d("SalesOrderViewModel", "Loaded ${orders.size} sales orders")
                }
            } catch (e: Exception) {
                android.util.Log.e("SalesOrderViewModel", "Error loading sales orders", e)
            }
        }
    }

    private fun updateTodaySummary(orders: List<SalesOrder>) {
        val calendar = Calendar.getInstance()
        calendar.set(Calendar.HOUR_OF_DAY, 0)
        calendar.set(Calendar.MINUTE, 0)
        calendar.set(Calendar.SECOND, 0)
        calendar.set(Calendar.MILLISECOND, 0)
        val startOfDay = calendar.time

        val todayOrders = orders.filter { it.dateTime >= startOfDay }
        _todayRevenue.value = todayOrders.sumOf { it.totalRevenue }
        _todayProfit.value = todayOrders.sumOf { it.totalProfit }
    }

    private fun updateDateRangeSummary(orders: List<SalesOrder>) {
        if (selectedStartDate == null || selectedEndDate == null) return

        val calendar = Calendar.getInstance()
        
        // Set start date to beginning of day
        calendar.time = selectedStartDate!!
        calendar.set(Calendar.HOUR_OF_DAY, 0)
        calendar.set(Calendar.MINUTE, 0)
        calendar.set(Calendar.SECOND, 0)
        calendar.set(Calendar.MILLISECOND, 0)
        val startDate = calendar.time

        // Set end date to end of day
        calendar.time = selectedEndDate!!
        calendar.set(Calendar.HOUR_OF_DAY, 23)
        calendar.set(Calendar.MINUTE, 59)
        calendar.set(Calendar.SECOND, 59)
        calendar.set(Calendar.MILLISECOND, 999)
        val endDate = calendar.time

        // Filter orders that fall within the date range (inclusive)
        val filteredOrders = orders.filter { order ->
            val orderDate = order.dateTime
            orderDate >= startDate && orderDate <= endDate
        }

        _dateRangeRevenue.value = filteredOrders.sumOf { it.totalRevenue }
        _dateRangeProfit.value = filteredOrders.sumOf { it.totalProfit }
    }

    fun setDateRange(startDate: Date, endDate: Date) {
        selectedStartDate = startDate
        selectedEndDate = endDate
        updateDateRangeSummary(_salesOrders.value)
    }
    
    fun refreshSalesOrders() {
        android.util.Log.d("SalesOrderViewModel", "Manually refreshing sales orders")
        viewModelScope.launch {
            try {
                salesOrderRepository.getAllSalesOrders().collect { orders ->
                    _salesOrders.value = orders
                    updateTodaySummary(orders)
                    updateDateRangeSummary(orders)
                    android.util.Log.d("SalesOrderViewModel", "Refreshed ${orders.size} sales orders")
                }
            } catch (e: Exception) {
                android.util.Log.e("SalesOrderViewModel", "Error refreshing sales orders", e)
            }
        }
    }

    fun createSalesOrder(products: List<Pair<Product, Int>>) {
        viewModelScope.launch {
            val totalRevenue = products.sumOf { (product, quantity) -> product.price * quantity }
            val totalProfit = products.sumOf { (product, quantity) -> (product.price - product.basePrice) * quantity }

            val salesOrder = SalesOrder(
                id = StringUtils.generateRandomAlphanumeric(),
                dateTime = Date(),
                totalRevenue = totalRevenue,
                totalProfit = totalProfit
            )

            // Create sales order first to get the ID
            val salesOrderId = salesOrderRepository.createSalesOrder(salesOrder)

            // Create and add sales order items
            val salesOrderItems = products.map { (product, quantity) ->
                SalesOrderItem(
                    id = StringUtils.generateRandomAlphanumeric(),
                    salesOrderId = salesOrderId,
                    productId = product.id,
                    quantity = quantity,
                    price = product.price,
                    profit = product.price - product.basePrice,
                    productCode = product.productCode,
                    productName = product.name.ifEmpty { "Unknown" },
                    productCategory = product.category.ifEmpty { "Uncategorized" }
                )
            }
            salesOrderRepository.addSalesOrderItems(salesOrderItems)
        }
    }

    class Factory(private val salesOrderRepository: SalesOrderRepository) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(SalesOrderViewModel::class.java)) {
                return SalesOrderViewModel(salesOrderRepository) as T
            }
            throw IllegalArgumentException("Unknown ViewModel class")
        }
    }
}