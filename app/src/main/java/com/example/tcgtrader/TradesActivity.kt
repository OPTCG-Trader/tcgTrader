package com.example.tcgtrader

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.bottomnavigation.BottomNavigationView

class TradesActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_trades_list)
        val bottomNavigation = findViewById<BottomNavigationView>(R.id.bottom_navigation)

        bottomNavigation.setOnItemSelectedListener { item ->

            when (item.itemId) {
                R.id.nav_home -> {
                    val intent = Intent(this, HomeActivity::class.java)
                    startActivity(intent)
                    true
                }
                R.id.nav_trades -> {
                    true
                }
                R.id.nav_desired -> {
                    // Placeholder for DesiredActivity navigation
                    Toast.makeText(this, "Navigate to Desired", Toast.LENGTH_SHORT).show()
                    true
                }
                else -> false
            }
        }

        bottomNavigation.selectedItemId = R.id.nav_trades

    }

}
