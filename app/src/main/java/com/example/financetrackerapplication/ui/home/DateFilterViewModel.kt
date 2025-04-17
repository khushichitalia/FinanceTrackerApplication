package com.example.financetrackerapplication.ui.home

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class DateFilterViewModel : ViewModel() {

    // Pair of (month, year), where month is 0-based (January = 0)
    private val _monthYear = MutableLiveData<Pair<Int, Int>>()
    val monthYear: LiveData<Pair<Int, Int>> = _monthYear

    init {
        // Default to current month/year
        val now = java.util.Calendar.getInstance()
        _monthYear.value = Pair(now.get(java.util.Calendar.MONTH), now.get(java.util.Calendar.YEAR))
    }

    fun setMonthYear(month: Int, year: Int) {
        _monthYear.value = Pair(month, year)
    }
}
