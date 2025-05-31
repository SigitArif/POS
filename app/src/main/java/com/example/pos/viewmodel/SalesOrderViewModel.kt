package com.example.pos.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.pos.data.repository.SalesOrderRepository
import com.example.pos.model.Product
import com.example.pos.model.SalesOrder
import com.example.pos.model.SalesOrderItem
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.util.Date

class SalesOrderViewModel(
    private val salesOrderRepository: SalesOrderRepository
) : ViewModel() {

    private val _salesOrders = MutableStateFlow<List<SalesOrder>>(emptyList())
    val salesOrders: StateFlow<List<SalesOrder>> = _salesOrders.asStateFlow()

    init {
        viewModelScope.launch {
            salesOrderRepository.getAllSalesOrders().collect { orders ->
                _salesOrders.value = orders
            }
        }
    }

    fun createSalesOrder(products: List<Pair<Product, Int>>) {
        viewModelScope.launch {
            val totalRevenue = products.sumOf { (product, quantity) -> product.price * quantity }
            val totalProfit = products.sumOf { (product, quantity) -> (product.price - product.basePrice) * quantity }

            val salesOrder = SalesOrder(
                dateTime = Date(),
                totalRevenue = totalRevenue,
                totalProfit = totalProfit
            )

            // Create sales order first to get the ID
            val salesOrderId = salesOrderRepository.createSalesOrder(salesOrder)

            // Create items with the sales order ID
            val items = products.map { (product, quantity) ->
                SalesOrderItem(
                    salesOrderId = salesOrderId,
                    productId = product.id,
                    quantity = quantity,
                    price = product.price,
                    profit = (product.price - product.basePrice) * quantity
                )
            }

            // Add items to the sales order
            salesOrderRepository.addSalesOrderItems(items)
        }
    }

    class Factory(private val salesOrderRepository: SalesOrderRepository) : ViewModelProvider.Factory {
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(SalesOrderViewModel::class.java)) {
                @Suppress("UNCHECKED_CAST")
                return SalesOrderViewModel(salesOrderRepository) as T
            }
            throw IllegalArgumentException("Unknown ViewModel class")
        }
    }
} 