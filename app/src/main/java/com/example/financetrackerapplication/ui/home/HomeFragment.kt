package com.example.financetrackerapplication.ui.home

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.example.financetrackerapplication.databinding.FragmentHomeBinding
import com.example.financetrackerapplication.repository.PlaidRepository
import com.example.financetrackerapplication.utils.SharedPrefUtils
import com.plaid.link.OpenPlaidLink
import com.plaid.link.linkTokenConfiguration
import com.plaid.link.result.LinkExit
import com.plaid.link.result.LinkSuccess
import java.util.Calendar

class HomeFragment : Fragment() {

    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!
    private lateinit var plaidRepository: PlaidRepository

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

                    // Exchange the `public_token` for `access_token`
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
        val homeViewModel = ViewModelProvider(this).get(HomeViewModel::class.java)
        _binding = FragmentHomeBinding.inflate(inflater, container, false)
        val root: View = binding.root

        plaidRepository = PlaidRepository(requireContext())

        val linkButton: Button = binding.openLink
        linkButton.setOnClickListener {
            val linkTokenConfiguration = linkTokenConfiguration {
                token = "link-sandbox-2426abfa-9080-494a-b453-565edcf64bcf"
            }
            linkAccountToPlaid.launch(linkTokenConfiguration)
        }

        binding.monthYearInput.setOnClickListener {
            val monthYearPicker = MonthYearPickerDialog()
            monthYearPicker.setListener { _, year, month, _ ->
                val calendar = Calendar.getInstance()
                calendar.set(Calendar.YEAR, year)
                calendar.set(Calendar.MONTH, month)

                val formattedDate = android.text.format.DateFormat.format("MMMM yyyy", calendar.time)
                binding.monthYearInput.setText(formattedDate.toString())
            }
            monthYearPicker.show(parentFragmentManager, "MonthYearPickerDialog")
        }

        return root
    }

    private fun exchangePublicTokenForAccessToken(publicToken: String) {
        plaidRepository.exchangePublicToken(publicToken) { accessToken ->
            if (accessToken != null) {
                // Save `access_token` securely
                SharedPrefUtils.saveAccessToken(requireContext(), accessToken)
                Toast.makeText(requireContext(), "Bank Account Linked!", Toast.LENGTH_SHORT).show()
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
