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
    private lateinit var transactionsAdapter: TransactionsAdapter

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        dashboardViewModel = ViewModelProvider(requireActivity())[DashboardViewModel::class.java]
        _binding = FragmentDashboardBinding.inflate(inflater, container, false)
        val root: View = binding.root

        // Initialize RecyclerView
        transactionsAdapter = TransactionsAdapter(emptyList())
        binding.transactionsRecyclerView.layoutManager = LinearLayoutManager(requireContext())
        binding.transactionsRecyclerView.adapter = transactionsAdapter

        // Observe transactions from ViewModel
        dashboardViewModel.transactions.observe(viewLifecycleOwner) { transactions ->
            transactionsAdapter.updateTransactions(transactions)
        }

        // Get stored access token
        val accessToken = SharedPrefUtils.getAccessToken(requireContext())

        if (accessToken != null) {
            // If access token exists, fetch transactions
            dashboardViewModel.fetchTransactionsFromDB()
            dashboardViewModel.fetchTransactionsFromAPI(accessToken)
        } else {
            // Show message that account needs linking if no access token
            Toast.makeText(requireContext(), "Please link your account on the Home Page", Toast.LENGTH_LONG).show()
        }

        return root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
