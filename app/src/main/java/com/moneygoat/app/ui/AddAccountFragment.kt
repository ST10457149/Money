package com.moneygoat.app.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.moneygoat.app.R
import com.moneygoat.app.data.entity.Account
import com.moneygoat.app.viewmodel.AccountViewModel

class AddAccountFragment : Fragment() {
    private lateinit var viewModel: AccountViewModel

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_add_account, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        viewModel = ViewModelProvider(this)[AccountViewModel::class.java]
        val userId = (requireActivity() as MainActivity).userId

        val etName = view.findViewById<EditText>(R.id.etAccountName)
        val spinnerType = view.findViewById<Spinner>(R.id.spinnerAccountType)
        val etBalance = view.findViewById<EditText>(R.id.etInitialBalance)
        val btnAdd = view.findViewById<Button>(R.id.btnAddAccount)

        // Populate Account Types
        val types = listOf("BANK", "INVESTMENT", "LOAN", "BOND", "CASH", "CREDIT_CARD")
        val adapter = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_item, types)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinnerType.adapter = adapter

        btnAdd.setOnClickListener {
            val name = etName.text.toString().trim()
            val type = spinnerType.selectedItem.toString()
            val balanceStr = etBalance.text.toString().trim()

            if (name.isEmpty() || balanceStr.isEmpty()) {
                Toast.makeText(requireContext(), "Please fill all fields", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val balance = balanceStr.toDoubleOrNull() ?: 0.0
            val account = Account(userId = userId, name = name, type = type, balance = balance)
            
            viewModel.addAccount(account)
            Toast.makeText(requireContext(), "Account added", Toast.LENGTH_SHORT).show()
            parentFragmentManager.popBackStack()
        }
    }
}
