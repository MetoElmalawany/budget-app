package com.budgetapp.personal.domain

import java.math.BigDecimal
import java.math.RoundingMode

data class EndDayResult(
    val newMonthlyRemaining: Double,
    val newDaysRemaining: Int,
    val newDailyBudget: Double,
    val surplusOrDeficit: Double // positive if surplus (under budget), negative if deficit (over budget)
)

object BudgetEngine {
    fun round(value: Double): Double {
        return BigDecimal.valueOf(value)
            .setScale(2, RoundingMode.HALF_UP)
            .toDouble()
    }

    fun calculateDailyBudget(monthlyRemaining: Double, daysRemaining: Int): Double {
        if (daysRemaining <= 0) return round(monthlyRemaining)
        return round(monthlyRemaining / daysRemaining)
    }

    fun calculateTodayRemaining(dailyBudget: Double, todaySpent: Double): Double {
        return round(dailyBudget - todaySpent)
    }

    fun isDeficit(dailyBudget: Double, todaySpent: Double): Boolean {
        return todaySpent > dailyBudget
    }

    fun endDaySettlement(
        monthlyRemaining: Double,
        todaySpent: Double,
        daysRemaining: Int
    ): EndDayResult {
        val dailyBudget = calculateDailyBudget(monthlyRemaining, daysRemaining)
        val surplusOrDeficit = round(dailyBudget - todaySpent)
        val newMonthly = round(monthlyRemaining - todaySpent)
        val newDays = if (daysRemaining > 0) daysRemaining - 1 else 0
        val newDaily = if (newDays > 0) calculateDailyBudget(newMonthly, newDays) else 0.0

        return EndDayResult(
            newMonthlyRemaining = newMonthly,
            newDaysRemaining = newDays,
            newDailyBudget = newDaily,
            surplusOrDeficit = surplusOrDeficit
        )
    }

    fun addMonthlyIncome(monthlyRemaining: Double, additionAmount: Double): Double {
        return round(monthlyRemaining + additionAmount)
    }

    fun subtractMonthlyExpense(monthlyRemaining: Double, amount: Double): Double {
        return round(monthlyRemaining - amount)
    }

    fun adjustLuxuryBudget(currentLuxury: Double, delta: Double): Double {
        return round(currentLuxury + delta)
    }
}
