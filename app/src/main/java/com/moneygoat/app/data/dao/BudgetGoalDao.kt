package com.moneygoat.app.data.dao
import androidx.lifecycle.LiveData
import androidx.room.*
import com.moneygoat.app.data.entity.BudgetGoal

/**
 * Data Access Object for the BudgetGoal entity.
 * Manages monthly spending and saving targets.
 */
@Dao
interface BudgetGoalDao {
    /**
     * Inserts or updates a budget goal.
     * If a goal already exists, it is replaced.
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdate(goal: BudgetGoal): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(goals: List<BudgetGoal>)

    /**
     * Retrieves the budget goal for a specific user, month, and year as LiveData.
     */
    @Query("SELECT * FROM budget_goals WHERE userId = :userId AND month = :month AND year = :year LIMIT 1")
    fun getGoal(userId: Long, month: Int, year: Int): LiveData<BudgetGoal?>

    /**
     * Retrieves the budget goal for a specific user, month, and year (one-shot).
     */
    @Query("SELECT * FROM budget_goals WHERE userId = :userId AND month = :month AND year = :year LIMIT 1")
    suspend fun getGoalDirect(userId: Long, month: Int, year: Int): BudgetGoal?
}
