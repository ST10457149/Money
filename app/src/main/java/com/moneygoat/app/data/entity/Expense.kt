package com.moneygoat.app.data.entity
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

/**
 * Data class representing an expense entry.
 * It has foreign key relationships with User and Category entities.
 */
@Entity(tableName = "expenses",
    foreignKeys = [
        ForeignKey(entity = User::class, parentColumns = ["id"], childColumns = ["userId"], onDelete = ForeignKey.CASCADE),
        ForeignKey(entity = Category::class, parentColumns = ["id"], childColumns = ["categoryId"], onDelete = ForeignKey.CASCADE)],
    indices = [Index(value = ["userId"]), Index(value = ["categoryId"])])
data class Expense(
    @PrimaryKey(autoGenerate = true) val id: Long = 0, // Unique identifier for each expense
    val date: String, // Date of the expense
    val startTime: String, // Start time of the expense activity
    val endTime: String, // End time of the expense activity
    val description: String, // Description of what the money was spent on
    val amount: Double, // The monetary value of the expense
    val type: TransactionType = TransactionType.EXPENSE, // Type of transaction (Expense, Income, Transfer)
    val categoryId: Long, // ID of the category this expense belongs to
    val userId: Long, // ID of the user who recorded this expense
    val accountId: Long = 0, // Default to 0 as accounts are now optional/hidden
    val photoPath: String? = null // Optional path to a photo of the receipt
)
