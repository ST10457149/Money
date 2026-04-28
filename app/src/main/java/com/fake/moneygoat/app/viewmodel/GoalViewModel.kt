package com.moneygoat.app.viewmodel
import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.viewModelScope
import com.moneygoat.app.data.database.AppDatabase
import com.moneygoat.app.data.entity.BudgetGoal
import kotlinx.coroutines.launch

class GoalViewModel(application: Application) : AndroidViewModel(application) {
    private val TAG = "MoneyGoat_Goal"
    private val dao = AppDatabase.getDatabase(application).budgetGoalDao()
    fun getGoal(userId: Long, month: Int, year: Int): LiveData<BudgetGoal?> {
        Log.d(TAG, "Reading budget goal for user $userId, month $month, year $year")
        return dao.getGoal(userId, month, year)
    }
    fun saveGoal(userId: Long, month: Int, year: Int, minGoal: Double, maxGoal: Double) {
        viewModelScope.launch {
            try {
                Log.d(TAG, "Saving budget goal for user $userId, month $month, year $year")
                val existing = dao.getGoalDirect(userId, month, year)
                dao.insertOrUpdate(BudgetGoal(id = existing?.id ?: 0, userId = userId, month = month, year = year, minimumGoal = minGoal, maximumGoal = maxGoal))
                Log.d(TAG, "Budget goal saved successfully")
            } catch (e: Exception) {
                Log.e(TAG, "Error saving budget goal", e)
            }
        }
    }
}
