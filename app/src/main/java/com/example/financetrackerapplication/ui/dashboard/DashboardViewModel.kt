package com.example.financetrackerapplication.ui.dashboard

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.example.financetrackerapplication.database.DatabaseHelper
import com.example.financetrackerapplication.models.Transaction
import com.example.financetrackerapplication.repository.PlaidRepository // Import correctly
import kotlinx.coroutines.launch

class DashboardViewModel(application: Application) : AndroidViewModel(application) {
    private val _transactions = MutableLiveData<List<Transaction>>()
    val transactions: LiveData<List<Transaction>> get() = _transactions
    private val dbHelper: DatabaseHelper = DatabaseHelper(application)
    private val plaidRepository: PlaidRepository = PlaidRepository(application) // Correct instantiation

    fun fetchTransactionsFromAPI(accessToken: String) {
        viewModelScope.launch {
            val transactionsList = plaidRepository.getTransactions(accessToken) // Use instance variable

            if (transactionsList.isNotEmpty()) { // Check if transactionsList is not empty
                _transactions.postValue(transactionsList)

                // Save transactions safely
                transactionsList.forEach { transaction ->
                    dbHelper.insertTransaction(transaction)
                }
            }
        }
    }

    fun fetchTransactionsFromDB() {
        viewModelScope.launch {
            _transactions.postValue(dbHelper.getAllTransactions())
        }
    }
}
