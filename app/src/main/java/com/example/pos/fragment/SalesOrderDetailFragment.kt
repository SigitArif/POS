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
import androidx.navigation.fragment.navArgs
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.pos.adapter.SalesOrderItemAdapter
import com.example.pos.data.local.AppDatabase
import com.example.pos.data.repository.SalesOrderRepositoryImpl
import com.example.pos.databinding.FragmentSalesOrderDetailBinding
import com.example.pos.model.SalesOrderItem
import com.example.pos.viewmodel.SalesOrderViewModel
import kotlinx.coroutines.launch
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class SalesOrderDetailFragment : Fragment() {
    private var _binding: FragmentSalesOrderDetailBinding? = null
    private val binding get() = _binding!!
    private val args: SalesOrderDetailFragmentArgs by navArgs()
    private val viewModel: SalesOrderViewModel by viewModels {
        val database = AppDatabase.getDatabase(requireContext())
        val salesOrderRepository = SalesOrderRepositoryImpl(database.salesOrderDao(), database.salesOrderItemDao())
        SalesOrderViewModel.Factory(salesOrderRepository)
    }
    private val adapter = SalesOrderItemAdapter()
    private val numberFormat = NumberFormat.getCurrencyInstance(Locale("id", "ID"))
    private val dateFormat = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault())

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentSalesOrderDetailBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupRecyclerView()
        loadSalesOrderDetails()
        observeSalesOrderItems()
    }

    override fun onResume() {
        super.onResume()
        // Refresh data when returning to this fragment
        loadSalesOrderDetails()
    }

    private fun setupRecyclerView() {
        binding.rvOrderItems.apply {
            layoutManager = LinearLayoutManager(context)
            adapter = this@SalesOrderDetailFragment.adapter
        }
    }

    private fun loadSalesOrderDetails() {
        viewModel.getSalesOrderItems(args.salesOrderId)
    }

    private fun observeSalesOrderItems() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.salesOrderItems.collect { items ->
                    adapter.submitList(items)
                    updateSummary(items)
                }
            }
        }
    }

    private fun updateSummary(items: List<SalesOrderItem>) {
        val totalRevenue = items.sumOf { it.price * it.quantity }
        val totalProfit = items.sumOf { it.profit * it.quantity }

        binding.tvOrderId.text = "#${args.salesOrderId}"
        binding.tvDateTime.text = dateFormat.format(Date())
        binding.tvTotalRevenue.text = "Revenue: ${numberFormat.format(totalRevenue)}"
        binding.tvTotalProfit.text = "Profit: ${numberFormat.format(totalProfit)}"
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}