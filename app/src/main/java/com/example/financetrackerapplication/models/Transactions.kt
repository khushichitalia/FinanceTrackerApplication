package com.example.financetrackerapplication.models

data class Transaction(
    val transactionId: String,
    val amount: Double,
    val date: String,
    val name: String
)
