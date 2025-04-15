package com.example.financetrackerapplication.repository

import android.content.Context
import android.util.Log
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import com.example.financetrackerapplication.models.Transaction
import com.example.financetrackerapplication.models.AccountRequest
import com.example.financetrackerapplication.models.AccountResponse
import com.example.financetrackerapplication.models.LinkedAccount

class PlaidRepository(private val context: Context) {
    private val retrofit = Retrofit.Builder()
        .baseUrl("http://10.0.2.2:3000/") // backend URL
        .addConverterFactory(GsonConverterFactory.create())
        .build()

    private val apiService = retrofit.create(PlaidApi::class.java)
    private val plaidService: PlaidBackendService = retrofit.create(PlaidBackendService::class.java)

    fun exchangePublicToken(publicToken: String, callback: (String?) -> Unit) {
        val call = apiService.exchangePublicToken(ExchangeTokenRequest(publicToken))
        call.enqueue(object : Callback<ExchangeTokenResponse> {
            override fun onResponse(call: Call<ExchangeTokenResponse>, response: Response<ExchangeTokenResponse>) {
                if (response.isSuccessful) {
                    val accessToken = response.body()?.access_token
                    callback(accessToken)
                } else {
                    Log.e("PlaidRepository", "Error exchanging token")
                    callback(null)
                }
            }

            override fun onFailure(call: Call<ExchangeTokenResponse>, t: Throwable) {
                Log.e("PlaidRepository", "Network error: ${t.message}")
                callback(null)
            }
        })
    }

    suspend fun getTransactions(accessToken: String): List<Transaction> {
        return try {
            val response = plaidService.getTransactions().execute()
            Log.d("PlaidRepository", "Response success: ${response.isSuccessful}")

            if (response.isSuccessful) {
                val transactions = response.body()?.transactions ?: emptyList()
                Log.d("PlaidRepository", "Parsed ${transactions.size} transactions")
                transactions
            } else {
                val errorJson = response.errorBody()?.string()
                Log.e("PlaidRepository", "Error body: $errorJson")
                emptyList()
            }
        } catch (e: Exception) {
            Log.e("PlaidRepository", "Exception: ", e)
            emptyList()
        }
    }

    // ✅ NEW FUNCTION: Get accounts linked to access token
    fun getAccounts(accessToken: String, callback: (List<LinkedAccount>) -> Unit) {
        val call = apiService.getAccounts(AccountRequest(accessToken))
        call.enqueue(object : Callback<AccountResponse> {
            override fun onResponse(call: Call<AccountResponse>, response: Response<AccountResponse>) {
                if (response.isSuccessful) {
                    val accounts = response.body()?.accounts ?: emptyList()
                    callback(accounts)
                } else {
                    Log.e("PlaidRepository", "Failed to fetch accounts")
                    callback(emptyList())
                }
            }

            override fun onFailure(call: Call<AccountResponse>, t: Throwable) {
                Log.e("PlaidRepository", "Network error: ${t.message}")
                callback(emptyList())
            }
        })
    }
}
