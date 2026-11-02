package com.budgetapp.personal.ui.util

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class FormatUtilsTest {

    @Test
    fun `formatCurrency formats positive amounts with euro symbol and two decimal places`() {
        assertEquals("€2,450.00", FormatUtils.formatCurrency(2450.0))
        assertEquals("€75.50", FormatUtils.formatCurrency(75.50))
        assertEquals("€100.00", FormatUtils.formatCurrency(100.0))
        assertEquals("€0.00", FormatUtils.formatCurrency(0.0))
    }

    @Test
    fun `formatCurrency formats negative amounts with leading minus before euro symbol`() {
        assertEquals("-€15.00", FormatUtils.formatCurrency(-15.0))
        assertEquals("-€0.50", FormatUtils.formatCurrency(-0.5))
    }

    @Test
    fun `formatTransactionAmount formats expense with minus and addition with plus`() {
        assertEquals("-€15.00", FormatUtils.formatTransactionAmount(15.0, isExpense = true))
        assertEquals("+€50.00", FormatUtils.formatTransactionAmount(50.0, isExpense = false))
        assertEquals("+€1,200.00", FormatUtils.formatTransactionAmount(1200.0, isExpense = false))
    }

    @Test
    fun `formatTime formats timestamp into non-empty time string`() {
        val now = System.currentTimeMillis()
        val formatted = FormatUtils.formatTime(now)
        assertTrue(formatted.contains("AM") || formatted.contains("PM"))
    }
}
