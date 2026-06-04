package com.moneygoat.app.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.moneygoat.app.R
import com.moneygoat.app.data.entity.BudgetGoal
import com.moneygoat.app.viewmodel.GoalViewModel
import java.util.*

class SavingsGoalsFragment : Fragment() {
    private lateinit var viewModel: GoalViewModel

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_savings_goals, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        viewModel = ViewModelProvider(this)[GoalViewModel::class.java]
        val userId = (requireActivity() as MainActivity).userId

        val etGoal = view.findViewById<EditText>(R.id.etSavingsGoal)
        val btnSave = view.findViewById<Button>(R.id.btnSetGoal)
        val tvCurrentGoal = view.findViewById<TextView>(R.id.tvCurrentGoalDisplay)
        val btnExport = view.findViewById<Button>(R.id.btnExportReport)

        btnExport.setOnClickListener {
            Toast.makeText(requireContext(), "Generating PDF Report...", Toast.LENGTH_LONG).show()
            // In a real app, this would use iText or similar library to generate and share a PDF
        }

        val cal = Calendar.getInstance()
        val month = cal.get(Calendar.MONTH) + 1
        val year = cal.get(Calendar.YEAR)

        viewModel.getGoal(userId, month, year).observe(viewLifecycleOwner) { goal ->
            if (goal != null) {
                tvCurrentGoal.text = "Current Monthly Limit: R %.2f".format(goal.maximumGoal)
            } else {
                tvCurrentGoal.text = "No goal set for this month."
            }
        }

        btnSave.setOnClickListener {
            val amount = etGoal.text.toString().toDoubleOrNull() ?: 0.0
            if (amount <= 0) {
                Toast.makeText(requireContext(), "Enter valid amount", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val newGoal = BudgetGoal(
                userId = userId,
                month = month,
                year = year,
                minimumGoal = 0.0,
                maximumGoal = amount
            )
            viewModel.updateGoal(newGoal)
            Toast.makeText(requireContext(), "Goal Updated", Toast.LENGTH_SHORT).show()
        }
    }
}
