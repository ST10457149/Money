package com.moneygoat.app.data.repository

import android.util.Log
import androidx.lifecycle.LiveData
import com.google.firebase.database.FirebaseDatabase
import com.moneygoat.app.data.dao.AccountDao
import com.moneygoat.app.data.dao.RecurringTransactionDao
import com.moneygoat.app.data.dao.BudgetGoalDao
import com.moneygoat.app.data.dao.CategoryDao
import com.moneygoat.app.data.dao.ExpenseDao
import com.moneygoat.app.data.entity.*
import kotlinx.coroutines.tasks.await

class FinanceRepository(
    private val expenseDao: ExpenseDao,
    private val categoryDao: CategoryDao,
    private val goalDao: BudgetGoalDao,
    private val accountDao: AccountDao,
    private val recurringDao: RecurringTransactionDao,
    private val fbDb: FirebaseDatabase = FirebaseDatabase.getInstance()
) {
    private val TAG = "MoneyGoat_Repo"

    // --- Expense Operations ---
    fun getFilteredExpenses(userId: Long, start: String, end: String): LiveData<List<ExpenseWithCategory>> =
        expenseDao.getExpensesByDateRangeWithCategory(userId, start, end)

    fun getFilteredExpensesByAccount(userId: Long, accountId: Long, start: String, end: String): LiveData<List<ExpenseWithCategory>> =
        expenseDao.getExpensesByDateRangeWithCategoryByAccount(userId, accountId, start, end)

    fun getTotalSpent(userId: Long, start: String, end: String): LiveData<Double?> =
        expenseDao.getTotalSpent(userId, start, end)

    fun getCategoryTotals(userId: Long, start: String, end: String): LiveData<List<CategoryTotal>> =
        expenseDao.getCategoryTotals(userId, start, end)

    fun getCategoryTotalsByAccount(userId: Long, accountId: Long, start: String, end: String): LiveData<List<CategoryTotal>> =
        expenseDao.getCategoryTotalsByAccount(userId, accountId, start, end)

    fun getTotalSpentByAccount(userId: Long, accountId: Long, start: String, end: String): LiveData<Double?> =
        expenseDao.getTotalSpentByAccount(userId, accountId, start, end)

    fun getExpensesByAccount(userId: Long, accountId: Long): LiveData<List<Expense>> =
        expenseDao.getByAccount(userId, accountId)

    suspend fun addExpense(expense: Expense) {
        val id = expenseDao.insert(expense)
        val expenseWithId = expense.copy(id = id)
        // Sync to Firebase
        fbDb.getReference("expenses").child(expense.userId.toString()).child(id.toString())
            .setValue(expenseWithId)
    }

    suspend fun deleteExpense(expense: Expense) {
        expenseDao.delete(expense)
        fbDb.getReference("expenses").child(expense.userId.toString()).child(expense.id.toString())
            .removeValue()
    }

    // --- Category Operations ---
    fun getCategories(userId: Long): LiveData<List<Category>> = 
        categoryDao.getCategoriesByUser(userId)

    fun getCategoriesWithCounts(userId: Long): LiveData<List<CategoryWithCount>> =
        categoryDao.getCategoriesWithCounts(userId)

    suspend fun addCategory(category: Category) {
        val id = categoryDao.insert(category)
        val catWithId = category.copy(id = id)
        fbDb.getReference("categories").child(category.userId.toString()).child(id.toString())
            .setValue(catWithId)
    }

    suspend fun deleteCategory(category: Category) {
        categoryDao.delete(category)
        fbDb.getReference("categories").child(category.userId.toString()).child(category.id.toString())
            .removeValue()
    }

    // --- Goal Operations ---
    fun getGoal(userId: Long, month: Int, year: Int): LiveData<BudgetGoal?> =
        goalDao.getGoal(userId, month, year)

    suspend fun saveGoal(goal: BudgetGoal) {
        goalDao.insertOrUpdate(goal)
        val key = "${goal.year}_${goal.month}"
        fbDb.getReference("goals").child(goal.userId.toString()).child(key)
            .setValue(goal)
    }

    // --- Account Operations ---
    fun getAccounts(userId: Long): LiveData<List<Account>> = accountDao.getAccountsByUser(userId)
    fun getTotalBalance(userId: Long): LiveData<Double?> = accountDao.getTotalBalance(userId)
    suspend fun addAccount(account: Account) {
        val id = accountDao.insert(account)
        fbDb.getReference("accounts").child(account.userId.toString()).child(id.toString()).setValue(account.copy(id = id))
    }

    // --- Recurring Transaction Operations ---
    fun getRecurring(userId: Long): LiveData<List<RecurringTransaction>> = 
        recurringDao.getActiveByUser(userId)

    suspend fun addRecurringTransaction(transaction: RecurringTransaction) {
        val id = recurringDao.insert(transaction)
        fbDb.getReference("recurring").child(transaction.userId.toString()).child(id.toString())
            .setValue(transaction.copy(id = id))
    }

    // New property to match ViewModel usage
    val allCategories: LiveData<List<Category>> = categoryDao.getAllCategories()

    // --- Sync Logic ---
    suspend fun syncAll(userId: Long) {
        try {
            // Sync Expenses
            val expSnapshot = fbDb.getReference("expenses").child(userId.toString()).get().await()
            val expenses = expSnapshot.children.mapNotNull { it.getValue(Expense::class.java) }
            if (expenses.isNotEmpty()) expenseDao.insertAll(expenses)

            // Sync Categories
            val catSnapshot = fbDb.getReference("categories").child(userId.toString()).get().await()
            val categories = catSnapshot.children.mapNotNull { it.getValue(Category::class.java) }
            if (categories.isNotEmpty()) categoryDao.insertAll(categories)

            // Sync Goals
            val goalSnapshot = fbDb.getReference("goals").child(userId.toString()).get().await()
            val goals = goalSnapshot.children.mapNotNull { it.getValue(BudgetGoal::class.java) }
            if (goals.isNotEmpty()) goalDao.insertAll(goals)

            // Sync Accounts
            val accSnapshot = fbDb.getReference("accounts").child(userId.toString()).get().await()
            val accounts = accSnapshot.children.mapNotNull { it.getValue(Account::class.java) }
            if (accounts.isNotEmpty()) accountDao.insertAll(accounts)
            
            Log.d(TAG, "Full sync completed for user $userId")
        } catch (e: Exception) {
            Log.e(TAG, "Sync failed", e)
        }
    }
}
