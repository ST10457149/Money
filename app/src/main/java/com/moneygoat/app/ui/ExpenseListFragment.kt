package com.moneygoat.app.ui

import android.app.DatePickerDialog
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.core.content.FileProvider
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.moneygoat.app.R
import com.moneygoat.app.adapter.ExpenseAdapter
import com.moneygoat.app.viewmodel.ExpenseViewModel
import java.io.File
import java.io.FileOutputStream
import java.util.Calendar

/**
 * Fragment that displays a full list of expenses.
 * Allows users to filter the list by a custom date range.
 */
class ExpenseListFragment : Fragment() {
    private lateinit var expenseVM: ExpenseViewModel
    private lateinit var adapter: ExpenseAdapter
    private var startDate = ""
    private var endDate = ""

    override fun onCreateView(inflater: LayoutInflater, c: ViewGroup?, s: Bundle?): View? = 
        inflater.inflate(R.layout.fragment_expense_list, c, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val userId = (requireActivity() as MainActivity).userId
        expenseVM = ViewModelProvider(this)[ExpenseViewModel::class.java]
        adapter = ExpenseAdapter(requireContext())

        val btnStart = view.findViewById<Button>(R.id.btnFilterStart)
        val btnEnd = view.findViewById<Button>(R.id.btnFilterEnd)
        val btnApply = view.findViewById<Button>(R.id.btnApplyFilter)
        val rv = view.findViewById<RecyclerView>(R.id.rvExpenses)
        val tvEmpty = view.findViewById<TextView>(R.id.tvEmptyList)
        val etSearch = view.findViewById<EditText>(R.id.etSearchExpense)
        val btnExport = view.findViewById<Button>(R.id.btnExportCsv)
        val tvSummary = view.findViewById<TextView>(R.id.tvListSummary)
        val tvCount = view.findViewById<TextView>(R.id.tvListCount)

        // Setup RecyclerView
        rv.layoutManager = LinearLayoutManager(requireContext())
        rv.adapter = adapter
        
        // Initialize date range to current month
        val cal = Calendar.getInstance()
        cal.set(Calendar.DAY_OF_MONTH, 1)
        startDate = "%04d-%02d-%02d".format(cal.get(Calendar.YEAR), cal.get(Calendar.MONTH) + 1, 1)
        cal.set(Calendar.DAY_OF_MONTH, cal.getActualMaximum(Calendar.DAY_OF_MONTH))
        endDate = "%04d-%02d-%02d".format(cal.get(Calendar.YEAR), cal.get(Calendar.MONTH) + 1, cal.get(Calendar.DAY_OF_MONTH))
        
        btnStart.text = startDate
        btnEnd.text = endDate

        // Start Date selection
        btnStart.setOnClickListener { 
            val c = Calendar.getInstance()
            DatePickerDialog(requireContext(), { _, y, m, d -> 
                startDate = "%04d-%02d-%02d".format(y, m + 1, d)
                btnStart.text = startDate 
            }, c.get(Calendar.YEAR), c.get(Calendar.MONTH), c.get(Calendar.DAY_OF_MONTH)).show() 
        }
        
        // End Date selection
        btnEnd.setOnClickListener { 
            val c = Calendar.getInstance()
            DatePickerDialog(requireContext(), { _, y, m, d -> 
                endDate = "%04d-%02d-%02d".format(y, m + 1, d)
                btnEnd.text = endDate 
            }, c.get(Calendar.YEAR), c.get(Calendar.MONTH), c.get(Calendar.DAY_OF_MONTH)).show() 
        }
        
        // Apply filter button
        btnApply.setOnClickListener { loadData(userId, tvEmpty, tvSummary, tvCount) }

        // Search functionality with ViewModel-driven logic
        etSearch.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {
                expenseVM.setSearchQuery(s.toString().trim())
            }
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
        })

        // CSV Export functionality
        btnExport.setOnClickListener {
            exportExpensesToCsv(userId)
        }
        
        // Initial data load and observation
        expenseVM.setDateFilter(userId, startDate, endDate)
        expenseVM.filteredExpensesWithCategory.observe(viewLifecycleOwner) { expenses ->
            adapter.submitList(expenses)
            tvEmpty.visibility = if (expenses.isEmpty()) View.VISIBLE else View.GONE
            updateSummary(expenses.map { it.expense }, tvSummary, tvCount)
        }
    }

    /**
     * Updates the ViewModel's filter.
     */
    private fun loadData(userId: Long, tvEmpty: TextView, tvSummary: TextView, tvCount: TextView) {
        expenseVM.setDateFilter(userId, startDate, endDate)
    }

    private fun updateSummary(expenses: List<com.moneygoat.app.data.entity.Expense>, tvSummary: TextView, tvCount: TextView) {
        val total = expenses.sumOf { it.amount }
        tvSummary.text = "Total for period: R %.2f".format(total)
        tvCount.text = "${expenses.size} items"
    }

    /**
     * Exports the filtered expenses to a CSV file and shares it.
     */
    private fun exportExpensesToCsv(userId: Long) {
        val expenseItems = adapter.currentList
        if (expenseItems.isEmpty()) {
            Toast.makeText(requireContext(), "No data to export", Toast.LENGTH_SHORT).show()
            return
        }

        try {
            val fileName = "MoneyGoat_Expenses_${System.currentTimeMillis()}.csv"
            val file = File(requireContext().cacheDir, fileName)
            val fos = FileOutputStream(file)
            
            // CSV Header
            fos.write("Date,Time,Description,Amount,Category\n".toByteArray())
            
            // CSV Data
            expenseItems.forEach { item ->
                val exp = item.expense
                val catName = item.category?.name ?: "Uncategorized"
                val line = "${exp.date},${exp.startTime}-${exp.endTime},${exp.description},${exp.amount},$catName\n"
                fos.write(line.toByteArray())
            }
            fos.close()

            // Share the file
            val contentUri: Uri = FileProvider.getUriForFile(requireContext(), "${requireContext().packageName}.fileprovider", file)
            val intent = Intent(Intent.ACTION_SEND).apply {
                type = "text/csv"
                putExtra(Intent.EXTRA_STREAM, contentUri)
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            }
            startActivity(Intent.createChooser(intent, "Export Expenses"))
            
        } catch (e: Exception) {
            Toast.makeText(requireContext(), "Export failed: ${e.message}", Toast.LENGTH_SHORT).show()
        }
    }
}
