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
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.pos.R
import com.example.pos.adapter.RestockActivityAdapter
import com.example.pos.data.local.AppDatabase
import com.example.pos.data.repository.ActivityRepositoryImpl
import com.example.pos.viewmodel.ActivityViewModel
import com.google.android.material.floatingactionbutton.FloatingActionButton
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

class RestockActivityListFragment : Fragment() {

    private val viewModel: ActivityViewModel by viewModels {
        val database = AppDatabase.getDatabase(requireContext())
        val activityRepository = ActivityRepositoryImpl(database)
        ActivityViewModel.Factory(activityRepository)
    }
    private lateinit var adapter: RestockActivityAdapter
    private lateinit var recyclerView: RecyclerView
    private lateinit var emptyView: TextView
    private lateinit var progressBar: ProgressBar
    private lateinit var fabAddActivity: FloatingActionButton

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_restock_activity_list, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        setupViews(view)
        setupRecyclerView()
        setupObservers()
        setupClickListeners()
    }

    private fun setupViews(view: View) {
        recyclerView = view.findViewById(R.id.recyclerView)
        emptyView = view.findViewById(R.id.emptyView)
        progressBar = view.findViewById(R.id.progressBar)
        fabAddActivity = view.findViewById(R.id.fabAddActivity)
    }

    private fun setupRecyclerView() {
        adapter = RestockActivityAdapter { activity ->
            // Navigate to activity detail
            val action = RestockActivityListFragmentDirections
                .actionRestockActivityListToRestockActivityDetail(activity.activityId)
            findNavController().navigate(action)
        }
        
        recyclerView.apply {
            layoutManager = LinearLayoutManager(context)
            adapter = this@RestockActivityListFragment.adapter
        }
    }

    private fun setupObservers() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.activities.collectLatest { activities ->
                adapter.submitList(activities)
                updateEmptyView(activities.isEmpty())
            }
        }

        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.isLoading.collectLatest { isLoading ->
                progressBar.visibility = if (isLoading) View.VISIBLE else View.GONE
            }
        }
    }

    private fun setupClickListeners() {
        fabAddActivity.setOnClickListener {
            val action = RestockActivityListFragmentDirections
                .actionRestockActivityListToCreateRestockActivity()
            findNavController().navigate(action)
        }
    }

    private fun updateEmptyView(isEmpty: Boolean) {
        emptyView.visibility = if (isEmpty) View.VISIBLE else View.GONE
        recyclerView.visibility = if (isEmpty) View.GONE else View.VISIBLE
    }
} 