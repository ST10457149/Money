package com.moneygoat.app.data.entity

/**
 * A simple data class (POJO) used to hold aggregated spending data per category.
 * This is not a Room entity, but a result of a JOIN query in ExpenseDao.
 */
data class CategoryTotal(
    val categoryName: String, // The name of the category
    val totalAmount: Double // The sum of all expenses in this category
)
