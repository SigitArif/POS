package com.example.pos.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.pos.R
import com.example.pos.adapter.SalesOrderAdapter
import com.example.pos.data.local.AppDatabase
import com.example.pos.data.repository.SalesOrderRepositoryImpl
import com.example.pos.viewmodel.SalesOrderViewModel
import com.google.android.material.floatingactionbutton.FloatingActionButton
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

class SalesOrderListFragment : Fragment() {
    private lateinit var recyclerView: RecyclerView
    private lateinit var fabAddOrder: FloatingActionButton
    private val adapter = SalesOrderAdapter()

    private val viewModel: SalesOrderViewModel by viewModels {
        val database = AppDatabase.getDatabase(requireContext())
        val salesOrderRepository = SalesOrderRepositoryImpl(database.salesOrderDao(), database.salesOrderItemDao())
        SalesOrderViewModel.Factory(salesOrderRepository)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_sales_order_list, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupViews(view)
        setupRecyclerView(view)
        observeSalesOrders()
    }

    private fun setupViews(view: View) {
        recyclerView = view.findViewById(R.id.rvSalesOrders)
        fabAddOrder = view.findViewById(R.id.fabAddOrder)
        
        fabAddOrder.setOnClickListener {
            findNavController().navigate(R.id.action_sales_order_to_product_selection)
        }
    }

    private fun setupRecyclerView(view: View) {
        recyclerView = view.findViewById(R.id.rvSalesOrders)
        recyclerView.layoutManager = LinearLayoutManager(context)
        recyclerView.adapter = adapter
    }

    private fun observeSalesOrders() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.salesOrders.collectLatest { orders ->
                    adapter.submitList(orders)
                }
            }
        }
    }
} 