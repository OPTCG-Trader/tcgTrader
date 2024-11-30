package com.example.tcgtrader

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class TradesAdapter(
    private val cards: MutableList<Card>,
    private val deleteCallback: (Card) -> Unit
) : RecyclerView.Adapter<TradesAdapter.TradesViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TradesViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_trade_desired, parent, false)
        return TradesViewHolder(view)
    }

    override fun onBindViewHolder(holder: TradesViewHolder, position: Int) {
        val card = cards[position]
        holder.bind(card)

        holder.itemView.setOnLongClickListener {
            deleteCallback(card)
            true
        }
    }

    override fun getItemCount(): Int = cards.size

    class TradesViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val cardId: TextView = itemView.findViewById(R.id.card_id)
        private val cardName: TextView = itemView.findViewById(R.id.card_name)
        private val cardRarity: TextView = itemView.findViewById(R.id.card_rarity)
        private val cardColor: TextView = itemView.findViewById(R.id.card_color)
        private val cardMarketPrice: TextView = itemView.findViewById(R.id.card_market_price)

        fun bind(card: Card) {
            cardId.text = "ID: ${card.id}"
            cardName.text = card.name
            cardRarity.text = "Rarity: ${card.rarity ?: "Unknown"}"
            cardColor.text = "Color: ${card.color ?: "Unknown"}"
            cardMarketPrice.text = "Price: $${card.marketPrice ?: 0.0}"
        }
    }
}
