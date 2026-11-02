package com.budgetapp.personal.ui

import com.budgetapp.personal.data.BudgetDao
import com.budgetapp.personal.data.BudgetRepository
import com.budgetapp.personal.model.BudgetStateEntity
import com.budgetapp.personal.model.TransactionEntity
import com.budgetapp.personal.model.TransactionType
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import java.time.LocalDate

@OptIn(ExperimentalCoroutinesApi::class)
class BudgetViewModelTest {

    private val testDispatcher = StandardTestDispatcher()
    private lateinit var fakeDao: FakeBudgetDao
    private lateinit var repository: BudgetRepository
    private lateinit var viewModel: BudgetViewModel

    // Fixed date: Nov 2, 2026 in a 30-day month -> 30 - 2 + 1 = 29 days remaining
    private val fixedDate = LocalDate.of(2026, 11, 2)

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        fakeDao = FakeBudgetDao()
        repository = BudgetRepository(fakeDao)
        viewModel = BudgetViewModel(
            repository = repository,
            dateProvider = { fixedDate }
        )
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun testInitialState_isNotInitialized() = runTest {
        advanceUntilIdle()
        val state = viewModel.uiState.value
        assertFalse(state.isInitialized)
        assertEquals(0.0, state.monthlyBudgetRemaining, 0.001)
        assertEquals(0, state.daysRemainingInMonth)
        assertEquals(0.0, state.dailyBudget, 0.001)
        assertEquals(0.0, state.todaySpent, 0.001)
        assertEquals(0.0, state.todayRemaining, 0.001)
        assertEquals(0.0, state.luxuryBudget, 0.001)
        assertFalse(state.isDeficit)
        assertTrue(state.todayTransactions.isEmpty())
        assertEquals("2026-11-02", state.activeDateIso)
        assertTrue(state.currentDateFormatted.isNotEmpty())
    }

    @Test
    fun testInitializeBudget() = runTest {
        // Nov 2 in 30-day month: 29 days remaining
        // 2900.0 / 29 days = 100.00 daily target
        viewModel.initializeBudget(2900.0)
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertTrue(state.isInitialized)
        assertEquals(2900.0, state.monthlyBudgetRemaining, 0.001)
        assertEquals(29, state.daysRemainingInMonth)
        assertEquals(100.0, state.dailyBudget, 0.001)
        assertEquals(0.0, state.todaySpent, 0.001)
        assertEquals(100.0, state.todayRemaining, 0.001)
        assertEquals(0.0, state.luxuryBudget, 0.001)
        assertFalse(state.isDeficit)
        assertTrue(state.todayTransactions.isEmpty())
    }

    @Test
    fun testInitializeBudget_invalidAmountIgnored() = runTest {
        viewModel.initializeBudget(0.0)
        advanceUntilIdle()
        assertFalse(viewModel.uiState.value.isInitialized)

        viewModel.initializeBudget(-500.0)
        advanceUntilIdle()
        assertFalse(viewModel.uiState.value.isInitialized)
    }

    @Test
    fun testUpdateMonthlyBudget() = runTest {
        viewModel.initializeBudget(2900.0) // 29 days remaining -> daily €100.0
        advanceUntilIdle()

        viewModel.updateMonthlyBudget(3480.0) // 3480 / 29 = 120.0
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertEquals(3480.0, state.monthlyBudgetRemaining, 0.001)
        assertEquals(120.0, state.dailyBudget, 0.001)
        assertEquals(120.0, state.todayRemaining, 0.001)
    }

    @Test
    fun testUpdateMonthlyBudget_negativeIgnored() = runTest {
        viewModel.initializeBudget(2900.0)
        advanceUntilIdle()

        viewModel.updateMonthlyBudget(-100.0)
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertEquals(2900.0, state.monthlyBudgetRemaining, 0.001)
    }

    @Test
    fun testUpdateLuxuryBudget() = runTest {
        viewModel.initializeBudget(2900.0)
        advanceUntilIdle()
        assertEquals(0.0, viewModel.uiState.value.luxuryBudget, 0.001)

        viewModel.updateLuxuryBudget(500.0)
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertEquals(500.0, state.luxuryBudget, 0.001)
        // Monthly and daily budget unaffected
        assertEquals(2900.0, state.monthlyBudgetRemaining, 0.001)
        assertEquals(100.0, state.dailyBudget, 0.001)
    }

