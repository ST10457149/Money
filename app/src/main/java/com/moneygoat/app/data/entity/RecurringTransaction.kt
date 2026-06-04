package com.moneygoat.app.data.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(tableName = "recurring_transactions",
    foreignKeys = [
        ForeignKey(entity = User::class, parentColumns = ["id"], childColumns = ["userId"], onDelete = ForeignKey.CASCADE),
        ForeignKey(entity = Category::class, parentColumns = ["id"], childColumns = ["categoryId"], onDelete = ForeignKey.CASCADE)],
    indices = [Index(value = ["userId"]), Index(value = ["categoryId"])])
data class RecurringTransaction(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val userId: Long,
    val categoryId: Long,
    val description: String,
    val amount: Double,
    val type: TransactionType,
    val frequency: String, // DAILY, WEEKLY, MONTHLY, YEARLY
    val nextDueDate: String,
    val isActive: Boolean = true
)
