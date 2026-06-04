package com.moneygoat.app.data.dao
import androidx.lifecycle.LiveData
import androidx.room.*
import com.moneygoat.app.data.entity.CategoryTotal
import com.moneygoat.app.data.entity.Expense

/**
 * Data Access Object for the Expense entity.
 * Handles all database operations related to tracking expenses.
 */
@Dao
interface ExpenseDao {
    /**
     * Inserts a new expense into the database.
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(expense: Expense): Long

    /**
     * Inserts a list of expenses in a single transaction.
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(expenses: List<Expense>)

    /**
     * Deletes an existing expense from the database.
     */
    @Delete
    suspend fun delete(expense: Expense)

    /**
     * Retrieves all expenses for a specific user, ordered by date and time.
     */
    @Query("SELECT * FROM expenses WHERE userId = :userId ORDER BY date DESC, startTime DESC")
    fun getAllByUser(userId: Long): LiveData<List<Expense>>

    /**
     * Retrieves expenses for a specific user within a given date range.
     */
    @Query("SELECT * FROM expenses WHERE userId = :userId AND date BETWEEN :startDate AND :endDate ORDER BY date DESC, startTime DESC")
    fun getExpensesByDateRange(userId: Long, startDate: String, endDate: String): LiveData<List<Expense>>

    /**
     * Retrieves all expenses for a specific user with category details.
     */
    @Transaction
    @Query("SELECT * FROM expenses WHERE userId = :userId ORDER BY date DESC, startTime DESC")
    fun getAllWithCategory(userId: Long): LiveData<List<com.moneygoat.app.data.entity.ExpenseWithCategory>>

    /**
     * Retrieves expenses for a specific user within a given date range with category details.
     */
    @Transaction
    @Query("SELECT * FROM expenses WHERE userId = :userId AND date BETWEEN :startDate AND :endDate ORDER BY date DESC, startTime DESC")
    fun getExpensesByDateRangeWithCategory(userId: Long, startDate: String, endDate: String): LiveData<List<com.moneygoat.app.data.entity.ExpenseWithCategory>>

    /**
     * Calculates the average monthly spending for a user.
     */
    @Query("SELECT AVG(total) FROM (SELECT SUM(amount) as total FROM expenses WHERE userId = :userId GROUP BY strftime('%Y-%m', date))")
    suspend fun getAverageMonthlySpend(userId: Long): Double?

    /**
     * Calculates the total amount spent per category for a user within a date range.
     * Returns a list of CategoryTotal objects.
     */
    @Query("SELECT c.name AS categoryName, SUM(e.amount) AS totalAmount FROM expenses e INNER JOIN categories c ON e.categoryId = c.id WHERE e.userId = :userId AND e.date BETWEEN :startDate AND :endDate GROUP BY e.categoryId ORDER BY totalAmount DESC")
    fun getCategoryTotals(userId: Long, startDate: String, endDate: String): LiveData<List<CategoryTotal>>

    /**
     * Calculates the grand total of all expenses for a user within a date range.
     */
    @Query("SELECT SUM(amount) FROM expenses WHERE userId = :userId AND date BETWEEN :startDate AND :endDate")
    fun getTotalSpent(userId: Long, startDate: String, endDate: String): LiveData<Double?>

    /**
     * Calculates the grand total of all expenses for a user within a date range (one-shot).
     */
    @Query("SELECT SUM(amount) FROM expenses WHERE userId = :userId AND date BETWEEN :startDate AND :endDate")
    suspend fun getTotalSpentDirect(userId: Long, startDate: String, endDate: String): Double?

    /**
     * Retrieves the most recent expense for a user.
     */
    @Query("SELECT * FROM expenses WHERE userId = :userId ORDER BY date DESC, startTime DESC LIMIT 1")
    suspend fun getLastExpense(userId: Long): Expense?

    /**
     * Calculates the total amount spent per category for a user within a date range and specific account.
     */
    @Query("SELECT c.name AS categoryName, SUM(e.amount) AS totalAmount FROM expenses e INNER JOIN categories c ON e.categoryId = c.id WHERE e.userId = :userId AND e.accountId = :accountId AND e.date BETWEEN :startDate AND :endDate GROUP BY e.categoryId ORDER BY totalAmount DESC")
    fun getCategoryTotalsByAccount(userId: Long, accountId: Long, startDate: String, endDate: String): LiveData<List<CategoryTotal>>

    /**
     * Calculates the grand total of all expenses for a user within a date range and specific account.
     */
    @Query("SELECT SUM(amount) FROM expenses WHERE userId = :userId AND accountId = :accountId AND date BETWEEN :startDate AND :endDate")
    fun getTotalSpentByAccount(userId: Long, accountId: Long, startDate: String, endDate: String): LiveData<Double?>

    /**
     * Retrieves all expenses for a specific user and account.
     */
    @Query("SELECT * FROM expenses WHERE userId = :userId AND accountId = :accountId ORDER BY date DESC, startTime DESC")
    fun getByAccount(userId: Long, accountId: Long): LiveData<List<Expense>>

    /**
     * Retrieves expenses for a specific user within a given date range with category details and specific account.
     */
    @Transaction
    @Query("SELECT * FROM expenses WHERE userId = :userId AND accountId = :accountId AND date BETWEEN :startDate AND :endDate ORDER BY date DESC, startTime DESC")
    fun getExpensesByDateRangeWithCategoryByAccount(userId: Long, accountId: Long, startDate: String, endDate: String): LiveData<List<com.moneygoat.app.data.entity.ExpenseWithCategory>>
}
