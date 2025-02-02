package com.example.hotelio

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.view.MenuItem
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout
import androidx.fragment.app.Fragment
import com.google.android.material.navigation.NavigationView

class HomeMainPage : AppCompatActivity() {

    private lateinit var drawerLayout: DrawerLayout
    private lateinit var toggle: ActionBarDrawerToggle

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_home_main_page)

        // Initialize DrawerLayout and Toolbar
        drawerLayout = findViewById(R.id.drawer_layout)
        val toolbar: Toolbar = findViewById(R.id.toolbar)
        setSupportActionBar(toolbar)

        // ActionBarDrawerToggle to handle the hamburger icon and drawer
        toggle = ActionBarDrawerToggle(
            this, drawerLayout, toolbar,
            R.string.open_drawer, R.string.close_drawer
        )
        drawerLayout.addDrawerListener(toggle)
        toggle.syncState()

        // Handle menu item clicks
        val navigationView: NavigationView = findViewById(R.id.nav_view)
        navigationView.setNavigationItemSelectedListener { item ->
            handleMenuItemSelection(item)
            true
        }

        // Load the default fragment (e.g., HotelDetailsActivity)
        loadFragment(HotelDetail())
    }

    private fun handleMenuItemSelection(item: MenuItem) {
        when (item.itemId) {
            R.id.nav_hotel_details -> {
                loadFragment(HotelDetail())
            }
            R.id.nav_table_management -> {
                loadFragment(TableManagement())
            }
            R.id.nav_menu_management -> {
                loadFragment(MenuManagement())
            }
            R.id.nav_orders -> {
                loadFragment(order())
            }
            R.id.nav_payment_management -> {
                loadFragment(PaymentManagement())
            }
            R.id.nav_settings -> {
                loadFragment(Setting())
            }
            R.id.nav_profile -> {
                loadFragment(Profile())
            }
        }

        // Close the drawer after selection
        drawerLayout.closeDrawer(GravityCompat.START)
    }

    private fun loadFragment(fragment: Fragment) {
        // Replace the fragment in the FrameLayout container
        val transaction = supportFragmentManager.beginTransaction()
        transaction.replace(R.id.fragment_container, fragment)
        transaction.addToBackStack(null)  // Add to back stack to allow back navigation
        transaction.commit()
    }
}
