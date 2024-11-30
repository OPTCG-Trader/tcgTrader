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

class TradesActivity : AppCompatActivity() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: TradesAdapter
    private val tradesList = mutableListOf<Card>()
    private val firestore = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_trades_list)


        recyclerView = findViewById(R.id.trades_recycler_view)
        recyclerView.layoutManager = LinearLayoutManager(this)
        adapter = TradesAdapter(tradesList) { card -> deleteCardFromTrades(card) }
        recyclerView.adapter = adapter


        fetchTrades()


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
                    val intent = Intent(this, DesiredActivity::class.java)
                    startActivity(intent)
                    true
                }
                else -> false
            }
        }
        bottomNavigation.selectedItemId = R.id.nav_trades
    }

    private fun fetchTrades() {
        val currentUser = auth.currentUser
        if (currentUser == null) {
            Toast.makeText(this, "User not logged in!", Toast.LENGTH_SHORT).show()
            return
        }

        val userId = currentUser.uid
        val tradesCollection = firestore.collection("users").document(userId).collection("trades")

        tradesCollection.get().addOnSuccessListener { querySnapshot ->
            tradesList.clear()
            for (document in querySnapshot.documents) {
                val card = document.toObject(Card::class.java)
                if (card != null) {
                    tradesList.add(card)
                }
            }
            adapter.notifyDataSetChanged()
        }.addOnFailureListener {
            Toast.makeText(this, "Failed to fetch Trades list.", Toast.LENGTH_SHORT).show()
        }
    }

    private fun deleteCardFromTrades(card: Card) {
        val currentUser = auth.currentUser
        if (currentUser == null) {
            Toast.makeText(this, "User not logged in!", Toast.LENGTH_SHORT).show()
            return
        }

        val userId = currentUser.uid
        val tradesCollection = firestore.collection("users").document(userId).collection("trades")

        tradesCollection.document(card.id).delete()
            .addOnSuccessListener {
                tradesList.remove(card)
                adapter.notifyDataSetChanged()
                Toast.makeText(this, "${card.name} deleted from Trades!", Toast.LENGTH_SHORT).show()
            }
            .addOnFailureListener {
                Toast.makeText(this, "Failed to delete ${card.name} from Trades.", Toast.LENGTH_SHORT).show()
            }
    }
}
