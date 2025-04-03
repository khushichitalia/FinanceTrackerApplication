package com.example.financetrackerapplication.repository

import com.example.financetrackerapplication.models.TransactionResponse
import retrofit2.Call
import retrofit2.http.GET

interface PlaidBackendService {
    @GET("api/transactions")
    fun getTransactions(): Call<TransactionResponse>
}