    @Test
    fun testUpdateLuxuryBudget_negativeIgnored() = runTest {
        viewModel.initializeBudget(2900.0)
        advanceUntilIdle()

        viewModel.updateLuxuryBudget(200.0)
        advanceUntilIdle()
        assertEquals(200.0, viewModel.uiState.value.luxuryBudget, 0.001)

        viewModel.updateLuxuryBudget(-50.0)
        advanceUntilIdle()
        assertEquals(200.0, viewModel.uiState.value.luxuryBudget, 0.001)
    }

    @Test
    fun testAddDailyExpense() = runTest {
        viewModel.initializeBudget(2900.0)
        advanceUntilIdle()

        // Daily target is €100.0. Add expense of €35.50
        viewModel.addTransaction(
            amount = 35.50,
            description = "Groceries",
            type = TransactionType.DAILY_EXPENSE
        )
        advanceUntilIdle()

        var state = viewModel.uiState.value
        assertEquals(35.50, state.todaySpent, 0.001)
        assertEquals(64.50, state.todayRemaining, 0.001)
        assertFalse(state.isDeficit)
        assertEquals(1, state.todayTransactions.size)
        assertEquals("Groceries", state.todayTransactions[0].description)
        assertEquals(35.50, state.todayTransactions[0].amount, 0.001)

        // Add second expense causing deficit: €80.00 -> total spent €115.50, remaining -€15.50
        viewModel.addTransaction(
            amount = 80.00,
            description = "Dinner",
            type = TransactionType.DAILY_EXPENSE
        )
        advanceUntilIdle()

        state = viewModel.uiState.value
        assertEquals(115.50, state.todaySpent, 0.001)
        assertEquals(-15.50, state.todayRemaining, 0.001)
        assertTrue(state.isDeficit)
        assertEquals(2, state.todayTransactions.size)
    }

    @Test
    fun testAddTransaction_invalidAmountIgnored() = runTest {
        viewModel.initializeBudget(2900.0)
        advanceUntilIdle()

        viewModel.addTransaction(0.0, "Zero", TransactionType.DAILY_EXPENSE)
        advanceUntilIdle()
        assertTrue(viewModel.uiState.value.todayTransactions.isEmpty())

        viewModel.addTransaction(-10.0, "Negative", TransactionType.DAILY_EXPENSE)
        advanceUntilIdle()
        assertTrue(viewModel.uiState.value.todayTransactions.isEmpty())
    }

    @Test
    fun testAddMonthlyAddition() = runTest {
        viewModel.initializeBudget(2900.0) // 29 days remaining -> daily €100.0
        advanceUntilIdle()

        // Add €290.0 income: new monthly = €3190.0 -> new daily = €3190 / 29 = €110.0
        viewModel.addTransaction(
            amount = 290.0,
            description = "Bonus",
            type = TransactionType.MONTHLY_ADDITION
        )
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertEquals(3190.0, state.monthlyBudgetRemaining, 0.001)
        assertEquals(110.0, state.dailyBudget, 0.001)
        assertEquals(0.0, state.todaySpent, 0.001) // Monthly addition is not an expense
        assertEquals(110.0, state.todayRemaining, 0.001)
        assertEquals(1, state.todayTransactions.size)
        assertEquals(TransactionType.MONTHLY_ADDITION, state.todayTransactions[0].type)
    }

    @Test
    fun testAddMonthlyExpense() = runTest {
        viewModel.initializeBudget(2900.0) // 29 days left -> daily €100.0
        advanceUntilIdle()

        // Monthly bill: €290.0 deducted from monthly budget pool
        viewModel.addTransaction(
            amount = 290.0,
            description = "Internet & Electricity",
            type = TransactionType.MONTHLY_EXPENSE
        )
        advanceUntilIdle()

        val state = viewModel.uiState.value
        // Monthly budget decreased from 2900 to 2610
        assertEquals(2610.0, state.monthlyBudgetRemaining, 0.001)
        // Daily budget recalculated: 2610 / 29 = 90.0
        assertEquals(90.0, state.dailyBudget, 0.001)
        // todaySpent remains 0.0 because it's a monthly expense, not a daily expense
        assertEquals(0.0, state.todaySpent, 0.001)
        assertEquals(90.0, state.todayRemaining, 0.001)
        assertEquals(1, state.todayTransactions.size)
        assertEquals(TransactionType.MONTHLY_EXPENSE, state.todayTransactions[0].type)
    }

