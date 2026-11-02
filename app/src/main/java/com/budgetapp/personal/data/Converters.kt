package com.budgetapp.personal.data

import androidx.room.TypeConverter
import com.budgetapp.personal.model.TransactionType

class Converters {
    @TypeConverter
    fun fromTransactionType(type: TransactionType?): String? {
        return type?.name
    }

    @TypeConverter
    fun toTransactionType(value: String?): TransactionType? {
        return value?.let { TransactionType.valueOf(it) }
    }
}
