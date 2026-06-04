package com.moneygoat.app.data.entity

import androidx.room.Embedded
import androidx.room.Relation

/**
 * Data class representing an expense combined with its category details.
 */
data class ExpenseWithCategory(
    @Embedded val expense: Expense,
    @Relation(
        parentColumn = "categoryId",
        entityColumn = "id"
    )
    val category: Category?
)
