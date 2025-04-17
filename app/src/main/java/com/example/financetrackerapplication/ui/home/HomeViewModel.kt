package com.example.financetrackerapplication.ui.home

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.example.financetrackerapplication.database.DatabaseHelper
import com.example.financetrackerapplication.models.Transaction

class HomeViewModel(application: Application) : AndroidViewModel(application) {

    private val _transactions = MutableLiveData<List<Transaction>>()
    val transactions: LiveData<List<Transaction>> = _transactions

    private val dbHelper = DatabaseHelper(application)

    fun loadTransactions(month: Int, year: Int) {
        _transactions.value = dbHelper.getTransactionsByMonthYear(month, year)
    }
}
