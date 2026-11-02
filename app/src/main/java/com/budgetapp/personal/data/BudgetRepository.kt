package com.budgetapp.personal.data

import com.budgetapp.personal.model.BudgetStateEntity
import com.budgetapp.personal.model.TransactionEntity
import kotlinx.coroutines.flow.Flow

class BudgetRepository(private val dao: BudgetDao) {

    val budgetState: Flow<BudgetStateEntity?> = dao.getBudgetState()

    suspend fun getBudgetStateSync(): BudgetStateEntity? {
        return dao.getBudgetStateSync()
    }

    suspend fun saveBudgetState(state: BudgetStateEntity) {
        dao.saveBudgetState(state)
    }

    suspend fun updateMonthlyBudget(newMonthlyBudget: Double) {
        val current = getBudgetStateSync() ?: return
        saveBudgetState(current.copy(monthlyBudgetRemaining = newMonthlyBudget))
    }

    suspend fun updateLuxuryBudget(newLuxuryBudget: Double) {
        val current = getBudgetStateSync() ?: return
        saveBudgetState(current.copy(luxuryBudget = newLuxuryBudget))
    }

    fun getActiveTodayTransactions(date: String): Flow<List<TransactionEntity>> {
        return dao.getActiveTodayTransactions(date)
    }

    suspend fun getActiveTodayExpensesSync(date: String): List<TransactionEntity> {
        return dao.getActiveTodayExpensesSync(date)
    }

    suspend fun addTransaction(transaction: TransactionEntity): Long {
        return dao.insertTransaction(transaction)
    }

    suspend fun deleteTransaction(id: Long) {
        dao.deleteTransaction(id)
    }

    suspend fun settleTransactionsForDate(date: String) {
        dao.settleTransactionsForDate(date)
    }

    suspend fun clearBudgetState() {
        dao.clearBudgetState()
    }

    suspend fun clearTransactions() {
        dao.clearTransactions()
    }
}
