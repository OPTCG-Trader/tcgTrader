package com.example.tcgtrader

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class DesiredAdapter(
    private val cards: MutableList<Card>,
    private val deleteCallback: (Card) -> Unit // Callback for delete action
) : RecyclerView.Adapter<DesiredAdapter.DesiredViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): DesiredViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_trade_desired, parent, false)
        return DesiredViewHolder(view)
    }

    override fun onBindViewHolder(holder: DesiredViewHolder, position: Int) {
        val card = cards[position]
        holder.bind(card)

        // Set long-press listener for delete action
        holder.itemView.setOnLongClickListener {
            deleteCallback(card) // Trigger delete callback with the selected card
            true
        }
    }

    override fun getItemCount(): Int = cards.size

    class DesiredViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
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
