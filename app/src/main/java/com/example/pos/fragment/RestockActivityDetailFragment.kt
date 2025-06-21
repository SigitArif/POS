package com.example.pos.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ProgressBar
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.pos.R
import com.example.pos.adapter.RestockActivityItemAdapter
import com.example.pos.data.local.AppDatabase
import com.example.pos.data.repository.ActivityRepositoryImpl
import com.example.pos.viewmodel.ActivityViewModel
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Locale

class RestockActivityDetailFragment : Fragment() {

    private val viewModel: ActivityViewModel by viewModels {
        val database = AppDatabase.getDatabase(requireContext())
        val activityRepository = ActivityRepositoryImpl(database)
        ActivityViewModel.Factory(activityRepository)
    }
    private val args: RestockActivityDetailFragmentArgs by navArgs()
    private lateinit var adapter: RestockActivityItemAdapter
    private lateinit var recyclerView: RecyclerView
    private lateinit var emptyView: TextView
    private lateinit var progressBar: ProgressBar
    private lateinit var tvActivityId: TextView
    private lateinit var tvStatus: TextView
    private lateinit var tvCreatedDate: TextView
    private lateinit var tvUpdatedDate: TextView

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_restock_activity_detail, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        setupViews(view)
        setupRecyclerView()
        setupObservers()
        loadActivityData()
    }

    private fun setupViews(view: View) {
        recyclerView = view.findViewById(R.id.recyclerView)
        emptyView = view.findViewById(R.id.emptyView)
        progressBar = view.findViewById(R.id.progressBar)
        tvActivityId = view.findViewById(R.id.tvActivityId)
        tvStatus = view.findViewById(R.id.tvStatus)
        tvCreatedDate = view.findViewById(R.id.tvCreatedDate)
        tvUpdatedDate = view.findViewById(R.id.tvUpdatedDate)
    }

    private fun setupRecyclerView() {
        adapter = RestockActivityItemAdapter { activityItem, isFulfill ->
            viewModel.updateActivityItemFulfillment(activityItem, isFulfill)
        }
        
        recyclerView.apply {
            layoutManager = LinearLayoutManager(context)
            adapter = this@RestockActivityDetailFragment.adapter
        }
    }

    private fun setupObservers() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.selectedActivity.collectLatest { activity ->
                activity?.let { updateActivityInfo(it) }
            }
        }

        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.activityItems.collectLatest { items ->
                adapter.submitList(items)
                updateEmptyView(items.isEmpty())
            }
        }

        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.isLoading.collectLatest { isLoading ->
                progressBar.visibility = if (isLoading) View.VISIBLE else View.GONE
            }
        }
    }

    private fun loadActivityData() {
        // Load activity by ID from repository
        viewModel.loadActivityById(args.activityId)
    }

    private fun updateActivityInfo(activity: com.example.pos.model.Activity) {
        val dateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault())
        
        tvActivityId.text = activity.activityId
        tvStatus.text = activity.status.name
        tvCreatedDate.text = "Created: ${dateFormat.format(activity.createdAt)}"
        tvUpdatedDate.text = "Updated: ${dateFormat.format(activity.updatedAt)}"
        
        // Set status background color
        val statusColor = when (activity.status) {
            com.example.pos.model.ActivityStatus.CREATED -> android.R.color.holo_blue_light
            com.example.pos.model.ActivityStatus.INPROGRESS -> android.R.color.holo_orange_light
            com.example.pos.model.ActivityStatus.COMPLETE -> android.R.color.holo_green_light
        }
        tvStatus.setBackgroundResource(statusColor)
    }

    private fun updateEmptyView(isEmpty: Boolean) {
        emptyView.visibility = if (isEmpty) View.VISIBLE else View.GONE
        recyclerView.visibility = if (isEmpty) View.GONE else View.VISIBLE
    }
} 