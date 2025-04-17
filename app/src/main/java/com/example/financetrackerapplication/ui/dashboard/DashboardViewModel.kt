package com.example.financetrackerapplication.ui.dashboard

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.example.financetrackerapplication.database.DatabaseHelper
import com.example.financetrackerapplication.models.Transaction
import com.example.financetrackerapplication.repository.PlaidRepository
import kotlinx.coroutines.launch
import android.util.Log
import kotlinx.coroutines.Dispatchers

class DashboardViewModel(application: Application) : AndroidViewModel(application) {
    private val _transactions = MutableLiveData<List<Transaction>>()
    val transactions: LiveData<List<Transaction>> get() = _transactions

    private val dbHelper: DatabaseHelper = DatabaseHelper(application)
    private val plaidRepository: PlaidRepository = PlaidRepository(application)

    fun fetchTransactionsFromAPI(accessToken: String) {
        viewModelScope.launch(Dispatchers.IO) {
            val transactionsList = plaidRepository.getTransactions(accessToken)

            transactionsList.forEach {
                Log.d("DEBUG_TRANSACTIONS", "${it.name}: ${it.amount}, category: ${it.category}")
            }

            Log.d("DashboardVM", "Fetched ${transactionsList.size} transactions from API")

            // ❗️ONLY insert into database, do NOT set _transactions directly
            transactionsList.forEach { transaction ->
                dbHelper.insertTransaction(transaction)
            }

            // ✅ Let observer re-trigger filtered fetch afterward
        }
    }

    fun fetchTransactionsFromDB() {
        viewModelScope.launch {
            _transactions.postValue(dbHelper.getAllTransactions())
        }
    }

    fun fetchTransactionsByMonthYear(month: Int, year: Int) {
        viewModelScope.launch {
            Log.d("TXN_FILTER", "Filtering for $month/$year")
            _transactions.postValue(dbHelper.getTransactionsByMonthYear(month, year))
        }
    }
}
