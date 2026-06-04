package com.moneygoat.app.data.entity
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

/**
 * Data class representing a monthly budget goal for a user.
 * Defines the minimum and maximum spending targets for a specific month and year.
 */
@Entity(tableName = "budget_goals",
    foreignKeys = [ForeignKey(entity = User::class, parentColumns = ["id"], childColumns = ["userId"], onDelete = ForeignKey.CASCADE)],
    indices = [Index(value = ["userId"])])
data class BudgetGoal(
    @PrimaryKey(autoGenerate = true) val id: Long = 0, // Unique identifier for the budget goal
    val userId: Long, // ID of the user this goal belongs to
    val month: Int, // The month for which the goal is set (1-12)
    val year: Int, // The year for which the goal is set
    val categoryId: Long? = null, // Optional: Category-specific budget
    val minimumGoal: Double, // The minimum savings or spending goal
    val maximumGoal: Double // The maximum spending limit
)
