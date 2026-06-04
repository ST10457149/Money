package com.moneygoat.app.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.moneygoat.app.R
import com.moneygoat.app.viewmodel.ExpenseViewModel
import com.moneygoat.app.viewmodel.GoalViewModel
import java.text.SimpleDateFormat
import java.util.*

/**
 * Fragment for setting monthly financial goals.
 * Users can set a minimum savings goal and a maximum spending limit using SeekBars or EditTexts.
 */
class GoalSettingsFragment : Fragment() {
    private lateinit var goalVM: GoalViewModel

    override fun onCreateView(inflater: LayoutInflater, c: ViewGroup?, s: Bundle?): View? = 
        inflater.inflate(R.layout.fragment_goal_settings, c, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val userId = (requireActivity() as MainActivity).userId
        goalVM = ViewModelProvider(this)[GoalViewModel::class.java]
        val expenseVM = ViewModelProvider(this)[ExpenseViewModel::class.java]

        val tvMonth = view.findViewById<TextView>(R.id.tvGoalMonth)
        val etMin = view.findViewById<EditText>(R.id.etMinGoal)
        val etMax = view.findViewById<EditText>(R.id.etMaxGoal)
        val seekMin = view.findViewById<SeekBar>(R.id.seekBarMin)
        val seekMax = view.findViewById<SeekBar>(R.id.seekBarMax)
        val tvMinVal = view.findViewById<TextView>(R.id.tvMinValue)
        val tvMaxVal = view.findViewById<TextView>(R.id.tvMaxValue)
        val btnSave = view.findViewById<Button>(R.id.btnSaveGoal)
        
        val layoutSuggestion = view.findViewById<LinearLayout>(R.id.layoutSuggestion)
        val tvSuggestion = view.findViewById<TextView>(R.id.tvSuggestion)
        val btnApplySuggestion = view.findViewById<Button>(R.id.btnApplySuggestion)

        // Set the context to the current month and year
        val cal = Calendar.getInstance()
        val month = cal.get(Calendar.MONTH) + 1
        val year = cal.get(Calendar.YEAR)
        tvMonth.text = SimpleDateFormat("MMMM yyyy", Locale.getDefault()).format(cal.time)

        // Smart Suggestion Logic
        expenseVM.getAverageMonthlySpend(userId).observe(viewLifecycleOwner) { avg ->
            if (avg != null && avg > 0) {
                layoutSuggestion.visibility = View.VISIBLE
                tvSuggestion.text = "Based on history, we suggest: R %.2f".format(avg)
                btnApplySuggestion.setOnClickListener {
                    etMax.setText("%.2f".format(avg))
                    seekMax.progress = avg.toInt()
                }
            }
        }

        // Configure Minimum Goal SeekBar
        seekMin.max = 50000
        seekMin.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(sb: SeekBar?, p: Int, fromUser: Boolean) { 
                if (fromUser) { 
                    etMin.setText(p.toString())
                    tvMinVal.text = "R $p" 
                } 
            }
            override fun onStartTrackingTouch(sb: SeekBar?) {}
            override fun onStopTrackingTouch(sb: SeekBar?) {}
        })

        // Configure Maximum Goal SeekBar
        seekMax.max = 100000
        seekMax.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(sb: SeekBar?, p: Int, fromUser: Boolean) { 
                if (fromUser) { 
                    etMax.setText(p.toString())
                    tvMaxVal.text = "R $p" 
                } 
            }
            override fun onStartTrackingTouch(sb: SeekBar?) {}
            override fun onStopTrackingTouch(sb: SeekBar?) {}
        })

        // Observe current goal and populate UI if it exists
        goalVM.getGoal(userId, month, year).observe(viewLifecycleOwner) { goal ->
            if (goal != null) {
                etMin.setText(goal.minimumGoal.toInt().toString())
                etMax.setText(goal.maximumGoal.toInt().toString())
                seekMin.progress = goal.minimumGoal.toInt()
                seekMax.progress = goal.maximumGoal.toInt()
                tvMinVal.text = "R ${goal.minimumGoal.toInt()}"
                tvMaxVal.text = "R ${goal.maximumGoal.toInt()}"
            }
        }

        // Save goals to the database
        btnSave.setOnClickListener {
            val min = etMin.text.toString().trim().toDoubleOrNull()
            val max = etMax.text.toString().trim().toDoubleOrNull()
            
            if (min == null || max == null) { 
                Toast.makeText(requireContext(), "Enter both goals", Toast.LENGTH_SHORT).show()
                return@setOnClickListener 
            }
            
            // Logic validation: Minimum goal shouldn't exceed the Maximum goal
            if (min > max) { 
                Toast.makeText(requireContext(), "Min must be less than max", Toast.LENGTH_SHORT).show()
                return@setOnClickListener 
            }

            goalVM.saveGoal(userId, month, year, min, max)
            Toast.makeText(requireContext(), "Goals saved!", Toast.LENGTH_SHORT).show()
        }
    }
}
