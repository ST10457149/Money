package com.moneygoat.app.data.entity

import androidx.room.Embedded

/**
 * Data class to hold a category along with the count of expenses assigned to it.
 */
data class CategoryWithCount(
    @Embedded val category: Category,
    val expenseCount: Int
)
