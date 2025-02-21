package com.example.financetrackerapplication.repository

import retrofit2.http.Body
import retrofit2.http.POST
import retrofit2.Call

interface PlaidApi {
    @POST("exchange_public_token") // API to exchange `public_token` for `access_token`
    fun exchangePublicToken(@Body request: ExchangeTokenRequest): Call<ExchangeTokenResponse>

    @POST("get_transactions") // API to fetch transactions
    suspend fun transactionsGet(@Body request: TransactionsGetRequest): TransactionsGetResponse
}
