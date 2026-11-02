package com.budgetapp.personal.domain

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class BudgetEngineTest {

    @Test
    fun testInitialDailyBudgetCalculation() {
        val dailyBudget = BudgetEngine.calculateDailyBudget(monthlyRemaining = 3000.0, daysRemaining = 30)
        assertEquals(100.00, dailyBudget, 0.001)
    }

    @Test
    fun testDailyRemainingUnderBudget() {
        val dailyBudget = 100.0
        val todaySpent = 35.50
        val remaining = BudgetEngine.calculateTodayRemaining(dailyBudget = dailyBudget, todaySpent = todaySpent)
        val isDeficit = BudgetEngine.isDeficit(dailyBudget = dailyBudget, todaySpent = todaySpent)

        assertEquals(64.50, remaining, 0.001)
        assertFalse(isDeficit)
    }

    @Test
    fun testDailyRemainingDeficit() {
        val dailyBudget = 100.0
        val todaySpent = 120.00
        val remaining = BudgetEngine.calculateTodayRemaining(dailyBudget = dailyBudget, todaySpent = todaySpent)
        val isDeficit = BudgetEngine.isDeficit(dailyBudget = dailyBudget, todaySpent = todaySpent)

        assertEquals(-20.00, remaining, 0.001)
        assertTrue(isDeficit)
    }

    @Test
    fun testEndDaySurplusRollover() {
        // Start: monthly €3,000, 30 days remaining (daily €100)
        // Spent €60 today (surplus +€40)
        // End day: newMonthly = €2,940, newDays = 29, newDaily = €101.38
        val result = BudgetEngine.endDaySettlement(
            monthlyRemaining = 3000.0,
            todaySpent = 60.0,
            daysRemaining = 30
        )

        assertEquals(2940.0, result.newMonthlyRemaining, 0.001)
        assertEquals(29, result.newDaysRemaining)
        assertEquals(40.0, result.surplusOrDeficit, 0.001)
        assertEquals(101.38, result.newDailyBudget, 0.001)
    }

    @Test
    fun testEndDayDeficitDeduction() {
        // Start: monthly €3,000, 30 days remaining (daily €100)
        // Spent €150 today (deficit -€50)
        // End day: newMonthly = €2,850, newDays = 29, newDaily = €98.28
        val result = BudgetEngine.endDaySettlement(
            monthlyRemaining = 3000.0,
            todaySpent = 150.0,
            daysRemaining = 30
        )

        assertEquals(2850.0, result.newMonthlyRemaining, 0.001)
        assertEquals(29, result.newDaysRemaining)
        assertEquals(-50.0, result.surplusOrDeficit, 0.001)
        assertEquals(98.28, result.newDailyBudget, 0.001)
    }

    @Test
    fun testAddMonthlyIncome() {
        // €2,500 + €300 bonus -> €2,800
        val updatedMonthly = BudgetEngine.addMonthlyIncome(
            monthlyRemaining = 2500.0,
            additionAmount = 300.0
        )
        assertEquals(2800.0, updatedMonthly, 0.001)
    }

    @Test
    fun testSubtractMonthlyExpense() {
        // €2,500 - €450 direct expense -> €2,050
        val updatedMonthly = BudgetEngine.subtractMonthlyExpense(
            monthlyRemaining = 2500.0,
            amount = 450.0
        )
        assertEquals(2050.0, updatedMonthly, 0.001)
    }

    @Test
    fun testSubtractMonthlyExpenseWithRounding() {
        val updatedMonthly = BudgetEngine.subtractMonthlyExpense(
            monthlyRemaining = 1000.0,
            amount = 33.333
        )
        assertEquals(966.67, updatedMonthly, 0.001)
    }

    @Test
    fun testAdjustLuxuryBudgetPositive() {
        val updatedLuxury = BudgetEngine.adjustLuxuryBudget(
            currentLuxury = 100.0,
            delta = 50.0
        )
        assertEquals(150.0, updatedLuxury, 0.001)
    }

    @Test
    fun testAdjustLuxuryBudgetNegative() {
        val updatedLuxury = BudgetEngine.adjustLuxuryBudget(
            currentLuxury = 150.0,
            delta = -75.50
        )
        assertEquals(74.50, updatedLuxury, 0.001)
    }

    @Test
    fun testAdjustLuxuryBudgetWithRounding() {
        val updatedLuxury = BudgetEngine.adjustLuxuryBudget(
            currentLuxury = 100.0,
            delta = 12.345
        )
        assertEquals(112.35, updatedLuxury, 0.001)
    }

    @Test
    fun testLastDayOfMonthSettlement() {
        // daysRemaining = 1, spent matches budget -> newDays = 0, newDaily = 0.0
        val result = BudgetEngine.endDaySettlement(
            monthlyRemaining = 100.0,
            todaySpent = 100.0,
            daysRemaining = 1
        )

        assertEquals(0.0, result.newMonthlyRemaining, 0.001)
        assertEquals(0, result.newDaysRemaining)
        assertEquals(0.0, result.surplusOrDeficit, 0.001)
        assertEquals(0.0, result.newDailyBudget, 0.001)
    }

    @Test
    fun testRoundingHalfUp() {
        assertEquals(33.33, BudgetEngine.round(33.33333), 0.0001)
        assertEquals(33.34, BudgetEngine.round(33.335), 0.0001)
        assertEquals(10.55, BudgetEngine.round(10.545), 0.0001)
    }

    @Test
    fun testCalculateDailyBudgetZeroOrNegativeDays() {
        assertEquals(500.0, BudgetEngine.calculateDailyBudget(500.0, 0), 0.001)
        assertEquals(500.0, BudgetEngine.calculateDailyBudget(500.0, -5), 0.001)
    }
}
