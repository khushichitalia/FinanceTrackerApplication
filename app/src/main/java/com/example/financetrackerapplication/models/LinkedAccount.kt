package com.example.financetrackerapplication.models

data class LinkedAccount(
    val account_id: String,
    val name: String,
    val institution_name: String? = null
)

data class AccountRequest(
    val access_token: String
)

data class AccountResponse(
    val accounts: List<LinkedAccount>
)

