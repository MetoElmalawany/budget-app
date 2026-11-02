package com.budgetapp.personal.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.budgetapp.personal.model.BudgetStateEntity
import com.budgetapp.personal.model.TransactionEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface BudgetDao {
    @Query("SELECT * FROM budget_state WHERE id = 1")
    fun getBudgetState(): Flow<BudgetStateEntity?>

    @Query("SELECT * FROM budget_state WHERE id = 1")
    suspend fun getBudgetStateSync(): BudgetStateEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun saveBudgetState(state: BudgetStateEntity)

    @Query("SELECT * FROM transactions WHERE date = :date AND isSettled = 0 ORDER BY timestamp DESC")
    fun getActiveTodayTransactions(date: String): Flow<List<TransactionEntity>>

    @Query("SELECT * FROM transactions WHERE date = :date AND isSettled = 0 AND type = 'DAILY_EXPENSE'")
    suspend fun getActiveTodayExpensesSync(date: String): List<TransactionEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTransaction(transaction: TransactionEntity): Long

    @Query("DELETE FROM transactions WHERE id = :id")
    suspend fun deleteTransaction(id: Long)

    @Query("UPDATE transactions SET isSettled = 1 WHERE date = :date AND isSettled = 0")
    suspend fun settleTransactionsForDate(date: String)

    @Query("DELETE FROM budget_state")
    suspend fun clearBudgetState()

    @Query("DELETE FROM transactions")
    suspend fun clearTransactions()
}
