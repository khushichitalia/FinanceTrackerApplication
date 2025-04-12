package com.example.financetrackerapplication.ui.dashboard

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.financetrackerapplication.databinding.ItemTransactionBinding
import com.example.financetrackerapplication.models.Transaction

class TransactionsAdapter(private var transactions: List<Transaction>) :
    RecyclerView.Adapter<TransactionsAdapter.TransactionViewHolder>() {

    inner class TransactionViewHolder(private val binding: ItemTransactionBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(transaction: Transaction) {
            binding.transactionName.text = transaction.name
            binding.transactionAmount.text = "$${"%.2f".format(transaction.amount)}"
            binding.transactionDate.text = transaction.date

            // Color amount based on value
            val amountColor = if (transaction.amount >= 0) {
                itemView.context.getColor(android.R.color.holo_green_dark)
            } else {
                itemView.context.getColor(android.R.color.holo_red_dark)
            }
            binding.transactionAmount.setTextColor(amountColor)
        }

    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TransactionViewHolder {
        val binding =
            ItemTransactionBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return TransactionViewHolder(binding)
    }

    override fun onBindViewHolder(holder: TransactionViewHolder, position: Int) {
        holder.bind(transactions[position])
    }

    override fun getItemCount(): Int = transactions.size

    fun updateTransactions(newTransactions: List<Transaction>) {
        transactions = newTransactions
        notifyDataSetChanged()
    }
}
