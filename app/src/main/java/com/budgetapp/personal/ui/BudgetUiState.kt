package com.budgetapp.personal.ui

import com.budgetapp.personal.model.TransactionEntity

data class BudgetUiState(
    val isInitialized: Boolean = false,
    val monthlyBudgetRemaining: Double = 0.0,
    val daysRemainingInMonth: Int = 0,
    val dailyBudget: Double = 0.0,
    val todaySpent: Double = 0.0,
    val todayRemaining: Double = 0.0,
    val isDeficit: Boolean = false,
    val todayTransactions: List<TransactionEntity> = emptyList(),
    val currentDateFormatted: String = "",
    val activeDateIso: String = ""
)
