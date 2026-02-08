package com.example.valenbisi

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import com.google.android.material.bottomnavigation.BottomNavigationView

/*
 * Class to populate the recycler view of bike stations, with its details, in the main station
 */
class MainActivity : AppCompatActivity() {
    // Function when the main activity is created where we set the view and the bottom navigation bar
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        if (savedInstanceState == null) {
            // Load the default fragment (Station List)
            loadFragment(BikeStationFragment())
        }

        // Setup Bottom Navigation
        setupBottomNavigation()
    }

    // Function to set up the buttons of the bottom navigation bar
    private fun setupBottomNavigation() {
        val bottomNav = findViewById<BottomNavigationView>(R.id.bottom_nav)
        bottomNav.itemIconTintList = null
        bottomNav.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.nav_home -> {
                    loadFragment(BikeStationFragment()) // Navigate back to station list main activity
                    true
                }
                R.id.nav_help -> {
                    loadFragment(HelpFragment()) // Navigate to HelpFragment
                    true
                }
                else -> false
            }
        }
    }

    // Function to change fragments
    private fun loadFragment(fragment: Fragment) {
        supportFragmentManager.beginTransaction()
            .setCustomAnimations(
                android.R.anim.fade_in,  // Enter animation
                android.R.anim.fade_out  // Exit animation
            )
            .replace(R.id.fragment_container, fragment)
            .commit()
    }

}
