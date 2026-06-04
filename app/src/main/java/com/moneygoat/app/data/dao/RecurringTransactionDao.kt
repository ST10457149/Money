package com.moneygoat.app.data.dao

import androidx.lifecycle.LiveData
import androidx.room.*
import com.moneygoat.app.data.entity.RecurringTransaction

@Dao
interface RecurringTransactionDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(transaction: RecurringTransaction): Long

    @Query("SELECT * FROM recurring_transactions WHERE userId = :userId AND isActive = 1")
    fun getActiveByUser(userId: Long): LiveData<List<RecurringTransaction>>

    @Update
    suspend fun update(transaction: RecurringTransaction)
}
