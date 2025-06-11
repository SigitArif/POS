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

    init {
        viewModelScope.launch {
            salesOrderRepository.getAllSalesOrders().collect { orders ->
                _salesOrders.value = orders
                updateTodaySummary(orders)
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
                    profit = product.price - product.basePrice
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