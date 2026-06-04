package com.moneygoat.app.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.moneygoat.app.data.entity.Account
import com.moneygoat.app.viewmodel.AccountViewModel
import com.moneygoat.app.R

import com.moneygoat.app.adapter.AccountAdapter

class AccountsFragment : Fragment() {
    private lateinit var accountVM: AccountViewModel
    private lateinit var adapter: AccountAdapter

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_accounts, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val userId = (requireActivity() as MainActivity).userId
        accountVM = ViewModelProvider(this)[AccountViewModel::class.java]

        val tvNetWorth = view.findViewById<TextView>(R.id.tvNetWorth)
        val rvAccounts = view.findViewById<RecyclerView>(R.id.rvAccounts)
        val btnRecurring = view.findViewById<View>(R.id.btnRecurringBills)

        btnRecurring.setOnClickListener {
            // Placeholder for list, but let's go straight to adding for now to show next step
            parentFragmentManager.beginTransaction()
                .replace(R.id.fragmentContainer, AddRecurringFragment())
                .addToBackStack(null)
                .commit()
        }
        
        adapter = AccountAdapter()
        rvAccounts.layoutManager = LinearLayoutManager(requireContext())
        rvAccounts.adapter = adapter
        
        val fabAdd = view.findViewById<View>(R.id.fabAddAccount)
        fabAdd.setOnClickListener {
            parentFragmentManager.beginTransaction()
                .replace(R.id.fragmentContainer, AddAccountFragment())
                .addToBackStack(null)
                .commit()
        }
        
        accountVM.getTotalBalance(userId).observe(viewLifecycleOwner) { total ->
            tvNetWorth.text = "Net Worth: R %.2f".format(total ?: 0.0)
        }
        
        accountVM.getAccounts(userId).observe(viewLifecycleOwner) { accounts ->
            adapter.submitList(accounts)
        }
    }
}
