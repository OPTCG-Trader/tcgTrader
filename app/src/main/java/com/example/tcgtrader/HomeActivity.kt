package com.example.tcgtrader

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.firebase.auth.FirebaseAuth

class HomeActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_home)

        // Logout button
        findViewById<Button>(R.id.logout_button).setOnClickListener {
            // Log out the user and return to login screen
            FirebaseAuth.getInstance().signOut()
            startActivity(Intent(this, MainActivity::class.java))
            finish()
        }

        // Settings button
        findViewById<Button>(R.id.settings_button).setOnClickListener {
            Toast.makeText(this, "Settings placeholder clicked", Toast.LENGTH_SHORT).show()
        }

        findViewById<Button>(R.id.search_button).setOnClickListener {
            // Placeholder for search functionality
        }

        val bottomNavigation = findViewById<BottomNavigationView>(R.id.bottom_navigation)
        bottomNavigation.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.nav_home -> {
                    true
                }
                R.id.nav_trades -> {
                    val intent = Intent(this, TradesActivity::class.java)
                    startActivity(intent)
                    true
                }
                R.id.nav_desired -> {
                    Toast.makeText(this, "Navigate to Desired", Toast.LENGTH_SHORT).show()
                    true
                }
                else -> false
            }
        }
        bottomNavigation.selectedItemId = R.id.nav_home

    }
}
