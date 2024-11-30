package com.example.tcgtrader

import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.loopj.android.http.AsyncHttpClient
import com.loopj.android.http.JsonHttpResponseHandler
import cz.msebera.android.httpclient.Header
import org.json.JSONArray
import org.json.JSONObject

class SearchActivity : AppCompatActivity() {

    private val client = AsyncHttpClient()
    private val firestore = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()
    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: CardAdapter
    private val allCards = mutableListOf<Card>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_search)

        // Initialize RecyclerView
        recyclerView = findViewById(R.id.cards_recycler_view)
        recyclerView.layoutManager = LinearLayoutManager(this)
        adapter = CardAdapter(
            cards = allCards,
            onAddToTrades = { card -> saveCardToFirestore(card, "trades") },
            onAddToDesired = { card -> saveCardToFirestore(card, "desired") }
        )
        recyclerView.adapter = adapter

        // Initialize Dropdown
        val dropdown = findViewById<Spinner>(R.id.set_deck_dropdown)
        fetchSetsAndDecks(dropdown)

        // Initialize Search Bar
        val searchInput = findViewById<EditText>(R.id.search_input)
        searchInput.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                val query = s.toString().trim()
                filterCards(query)
            }

            override fun afterTextChanged(s: Editable?) {}
        })

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
                    val intent = Intent(this, DesiredActivity::class.java)
                    startActivity(intent)
                    true
                }
                else -> false
            }
        }
    }

    private fun saveCardToFirestore(card: Card, collection: String) {
        val currentUser = auth.currentUser
        if (currentUser == null) {
            Toast.makeText(this, "User not logged in!", Toast.LENGTH_SHORT).show()
            return
        }

        val userId = currentUser.uid
        val userCollection = firestore.collection("users").document(userId).collection(collection)

        userCollection.document(card.id).get().addOnSuccessListener { documentSnapshot ->
            if (!documentSnapshot.exists()) {
                userCollection.document(card.id).set(card)
                    .addOnSuccessListener {
                        Toast.makeText(this, "${card.name} added to $collection!", Toast.LENGTH_SHORT).show()
                    }
                    .addOnFailureListener {
                        Toast.makeText(this, "Failed to add card to $collection.", Toast.LENGTH_SHORT).show()
                    }
            } else {
                Toast.makeText(this, "${card.name} is already in $collection!", Toast.LENGTH_SHORT).show()
            }
        }.addOnFailureListener {
            Toast.makeText(this, "Error checking duplicates in $collection.", Toast.LENGTH_SHORT).show()
        }
    }

    private fun fetchSetsAndDecks(dropdown: Spinner) {
        val setsUrl = "https://optcgapi.com/api/allSets/"
        val decksUrl = "https://optcgapi.com/api/allDecks/"
        val combinedList = mutableListOf<Map<String, String>>()

        // Fetch sets
        client.get(setsUrl, object : JsonHttpResponseHandler() {
            override fun onSuccess(statusCode: Int, headers: Array<out Header>?, response: JSONArray?) {
                response?.let {
                    for (i in 0 until it.length()) {
                        val set = it.getJSONObject(i)
                        combinedList.add(
                            mapOf(
                                "name" to set.getString("set_name"),
                                "id" to set.getString("set_id"),
                                "type" to "set"
                            )
                        )
                    }
                    fetchDecks(decksUrl, combinedList, dropdown)
                }
            }

            override fun onFailure(
                statusCode: Int,
                headers: Array<out Header>?,
                throwable: Throwable?,
                errorResponse: JSONObject?
            ) {
                Toast.makeText(this@SearchActivity, "Error fetching sets: ${throwable?.message}", Toast.LENGTH_LONG).show()
            }
        })
    }

    private fun fetchDecks(decksUrl: String, combinedList: MutableList<Map<String, String>>, dropdown: Spinner) {
        client.get(decksUrl, object : JsonHttpResponseHandler() {
            override fun onSuccess(statusCode: Int, headers: Array<out Header>?, response: JSONArray?) {
                response?.let {
                    for (i in 0 until it.length()) {
                        val deck = it.getJSONObject(i)
                        combinedList.add(
                            mapOf(
                                "name" to deck.getString("structure_deck_name"),
                                "id" to deck.getString("structure_deck_id"),
                                "type" to "deck"
                            )
                        )
                    }
                    populateDropdown(combinedList, dropdown)
                }
            }

            override fun onFailure(
                statusCode: Int,
                headers: Array<out Header>?,
                throwable: Throwable?,
                errorResponse: JSONObject?
            ) {
                Toast.makeText(this@SearchActivity, "Error fetching decks: ${throwable?.message}", Toast.LENGTH_LONG).show()
            }
        })
    }

    private fun populateDropdown(items: List<Map<String, String>>, dropdown: Spinner) {
        val adapter = ArrayAdapter(
            this,
            android.R.layout.simple_spinner_item,
            items.map { it["name"] ?: "Unknown" }
        )
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        dropdown.adapter = adapter

        dropdown.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>, view: View, position: Int, id: Long) {
                val selectedItem = items[position]
                val itemId = selectedItem["id"] ?: return
                val itemType = selectedItem["type"] ?: return

                if (itemType == "set") {
                    fetchCardsFromSet(itemId)
                } else if (itemType == "deck") {
                    fetchCardsFromDeck(itemId)
                }
            }

            override fun onNothingSelected(parent: AdapterView<*>) {}
        }
    }

    private fun fetchCardsFromSet(setId: String) {
        val url = "https://optcgapi.com/api/sets/$setId/"
        client.get(url, object : JsonHttpResponseHandler() {
            override fun onSuccess(statusCode: Int, headers: Array<out Header>?, response: JSONArray?) {
                response?.let {
                    val cards = parseCardJson(response)
                    updateRecyclerView(cards)
                }
            }

            override fun onFailure(
                statusCode: Int,
                headers: Array<out Header>?,
                throwable: Throwable?,
                errorResponse: JSONObject?
            ) {
                Toast.makeText(this@SearchActivity, "Error fetching cards from set: ${throwable?.message}", Toast.LENGTH_LONG).show()
            }
        })
    }

    private fun fetchCardsFromDeck(deckId: String) {
        val url = "https://optcgapi.com/api/decks/$deckId/"
        client.get(url, object : JsonHttpResponseHandler() {
            override fun onSuccess(statusCode: Int, headers: Array<out Header>?, response: JSONArray?) {
                response?.let {
                    val cards = parseCardJson(response)
                    updateRecyclerView(cards)
                }
            }

            override fun onFailure(
                statusCode: Int,
                headers: Array<out Header>?,
                throwable: Throwable?,
                errorResponse: JSONObject?
            ) {
                Toast.makeText(this@SearchActivity, "Error fetching cards from deck: ${throwable?.message}", Toast.LENGTH_LONG).show()
            }
        })
    }

    private fun parseCardJson(jsonArray: JSONArray): List<Card> {
        val cards = mutableListOf<Card>()
        for (i in 0 until jsonArray.length()) {
            val jsonObject = jsonArray.getJSONObject(i)
            cards.add(
                Card(
                    id = jsonObject.optString("card_set_id", ""), // Use "id" as the unique identifier
                    name = jsonObject.optString("card_name", "Unknown"),
                    rarity = jsonObject.optString("rarity", "Unknown"),
                    color = jsonObject.optString("card_color", "Unknown"),
                    cardText = jsonObject.optString("card_text", "No description"),
                    cardType = jsonObject.optString("card_type", "Unknown"),
                    marketPrice = jsonObject.optDouble("market_price", 0.0)
                )
            )
        }
        return cards
    }

    private fun updateRecyclerView(cards: List<Card>) {
        allCards.clear()
        allCards.addAll(cards)
        adapter.updateData(allCards)
    }

    private fun filterCards(query: String) {
        val filteredCards = allCards.filter {
            it.name.contains(query, ignoreCase = true)
        }
        adapter.updateData(filteredCards)
    }
}
