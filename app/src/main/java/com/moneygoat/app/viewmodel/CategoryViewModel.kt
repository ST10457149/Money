package com.moneygoat.app.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.viewModelScope
import com.moneygoat.app.data.database.AppDatabase
import com.moneygoat.app.data.entity.Category
import com.moneygoat.app.data.entity.CategoryWithCount
import com.moneygoat.app.data.repository.FinanceRepository
import kotlinx.coroutines.launch

class CategoryViewModel(application: Application) : AndroidViewModel(application) {
    private val repo: FinanceRepository
    
    init {
        val db = AppDatabase.getDatabase(application)
        repo = FinanceRepository(db.expenseDao(), db.categoryDao(), db.budgetGoalDao(), db.accountDao(), db.recurringTransactionDao())
    }

    fun getCategories(userId: Long): LiveData<List<Category>> = repo.getCategories(userId)

    fun getCategoriesWithCounts(userId: Long): LiveData<List<CategoryWithCount>> =
        repo.getCategoriesWithCounts(userId)

    fun addCategory(name: String, userId: Long) {
        viewModelScope.launch {
            repo.addCategory(Category(name = name, userId = userId))
        }
    }

    fun deleteCategory(category: Category) {
        viewModelScope.launch {
            repo.deleteCategory(category)
        }
    }

    fun syncFromFirebase(userId: Long) {
        viewModelScope.launch {
            repo.syncAll(userId)
        }
    }
}
