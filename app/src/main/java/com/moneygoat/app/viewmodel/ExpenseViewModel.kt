package com.moneygoat.app.viewmodel

import android.app.Application
import androidx.lifecycle.*
import com.moneygoat.app.data.database.AppDatabase
import com.moneygoat.app.data.entity.*
import com.moneygoat.app.data.repository.FinanceRepository
import kotlinx.coroutines.launch

class ExpenseViewModel(application: Application) : AndroidViewModel(application) {
    private val repo: FinanceRepository
    
    init {
        val db = AppDatabase.getDatabase(application)
        repo = FinanceRepository(db.expenseDao(), db.categoryDao(), db.budgetGoalDao(), db.accountDao(), db.recurringTransactionDao())
    }

    private val _dateFilter = MutableLiveData<FilterParams>()
    private val _searchQuery = MutableLiveData<String>("")

    data class FilterParams(val userId: Long, val startDate: String, val endDate: String, val accountId: Long = -1L)

    val filteredExpensesWithCategory: LiveData<List<ExpenseWithCategory>> = 
        _dateFilter.switchMap { params ->
            val liveData = if (params.accountId == -1L) {
                repo.getFilteredExpenses(params.userId, params.startDate, params.endDate)
            } else {
                repo.getFilteredExpensesByAccount(params.userId, params.accountId, params.startDate, params.endDate)
            }
            
            _searchQuery.switchMap { query ->
                liveData.map { items ->
                    if (query.isEmpty()) items
                    else items.filter { item ->
                        val e = item.expense
                        val catName = item.category?.name ?: "Uncategorized"
                        val matchesText = e.description.contains(query, ignoreCase = true) || 
                                          catName.contains(query, ignoreCase = true)
                        val matchesAmount = when {
                            query.startsWith(">") -> e.amount > (query.drop(1).toDoubleOrNull() ?: 0.0)
                            query.startsWith("<") -> e.amount < (query.drop(1).toDoubleOrNull() ?: 1000000.0)
                            else -> false
                        }
                        matchesText || matchesAmount
                    }
                }
            }
        }

    fun setDateFilter(userId: Long, startDate: String, endDate: String, accountId: Long = -1L) {
        _dateFilter.value = FilterParams(userId, startDate, endDate, accountId)
    }

    fun setSearchQuery(query: String) {
        _searchQuery.value = query
    }

    val categoryTotals: LiveData<List<CategoryTotal>> = _dateFilter.switchMap { params ->
        if (params.accountId == -1L) {
            repo.getCategoryTotals(params.userId, params.startDate, params.endDate)
        } else {
            repo.getCategoryTotalsByAccount(params.userId, params.accountId, params.startDate, params.endDate)
        }
    }

    val totalSpent: LiveData<Double?> = _dateFilter.switchMap { params ->
        if (params.accountId == -1L) {
            repo.getTotalSpent(params.userId, params.startDate, params.endDate)
        } else {
            repo.getTotalSpentByAccount(params.userId, params.accountId, params.startDate, params.endDate)
        }
    }

    fun addExpense(expense: Expense) {
        viewModelScope.launch {
            repo.addExpense(expense)
        }
    }

    fun addRecurringTransaction(rt: RecurringTransaction) {
        viewModelScope.launch {
            repo.addRecurringTransaction(rt)
        }
    }

    fun deleteExpense(expense: Expense) {
        viewModelScope.launch {
            repo.deleteExpense(expense)
        }
    }

    fun syncData(userId: Long) {
        viewModelScope.launch {
            repo.syncAll(userId)
        }
    }

    // Advanced: Average Monthly Spend for suggestions
    fun getAverageMonthlySpend(userId: Long): LiveData<Double?> {
        val result = MutableLiveData<Double?>()
        viewModelScope.launch {
            // Directly from DAO since it's a simple query
            val db = AppDatabase.getDatabase(getApplication())
            result.postValue(db.expenseDao().getAverageMonthlySpend(userId))
        }
        return result
    }

    // Logic for trend analysis
    fun getAllExpenses(userId: Long): LiveData<List<Expense>> {
        val db = AppDatabase.getDatabase(getApplication())
        return db.expenseDao().getAllByUser(userId)
    }

    fun getAllCategories(): LiveData<List<Category>> = repo.allCategories

    fun getExpensesByAccount(userId: Long, accountId: Long): LiveData<List<Expense>> =
        repo.getExpensesByAccount(userId, accountId)

    fun getRecurring(userId: Long): LiveData<List<RecurringTransaction>> = 
        repo.getRecurring(userId)

    fun getAccounts(userId: Long): LiveData<List<Account>> = repo.getAccounts(userId)
}
