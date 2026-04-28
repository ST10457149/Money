package com.moneygoat.app.viewmodel
import android.app.Application
import android.util.Log
import androidx.lifecycle.*
import com.moneygoat.app.data.database.AppDatabase
import com.moneygoat.app.data.entity.CategoryTotal
import com.moneygoat.app.data.entity.Expense
import kotlinx.coroutines.launch

class ExpenseViewModel(application: Application) : AndroidViewModel(application) {
    private val TAG = "MoneyGoat_Expense"
    private val dao = AppDatabase.getDatabase(application).expenseDao()
    private val _dateFilter = MutableLiveData<Triple<Long, String, String>>()

    val filteredExpenses: LiveData<List<Expense>> = _dateFilter.switchMap { (u, s, e) ->
        Log.d(TAG, "Reading expenses for user $u from $s to $e")
        dao.getExpensesByDateRange(u, s, e)
    }
    val categoryTotals: LiveData<List<CategoryTotal>> = _dateFilter.switchMap { (u, s, e) ->
        Log.d(TAG, "Reading category totals for user $u from $s to $e")
        dao.getCategoryTotals(u, s, e)
    }
    val totalSpent: LiveData<Double?> = _dateFilter.switchMap { (u, s, e) ->
        Log.d(TAG, "Reading total spent for user $u from $s to $e")
        dao.getTotalSpent(u, s, e)
    }
    fun setDateFilter(userId: Long, startDate: String, endDate: String) {
        Log.d(TAG, "Setting date filter: user $userId, start $startDate, end $endDate")
        _dateFilter.value = Triple(userId, startDate, endDate)
    }
    fun getAllExpenses(userId: Long): LiveData<List<Expense>> {
        Log.d(TAG, "Reading all expenses for user $userId")
        return dao.getAllByUser(userId)
    }
    fun addExpense(expense: Expense) {
        viewModelScope.launch {
            try {
                Log.d(TAG, "Saving expense: ${expense.description}, amount: ${expense.amount}")
                dao.insert(expense)
                Log.d(TAG, "Expense saved successfully")
            } catch (e: Exception) {
                Log.e(TAG, "Error saving expense", e)
            }
        }
    }
    fun deleteExpense(expense: Expense) {
        viewModelScope.launch {
            try {
                Log.d(TAG, "Deleting expense id: ${expense.id}")
                dao.delete(expense)
                Log.d(TAG, "Expense deleted successfully")
            } catch (e: Exception) {
                Log.e(TAG, "Error deleting expense", e)
            }
        }
    }
}
