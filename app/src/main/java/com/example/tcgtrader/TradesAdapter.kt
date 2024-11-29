package com.example.tcgtrader

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class TradesAdapter(private val trades: List<String>) :
    RecyclerView.Adapter<TradesAdapter.TradeViewHolder>() {

    // ViewHolder: Represents a single item view
    class TradeViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val tradeName: TextView = itemView.findViewById(R.id.trade_name)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TradeViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_trade, parent, false)
        return TradeViewHolder(view)
    }

    override fun onBindViewHolder(holder: TradeViewHolder, position: Int) {
        holder.tradeName.text = trades[position]
    }

    override fun getItemCount(): Int = trades.size
}
