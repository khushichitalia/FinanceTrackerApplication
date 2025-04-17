package com.example.financetrackerapplication.ui.dashboard

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.financetrackerapplication.databinding.FragmentDashboardBinding
import com.example.financetrackerapplication.ui.home.DateFilterViewModel
import com.example.financetrackerapplication.ui.home.MonthYearPickerDialog
import com.example.financetrackerapplication.utils.SharedPrefUtils
import java.util.Calendar

class DashboardFragment : Fragment() {

    private var _binding: FragmentDashboardBinding? = null
    private val binding get() = _binding!!
    private lateinit var dashboardViewModel: DashboardViewModel
    private val dateFilterViewModel: DateFilterViewModel by activityViewModels()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        dashboardViewModel = ViewModelProvider(requireActivity())[DashboardViewModel::class.java]
        _binding = FragmentDashboardBinding.inflate(inflater, container, false)
        val root: View = binding.root

        val incomeAdapter = TransactionsAdapter(emptyList())
        val expenseAdapter = TransactionsAdapter(emptyList())

        binding.incomeRecyclerView.layoutManager = LinearLayoutManager(requireContext())
        binding.expenseRecyclerView.layoutManager = LinearLayoutManager(requireContext())

        binding.incomeRecyclerView.adapter = incomeAdapter
        binding.expenseRecyclerView.adapter = expenseAdapter

        // Month-Year Picker Click
        binding.monthYearInput.setOnClickListener {
            val monthYearPicker = MonthYearPickerDialog()
            monthYearPicker.setListener { _, year, month, _ ->
                val calendar = Calendar.getInstance().apply {
                    set(Calendar.YEAR, year)
                    set(Calendar.MONTH, month)
                }
                val formattedDate = android.text.format.DateFormat.format("MMMM yyyy", calendar.time)
                binding.monthYearInput.setText(formattedDate.toString())

                // Share selection across fragments
                dateFilterViewModel.setMonthYear(month, year)
            }
            monthYearPicker.show(parentFragmentManager, "MonthYearPickerDialog")
        }

        // Observe Shared DateFilterViewModel
        dateFilterViewModel.monthYear.observe(viewLifecycleOwner) { (month, year) ->
            val calendar = Calendar.getInstance().apply {
                set(Calendar.YEAR, year)
                set(Calendar.MONTH, month)
            }
            val formattedDate = android.text.format.DateFormat.format("MMMM yyyy", calendar.time)
            binding.monthYearInput.setText(formattedDate.toString())

            dashboardViewModel.fetchTransactionsByMonthYear(month + 1, year)
        }

        dashboardViewModel.transactions.observe(viewLifecycleOwner) { transactions ->
            val income = transactions.filter {
                isIncomeCategory(it.category?.firstOrNull()) || isIncomeName(it.name)
            }

            val expenses = transactions.filter {
                !isIncomeCategory(it.category?.firstOrNull()) && !isIncomeName(it.name)
            }

            val totalIncome = income.sumOf { it.amount }
            val totalExpenses = expenses.sumOf { kotlin.math.abs(it.amount) }

            binding.incomeHeader.text = "Income: $%.2f".format(totalIncome)
            binding.expenseHeader.text = "Expenses: $%.2f".format(totalExpenses)

            incomeAdapter.updateTransactions(income)
            expenseAdapter.updateTransactions(expenses)
        }

        val accessToken = SharedPrefUtils.getAccessToken(requireContext())
        if (accessToken != null) {
            // Don't fetch unfiltered list — let observer trigger the filtered one
            dashboardViewModel.fetchTransactionsFromAPI(accessToken)
        } else {
            Toast.makeText(requireContext(), "Please link your account on the Home Page", Toast.LENGTH_LONG).show()
        }

        return root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private fun isExpenseName(name: String?): Boolean {
        val normalized = name?.lowercase() ?: return false
        return listOf("purchase", "payment", "withdrawal", "debit", "uber", "target", "transfer out").any {
            normalized.contains(it)
        }
    }

    private fun isIncomeCategory(category: String?): Boolean {
        val normalized = category?.lowercase() ?: return false
        return normalized == "income" || normalized == "transfer in"
    }

    private fun isIncomeName(name: String?): Boolean {
        val normalized = name?.lowercase() ?: return false
        return listOf("deposit", "paycheck", "refund", "payout", "transfer in", "income", "received").any {
            normalized.contains(it)
        }
    }
}