    @Test
    fun testAddLuxuryExpense() = runTest {
        viewModel.initializeBudget(2900.0)
        advanceUntilIdle()
        viewModel.updateLuxuryBudget(300.0)
        advanceUntilIdle()

        // Luxury purchase: €120.0
        viewModel.addTransaction(
            amount = 120.0,
            description = "Designer Shoes",
            type = TransactionType.LUXURY_EXPENSE
        )
        advanceUntilIdle()

        val state = viewModel.uiState.value
        // Luxury pool decreased: 300 - 120 = 180.0
        assertEquals(180.0, state.luxuryBudget, 0.001)
        // Daily and monthly budgets unaffected
        assertEquals(2900.0, state.monthlyBudgetRemaining, 0.001)
        assertEquals(100.0, state.dailyBudget, 0.001)
        assertEquals(0.0, state.todaySpent, 0.001)
        assertEquals(100.0, state.todayRemaining, 0.001)
        assertEquals(1, state.todayTransactions.size)
        assertEquals(TransactionType.LUXURY_EXPENSE, state.todayTransactions[0].type)
    }

    @Test
    fun testAddLuxuryAddition() = runTest {
        viewModel.initializeBudget(2900.0)
        advanceUntilIdle()
        viewModel.updateLuxuryBudget(150.0)
        advanceUntilIdle()

        viewModel.addTransaction(
            amount = 100.0,
            description = "Gift money",
            type = TransactionType.LUXURY_ADDITION
        )
        advanceUntilIdle()

        val state = viewModel.uiState.value
        // Luxury pool increased: 150 + 100 = 250.0
        assertEquals(250.0, state.luxuryBudget, 0.001)
        // Monthly pool unchanged
        assertEquals(2900.0, state.monthlyBudgetRemaining, 0.001)
        assertEquals(100.0, state.dailyBudget, 0.001)
        assertEquals(0.0, state.todaySpent, 0.001)
        assertEquals(1, state.todayTransactions.size)
        assertEquals(TransactionType.LUXURY_ADDITION, state.todayTransactions[0].type)
    }

    @Test
    fun testDeleteExpense() = runTest {
        viewModel.initializeBudget(2900.0)
        advanceUntilIdle()

        viewModel.addTransaction(40.0, "Coffee & Snacks", TransactionType.DAILY_EXPENSE)
        advanceUntilIdle()

        val tx = viewModel.uiState.value.todayTransactions[0]
        assertEquals(40.0, viewModel.uiState.value.todaySpent, 0.001)
        assertEquals(60.0, viewModel.uiState.value.todayRemaining, 0.001)

        // Delete expense and verify todayRemaining is restored to 100.0
        viewModel.deleteTransaction(tx)
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertEquals(0.0, state.todaySpent, 0.001)
        assertEquals(100.0, state.todayRemaining, 0.001)
        assertTrue(state.todayTransactions.isEmpty())
    }

    @Test
    fun testDeleteExpenseById() = runTest {
        viewModel.initializeBudget(2900.0)
        advanceUntilIdle()

        viewModel.addTransaction(40.0, "Books", TransactionType.DAILY_EXPENSE)
        advanceUntilIdle()

        val txId = viewModel.uiState.value.todayTransactions[0].id
        viewModel.deleteTransaction(txId)
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertEquals(0.0, state.todaySpent, 0.001)
        assertEquals(100.0, state.todayRemaining, 0.001)
        assertTrue(state.todayTransactions.isEmpty())
    }

    @Test
    fun testDeleteMonthlyAddition() = runTest {
        viewModel.initializeBudget(2900.0) // 29 days left -> daily €100.0
        advanceUntilIdle()

        viewModel.addTransaction(290.0, "Consulting", TransactionType.MONTHLY_ADDITION)
        advanceUntilIdle()

        assertEquals(3190.0, viewModel.uiState.value.monthlyBudgetRemaining, 0.001)
        assertEquals(110.0, viewModel.uiState.value.dailyBudget, 0.001)

        val tx = viewModel.uiState.value.todayTransactions[0]
        viewModel.deleteTransaction(tx)
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertEquals(2900.0, state.monthlyBudgetRemaining, 0.001)
        assertEquals(100.0, state.dailyBudget, 0.001)
        assertTrue(state.todayTransactions.isEmpty())
    }

