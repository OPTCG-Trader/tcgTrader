package com.example.tcgtrader

import android.Manifest
import android.annotation.SuppressLint
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Geocoder
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.*

class SettingsActivity : AppCompatActivity() {

    private val firestore = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()
    private lateinit var homeLocationText: TextView

    private lateinit var fusedLocationClient: FusedLocationProviderClient

    private val requestPermissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted ->
            if (isGranted) {
                fetchLocation()
            } else {
                Toast.makeText(this, "Location permission denied.", Toast.LENGTH_SHORT).show()
            }
        }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_settings)

        homeLocationText = findViewById(R.id.home_location_text)
        val setHomeLocationButton = findViewById<Button>(R.id.set_home_location_button)
        val bottomNavigation = findViewById<BottomNavigationView>(R.id.bottom_navigation)

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)

        loadHomeLocation()

        setHomeLocationButton.setOnClickListener {
            requestLocationPermission()
        }

        bottomNavigation.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.nav_home -> {
                    startActivity(Intent(this, HomeActivity::class.java))
                    true
                }
                R.id.nav_trades -> {
                    startActivity(Intent(this, TradesActivity::class.java))
                    true
                }
                R.id.nav_desired -> {
                    startActivity(Intent(this, DesiredActivity::class.java))
                    true
                }
                else -> false
            }
        }
    }

    @SuppressLint("SetTextI18n")
    private fun loadHomeLocation() {
        val currentUser = auth.currentUser
        if (currentUser == null) {
            Toast.makeText(this, "User not logged in!", Toast.LENGTH_SHORT).show()
            return
        }

        val userId = currentUser.uid
        val userDocument = firestore.collection("users").document(userId)

        userDocument.get().addOnSuccessListener { document ->
            if (document.exists()) {
                val homeLocation = document.getString("homeLocation")
                if (!homeLocation.isNullOrEmpty()) {
                    homeLocationText.text = "Home Location: $homeLocation"
                }
            }
        }.addOnFailureListener {
            Toast.makeText(this, "Failed to load home location.", Toast.LENGTH_SHORT).show()
        }
    }

    private fun requestLocationPermission() {
        if (checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            fetchLocation()
        } else {
            requestPermissionLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION)
        }
    }

    @SuppressLint("MissingPermission")
    private fun fetchLocation() {
        fusedLocationClient.lastLocation.addOnSuccessListener { location ->
            if (location != null) {
                Log.d("LocationDebug", "Lat: ${location.latitude}, Lng: ${location.longitude}")
                reverseGeocode(location.latitude, location.longitude)
            } else {
                Log.d("LocationDebug", "Location is null")
                Toast.makeText(this, "Failed to fetch location", Toast.LENGTH_SHORT).show()
            }
        }.addOnFailureListener { e ->
            Log.e("LocationDebug", "Error fetching location: ${e.localizedMessage}")
            Toast.makeText(this, "Error fetching location", Toast.LENGTH_SHORT).show()
        }
    }

    private fun reverseGeocode(lat: Double, lng: Double) {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val geocoder = Geocoder(this@SettingsActivity, Locale.getDefault())
                val addresses = geocoder.getFromLocation(lat, lng, 1)
                withContext(Dispatchers.Main) {
                    if (!addresses.isNullOrEmpty()) {
                        val cityName = addresses[0].locality ?: "Unknown City"
                        saveHomeLocation(cityName)
                    } else {
                        Toast.makeText(this@SettingsActivity, "Failed to fetch city name.", Toast.LENGTH_SHORT).show()
                    }
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    Toast.makeText(this@SettingsActivity, "Geocoding failed: ${e.localizedMessage}", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    private fun saveHomeLocation(cityName: String) {
        val currentUser = auth.currentUser
        if (currentUser == null) {
            Toast.makeText(this, "User not logged in!", Toast.LENGTH_SHORT).show()
            return
        }

        val userId = currentUser.uid
        Log.d("FirestoreDebug", "Current User UID: $userId")

        val userDocument = firestore.collection("users").document(userId)

        // Use set with merge to create or update the document
        userDocument.set(mapOf("homeLocation" to cityName), SetOptions.merge())
            .addOnSuccessListener {
                homeLocationText.text = "Home Location: $cityName"
                Toast.makeText(this, "Home location set to $cityName.", Toast.LENGTH_SHORT).show()
            }
            .addOnFailureListener { e ->
                Log.e("FirestoreDebug", "Error saving home location: ${e.localizedMessage}")
                Toast.makeText(this, "Failed to save home location.", Toast.LENGTH_SHORT).show()
            }
    }

}
