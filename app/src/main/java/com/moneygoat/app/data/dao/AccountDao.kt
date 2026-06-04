package com.moneygoat.app.data.dao

import androidx.lifecycle.LiveData
import androidx.room.*
import com.moneygoat.app.data.entity.Account

@Dao
interface AccountDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(account: Account): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(accounts: List<Account>)

    @Update
    suspend fun update(account: Account)

    @Delete
    suspend fun delete(account: Account)

    @Query("SELECT * FROM accounts WHERE userId = :userId")
    fun getAccountsByUser(userId: Long): LiveData<List<Account>>

    @Query("SELECT SUM(balance) FROM accounts WHERE userId = :userId")
    fun getTotalBalance(userId: Long): LiveData<Double?>
}
