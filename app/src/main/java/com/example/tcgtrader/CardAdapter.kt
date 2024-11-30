package com.example.tcgtrader

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class CardAdapter(
    private var cards: List<Card>,
    private val onAddToTrades: (Card) -> Unit,
    private val onAddToDesired: (Card) -> Unit
) : RecyclerView.Adapter<CardAdapter.CardViewHolder>() {

    class CardViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val cardName: TextView = itemView.findViewById(R.id.card_name)
        val cardMarketPrice: TextView = itemView.findViewById(R.id.card_market_price)
        val cardText: TextView = itemView.findViewById(R.id.card_text)
        val cardSetId: TextView = itemView.findViewById(R.id.card_set_id)
        val cardColor: TextView = itemView.findViewById(R.id.card_color)
        val cardType: TextView = itemView.findViewById(R.id.card_type)
        val addToTrades: Button = itemView.findViewById(R.id.add_to_trades)
        val addToDesired: Button = itemView.findViewById(R.id.add_to_desired)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CardViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_card, parent, false)
        return CardViewHolder(view)
    }

    @SuppressLint("SetTextI18n")
    override fun onBindViewHolder(holder: CardViewHolder, position: Int) {
        val card = cards[position]
        holder.cardName.text = card.name
        holder.cardMarketPrice.text = "Market Price: $${card.marketPrice}"
        holder.cardText.text = "Text: ${card.cardText}"
        holder.cardSetId.text = "Set ID: ${card.id}"
        holder.cardColor.text = "Color: ${card.color}"
        holder.cardType.text = "Type: ${card.cardType}"

        // Handle button clicks
        holder.addToTrades.setOnClickListener { onAddToTrades(card) }
        holder.addToDesired.setOnClickListener { onAddToDesired(card) }
    }

    override fun getItemCount(): Int = cards.size

    fun updateData(newCards: List<Card>) {
        cards = newCards
        notifyDataSetChanged()
    }
}
