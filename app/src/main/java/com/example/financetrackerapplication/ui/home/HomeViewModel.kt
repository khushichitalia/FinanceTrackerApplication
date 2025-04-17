package com.example.financetrackerapplication.ui.home

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.example.financetrackerapplication.database.DatabaseHelper
import com.example.financetrackerapplication.models.Transaction

class HomeViewModel(application: Application) : AndroidViewModel(application) {

    private val dbHelper = DatabaseHelper(application)

    private val _transactions = MutableLiveData<List<Transaction>>()
    val transactions: LiveData<List<Transaction>> = _transactions

    private val _budget = MutableLiveData<Double>()
    val budget: LiveData<Double> = _budget

    fun loadTransactions(month: Int, year: Int) {
        _transactions.value = dbHelper.getTransactionsByMonthYear(month, year)
    }

    fun loadBudget(month: Int, year: Int) {
        _budget.value = dbHelper.getBudget(month, year)
    }

    fun saveBudget(month: Int, year: Int, amount: Double) {
        dbHelper.insertOrUpdateBudget(month, year, amount)
        loadBudget(month, year)
    }
}
