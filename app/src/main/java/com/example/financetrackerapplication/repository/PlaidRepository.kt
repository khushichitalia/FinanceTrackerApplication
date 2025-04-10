package com.example.financetrackerapplication.repository

import android.content.Context
import android.util.Log
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import com.example.financetrackerapplication.models.Transaction

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

//    suspend fun getTransactions(accessToken: String): List<Transaction> {
//        return try {
//            val response = apiService.transactionsGet(
//                TransactionsGetRequest(
//                    access_token = accessToken,
//                    start_date = "2000-01-01",
//                    end_date = "2025-12-31"
//                )
//            )
//            response.transactions
//        } catch (e: Exception) {
//            e.printStackTrace()
//            emptyList()
//        }
//    }

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
}
