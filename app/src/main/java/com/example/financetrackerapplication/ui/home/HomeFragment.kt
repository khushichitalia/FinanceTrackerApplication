package com.example.financetrackerapplication.ui.home

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.ViewModelProvider
import com.example.financetrackerapplication.databinding.FragmentHomeBinding
import com.example.financetrackerapplication.models.LinkedAccount
import com.example.financetrackerapplication.repository.PlaidRepository
import com.example.financetrackerapplication.utils.SharedPrefUtils
import com.example.financetrackerapplication.ui.authentication.SignInActivity
import com.google.firebase.auth.FirebaseAuth
import com.plaid.link.OpenPlaidLink
import com.plaid.link.linkTokenConfiguration
import com.plaid.link.result.LinkExit
import com.plaid.link.result.LinkSuccess
import java.util.Calendar

class HomeFragment : Fragment() {

    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!
    private lateinit var homeViewModel: HomeViewModel
    private lateinit var plaidRepository: PlaidRepository
    private val dateFilterViewModel: DateFilterViewModel by activityViewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        activity?.actionBar?.hide()
    }

    private val linkAccountToPlaid =
        registerForActivityResult(OpenPlaidLink()) {
            when (it) {
                is LinkSuccess -> {
                    val publicToken = it.publicToken
                    Log.i("Plaid", "Public Token: $publicToken")
                    exchangePublicTokenForAccessToken(publicToken)
                }
                is LinkExit -> {
                    Toast.makeText(requireContext(), "Plaid Linking Failed", Toast.LENGTH_SHORT).show()
                }
            }
        }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentHomeBinding.inflate(inflater, container, false)
        val root: View = binding.root

        homeViewModel = ViewModelProvider(this)[HomeViewModel::class.java]
        plaidRepository = PlaidRepository(requireContext())

        binding.openLink.setOnClickListener {
            val linkTokenConfiguration = linkTokenConfiguration {
                token = "link-sandbox-546f12e0-b2f2-4dd4-9ddb-67e67bc6f551"
            }
            linkAccountToPlaid.launch(linkTokenConfiguration)
        }

        binding.monthYearInput.setOnClickListener {
            val picker = MonthYearPickerDialog()
            picker.setListener { _, year, month, _ ->
                val calendar = Calendar.getInstance().apply {
                    set(Calendar.YEAR, year)
                    set(Calendar.MONTH, month)
                }
                val formatted = android.text.format.DateFormat.format("MMMM yyyy", calendar.time)
                binding.monthYearInput.setText(formatted)
                dateFilterViewModel.setMonthYear(month, year)
            }
            picker.show(parentFragmentManager, "MonthYearPickerDialog")
        }

        // Load data when month/year changes
        dateFilterViewModel.monthYear.observe(viewLifecycleOwner) { (month, year) ->
            homeViewModel.loadTransactions(month + 1, year)
            homeViewModel.loadBudget(month + 1, year)
        }

        homeViewModel.budget.observe(viewLifecycleOwner) { amount ->
            binding.budgetInput.setText(if (amount == 0.0) "" else "%.2f".format(amount))
        }

        binding.budgetInput.setOnFocusChangeListener { _, hasFocus ->
            if (!hasFocus) {
                val text = binding.budgetInput.text.toString()
                val amount = text.toDoubleOrNull() ?: 0.0
                dateFilterViewModel.monthYear.value?.let { (month, year) ->
                    homeViewModel.saveBudget(month + 1, year, amount)
                }
            }
        }

        // Log Out Button Click
        binding.logoutButton.setOnClickListener {
            FirebaseAuth.getInstance().signOut()
            Toast.makeText(requireContext(), "Logged out", Toast.LENGTH_SHORT).show()

            val intent = Intent(requireContext(), SignInActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            startActivity(intent)
        }

        return root
    }

    private fun exchangePublicTokenForAccessToken(publicToken: String) {
        plaidRepository.exchangePublicToken(publicToken) { accessToken ->
            if (accessToken != null) {
                SharedPrefUtils.saveAccessToken(requireContext(), accessToken)

                plaidRepository.getAccounts(accessToken) { accounts ->
                    val dbHelper = com.example.financetrackerapplication.database.DatabaseHelper(requireContext())
                    var newLinkCount = 0

                    accounts.forEach { account ->
                        if (dbHelper.insertLinkedAccount(account.account_id, account.name, account.institution_name ?: "Unknown")) {
                            newLinkCount++
                        }
                    }

                    val message = if (newLinkCount > 0) {
                        "$newLinkCount new account(s) linked!"
                    } else {
                        "No new accounts linked (already added)"
                    }
                    Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show()
                }
            } else {
                Toast.makeText(requireContext(), "Failed to exchange token", Toast.LENGTH_SHORT).show()
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
