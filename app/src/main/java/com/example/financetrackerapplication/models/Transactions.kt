package com.example.financetrackerapplication.models

data class Transaction(
    val transaction_id: String,
    val amount: Double,
    val date: String,
    val name: String
)

data class TransactionResponse(
    val transactions: List<Transaction>
)
