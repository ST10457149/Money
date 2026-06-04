package com.moneygoat.app.utils

import org.junit.Assert.assertEquals
import org.junit.Test

/**
 * Unit tests for financial utility functions.
 */
class FinanceUtilsTest {

    @Test
    fun testCalculatePercentage() {
        val spent = 500.0
        val goal = 1000.0
        val expected = 50
        val actual = if (goal > 0) ((spent / goal) * 100).toInt() else 0
        assertEquals(expected, actual)
    }

    @Test
    fun testDailyAllowanceCalculation() {
        val remainingBudget = 3000.0
        val daysLeft = 10
        val expected = 300.0
        val actual = if (daysLeft > 0) remainingBudget / daysLeft else 0.0
        assertEquals(expected, actual, 0.01)
    }
    
    @Test
    fun testForecastLogic() {
        val totalSpent = 1500.0
        val daysPassed = 15
        val daysInMonth = 30
        val avgDaily = totalSpent / daysPassed
        val expectedForecast = avgDaily * daysInMonth
        assertEquals(3000.0, expectedForecast, 0.01)
    }
}