    @Test
    fun testDeleteMonthlyExpense_restoresMonthlyBudget() = runTest {
        viewModel.initializeBudget(2900.0) // daily 100.0
        advanceUntilIdle()

        viewModel.addTransaction(290.0, "Gym subscription", TransactionType.MONTHLY_EXPENSE)
        advanceUntilIdle()

        assertEquals(2610.0, viewModel.uiState.value.monthlyBudgetRemaining, 0.001)
        assertEquals(90.0, viewModel.uiState.value.dailyBudget, 0.001)

        val tx = viewModel.uiState.value.todayTransactions[0]
        viewModel.deleteTransaction(tx)
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertEquals(2900.0, state.monthlyBudgetRemaining, 0.001)
        assertEquals(100.0, state.dailyBudget, 0.001)
        assertTrue(state.todayTransactions.isEmpty())
    }

    @Test
    fun testDeleteLuxuryExpense_restoresLuxuryBudget() = runTest {
        viewModel.initializeBudget(2900.0)
        advanceUntilIdle()
        viewModel.updateLuxuryBudget(200.0)
        advanceUntilIdle()

        viewModel.addTransaction(80.0, "Smart Watch", TransactionType.LUXURY_EXPENSE)
        advanceUntilIdle()

        assertEquals(120.0, viewModel.uiState.value.luxuryBudget, 0.001)

        val tx = viewModel.uiState.value.todayTransactions[0]
        viewModel.deleteTransaction(tx)
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertEquals(200.0, state.luxuryBudget, 0.001)
        assertTrue(state.todayTransactions.isEmpty())
    }

    @Test
    fun testDeleteLuxuryAddition_decreasesLuxuryBudget() = runTest {
        viewModel.initializeBudget(2900.0)
        advanceUntilIdle()
        viewModel.updateLuxuryBudget(100.0)
        advanceUntilIdle()

        viewModel.addTransaction(50.0, "Sold old phone", TransactionType.LUXURY_ADDITION)
        advanceUntilIdle()

        assertEquals(150.0, viewModel.uiState.value.luxuryBudget, 0.001)

        val tx = viewModel.uiState.value.todayTransactions[0]
        viewModel.deleteTransaction(tx)
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertEquals(100.0, state.luxuryBudget, 0.001)
        assertTrue(state.todayTransactions.isEmpty())
    }

    @Test
    fun testDeleteTransactionsById_forMonthlyAndLuxuryTypes() = runTest {
        viewModel.initializeBudget(2900.0)
        advanceUntilIdle()
        viewModel.updateLuxuryBudget(100.0)
        advanceUntilIdle()

        viewModel.addTransaction(290.0, "Car Insurance", TransactionType.MONTHLY_EXPENSE)
        advanceUntilIdle()
        assertEquals(2610.0, viewModel.uiState.value.monthlyBudgetRemaining, 0.001)

        val monthlyTxId = viewModel.uiState.value.todayTransactions[0].id
        viewModel.deleteTransaction(monthlyTxId)
        advanceUntilIdle()
        assertEquals(2900.0, viewModel.uiState.value.monthlyBudgetRemaining, 0.001)

        viewModel.addTransaction(50.0, "Gadget", TransactionType.LUXURY_EXPENSE)
        advanceUntilIdle()
        assertEquals(50.0, viewModel.uiState.value.luxuryBudget, 0.001)

        val luxuryTxId = viewModel.uiState.value.todayTransactions[0].id
        viewModel.deleteTransaction(luxuryTxId)
        advanceUntilIdle()
        assertEquals(100.0, viewModel.uiState.value.luxuryBudget, 0.001)
    }

