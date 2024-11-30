package com.example.tcgtrader

data class Card(
    val id: String = "",
    val name: String = "",
    val rarity: String? = null,
    val color: String? = null,
    val cardText: String? = null,
    val cardType: String? = null,
    val marketPrice: Double? = 0.0
)