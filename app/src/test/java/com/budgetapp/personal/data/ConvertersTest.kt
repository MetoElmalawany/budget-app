package com.budgetapp.personal.data

import com.budgetapp.personal.model.TransactionType
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class ConvertersTest {

    private val converters = Converters()

    @Test
    fun fromTransactionType_convertsEnumToString() {
        assertEquals("DAILY_EXPENSE", converters.fromTransactionType(TransactionType.DAILY_EXPENSE))
        assertEquals("MONTHLY_ADDITION", converters.fromTransactionType(TransactionType.MONTHLY_ADDITION))
        assertNull(converters.fromTransactionType(null))
    }

    @Test
    fun toTransactionType_convertsStringToEnum() {
        assertEquals(TransactionType.DAILY_EXPENSE, converters.toTransactionType("DAILY_EXPENSE"))
        assertEquals(TransactionType.MONTHLY_ADDITION, converters.toTransactionType("MONTHLY_ADDITION"))
        assertNull(converters.toTransactionType(null))
    }
}
