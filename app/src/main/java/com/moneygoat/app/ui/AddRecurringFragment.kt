package com.moneygoat.app.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.moneygoat.app.R
import com.moneygoat.app.data.entity.Category
import com.moneygoat.app.data.entity.RecurringTransaction
import com.moneygoat.app.data.entity.TransactionType
import com.moneygoat.app.viewmodel.ExpenseViewModel
import java.util.*

class AddRecurringFragment : Fragment() {
    private lateinit var viewModel: ExpenseViewModel
    private var categories: List<Category> = emptyList()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_add_recurring, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        viewModel = ViewModelProvider(this)[ExpenseViewModel::class.java]
        val userId = (requireActivity() as MainActivity).userId

        val etDesc = view.findViewById<EditText>(R.id.etRecurringDesc)
        val etAmount = view.findViewById<EditText>(R.id.etRecurringAmount)
        val spinnerCat = view.findViewById<Spinner>(R.id.spinnerRecurringCategory)
        val spinnerFreq = view.findViewById<Spinner>(R.id.spinnerFrequency)
        val datePicker = view.findViewById<DatePicker>(R.id.datePickerRecurring)
        val btnSave = view.findViewById<Button>(R.id.btnSaveRecurring)

        viewModel.getAllCategories().observe(viewLifecycleOwner) { cats ->
            categories = cats
            val catNames = cats.map { it.name }
            val adapter = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_item, catNames)
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
            spinnerCat.adapter = adapter
        }

        btnSave.setOnClickListener {
            val desc = etDesc.text.toString().trim()
            val amountStr = etAmount.text.toString().trim()
            val freq = spinnerFreq.selectedItem.toString()
            
            if (desc.isEmpty() || amountStr.isEmpty() || categories.isEmpty()) {
                Toast.makeText(requireContext(), "Incomplete details", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val amount = amountStr.toDoubleOrNull() ?: 0.0
            val catId = categories[spinnerCat.selectedItemPosition].id
            
            val calendar = Calendar.getInstance()
            calendar.set(datePicker.year, datePicker.month, datePicker.dayOfMonth)
            val nextDate = "%d-%02d-%02d".format(datePicker.year, datePicker.month + 1, datePicker.dayOfMonth)

            val rt = RecurringTransaction(
                userId = userId,
                categoryId = catId,
                description = desc,
                amount = amount,
                type = TransactionType.EXPENSE,
                frequency = freq,
                nextDueDate = nextDate
            )

            viewModel.addRecurringTransaction(rt)
            Toast.makeText(requireContext(), "Recurring bill added", Toast.LENGTH_SHORT).show()
            parentFragmentManager.popBackStack()
        }
    }
}
