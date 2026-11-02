package com.budgetapp.personal.ui.util

import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlin.math.abs

object FormatUtils {

    fun formatCurrency(amount: Double): String {
        val formatted = String.format(Locale.US, "€%,.2f", abs(amount))
        return if (amount < 0.0) "-$formatted" else formatted
    }

    fun formatTransactionAmount(amount: Double, isExpense: Boolean): String {
        val formatted = String.format(Locale.US, "€%,.2f", abs(amount))
        return if (isExpense) "-$formatted" else "+$formatted"
    }

    fun formatTime(timestamp: Long): String {
        val sdf = SimpleDateFormat("h:mm a", Locale.getDefault())
        return sdf.format(Date(timestamp))
    }
}
