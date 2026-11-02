package com.budgetapp.personal.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "budget_state")
data class BudgetStateEntity(
    @PrimaryKey val id: Int = 1,
    val monthlyBudgetRemaining: Double,
    val daysRemainingInMonth: Int,
    val activeMonthYear: String,
    val lastClosedDate: String = "",
    val luxuryBudget: Double = 0.0
)
