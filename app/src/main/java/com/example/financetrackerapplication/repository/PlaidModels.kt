package com.example.financetrackerapplication.repository
import com.example.financetrackerapplication.models.Transaction

// Request model for exchanging a public token for an access token
data class ExchangeTokenRequest(val public_token: String)

// Response model for getting the access token
data class ExchangeTokenResponse(val access_token: String)

// Request model for fetching transactions
data class TransactionsGetRequest(
    val access_token: String,
    val start_date: String,
    val end_date: String
)

// Response model for fetching transactions
data class TransactionsGetResponse(
    val transactions: List<Transaction>
)

// Data model for a single transaction
data class Transaction(
    val transaction_id: String,
    val amount: Double,
    val date: String,
    val name: String
)