    @Test
    fun testEndDay() = runTest {
        // Start: monthly €2900, 29 days remaining (daily €100)
        // Spent €40 today (surplus +€60)
        viewModel.initializeBudget(2900.0)
        advanceUntilIdle()

        viewModel.addTransaction(40.0, "Lunch", TransactionType.DAILY_EXPENSE)
        advanceUntilIdle()

        assertEquals(40.0, viewModel.uiState.value.todaySpent, 0.001)

        // End day:
        // newMonthly = €2900 - €40 = €2860
        // newDays = 29 - 1 = 28
        // newDaily = €2860 / 28 = €102.14
        viewModel.endDay()
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertEquals(2860.0, state.monthlyBudgetRemaining, 0.001)
        assertEquals(28, state.daysRemainingInMonth)
        assertEquals(102.14, state.dailyBudget, 0.001)
        assertEquals(0.0, state.todaySpent, 0.001) // transactions settled
        assertEquals(102.14, state.todayRemaining, 0.001)
        assertFalse(state.isDeficit)
        assertTrue(state.todayTransactions.isEmpty()) // settled transactions excluded from active today
    }

    @Test
    fun testEndDayDeficit() = runTest {
        // Start: monthly €2900, 29 days remaining (daily €100)
        // Spent €150 today (deficit -€50)
        viewModel.initializeBudget(2900.0)
        advanceUntilIdle()

        viewModel.addTransaction(150.0, "Night Out", TransactionType.DAILY_EXPENSE)
        advanceUntilIdle()

        assertTrue(viewModel.uiState.value.isDeficit)

        // End day:
        // newMonthly = €2900 - €150 = €2750
        // newDays = 29 - 1 = 28
        // newDaily = €2750 / 28 = €98.21
        viewModel.endDay()
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertEquals(2750.0, state.monthlyBudgetRemaining, 0.001)
        assertEquals(28, state.daysRemainingInMonth)
        assertEquals(98.21, state.dailyBudget, 0.001)
        assertEquals(0.0, state.todaySpent, 0.001)
        assertEquals(98.21, state.todayRemaining, 0.001)
        assertFalse(state.isDeficit)
        assertTrue(state.todayTransactions.isEmpty())
    }

    @Test
    fun testEndDayWithNoExpenses() = runTest {
        // Start: monthly €2900, 29 days remaining (daily €100)
        // Spent €0 today (surplus +€100)
        viewModel.initializeBudget(2900.0)
        advanceUntilIdle()

        viewModel.endDay()
        advanceUntilIdle()

        // newMonthly = €2900, newDays = 28, newDaily = 2900 / 28 = €103.57
        val state = viewModel.uiState.value
        assertEquals(2900.0, state.monthlyBudgetRemaining, 0.001)
        assertEquals(28, state.daysRemainingInMonth)
        assertEquals(103.57, state.dailyBudget, 0.001)
        assertEquals(0.0, state.todaySpent, 0.001)
        assertEquals(103.57, state.todayRemaining, 0.001)
    }

    @Test
    fun testAliases_workIdentically() = runTest {
        viewModel.onInitializeBudget(2900.0)
        advanceUntilIdle()
        assertTrue(viewModel.uiState.value.isInitialized)

        viewModel.onAddTransaction(50.0, "Coffee", TransactionType.DAILY_EXPENSE)
        advanceUntilIdle()
        assertEquals(50.0, viewModel.uiState.value.todaySpent, 0.001)

        val tx = viewModel.uiState.value.todayTransactions[0]
        viewModel.onDeleteTransaction(tx)
        advanceUntilIdle()
        assertEquals(0.0, viewModel.uiState.value.todaySpent, 0.001)

        viewModel.onAddTransaction(30.0, "Tea", TransactionType.DAILY_EXPENSE)
        advanceUntilIdle()

        viewModel.onEndDay()
        advanceUntilIdle()
        assertEquals(2870.0, viewModel.uiState.value.monthlyBudgetRemaining, 0.001)
        assertEquals(28, viewModel.uiState.value.daysRemainingInMonth)

        viewModel.onUpdateMonthlyBudget(3000.0)
        advanceUntilIdle()
        assertEquals(3000.0, viewModel.uiState.value.monthlyBudgetRemaining, 0.001)

        viewModel.onUpdateLuxuryBudget(450.0)
        advanceUntilIdle()
        assertEquals(450.0, viewModel.uiState.value.luxuryBudget, 0.001)
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
