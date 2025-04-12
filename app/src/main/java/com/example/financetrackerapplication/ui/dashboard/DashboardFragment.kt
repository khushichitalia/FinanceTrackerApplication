package com.example.financetrackerapplication.ui.dashboard

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.financetrackerapplication.databinding.FragmentDashboardBinding
import com.example.financetrackerapplication.repository.PlaidRepository
import com.example.financetrackerapplication.utils.SharedPrefUtils

class DashboardFragment : Fragment() {

    private var _binding: FragmentDashboardBinding? = null
    private val binding get() = _binding!!
    private lateinit var dashboardViewModel: DashboardViewModel

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

        dashboardViewModel.transactions.observe(viewLifecycleOwner) { transactions ->
            val income = transactions.filter { it.amount >= 0 }
            val expenses = transactions.filter { it.amount < 0 }

            val totalIncome = income.sumOf { it.amount }
            val totalExpenses = expenses.sumOf { -it.amount }

            binding.incomeHeader.text = "Income: $%.2f".format(totalIncome)
            binding.expenseHeader.text = "Expenses: $%.2f".format(totalExpenses)

            incomeAdapter.updateTransactions(income)
            expenseAdapter.updateTransactions(expenses)
        }

        val accessToken = SharedPrefUtils.getAccessToken(requireContext())
        if (accessToken != null) {
            dashboardViewModel.fetchTransactionsFromDB()
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
}
