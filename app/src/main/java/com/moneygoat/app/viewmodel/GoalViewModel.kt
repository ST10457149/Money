package com.moneygoat.app.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.viewModelScope
import com.moneygoat.app.data.database.AppDatabase
import com.moneygoat.app.data.entity.BudgetGoal
import com.moneygoat.app.data.repository.FinanceRepository
import kotlinx.coroutines.launch

class GoalViewModel(application: Application) : AndroidViewModel(application) {
    private val repo: FinanceRepository
    
    init {
        val db = AppDatabase.getDatabase(application)
        repo = FinanceRepository(db.expenseDao(), db.categoryDao(), db.budgetGoalDao(), db.accountDao(), db.recurringTransactionDao())
    }

    fun getGoal(userId: Long, month: Int, year: Int): LiveData<BudgetGoal?> = 
        repo.getGoal(userId, month, year)

    fun saveGoal(userId: Long, month: Int, year: Int, minGoal: Double, maxGoal: Double) {
        viewModelScope.launch {
            // Check for existing to get ID
            val db = AppDatabase.getDatabase(getApplication())
            val existing = db.budgetGoalDao().getGoalDirect(userId, month, year)
            val goal = BudgetGoal(
                id = existing?.id ?: 0,
                userId = userId,
                month = month,
                year = year,
                minimumGoal = minGoal,
                maximumGoal = maxGoal
            )
            repo.saveGoal(goal)
        }
    }

    fun updateGoal(goal: BudgetGoal) {
        viewModelScope.launch {
            repo.saveGoal(goal)
        }
    }

    fun syncFromFirebase(userId: Long) {
        viewModelScope.launch {
            repo.syncAll(userId)
        }
    }
}
