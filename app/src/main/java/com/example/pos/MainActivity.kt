package com.example.pos

import android.os.Bundle
import android.util.Log
import android.widget.ImageButton
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.NavController
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.setupWithNavController
import com.example.pos.fragment.SettingsDialog
import com.google.android.material.bottomnavigation.BottomNavigationView

class MainActivity : AppCompatActivity() {
    private lateinit var navController: NavController

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Log.d("MainActivity", "onCreate called")
        setContentView(R.layout.activity_main)

        try {
            setupNavigation()
            setupSettingsButton()
        } catch (e: Exception) {
            Log.e("MainActivity", "Error in onCreate", e)
            Toast.makeText(this, "Error initializing app: ${e.message}", Toast.LENGTH_LONG).show()
        }
    }

    private fun setupNavigation() {
        try {
            Log.d("MainActivity", "Setting up navigation")
            val navHostFragment = supportFragmentManager.findFragmentById(R.id.nav_host_fragment) as NavHostFragment
            navController = navHostFragment.navController
            Log.d("MainActivity", "NavController initialized")

            val bottomNav = findViewById<BottomNavigationView>(R.id.bottom_navigation)
            bottomNav.setupWithNavController(navController)
            Log.d("MainActivity", "Bottom navigation setup completed")
        } catch (e: Exception) {
            Log.e("MainActivity", "Error setting up navigation", e)
            throw e
        }
    }

    private fun setupSettingsButton() {
        try {
            Log.d("MainActivity", "Setting up settings button")
            findViewById<ImageButton>(R.id.btnSettings).setOnClickListener {
                SettingsDialog.newInstance().show(supportFragmentManager, "SettingsDialog")
            }
            Log.d("MainActivity", "Settings button setup completed")
        } catch (e: Exception) {
            Log.e("MainActivity", "Error setting up settings button", e)
            throw e
        }
    }
} 