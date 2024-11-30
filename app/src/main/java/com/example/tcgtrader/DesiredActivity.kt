package com.example.tcgtrader

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class DesiredActivity : AppCompatActivity() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: DesiredAdapter
    private val desiredList = mutableListOf<Card>()
    private val firestore = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_trades_list)

        recyclerView = findViewById(R.id.trades_recycler_view)
        recyclerView.layoutManager = LinearLayoutManager(this)
        adapter = DesiredAdapter(desiredList) { card -> deleteCardFromDesired(card) }
        recyclerView.adapter = adapter

        fetchDesired()

        val bottomNavigation = findViewById<BottomNavigationView>(R.id.bottom_navigation)
        bottomNavigation.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.nav_home -> {
                    val intent = Intent(this, HomeActivity::class.java)
                    startActivity(intent)
                    true
                }
                R.id.nav_trades -> {
                    val intent = Intent(this, TradesActivity::class.java)
                    startActivity(intent)
                    true
                }
                R.id.nav_desired -> {
                    true
                }
                else -> false
            }
        }
        bottomNavigation.selectedItemId = R.id.nav_desired
    }

    private fun fetchDesired() {
        val currentUser = auth.currentUser
        if (currentUser == null) {
            Toast.makeText(this, "User not logged in!", Toast.LENGTH_SHORT).show()
            return
        }

        val userId = currentUser.uid
        val desiredCollection = firestore.collection("users").document(userId).collection("desired")

        desiredCollection.get().addOnSuccessListener { querySnapshot ->
            desiredList.clear()
            for (document in querySnapshot.documents) {
                val card = document.toObject(Card::class.java)
                if (card != null) {
                    desiredList.add(card)
                }
            }
            adapter.notifyDataSetChanged()
        }.addOnFailureListener {
            Toast.makeText(this, "Failed to fetch Desired list.", Toast.LENGTH_SHORT).show()
        }
    }

    private fun deleteCardFromDesired(card: Card) {
        val currentUser = auth.currentUser
        if (currentUser == null) {
            Toast.makeText(this, "User not logged in!", Toast.LENGTH_SHORT).show()
            return
        }

        val userId = currentUser.uid
        val desiredCollection = firestore.collection("users").document(userId).collection("desired")

        desiredCollection.document(card.id).delete()
            .addOnSuccessListener {
                desiredList.remove(card) // Remove card locally
                adapter.notifyDataSetChanged() // Notify adapter of the change
                Toast.makeText(this, "${card.name} deleted from Desired!", Toast.LENGTH_SHORT).show()
            }
            .addOnFailureListener {
                Toast.makeText(this, "Failed to delete ${card.name} from Desired.", Toast.LENGTH_SHORT).show()
            }
    }
}
