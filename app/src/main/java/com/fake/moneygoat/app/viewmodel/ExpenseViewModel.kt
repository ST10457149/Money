package com.moneygoat.app.viewmodel
import android.app.Application
import androidx.lifecycle.*
import com.moneygoat.app.data.database.AppDatabase
import com.moneygoat.app.data.entity.CategoryTotal
import com.moneygoat.app.data.entity.Expense
import kotlinx.coroutines.launch

class ExpenseViewModel(application: Application) : AndroidViewModel(application) {
    private val dao = AppDatabase.getDatabase(application).expenseDao()
    private val _dateFilter = MutableLiveData<Triple<Long, String, String>>()

    val filteredExpenses: LiveData<List<Expense>> = _dateFilter.switchMap { (u, s, e) -> dao.getExpensesByDateRange(u, s, e) }
    val categoryTotals: LiveData<List<CategoryTotal>> = _dateFilter.switchMap { (u, s, e) -> dao.getCategoryTotals(u, s, e) }
    val totalSpent: LiveData<Double?> = _dateFilter.switchMap { (u, s, e) -> dao.getTotalSpent(u, s, e) }
    fun setDateFilter(userId: Long, startDate: String, endDate: String) { _dateFilter.value = Triple(userId, startDate, endDate) }
    fun getAllExpenses(userId: Long): LiveData<List<Expense>> = dao.getAllByUser(userId)
    fun addExpense(expense: Expense) { viewModelScope.launch { dao.insert(expense) } }
    fun deleteExpense(expense: Expense) { viewModelScope.launch { dao.delete(expense) } }
}
