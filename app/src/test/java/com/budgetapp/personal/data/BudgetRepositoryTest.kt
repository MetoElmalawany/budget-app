package com.budgetapp.personal.data

import com.budgetapp.personal.model.BudgetStateEntity
import com.budgetapp.personal.model.TransactionEntity
import com.budgetapp.personal.model.TransactionType
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class BudgetRepositoryTest {

    private lateinit var fakeDao: FakeBudgetDao
    private lateinit var repository: BudgetRepository

    @Before
    fun setUp() {
        fakeDao = FakeBudgetDao()
        repository = BudgetRepository(fakeDao)
    }

    @Test
    fun getBudgetStateSync_returnsNullWhenEmpty() = runTest {
        val state = repository.getBudgetStateSync()
        assertNull(state)
    }

    @Test
    fun saveBudgetState_and_getBudgetStateSync_returnsSavedState() = runTest {
        val state = BudgetStateEntity(
            id = 1,
            monthlyBudgetRemaining = 1500.0,
            daysRemainingInMonth = 25,
            activeMonthYear = "2026-11",
            lastClosedDate = "2026-11-01"
        )
        repository.saveBudgetState(state)

        val retrieved = repository.getBudgetStateSync()
        assertNotNull(retrieved)
        assertEquals(1500.0, retrieved?.monthlyBudgetRemaining ?: 0.0, 0.001)
        assertEquals(25, retrieved?.daysRemainingInMonth)
        assertEquals("2026-11", retrieved?.activeMonthYear)
        assertEquals("2026-11-01", retrieved?.lastClosedDate)
    }

    @Test
    fun budgetStateFlow_emitsUpdatedState() = runTest {
        val state = BudgetStateEntity(
            id = 1,
            monthlyBudgetRemaining = 2000.0,
            daysRemainingInMonth = 30,
            activeMonthYear = "2026-11"
        )
        repository.saveBudgetState(state)

        val emitted = repository.budgetState.first()
        assertEquals(2000.0, emitted?.monthlyBudgetRemaining ?: 0.0, 0.001)
        assertEquals(30, emitted?.daysRemainingInMonth)
    }

    @Test
    fun addTransaction_insertsAndReturnsGeneratedId() = runTest {
        val tx = TransactionEntity(
            amount = 25.50,
            description = "Lunch",
            timestamp = 1000L,
            type = TransactionType.DAILY_EXPENSE,
            date = "2026-11-02",
            isSettled = false
        )
        val id = repository.addTransaction(tx)
        assertTrue(id > 0)

        val active = repository.getActiveTodayTransactions("2026-11-02").first()
        assertEquals(1, active.size)
        assertEquals("Lunch", active[0].description)
        assertEquals(id, active[0].id)
    }

    @Test
    fun getActiveTodayTransactions_ordersByTimestampDesc() = runTest {
        val tx1 = TransactionEntity(
            id = 1,
            amount = 10.0,
            description = "Coffee",
            timestamp = 1000L,
            type = TransactionType.DAILY_EXPENSE,
            date = "2026-11-02"
        )
        val tx2 = TransactionEntity(
            id = 2,
            amount = 35.0,
            description = "Groceries",
            timestamp = 2000L,
            type = TransactionType.DAILY_EXPENSE,
            date = "2026-11-02"
        )
        repository.addTransaction(tx1)
        repository.addTransaction(tx2)

        val active = repository.getActiveTodayTransactions("2026-11-02").first()
        assertEquals(2, active.size)
        assertEquals("Groceries", active[0].description)
        assertEquals("Coffee", active[1].description)
    }

    @Test
    fun getActiveTodayExpensesSync_onlyReturnsUnsettledDailyExpensesForDate() = runTest {
        val expense = TransactionEntity(
            id = 1,
            amount = 15.0,
            description = "Snack",
            timestamp = 1000L,
            type = TransactionType.DAILY_EXPENSE,
            date = "2026-11-02",
            isSettled = false
        )
        val addition = TransactionEntity(
            id = 2,
            amount = 500.0,
            description = "Bonus",
            timestamp = 2000L,
            type = TransactionType.MONTHLY_ADDITION,
            date = "2026-11-02",
            isSettled = false
        )
        val otherDayExpense = TransactionEntity(
            id = 3,
            amount = 20.0,
            description = "Yesterday coffee",
            timestamp = 500L,
            type = TransactionType.DAILY_EXPENSE,
            date = "2026-11-01",
            isSettled = false
        )
        val settledExpense = TransactionEntity(
            id = 4,
            amount = 40.0,
            description = "Settled expense",
            timestamp = 600L,
            type = TransactionType.DAILY_EXPENSE,
            date = "2026-11-02",
            isSettled = true
        )

        repository.addTransaction(expense)
        repository.addTransaction(addition)
        repository.addTransaction(otherDayExpense)
        repository.addTransaction(settledExpense)

        val expenses = repository.getActiveTodayExpensesSync("2026-11-02")
        assertEquals(1, expenses.size)
        assertEquals("Snack", expenses[0].description)
        assertEquals(TransactionType.DAILY_EXPENSE, expenses[0].type)
    }

    @Test
    fun deleteTransaction_removesTransaction() = runTest {
        val tx = TransactionEntity(
            id = 10,
            amount = 12.0,
            description = "Book",
            timestamp = 1000L,
            type = TransactionType.DAILY_EXPENSE,
            date = "2026-11-02"
        )
        repository.addTransaction(tx)
        assertEquals(1, repository.getActiveTodayTransactions("2026-11-02").first().size)

        repository.deleteTransaction(10)
        assertEquals(0, repository.getActiveTodayTransactions("2026-11-02").first().size)
    }

    @Test
    fun settleTransactionsForDate_marksAllUnsettledTransactionsAsSettled() = runTest {
        val tx1 = TransactionEntity(
            id = 1,
            amount = 12.0,
            description = "Item 1",
            timestamp = 1000L,
            type = TransactionType.DAILY_EXPENSE,
            date = "2026-11-02",
            isSettled = false
        )
        val tx2 = TransactionEntity(
            id = 2,
            amount = 18.0,
            description = "Item 2",
            timestamp = 2000L,
            type = TransactionType.DAILY_EXPENSE,
            date = "2026-11-02",
            isSettled = false
        )
        val otherDateTx = TransactionEntity(
            id = 3,
            amount = 25.0,
            description = "Tomorrow item",
            timestamp = 3000L,
            type = TransactionType.DAILY_EXPENSE,
            date = "2026-11-03",
            isSettled = false
        )

        repository.addTransaction(tx1)
        repository.addTransaction(tx2)
        repository.addTransaction(otherDateTx)

        repository.settleTransactionsForDate("2026-11-02")

        val activeToday = repository.getActiveTodayTransactions("2026-11-02").first()
        assertTrue(activeToday.isEmpty())

        val activeTomorrow = repository.getActiveTodayTransactions("2026-11-03").first()
        assertEquals(1, activeTomorrow.size)
        assertFalse(activeTomorrow[0].isSettled)
    }

    @Test
    fun clearBudgetState_and_clearTransactions() = runTest {
        repository.saveBudgetState(
            BudgetStateEntity(
                id = 1,
                monthlyBudgetRemaining = 100.0,
                daysRemainingInMonth = 5,
                activeMonthYear = "2026-11"
            )
        )
        repository.addTransaction(
            TransactionEntity(
                id = 1,
                amount = 10.0,
                description = "Test",
                timestamp = 1000L,
                type = TransactionType.DAILY_EXPENSE,
                date = "2026-11-02"
            )
        )

        repository.clearBudgetState()
        assertNull(repository.getBudgetStateSync())

        repository.clearTransactions()
        assertTrue(repository.getActiveTodayTransactions("2026-11-02").first().isEmpty())
    }

    private class FakeBudgetDao : BudgetDao {
        private val budgetStateFlow = MutableStateFlow<BudgetStateEntity?>(null)
        private val transactionsFlow = MutableStateFlow<List<TransactionEntity>>(emptyList())
        private var nextId = 1L

        override fun getBudgetState(): Flow<BudgetStateEntity?> = budgetStateFlow

        override suspend fun getBudgetStateSync(): BudgetStateEntity? = budgetStateFlow.value

        override suspend fun saveBudgetState(state: BudgetStateEntity) {
            budgetStateFlow.value = state
        }

        override fun getActiveTodayTransactions(date: String): Flow<List<TransactionEntity>> {
            return transactionsFlow.map { list ->
                list.filter { it.date == date && !it.isSettled }
                    .sortedByDescending { it.timestamp }
            }
        }

        override suspend fun getActiveTodayExpensesSync(date: String): List<TransactionEntity> {
            return transactionsFlow.value.filter {
                it.date == date && !it.isSettled && it.type == TransactionType.DAILY_EXPENSE
            }
        }

        override suspend fun insertTransaction(transaction: TransactionEntity): Long {
            val id = if (transaction.id == 0L) nextId++ else transaction.id
            val entity = transaction.copy(id = id)
            val current = transactionsFlow.value.toMutableList()
            current.removeAll { it.id == id }
            current.add(entity)
            transactionsFlow.value = current
            return id
        }

        override suspend fun deleteTransaction(id: Long) {
            transactionsFlow.value = transactionsFlow.value.filterNot { it.id == id }
        }

        override suspend fun settleTransactionsForDate(date: String) {
            transactionsFlow.value = transactionsFlow.value.map {
                if (it.date == date && !it.isSettled) {
                    it.copy(isSettled = true)
                } else {
                    it
                }
            }
        }

        override suspend fun clearBudgetState() {
            budgetStateFlow.value = null
        }

        override suspend fun clearTransactions() {
            transactionsFlow.value = emptyList()
        }
    }
}
