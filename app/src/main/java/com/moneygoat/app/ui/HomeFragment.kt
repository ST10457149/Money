package com.moneygoat.app.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ProgressBar
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.moneygoat.app.R
import com.moneygoat.app.viewmodel.ExpenseViewModel
import com.moneygoat.app.viewmodel.GoalViewModel
import com.moneygoat.app.data.entity.RecurringTransaction
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

/**
 * Fragment representing the dashboard/home screen.
 * Displays current month's spending, budget progress, and recent expenses.
 */
class HomeFragment : Fragment() {
    private lateinit var expenseVM: ExpenseViewModel
    private lateinit var goalVM: GoalViewModel
    private lateinit var accountVM: com.moneygoat.app.viewmodel.AccountViewModel

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? =
        inflater.inflate(R.layout.fragment_home, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val act = requireActivity() as MainActivity
        val userId = act.userId
        
        // Initialize ViewModels
        expenseVM = ViewModelProvider(this)[ExpenseViewModel::class.java]
        goalVM = ViewModelProvider(this)[GoalViewModel::class.java]
        accountVM = ViewModelProvider(this)[com.moneygoat.app.viewmodel.AccountViewModel::class.java]

        val tvWelcome = view.findViewById<TextView>(R.id.tvWelcome)
        val tvMonth = view.findViewById<TextView>(R.id.tvMonthLabel)
        val tvSpent = view.findViewById<TextView>(R.id.tvSpentAmount)
        val tvGoalInfo = view.findViewById<TextView>(R.id.tvGoalInfo)
        val tvMinGoal = view.findViewById<TextView>(R.id.tvMinGoal)
        val tvMaxGoal = view.findViewById<TextView>(R.id.tvMaxGoal)
        val progress = view.findViewById<ProgressBar>(R.id.progressBudget)
        val tvPercent = view.findViewById<TextView>(R.id.tvProgressPercent)
        val tvRecent = view.findViewById<TextView>(R.id.tvRecentExpenses)
        val tvUpcoming = view.findViewById<TextView>(R.id.tvUpcomingBills)
        val tvMascot = view.findViewById<TextView>(R.id.tvGoatMascot)
        val tvNetWorth = view.findViewById<TextView>(R.id.tvNetWorthSummary)
        val cardRecent = view.findViewById<View>(R.id.tvRecentTitle).parent.parent as View

        tvWelcome.text = "Welcome, ${act.username}!"
        
        cardRecent.setOnClickListener {
            (requireActivity() as MainActivity).findViewById<BottomNavigationView>(R.id.bottomNavigation).selectedItemId = R.id.nav_analytics
        }
        
        tvNetWorth.visibility = View.VISIBLE
        accountVM.getTotalBalance(userId).observe(viewLifecycleOwner) { total ->
            tvNetWorth.text = "Net Worth: R %.2f".format(total ?: 0.0)
        }
        
        // Determine current month and year
        val cal = Calendar.getInstance()
        val month = cal.get(Calendar.MONTH) + 1
        val year = cal.get(Calendar.YEAR)
        tvMonth.text = SimpleDateFormat("MMMM yyyy", Locale.getDefault()).format(cal.time)

        // Set date filter for the entire current month
        cal.set(Calendar.DAY_OF_MONTH, 1)
        val startDate = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(cal.time)
        cal.set(Calendar.DAY_OF_MONTH, cal.getActualMaximum(Calendar.DAY_OF_MONTH))
        val endDate = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(cal.time)
        
        // Trigger data load in ViewModel
        expenseVM.setDateFilter(userId, startDate, endDate)

        // Observe total spending and update budget progress
        expenseVM.totalSpent.observe(viewLifecycleOwner) { total ->
            val spent = total ?: 0.0
            tvSpent.text = String.format("R %.2f", spent)
            
            // Fetch goals for this month to calculate progress percentage
            goalVM.getGoal(userId, month, year).observe(viewLifecycleOwner) { goal ->
                if (goal != null) {
                    tvMinGoal.text = "Min Save Goal: R %.0f".format(goal.minimumGoal)
                    tvMaxGoal.text = "Max Spend: R %.0f".format(goal.maximumGoal)

                    val pct = if (goal.maximumGoal > 0) ((spent / goal.maximumGoal) * 100).toInt() else 0
                    progress.progress = pct.coerceAtMost(100)
                    tvPercent.text = "${pct.coerceAtMost(100)}%"
                    
                    // Smart Insights: Daily Allowance & Projection
                    val daysInMonth = cal.getActualMaximum(Calendar.DAY_OF_MONTH)
                    val currentDay = cal.get(Calendar.DAY_OF_MONTH)
                    val daysLeft = (daysInMonth - currentDay) + 1
                    
                    val remainingBudget = (goal.maximumGoal - spent).coerceAtLeast(0.0)
                    val dailyAllowance = if (daysLeft > 0) remainingBudget / daysLeft else 0.0
                    
                    val avgDailySpend = if (currentDay > 0) spent / currentDay else spent
                    val projectedTotal = avgDailySpend * daysInMonth
                    
                    val insight = if (spent > goal.maximumGoal) {
                        "Over budget by R%.2f".format(spent - goal.maximumGoal)
                    } else {
                        "Daily Allowance: R%.2f | Projection: R%.2f".format(dailyAllowance, projectedTotal)
                    }
                    
                    tvGoalInfo.text = "Goal: R %.2f | %s".format(goal.maximumGoal, insight)
                    
                    // Mascot Logic
                    when {
                        spent > goal.maximumGoal -> {
                            tvSpent.setTextColor(resources.getColor(R.color.expense_red, null))
                            tvMascot.text = "😫" // Stressed/Angry Goat
                        }
                        spent > goal.maximumGoal * 0.8 -> {
                            tvSpent.setTextColor(resources.getColor(R.color.accent_gold, null))
                            tvMascot.text = "😰" // Warning Goat
                        }
                        else -> {
                            tvSpent.setTextColor(resources.getColor(R.color.primary_green, null))
                            tvMascot.text = "😎" // Cool/Happy Goat
                        }
                    }
                } else { 
                    progress.progress = 0
                    tvPercent.text = "0%"
                    tvGoalInfo.text = "No goals set yet." 
                }
            }
        }
        
        // Observe expenses to show the 5 most recent ones
        expenseVM.filteredExpensesWithCategory.observe(viewLifecycleOwner) { items ->
            val recent = items.map { it.expense }.take(5)
            tvRecent.text = if (recent.isEmpty()) "No expenses this month yet."
            else recent.joinToString("\n") { "\u2022 ${it.description} \u2014 R %.2f (${it.date})".format(it.amount) }
        }

        // Enterprise Feature: Upcoming Bills tracking (Surpassing Monarch)
        expenseVM.getRecurring(userId).observe(viewLifecycleOwner) { recurring ->
            if (recurring.isNullOrEmpty()) {
                tvUpcoming.text = "No upcoming bills detected."
            } else {
                val nextBills = recurring.take(3).joinToString("\n") { 
                    "\u2022 ${it.description} (R %.2f) due soon".format(it.amount)
                }
                tvUpcoming.text = nextBills
            }
        }
    }
}
