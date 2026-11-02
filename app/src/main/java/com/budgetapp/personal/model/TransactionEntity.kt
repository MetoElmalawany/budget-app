package com.budgetapp.personal.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "transactions")
data class TransactionEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val amount: Double,
    val description: String,
    val timestamp: Long = System.currentTimeMillis(),
    val type: TransactionType,
    val date: String,
    val isSettled: Boolean = false
)
