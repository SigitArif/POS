package com.example.pos

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.SearchView
import androidx.fragment.app.Fragment
import androidx.viewpager2.widget.ViewPager2
import com.example.pos.adapter.CategoryPagerAdapter
import com.example.pos.fragment.AddProductFragment
import com.example.pos.fragment.ProductListFragment
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayoutMediator

class MainActivity : AppCompatActivity() {
    private lateinit var viewPager: ViewPager2
    private lateinit var tabLayout: TabLayout
    private lateinit var searchView: SearchView
    private lateinit var fabAddProduct: FloatingActionButton
    private lateinit var categoryPagerAdapter: CategoryPagerAdapter
    private var isSearching = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        setupViewPager()
        setupTabLayout()
        setupSearchView()
        setupFab()
    }

    private fun setupViewPager() {
        viewPager = findViewById(R.id.viewPager)
        categoryPagerAdapter = CategoryPagerAdapter(this, this)
        viewPager.adapter = categoryPagerAdapter
    }

    private fun setupTabLayout() {
        tabLayout = findViewById(R.id.tabLayout)
        TabLayoutMediator(tabLayout, viewPager) { tab, position ->
            tab.text = categoryPagerAdapter.categories.value[position].name
        }.attach()
    }

    private fun setupSearchView() {
        searchView = findViewById(R.id.searchView)
        searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                return false
            }

            override fun onQueryTextChange(newText: String?): Boolean {
                val query = newText ?: ""
                if (query.isNotEmpty() && !isSearching) {
                    // Switch to ALL tab when starting to search
                    viewPager.setCurrentItem(0, true) // 0 is the ALL tab
                    isSearching = true
                } else if (query.isEmpty()) {
                    isSearching = false
                }

                // Update all fragments with the search query
                for (i in 0 until categoryPagerAdapter.categories.value.size) {
                    val fragment = supportFragmentManager.findFragmentByTag("f$i")
                    if (fragment is ProductListFragment) {
                        fragment.updateSearchQuery(query)
                    }
                }
                return true
            }
        })
    }

    private fun setupFab() {
        fabAddProduct = findViewById(R.id.fabAddProduct)
        fabAddProduct.setOnClickListener {
            showAddProductFragment()
        }
    }

    private fun showAddProductFragment() {
        val addProductFragment = AddProductFragment()
        addProductFragment.show(supportFragmentManager, "AddProductFragment")
    }

    private fun getCurrentFragment(): Fragment? {
        val position = viewPager.currentItem
        return supportFragmentManager.findFragmentByTag("f$position")
    }
} 