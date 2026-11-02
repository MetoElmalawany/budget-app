package com.budgetapp.personal.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.budgetapp.personal.data.BudgetRepository
import com.budgetapp.personal.domain.BudgetEngine
import com.budgetapp.personal.model.BudgetStateEntity
import com.budgetapp.personal.model.TransactionEntity
import com.budgetapp.personal.model.TransactionType
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Locale

class BudgetViewModel(
    private val repository: BudgetRepository,
    private val dateProvider: () -> LocalDate = { LocalDate.now() },
    sharingStarted: SharingStarted = SharingStarted.Eagerly
) : ViewModel() {

    private val _currentDate = MutableStateFlow(dateProvider())

    @OptIn(ExperimentalCoroutinesApi::class)
    val uiState: StateFlow<BudgetUiState> = _currentDate
        .flatMapLatest { date ->
            val activeDateIso = date.toString()
            val formattedDate = formatDate(date)
            combine(
                repository.budgetState,
                repository.getActiveTodayTransactions(activeDateIso)
            ) { stateEntity, transactions ->
                if (stateEntity == null) {
                    BudgetUiState(
                        isInitialized = false,
                        currentDateFormatted = formattedDate,
                        activeDateIso = activeDateIso
                    )
                } else {
                    val todayExpenses = transactions.filter { it.type == TransactionType.DAILY_EXPENSE }
                    val todaySpent = BudgetEngine.round(todayExpenses.sumOf { it.amount })
                    val dailyBudget = BudgetEngine.calculateDailyBudget(
                        stateEntity.monthlyBudgetRemaining,
                        stateEntity.daysRemainingInMonth
                    )
                    val todayRemaining = BudgetEngine.calculateTodayRemaining(dailyBudget, todaySpent)
                    val isDeficit = BudgetEngine.isDeficit(dailyBudget, todaySpent)

                    BudgetUiState(
                        isInitialized = true,
                        monthlyBudgetRemaining = stateEntity.monthlyBudgetRemaining,
                        daysRemainingInMonth = stateEntity.daysRemainingInMonth,
                        dailyBudget = dailyBudget,
                        todaySpent = todaySpent,
                        todayRemaining = todayRemaining,
                        isDeficit = isDeficit,
                        todayTransactions = transactions,
                        currentDateFormatted = formattedDate,
                        activeDateIso = activeDateIso,
                        luxuryBudget = stateEntity.luxuryBudget
                    )
                }
            }
        }
        .stateIn(
            scope = viewModelScope,
            started = sharingStarted,
            initialValue = run {
                val initialDate = dateProvider()
                BudgetUiState(
                    isInitialized = false,
                    currentDateFormatted = formatDate(initialDate),
                    activeDateIso = initialDate.toString()
                )
            }
        )

    fun initializeBudget(startingMonthlyBudget: Double) {
        if (startingMonthlyBudget <= 0.0) return
        viewModelScope.launch {
            val date = _currentDate.value
            val daysInMonth = date.lengthOfMonth()
            val daysRemaining = daysInMonth - date.dayOfMonth + 1
            val monthYear = "${date.year}-${String.format(Locale.ROOT, "%02d", date.monthValue)}"
            val state = BudgetStateEntity(
                id = 1,
                monthlyBudgetRemaining = BudgetEngine.round(startingMonthlyBudget),
                daysRemainingInMonth = daysRemaining,
                activeMonthYear = monthYear,
                lastClosedDate = "",
                luxuryBudget = 0.0
            )
            repository.saveBudgetState(state)
        }
    }

    fun updateMonthlyBudget(newMonthlyBudget: Double) {
        if (newMonthlyBudget < 0.0) return
        viewModelScope.launch {
            repository.updateMonthlyBudget(BudgetEngine.round(newMonthlyBudget))
        }
    }

    fun updateLuxuryBudget(newLuxuryBudget: Double) {
        if (newLuxuryBudget < 0.0) return
        viewModelScope.launch {
            repository.updateLuxuryBudget(BudgetEngine.round(newLuxuryBudget))
        }
    }

    fun addTransaction(amount: Double, description: String, type: TransactionType) {
        if (amount <= 0.0) return
        viewModelScope.launch {
            val roundedAmount = BudgetEngine.round(amount)
            val activeDateIso = _currentDate.value.toString()

            val currentState = repository.getBudgetStateSync()
            if (currentState != null) {
                when (type) {
                    TransactionType.DAILY_EXPENSE -> {
                        // Daily expenses are tracked through the active transactions flow
                    }
                    TransactionType.MONTHLY_EXPENSE -> {
                        val newMonthly = BudgetEngine.subtractMonthlyExpense(
                            currentState.monthlyBudgetRemaining,
                            roundedAmount
                        )
                        repository.saveBudgetState(currentState.copy(monthlyBudgetRemaining = newMonthly))
                    }
                    TransactionType.LUXURY_EXPENSE -> {
                        val newLuxury = BudgetEngine.adjustLuxuryBudget(
                            currentState.luxuryBudget,
                            -roundedAmount
                        )
                        repository.saveBudgetState(currentState.copy(luxuryBudget = newLuxury))
                    }
                    TransactionType.MONTHLY_ADDITION -> {
                        val newMonthly = BudgetEngine.addMonthlyIncome(
                            currentState.monthlyBudgetRemaining,
                            roundedAmount
                        )
                        repository.saveBudgetState(currentState.copy(monthlyBudgetRemaining = newMonthly))
                    }
                    TransactionType.LUXURY_ADDITION -> {
                        val newLuxury = BudgetEngine.adjustLuxuryBudget(
                            currentState.luxuryBudget,
                            roundedAmount
                        )
                        repository.saveBudgetState(currentState.copy(luxuryBudget = newLuxury))
                    }
                }
            }

            val transaction = TransactionEntity(
                amount = roundedAmount,
                description = description.trim(),
                timestamp = System.currentTimeMillis(),
                type = type,
                date = activeDateIso,
                isSettled = false
            )
            repository.addTransaction(transaction)
        }
    }

    fun deleteTransaction(transaction: TransactionEntity) {
        viewModelScope.launch {
            rollbackTransaction(transaction)
            repository.deleteTransaction(transaction.id)
        }
    }

    fun deleteTransaction(transactionId: Long) {
        viewModelScope.launch {
            val tx = uiState.value.todayTransactions.find { it.id == transactionId }
            if (tx != null) {
                rollbackTransaction(tx)
            }
            repository.deleteTransaction(transactionId)
        }
    }

    private suspend fun rollbackTransaction(transaction: TransactionEntity) {
        val currentState = repository.getBudgetStateSync() ?: return
        when (transaction.type) {
            TransactionType.MONTHLY_EXPENSE -> {
                val newMonthly = BudgetEngine.addMonthlyIncome(
                    currentState.monthlyBudgetRemaining,
                    transaction.amount
                )
                repository.saveBudgetState(currentState.copy(monthlyBudgetRemaining = newMonthly))
            }
            TransactionType.LUXURY_EXPENSE -> {
                val newLuxury = BudgetEngine.adjustLuxuryBudget(
                    currentState.luxuryBudget,
                    transaction.amount
                )
                repository.saveBudgetState(currentState.copy(luxuryBudget = newLuxury))
            }
            TransactionType.MONTHLY_ADDITION -> {
                val newMonthly = BudgetEngine.subtractMonthlyExpense(
                    currentState.monthlyBudgetRemaining,
                    transaction.amount
                )
                repository.saveBudgetState(currentState.copy(monthlyBudgetRemaining = newMonthly))
            }
            TransactionType.LUXURY_ADDITION -> {
                val newLuxury = BudgetEngine.adjustLuxuryBudget(
                    currentState.luxuryBudget,
                    -transaction.amount
                )
                repository.saveBudgetState(currentState.copy(luxuryBudget = newLuxury))
            }
            TransactionType.DAILY_EXPENSE -> {
                // Deleting active row automatically updates todaySpent and todayRemaining reactively
            }
        }
    }

    fun endDay() {
        viewModelScope.launch {
            val currentState = repository.getBudgetStateSync() ?: return@launch
            val activeDateIso = _currentDate.value.toString()
            val todayExpenses = repository.getActiveTodayExpensesSync(activeDateIso)
            val todaySpent = BudgetEngine.round(todayExpenses.sumOf { it.amount })

            val settlement = BudgetEngine.endDaySettlement(
                monthlyRemaining = currentState.monthlyBudgetRemaining,
                todaySpent = todaySpent,
                daysRemaining = currentState.daysRemainingInMonth
            )

            repository.settleTransactionsForDate(activeDateIso)

            val updatedState = currentState.copy(
                monthlyBudgetRemaining = settlement.newMonthlyRemaining,
                daysRemainingInMonth = settlement.newDaysRemaining,
                lastClosedDate = activeDateIso
            )
            repository.saveBudgetState(updatedState)
        }
    }

    fun refreshDate() {
        _currentDate.value = dateProvider()
    }

    fun setDate(date: LocalDate) {
        _currentDate.value = date
    }

    // Aliases for event handler naming consistency
    fun onInitializeBudget(startingMonthlyBudget: Double) = initializeBudget(startingMonthlyBudget)
    fun onAddTransaction(amount: Double, description: String, type: TransactionType) = addTransaction(amount, description, type)
    fun onDeleteTransaction(transaction: TransactionEntity) = deleteTransaction(transaction)
    fun onDeleteTransaction(transactionId: Long) = deleteTransaction(transactionId)
    fun onEndDay() = endDay()
    fun onUpdateMonthlyBudget(newMonthlyBudget: Double) = updateMonthlyBudget(newMonthlyBudget)
    fun onUpdateLuxuryBudget(newLuxuryBudget: Double) = updateLuxuryBudget(newLuxuryBudget)

    private fun formatDate(date: LocalDate): String {
        return date.format(DateTimeFormatter.ofPattern("EEEE, MMM d", Locale.getDefault()))
    }
}
