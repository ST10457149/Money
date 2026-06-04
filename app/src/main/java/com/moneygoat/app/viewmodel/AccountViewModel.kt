package com.moneygoat.app.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.viewModelScope
import com.moneygoat.app.data.database.AppDatabase
import com.moneygoat.app.data.entity.Account
import com.moneygoat.app.data.repository.FinanceRepository
import kotlinx.coroutines.launch

class AccountViewModel(application: Application) : AndroidViewModel(application) {
    private val repository: FinanceRepository

    init {
        val db = AppDatabase.getDatabase(application)
        repository = FinanceRepository(db.expenseDao(), db.categoryDao(), db.budgetGoalDao(), db.accountDao(), db.recurringTransactionDao())
    }

    fun getAccounts(userId: Long): LiveData<List<Account>> = repository.getAccounts(userId)
    fun getTotalBalance(userId: Long): LiveData<Double?> = repository.getTotalBalance(userId)

    fun addAccount(account: Account) = viewModelScope.launch {
        repository.addAccount(account)
    }
}
